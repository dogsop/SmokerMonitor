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

import com.parse.FindCallback;
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
 * {@link TemperatureFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TemperatureFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class TemperatureFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final String TAG = "TemperatureFragment";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private Date createdAt;
    private int meatTemperature;
    private int smokerTemperature;
    private int blowerSetting;
    private String lastPollingAttemptString;
    private String createdAtString;

    private TextView meatTempView;
    private TextView smokerTempView;
    private TextView blowerSettingView;
    private TextView lastUpdateView;
    private TextView timestampView;

    private Handler mHandler;

    private OnFragmentInteractionListener mListener;

    private Timer autoUpdateTimer;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TemperatureFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TemperatureFragment newInstance() {
        TemperatureFragment fragment = new TemperatureFragment();
        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public TemperatureFragment() {
        // Required empty public constructor

        // Defines a Handler object that's attached to the UI thread
        mHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        meatTemperature = 0;
        smokerTemperature = 0;
        blowerSetting = 0;
        //createdAt = new Date();
        createdAt = null;
        createdAtString = "";
        lastPollingAttemptString = "";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_temperature, container, false);

        meatTempView = (TextView)v.findViewById(R.id.meatTempView);
        smokerTempView = (TextView)v.findViewById(R.id.smokerTempView);
        blowerSettingView = (TextView)v.findViewById(R.id.blowerSettingView);
        lastUpdateView = (TextView)v.findViewById(R.id.lastUpdateView);
        timestampView = (TextView)v.findViewById(R.id.timestampView);

        meatTempView.setText(Integer.toString(meatTemperature)+ " \u2109");
        smokerTempView.setText(Integer.toString(smokerTemperature)+ " \u2109");
        blowerSettingView.setText(Integer.toString(blowerSetting)+ "%");
        lastUpdateView.setText(lastPollingAttemptString);
        timestampView.setText(createdAtString);

        return v;
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume()");
        super.onResume();
        //refreshTemps();

        if(autoUpdateTimer == null) {
            autoUpdateTimer = new Timer();
            autoUpdateTimer.schedule(new TimerTask() {
                public void run() {
                    Log.i(TAG, "autoUpdateTimer - calling refreshTemps()");
                    refreshTemps();
                }
            }, 200, 20 * 1000); // updates each 40 secs
        }
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause()");
        //autoUpdateTimer.cancel();
        super.onPause();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    private void refreshTemps() {
        final Date currentDateTime = new Date();
        lastPollingAttemptString = currentDateTime.toString();
        Log.i(TAG, "refreshTemps() " + lastPollingAttemptString);

        ParseQuery<ParseObject> tempQuery = ParseQuery.getQuery("RealTimeTempData");
        tempQuery.orderByDescending("createdAt");
        tempQuery.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ONLY);
        //tempQuery.orderByAscending("createdAtString");
        Log.i(TAG, "tempData = tempQuery.getFirstInBackground()");
        tempQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                Log.i(TAG, "refreshTemps done called");
                if (e == null) {
                    if (object == null) {
                        Log.i(TAG, "object == null");
                        meatTemperature = 0;
                        smokerTemperature = 0;
                        blowerSetting = 0;
                        createdAt = null;
                    } else {
                        Log.i(TAG, "Retrieved the object.");
                        String objectId = object.getObjectId();
                        createdAt = object.getCreatedAt();
                        createdAtString = createdAt.toString();
                        Log.i(TAG, "ObjectId - " + objectId + " createdAt - " + createdAtString);
                        meatTemperature = object.getInt("MeatTemp");
                        smokerTemperature = object.getInt("SmokerTemp");
                        blowerSetting = object.getInt("BlowerSetting");
                    }
                } else {
                    Log.i("refreshTemps", "Error: " + e.getMessage());
                    meatTemperature = 0;
                    smokerTemperature = 0;
                    blowerSetting = 0;
                    createdAt = null;
                }

                meatTempView.setText(Integer.toString(meatTemperature) + " \u2109");
                smokerTempView.setText(Integer.toString(smokerTemperature) + " \u2109");
                blowerSettingView.setText(Integer.toString(blowerSetting) + "%");
                timestampView.setText(createdAtString);

                Calendar c = Calendar.getInstance();
                int zoneOffset = c.get(java.util.Calendar.ZONE_OFFSET);
                int dstOffset = c.get(java.util.Calendar.DST_OFFSET);
                //long createdTime = createdAt.getTime();
                //createdTime += (zoneOffset + dstOffset);
                long currentTime = currentDateTime.getTime();

                if(createdAt != null) {
                    // Code here will run in UI thread
                    long diff = currentDateTime.getTime() - createdAt.getTime();

                    if (diff > 30000) {
                        if (diff > 120000) {
                            meatTempView.setTextColor(Color.RED);
                            smokerTempView.setTextColor(Color.RED);
                        } else {
                            meatTempView.setTextColor(Color.YELLOW);
                            smokerTempView.setTextColor(Color.YELLOW);
                        }
                    } else {
                        meatTempView.setTextColor(Color.BLACK);
                        smokerTempView.setTextColor(Color.BLACK);
                    }
                } else {
                    meatTempView.setTextColor(Color.BLACK);
                    smokerTempView.setTextColor(Color.BLACK);
                }
                lastUpdateView.setText(lastPollingAttemptString);
            }
        });


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
