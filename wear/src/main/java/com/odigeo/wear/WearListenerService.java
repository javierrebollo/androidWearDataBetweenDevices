package com.odigeo.wear;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

public class WearListenerService extends WearableListenerService implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String WEARABLE_DATA_PATH = "/wearable_data";
    GoogleApiClient googleClient;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(getClass().getSimpleName(), "WearService Created");

        initGoogleApiClient();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d(getClass().getSimpleName(), "In dataChanged");

        DataMap dataMap;
        for (DataEvent event : dataEvents) {

            // Check the data type
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // Check the data path
                String path = event.getDataItem().getUri().getPath();
                if (path.equals(WEARABLE_DATA_PATH)) {
                    dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                    Log.v(getClass().getSimpleName(), "Path phone: " + path);
                    Log.v(getClass().getSimpleName(), "DataMap received from watch: " + dataMap);

                    Intent messageIntent = new Intent();
                    messageIntent.setAction(Intent.ACTION_SEND);
                    messageIntent.putExtra("time", System.currentTimeMillis());
                    messageIntent.putExtra("DataMap", dataMap.toBundle());
                    LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent);

                    // Create a DataMap object and send it to the data layer
                    dataMap = new DataMap();
                    dataMap.putString("Pong", "Pong" + String.valueOf(System.currentTimeMillis()));
                    dataMap.putLong("time", System.currentTimeMillis());
                }
            }
        }
    }

    private void initGoogleApiClient() {
        // Build a new GoogleApiClient for the the Wearable API

        Log.d(getClass().getSimpleName(), "Initialaizing GoogleClient");

        if (googleClient == null) {
            googleClient = new GoogleApiClient.Builder(this)
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }

        if (!googleClient.isConnected()) {
            Log.d(getClass().getSimpleName(), "Tring to connect to GoogleApi...");

            googleClient.connect();

        }

        Log.d(getClass().getSimpleName(), "Google Client ID = " + googleClient.toString());
    }

    // Disconnect from the data layer when the Activity stops
    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(getClass().getSimpleName(), "WearService: onDestroy");

        if (null != googleClient && googleClient.isConnected()) {
            googleClient.disconnect();
        }


    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(getClass().getSimpleName(), "onConnected entered");
        Log.d(getClass().getSimpleName(), "GoogleAPI now status:" + googleClient.isConnected());
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.e(getClass().getSimpleName(), "Connection to google api has failed. " + result.getErrorMessage());
    }
}