package com.odigeo.wear;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.DotsPageIndicator;
import android.support.wearable.view.GridViewPager;
import android.support.wearable.view.WatchViewStub;

import com.odigeo.wear.views.adapters.BookingInfoAdapter;

/**
 * Created by javier.rebollo on 1/7/16.
 */
public class HomeNavigator extends Activity{

    private GridViewPager mGvpPages;
    private DotsPageIndicator mDpiIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_view);

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mGvpPages = (GridViewPager) findViewById(R.id.gvpPages);
                mDpiIndicator = (DotsPageIndicator) findViewById(R.id.dpiIndicator);

                mGvpPages.setAdapter(new BookingInfoAdapter(getApplicationContext()));
                mDpiIndicator.setPager(mGvpPages);
            }
        });
    }

}
