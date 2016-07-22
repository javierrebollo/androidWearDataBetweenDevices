package com.odigeo.wear;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.data.DataLayerResultListener;
import com.example.data.SendToDataLayerHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

public class BookingListenerService extends WearableListenerService implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String WEARABLE_DATA_PATH = "/wearable_data";
    private static final String HANDHELD_DATA_PATH = "/handheld_data";
    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(getClass().getSimpleName(), "WearService Created");

        initGoogleApiClient();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d(getClass().getSimpleName(), "In dataChanged");

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

        DataMap dataMap;
        for (DataEvent event : dataEvents) {

            // Check the data type
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // Check the data path
                String path = event.getDataItem().getUri().getPath();
                if (path.equals(HANDHELD_DATA_PATH)) {
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

                    //Requires a new thread to avoid blocking the UI
                    new SendToDataLayerHelper(WEARABLE_DATA_PATH, dataMap, mGoogleApiClient, new DataLayerResultListener() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {

                        }
                    }).run();
                }
            }
        }
    }

    private void initGoogleApiClient() {
        // Build a new GoogleApiClient for the the Wearable API

        Log.d(getClass().getSimpleName(), "Initialaizing GoogleClient");

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }

        if (!mGoogleApiClient.isConnected()) {
            Log.d(getClass().getSimpleName(), "Tring to connect to GoogleApi...");

            mGoogleApiClient.connect();

        }


        Log.d(getClass().getSimpleName(), "Google Client ID = " + mGoogleApiClient.toString());
    }

    // Disconnect from the data layer when the Activity stops
    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(getClass().getSimpleName(), "WearService: onDestroy");

        if (null != mGoogleApiClient && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(getClass().getSimpleName(), "onConnected entered");
        Log.d(getClass().getSimpleName(), "GoogleAPI now status:" + mGoogleApiClient.isConnected());
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.e(getClass().getSimpleName(), "Connection to google api has failed. " + result.getErrorMessage());
    }
}