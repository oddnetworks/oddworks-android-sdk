package io.oddworks.oddsample;

import android.app.Application;

import io.oddworks.device.handler.OddMetricHandler;


public class OddSample extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Enable handling of published analytics events
        OddMetricHandler.enable(this);
    }
}
