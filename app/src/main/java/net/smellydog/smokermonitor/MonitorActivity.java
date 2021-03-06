package net.smellydog.smokermonitor;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;


public class MonitorActivity extends Activity
implements ActionBar.TabListener,
        TemperatureFragment.OnFragmentInteractionListener,
        SetPointFragment.OnFragmentInteractionListener,
        PidSettingsFragment.OnFragmentInteractionListener {

    private static final String TAG = "MonitorActivity";

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);

        // Set up the action bar .
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.monitor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        else if (id == R.id.action_erase) {
            new DeleteOldData().execute("");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private static final String TAG = "SectionsPagerAdapter";

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch(position) {
                case 0:
                    Log.i(TAG, "TemperatureFragment.newInstance()");
                    return TemperatureFragment.newInstance();
                case 2:
                    Log.i(TAG, "SetPointFragment.newInstance()");
                    return SetPointFragment.newInstance();
                case 3:
                    Log.i(TAG, "PidSettingsFragment.newInstance()");
                    return PidSettingsFragment.newInstance();
                default:
                    return PlaceholderFragment.newInstance(position + 1);
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.title_section1);
                case 1:
                    return getString(R.string.title_section2);
                case 2:
                    return getString(R.string.title_section3);
                case 3:
                    return getString(R.string.title_section4);
            }
            return null;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String TAG = "PlaceholderFragment";

        private static final String ARG_SECTION_NUMBER = "section_number";

        TextView sectionLabel;

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {

            Bundle bundle=getArguments();

            View rootView = inflater.inflate(R.layout.fragment_monitor, container, false);

            sectionLabel = (TextView)rootView.findViewById(R.id.section_label);

            int sectionNumber;
            String sectionLabelText;
            sectionNumber = bundle.getInt(ARG_SECTION_NUMBER, -1);

            sectionLabelText = String.format("Section %d", sectionNumber);
            sectionLabel.setText(sectionLabelText);

            return rootView;
        }
    }


    private class DeleteOldData extends AsyncTask<String, Void, String> {

        private static final String TAG = "DeleteOldData";

        protected String  doInBackground(String... none) {
            EraseTempData();
            ErasePidData();
            return "executed";
        }

        protected void onProgressUpdate(Void... none) {
        }

        protected void onPostExecute(String result) {
        }

        private void EraseTempData() {
            while(true) {
                try {
                    ParseQuery<ParseObject> query = ParseQuery.getQuery("RealTimeTempData");
                    query.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ONLY);
                    List<ParseObject> realTimeTempDataList = query.find();
                    Log.i(TAG, "Retrieved " + realTimeTempDataList.size() + " temp measurements");
                    if(realTimeTempDataList.size() == 0) {
                        return;
                    } else {
                        // Accepts a parameter of type: List<ParseObject>
                        ParseObject.deleteAll(realTimeTempDataList);
                    }
                } catch(ParseException e) {
                    Log.i(TAG, "Error: " + e.getMessage());
                }
            }
        }

        private void ErasePidData() {
            while(true) {
                try {
                    ParseQuery<ParseObject> query = ParseQuery.getQuery("RealTimePidData");
                    query.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ONLY);
                    List<ParseObject> realTimePidDataList = query.find();
                    Log.i(TAG, "Retrieved " + realTimePidDataList.size() + " pid measurements");
                    if(realTimePidDataList.size() == 0) {
                        return;
                    } else {
                        // Accepts a parameter of type: List<ParseObject>
                        ParseObject.deleteAll(realTimePidDataList);
                    }
                } catch(ParseException e) {
                    Log.i(TAG, "Error: " + e.getMessage());
                }
            }
        }

    }
}
