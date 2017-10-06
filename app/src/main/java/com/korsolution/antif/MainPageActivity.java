package com.korsolution.antif;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageButton;
import android.widget.TextView;

public class MainPageActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;


    private AccountDBClass AccountDB;

    static ImageButton btnAnnouncement;
    static TextView textOne;
    static int mNotifCount = 10;

    private String USER_ID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        mViewPager.setCurrentItem(0);


        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar_layout);

        AccountDB = new AccountDBClass(this);
        String[][] arrData = AccountDB.SelectAllAccount();
        if (arrData != null) {
            USER_ID = arrData[0][1].toString();
        }

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_refresh, menu);

        MenuItem itemRefresh = menu.findItem(R.id.action_refresh);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        }*/

        if (id == R.id.action_refresh) {

            mViewPager.setCurrentItem(0);

            Fragment fragment = getActiveFragment(mViewPager, 0);   // 0 is current page
            MyCarFragment MyCarFragment = (MyCarFragment)fragment;
            MyCarFragment.loadMyCar();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            Fragment mFragment;

            switch (position) {
                case 0:
                    mFragment = new MyCarFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("USER_ID", USER_ID);
                    mFragment.setArguments(bundle);
                    return mFragment;
                //return new LogInFragment();
                default:
                    mFragment = new HistoryFragment();
                    Bundle bundle1 = new Bundle();
                    bundle1.putString("USER_ID", USER_ID);
                    mFragment.setArguments(bundle1);
                    return mFragment;
                //return new RegisterFragment();
            }
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
                    return "My Car";
                case 1:
                    return "History";
            }
            return null;
        }
    }

    /*@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            System.out.println("KEYCODE_BACK");

            moveTaskToBack(true);
        }
        return false;
    }*/

    public Fragment getActiveFragment(ViewPager container, int position) {
        String name = "android:switcher:" + container.getId() + ":" + position;
        return getSupportFragmentManager().findFragmentByTag(name);
    }
}
