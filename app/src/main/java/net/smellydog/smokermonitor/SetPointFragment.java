package net.smellydog.smokermonitor;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SetPointFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SetPointFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class SetPointFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final String TAG = "SetPointFragment";


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private int setPointTemperature;
    private Boolean controllerRunning;
    private TextView setPointTempView;
    private EditText setPointEditText;
    private ToggleButton toggleControllerButton;
    private Button editSetPointButton;

    private String lastPollingAttemptString;
    private String updatedAtString;

    private Handler mHandler;

    private Timer autoUpdateTimer;

    private boolean setPointEditEnabled;

    private String objectId;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SetPointFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SetPointFragment newInstance() {
        SetPointFragment fragment = new SetPointFragment();
        return fragment;
    }
    public SetPointFragment() {
        Log.i(TAG, "SetPointFragment()");
        // Required empty public constructor
        // Defines a Handler object that's attached to the UI thread
        mHandler = new Handler(Looper.getMainLooper());

        setPointTemperature = 0;
        controllerRunning = false;
        objectId = null;
        setPointEditEnabled = false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView()");
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_set_point, container, false);

        setPointTempView = (TextView)v.findViewById(R.id.setPointTempView);
        setPointEditText = (EditText)v.findViewById(R.id.setPointEditText);

        setPointTempView.setVisibility(View.VISIBLE);
        setPointEditText.setVisibility(View.GONE);

        editSetPointButton = (Button)v.findViewById(R.id.editSetPointButton);
        editSetPointButton.setEnabled(false);
        editSetPointButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(setPointEditEnabled == false) {
                    toggleControllerButton.setEnabled(false);
                    setPointEditEnabled = true;
                    editSetPointButton.setText("Save Set Point");
                    setPointTempView.setVisibility(View.GONE);
                    setPointEditText.setVisibility(View.VISIBLE);
                    setPointEditText.setText(Integer.toString(setPointTemperature));
                } else {
                    toggleControllerButton.setEnabled(true);
                    setPointEditEnabled = false;
                    editSetPointButton.setText("Edit Set Point");
                    setPointTempView.setVisibility(View.VISIBLE);
                    setPointEditText.setVisibility(View.GONE);
                    setPointTemperature = Integer.parseInt(setPointEditText.getText().toString());
                    setPointTempView.setText(Integer.toString(setPointTemperature) + " \u2109");
                    if(objectId != null) {
                        ParseQuery<ParseObject> setPointQuery = ParseQuery.getQuery("SetPointSettings");
                        setPointQuery.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ONLY);
                        // Retrieve the object by id
                        setPointQuery.getInBackground(objectId, new GetCallback<ParseObject>() {
                            public void done(ParseObject setPointSettings, ParseException e) {
                                if (e == null) {
                                    // Now let's update it with some new data. In this case, only controllerRunning
                                    // will get sent to the Parse Cloud.
                                    setPointSettings.put("SetPointTemp", setPointTemperature);
                                    setPointSettings.saveInBackground();
                                }
                            }
                        });
                    }
                }
            }
        });

        toggleControllerButton = (ToggleButton)v.findViewById(R.id.toggleControllerButton);
        toggleControllerButton.setEnabled(false);
        toggleControllerButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                controllerRunning = toggleControllerButton.isChecked();
                if(objectId != null) {
                    ParseQuery<ParseObject> setPointQuery = ParseQuery.getQuery("SetPointSettings");
                    setPointQuery.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ONLY);
                    // Retrieve the object by id
                    setPointQuery.getInBackground(objectId, new GetCallback<ParseObject>() {
                        public void done(ParseObject setPointSettings, ParseException e) {
                            if (e == null) {
                                // Now let's update it with some new data. In this case, only controllerRunning
                                // will get sent to the Parse Cloud.
                                setPointSettings.put("ControllerRunning", controllerRunning);
                                setPointSettings.saveInBackground();
                            }
                        }
                    });
                }
            }
        });


        return v;

    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume()");
        super.onResume();

        if(objectId == null) {
            getObjectId();
        } else {
            setPointTempView.setText(Integer.toString(setPointTemperature) + " \u2109");
            editSetPointButton.setEnabled(true);
            toggleControllerButton.setEnabled(true);
            toggleControllerButton.setChecked(controllerRunning);
            setPointEditEnabled = false;
            setPointTempView.setVisibility(View.VISIBLE);
            setPointEditText.setVisibility(View.GONE);
        }

        if(autoUpdateTimer == null) {
            autoUpdateTimer = new Timer();
            autoUpdateTimer.schedule(new TimerTask() {
                public void run() {
                    Log.i(TAG, "autoUpdate - calling refreshScreen()");
                    refreshScreen();
                }
            }, 10000, 21 * 1000); // updates each 60 secs
        }
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause()");
        //autoUpdateTimer.cancel();
        super.onPause();
    }

    private void getObjectId() {
        final Date currentDateTime = new Date();
        lastPollingAttemptString = currentDateTime.toString();
        Log.i(TAG, "refreshScreen() " + lastPollingAttemptString);

        if(objectId == null) {
            ParseQuery<ParseObject> setPointQuery = ParseQuery.getQuery("SetPointSettings");
            setPointQuery.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ONLY);
            setPointQuery.orderByDescending("createdAt");
            //setPointQuery.orderByAscending("createdAtString");
            Log.i(TAG, "tempData = setPointQuery.getFirstInBackground()");
            setPointQuery.getFirstInBackground(new GetCallback<ParseObject>() {
                public void done(ParseObject object, ParseException e) {
                    Log.i(TAG, "done called");
                    if (e == null) {
                        if (object == null) {
                            Log.i(TAG, "object == null");
                        } else {
                            Log.i(TAG, "Retrieved the object.");
                            objectId = object.getObjectId();
                            Log.i(TAG, "ObjectId - " + objectId);
                            updatedAtString = object.getUpdatedAt().toString();
                            setPointTemperature = object.getInt("SetPointTemp");
                            controllerRunning = object.getBoolean("ControllerRunning");
                            // Code here will run in UI thread
                            setPointTempView.setText(Integer.toString(setPointTemperature) + " \u2109");
                            editSetPointButton.setEnabled(true);
                            toggleControllerButton.setEnabled(true);
                            toggleControllerButton.setChecked(controllerRunning);
                        }
                    } else {
                        Log.i(TAG, "Error: " + e.getMessage());
                    }
                }
            });
        }
    }

    private void refreshScreen() {
        final Date currentDateTime = new Date();
        lastPollingAttemptString = currentDateTime.toString();
        Log.i(TAG, "refreshScreen() " + lastPollingAttemptString);

        if(objectId != null) {
            ParseQuery<ParseObject> setPointQuery = ParseQuery.getQuery("SetPointSettings");
            setPointQuery.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ONLY);
            setPointQuery.getInBackground(objectId, new GetCallback<ParseObject>() {
                public void done(ParseObject object, ParseException e) {
                    Log.i(TAG, "done called");
                    if (e == null) {
                        if (object == null) {
                            Log.i(TAG, "object == null");
                        } else {
                            Log.i(TAG, "updating object.");
                            updatedAtString = object.getUpdatedAt().toString();
                            setPointTemperature = object.getInt("SetPointTemp");
                            controllerRunning = object.getBoolean("ControllerRunning");
                            // Code here will run in UI thread
                            setPointTempView.setText(Integer.toString(setPointTemperature)+ " \u2109");
                            toggleControllerButton.setChecked(controllerRunning);
                        }
                    } else {
                        Log.i(TAG, "Error: " + e.getMessage());
                    }
                }
            });
        }

    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onControllerButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
