package com.example.brittanyhsu.bhspotify;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by brittanyhsu on 8/24/17.
 */

public class ViewPagerActivity extends AppCompatActivity implements FragmentCommunicator {
    FragmentPagerAdapter adapterViewPager;

    String TAG = "ViewPagerActivity";

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.viewpager);

        ViewPager vp = (ViewPager) findViewById(R.id.viewpager);
        adapterViewPager = new MyPagerAdapter(getSupportFragmentManager());
        vp.setAdapter(adapterViewPager);

        PagerTabStrip pagerTabStrip = (PagerTabStrip) findViewById(R.id.pager_tab);
        pagerTabStrip.setPadding(0,30,0,30);
        pagerTabStrip.setTabIndicatorColor(getColor(R.color.background));
    }

    @Override
    public void refreshFragment() {
        Log.d(TAG, "refreshFragment called");
        HistoryFragment frag = new HistoryFragment();
        FragmentManager manager = this.getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.fragment1_container ,frag).addToBackStack(null).commit();
    }

}
