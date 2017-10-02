package com.example.brittanyhsu.bhspotify;

import android.app.Application;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by brittanyhsu on 10/1/17.
 */

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
        .setDefaultFontPath("fonts/OpenSans-Regular.ttf")
        .setFontAttrId(R.attr.fontPath)
        .build());
    }
}
