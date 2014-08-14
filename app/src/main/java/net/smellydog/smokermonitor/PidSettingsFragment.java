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
 * {@link PidSettingsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PidSettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class PidSettingsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    private TextView settingKpView;
    private TextView settingKiView;
    private TextView settingKdView;

    private EditText settingKpEditText;
    private EditText settingKiEditText;
    private EditText settingKdEditText;

    private Button editPidSettingsButton;

    private String objectId;

    private String lastPollingAttemptString;
    private String updatedAtString;

    private double Kp;
    private double Ki;
    private double Kd;

    private Handler mHandler;

    private Timer autoUpdateTimer;

    private boolean queryOutstanding;
    ParseQuery<ParseObject> pidQuery;

    private boolean setPidSettingsEnabled;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PidSettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PidSettingsFragment newInstance() {
        PidSettingsFragment fragment = new PidSettingsFragment();
        return fragment;
    }
    public PidSettingsFragment() {
        // Defines a Handler object that's attached to the UI thread
        mHandler = new Handler(Looper.getMainLooper());

        objectId = null;
        queryOutstanding = false;
        setPidSettingsEnabled = false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_pid_settings, container, false);

        settingKpView = (TextView)v.findViewById(R.id.settingKpView);
        settingKiView = (TextView)v.findViewById(R.id.settingKiView);
        settingKdView = (TextView)v.findViewById(R.id.settingKdView);

        settingKpEditText = (EditText)v.findViewById(R.id.settingKpEditText);
        settingKiEditText = (EditText)v.findViewById(R.id.settingKiEditText);
        settingKdEditText = (EditText)v.findViewById(R.id.settingKdEditText);

        settingKpView.setVisibility(View.VISIBLE);
        settingKpEditText.setVisibility(View.GONE);
        settingKiView.setVisibility(View.VISIBLE);
        settingKiEditText.setVisibility(View.GONE);
        settingKdView.setVisibility(View.VISIBLE);
        settingKdEditText.setVisibility(View.GONE);

        editPidSettingsButton = (Button)v.findViewById(R.id.editPidSettingsButton);
        editPidSettingsButton.setEnabled(false);
        editPidSettingsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (setPidSettingsEnabled == false) {
                    setPidSettingsEnabled = true;
                    editPidSettingsButton.setText("Save PID Settings");
                    settingKpView.setVisibility(View.GONE);
                    settingKpEditText.setVisibility(View.VISIBLE);
                    settingKiView.setVisibility(View.GONE);
                    settingKiEditText.setVisibility(View.VISIBLE);
                    settingKdView.setVisibility(View.GONE);
                    settingKdEditText.setVisibility(View.VISIBLE);
                    settingKpEditText.setText(Double.toString(Kp));
                    settingKiEditText.setText(Double.toString(Ki));
                    settingKdEditText.setText(Double.toString(Kd));
                } else {
                    if (queryOutstanding == false) {
                        setPidSettingsEnabled = false;
                        editPidSettingsButton.setText("Edit PID Settings");
                        settingKpView.setVisibility(View.VISIBLE);
                        settingKpEditText.setVisibility(View.GONE);
                        settingKiView.setVisibility(View.VISIBLE);
                        settingKiEditText.setVisibility(View.GONE);
                        settingKdView.setVisibility(View.VISIBLE);
                        settingKdEditText.setVisibility(View.GONE);
                        Kp = Double.parseDouble(settingKpEditText.getText().toString());
                        Ki = Double.parseDouble(settingKiEditText.getText().toString());
                        Kd = Double.parseDouble(settingKdEditText.getText().toString());
                        settingKpView.setText(Double.toString(Kp));
                        settingKiView.setText(Double.toString(Ki));
                        settingKdView.setText(Double.toString(Kd));
                        if (objectId != null) {
                            pidQuery = ParseQuery.getQuery("PidSettings");
                            pidQuery.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ONLY);
                            // Retrieve the object by id
                            queryOutstanding = true;
                            pidQuery.getInBackground(objectId, new GetCallback<ParseObject>() {
                                public void done(ParseObject pidSettings, ParseException e) {
                                    queryOutstanding = false;
                                    if (e == null) {
                                        // Now let's update it with some new data. In this case, only controllerRunning
                                        // will get sent to the Parse Cloud.
                                        pidSettings.put("Kp", Kp);
                                        pidSettings.put("Ki", Ki);
                                        pidSettings.put("Kd", Kd);
                                        pidSettings.saveInBackground();
                                    }
                                }
                            });
                        }
                    }
                }
            }
        });


        return v;
    }

    @Override
    public void onResume() {
        Log.i("SetPointFragment", "onResume()");
        super.onResume();

        if(objectId == null) {
            getObjectId();
        } else {
            settingKpView.setText(Double.toString(Kp));
            settingKiView.setText(Double.toString(Ki));
            settingKdView.setText(Double.toString(Kd));
            editPidSettingsButton.setEnabled(true);
            setPidSettingsEnabled = false;
            settingKpView.setVisibility(View.VISIBLE);
            settingKpEditText.setVisibility(View.GONE);
            settingKiView.setVisibility(View.VISIBLE);
            settingKiEditText.setVisibility(View.GONE);
            settingKdView.setVisibility(View.VISIBLE);
            settingKdEditText.setVisibility(View.GONE);
        }

        autoUpdateTimer = new Timer();
        autoUpdateTimer.schedule(new TimerTask() {
            public void run() {
                if(queryOutstanding == false) {
                    Log.i("SetPointFragment", "autoUpdate - calling refreshScreen()");
                    refreshScreen();
                } else {
                    Log.e("SetPointFragment", "autoUpdate - unable to call refreshScreen(), query pending");
                }
            }
        }, 10000, 240 * 1000); // updates each 240 secs
    }

    @Override
    public void onPause() {
        Log.i("SetPointFragment", "onPause()");
        autoUpdateTimer.cancel();
        if(queryOutstanding == true) {
            pidQuery.cancel();
            queryOutstanding = false;
        }
        super.onPause();
    }

    private void getObjectId() {
        final Date currentDateTime = new Date();
        lastPollingAttemptString = currentDateTime.toString();
        Log.i("SetPointFragment", "refreshScreen() " + lastPollingAttemptString);

        if(objectId == null) {
            pidQuery = ParseQuery.getQuery("PidSettings");
            pidQuery.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ONLY);
            pidQuery.orderByDescending("createdAt");
            //pidQuery.orderByAscending("createdAtString");
            Log.i("SetPointFragment", "tempData = pidQuery.getFirstInBackground()");
            queryOutstanding = true;
            pidQuery.getFirstInBackground(new GetCallback<ParseObject>() {
                public void done(ParseObject object, ParseException e) {
                    queryOutstanding = false;
                    if (object == null) {
                        Log.i("SetPointFragment", "The getFirst request failed.");
                    } else {
                        Log.i("SetPointFragment", "Retrieved the object.");
                        objectId = object.getObjectId();
                        Log.i("SetPointFragment", "ObjectId - " + objectId);
                        updatedAtString = object.getUpdatedAt().toString();
                        Kp = object.getDouble("Kp");
                        Ki = object.getDouble("Ki");
                        Kd = object.getDouble("Kd");
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.i("SetPointFragment", "refreshScreen().findInBackground.run()");

                                // Code here will run in UI thread
                                settingKpView.setText(Double.toString(Kp));
                                settingKiView.setText(Double.toString(Ki));
                                settingKdView.setText(Double.toString(Kd));
                                editPidSettingsButton.setEnabled(true);
                                //timestampView.setText(createdAtString);
                            }
                        });
                    }
                }
            });
        }
    }

    private void refreshScreen() {
        final Date currentDateTime = new Date();
        lastPollingAttemptString = currentDateTime.toString();
        Log.i("SetPointFragment", "refreshScreen() " + lastPollingAttemptString);

        if(objectId != null) {
            pidQuery = ParseQuery.getQuery("PidSettings");
            pidQuery.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ONLY);
            queryOutstanding = true;
            pidQuery.getInBackground(objectId, new GetCallback<ParseObject>() {
                public void done(ParseObject object, ParseException e) {
                    queryOutstanding = false;
                    if (e == null) {
                        updatedAtString = object.getUpdatedAt().toString();
                        Kp = object.getDouble("Kp");
                        Ki = object.getDouble("Ki");
                        Kd = object.getDouble("Kd");
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.i("SetPointFragment", "refreshScreen().findInBackground.run()");

                                // Code here will run in UI thread
                                settingKpView.setText(Double.toString(Kp));
                                settingKiView.setText(Double.toString(Ki));
                                settingKdView.setText(Double.toString(Kd));
                                editPidSettingsButton.setEnabled(true);
                            }
                        });
                    } else {
                        Log.i("SetPointFragment", "The getInBackground request failed.");
                    }
                }
            });
        }

    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
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
