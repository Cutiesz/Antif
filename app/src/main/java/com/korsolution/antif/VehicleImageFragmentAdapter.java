package com.korsolution.antif;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

class VehicleImageFragmentAdapter extends FragmentPagerAdapter {
    protected static final String[] CONTENT = new String[] { "This", "Is", "A", "Test", };

    private int mCount = CONTENT.length;

    private AccountDBClass AccountDB;
    private String[][] arrData;
    private String USER_ID;
    private Context context;

    public VehicleImageFragmentAdapter(FragmentManager fm, Context c) {
        super(fm);

        context = c;
        AccountDB = new AccountDBClass(context);
    }

    @Override
    public Fragment getItem(int position) {
        //return TestFragment.newInstance(CONTENT[position % CONTENT.length]);

        Fragment mFragment;

        AccountDB = new AccountDBClass(context);
        arrData = AccountDB.SelectAllAccount();
        if (arrData != null) {
            USER_ID = arrData[0][1].toString();
        }

        switch (position) {
            case 0:
                mFragment = new VehiclePhoto1Fragment();
                Bundle bundle = new Bundle();
                bundle.putString("USER_ID", USER_ID);
                mFragment.setArguments(bundle);
                return mFragment;
            case 1:
                mFragment = new VehiclePhoto2Fragment();
                Bundle bundle1 = new Bundle();
                bundle1.putString("USER_ID", USER_ID);
                mFragment.setArguments(bundle1);
                return mFragment;
            case 2:
                mFragment = new VehiclePhoto3Fragment();
                Bundle bundle2 = new Bundle();
                bundle2.putString("USER_ID", USER_ID);
                mFragment.setArguments(bundle2);
                return mFragment;
            default:
                mFragment = new VehiclePhoto4Fragment();
                Bundle bundle3 = new Bundle();
                bundle3.putString("USER_ID", USER_ID);
                mFragment.setArguments(bundle3);
                return mFragment;
        }
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public CharSequence getPageTitle(int position) {
      //return TestFragmentAdapter.CONTENT[position % CONTENT.length];

        switch (position) {
            case 0:
                return "Vehicle Photo 1";
            case 1:
                return "Vehicle Photo 2";
            case 2:
                return "Vehicle Photo 3";
            case 3:
                return "Vehicle Photo 4";
        }
        return null;
    }

    public void setCount(int count) {
        if (count > 0 && count <= 10) {
            mCount = count;
            notifyDataSetChanged();
        }
    }
}