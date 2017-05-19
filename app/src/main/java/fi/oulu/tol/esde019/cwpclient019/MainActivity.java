package fi.oulu.tol.esde019.cwpclient019;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import fi.oulu.tol.esde019.cwpclient019.cwprotocol.CWPControl;
import fi.oulu.tol.esde019.cwpclient019.cwprotocol.CWPMessaging;
import fi.oulu.tol.esde019.cwpclient019.model.CWPModel;
import fi.oulu.tol.esde019.cwpclient019.model.CWPService;

public class MainActivity extends AppCompatActivity implements CWPProvider {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private CWPService mCWPService;
    boolean mBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        Intent intent = new Intent(this, CWPService.class);
        startService(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        Intent intent = new Intent(this, CWPService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            mCWPService.stopUsing();
            unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public CWPMessaging getMessaging() {
        if (mBound) {
            mCWPService.getMessaging();
        }
        return null;
    }

    @Override
    public CWPControl getControl() {
        if (mBound) {
            mCWPService.getControl();
        }
        return null;
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private TappingFragment tapper = null;
        private ControlFragment control = null;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            if (position == 0) {
                if (null == tapper) { // tapper is the TappingFragment member variable
                    tapper = new TappingFragment();
                }
                return tapper;
            }
            if (position == 1) {
                if (null == control) { // tapper is the TappingFragment member variable
                    control = new ControlFragment();
                }
                return control;
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Tapping";
                case 1:
                    return "Settings";
            }
            return null;
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            CWPService.CWPBinder binder = (CWPService.CWPBinder) service;
            mCWPService = binder.getService();
            mBound = true;
            mCWPService.startUsing();

            Fragment f0 = mSectionsPagerAdapter.getItem(0);
            if (null != f0) {
                ((TappingFragment) f0).setMessaging(mCWPService.getMessaging());
            }
            Fragment f1 = mSectionsPagerAdapter.getItem(1);
            if (null != f1) {
                ((ControlFragment) f1).setControl(mCWPService.getControl());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
            Fragment f0 = mSectionsPagerAdapter.getItem(0);
            if (null != f0) {
                ((TappingFragment) f0).setMessaging(null);
            }
            Fragment f1 = mSectionsPagerAdapter.getItem(1);
            if (null != f1) {
                ((ControlFragment) f1).setControl(null);
            }
        }
    };
}