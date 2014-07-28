package net.smellydog.smokermonitor;

import android.app.Activity;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
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

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private int setPointTemperature;
    private Boolean controllerRunning;
    private TextView setPointTempView;

    private String lastPollingAttemptString;
    private String updatedAtString;

    private Handler mHandler;

    private Timer autoUpdate;

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
        // Required empty public constructor
        // Defines a Handler object that's attached to the UI thread
        mHandler = new Handler(Looper.getMainLooper());

        setPointTemperature = 0;
        controllerRunning = false;
        objectId = null;
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
        View v = inflater.inflate(R.layout.fragment_set_point, container, false);

        setPointTempView = (TextView)v.findViewById(R.id.setPointTempView);

        return v;

    }

    @Override
    public void onResume() {
        Log.i("SetPointFragment", "onResume()");
        super.onResume();
        //refreshTemps();
        autoUpdate = new Timer();
        autoUpdate.schedule(new TimerTask() {
            public void run() {
                refreshScreen();
            }
        }, 1000, 60*1000); // updates each 40 secs
    }

    @Override
    public void onPause() {
        Log.i("SetPointFragment", "onPause()");
        autoUpdate.cancel();
        super.onPause();
    }

    private void refreshScreen() {
        final Date currentDateTime = new Date();
        lastPollingAttemptString = currentDateTime.toString();
        Log.i("SetPointFragment", "refreshScreen() " + lastPollingAttemptString);

        if(objectId == null) {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("SetPointSettings");
            query.orderByDescending("createdAt");
            //query.orderByAscending("createdAtString");
            Log.i("SetPointFragment", "tempData = query.getFirstInBackground()");
            query.getFirstInBackground(new GetCallback<ParseObject>() {
                public void done(ParseObject object, ParseException e) {
                    if (object == null) {
                        Log.i("SetPointFragment", "The getFirst request failed.");
                    } else {
                        Log.i("SetPointFragment", "Retrieved the object.");
                        objectId = object.getObjectId();
                        Log.i("SetPointFragment", "ObjectId - " + objectId );
                        updatedAtString = object.getUpdatedAt().toString();
                        setPointTemperature = object.getInt("SetPointTemp");
                        controllerRunning = object.getBoolean("ControllerRunning");
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.i("SetPointFragment", "refreshScreen().findInBackground.run()");

                                // Code here will run in UI thread
                                setPointTempView.setText(Integer.toString(setPointTemperature)+ " \u2109");
                                //timestampView.setText(createdAtString);
                            }
                        });
                    }
                }
            });
        } else {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("SetPointSettings");
            query.getInBackground(objectId, new GetCallback<ParseObject>() {
                public void done(ParseObject object, ParseException e) {
                    if (e == null) {
                        updatedAtString = object.getUpdatedAt().toString();
                        setPointTemperature = object.getInt("SetPointTemp");
                        controllerRunning = object.getBoolean("ControllerRunning");
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.i("SetPointFragment", "refreshScreen().findInBackground.run()");

                                // Code here will run in UI thread
                                setPointTempView.setText(Integer.toString(setPointTemperature)+ " \u2109");
                                //timestampView.setText(createdAtString);
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
