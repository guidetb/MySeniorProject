package com.example.guide.seniorproject;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by guide on 3/19/2017.
 */

public class Pager extends FragmentStatePagerAdapter {
    int tabcount;

    public Pager(FragmentManager fm, int tabcount) {
        super(fm);

        this.tabcount = tabcount;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                TescoTab tab1 = new TescoTab();
                return tab1;
            case 1:
                BIGCTab tab2 = new BIGCTab();
                return tab2;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return tabcount;
    }
}
