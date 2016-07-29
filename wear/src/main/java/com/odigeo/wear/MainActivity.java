package com.odigeo.wear;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.example.data.DataLayerResultListener;
import com.example.data.SendToDataLayerHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Wearable;

public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, DataApi.DataListener{

    private static final String WEARABLE_DATA_PATH = "/wearable_data";
    private static final String HANDHELD_DATA_PATH = "/handheld_data";
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initGoogleApiClient();
    }

    private void initGoogleApiClient() {
        if (mGoogleApiClient == null) {
            Log.d(getClass().getSimpleName(), "Building google client id...");
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            Log.d(getClass().getSimpleName(), "Google Client ID = " + mGoogleApiClient.toString());
        }

        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }

        Log.d(getClass().getSimpleName(), "Google Client ID = " + mGoogleApiClient.toString());
    }

    public void sendPing() {
        // Create a DataMap object and send it to the data layer
        DataMap dataMap = new DataMap();
        dataMap.putString("ping", "ping" + String.valueOf(System.currentTimeMillis()));
        dataMap.putLong("time", System.currentTimeMillis());
        //Requires a new thread to avoid blocking the UI

        new SendToDataLayerHelper(HANDHELD_DATA_PATH, dataMap, mGoogleApiClient, new DataLayerResultListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError() {

            }
        }).run();

    }

    // Placeholders for required connection callbacks
    @Override
    public void onConnectionSuspended(int cause) {
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.v(getClass().getSimpleName(), "OnConnected entered");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    // Disconnect from the data layer when the Activity stops
    @Override
    public void onDestroy() {
        if (null != mGoogleApiClient && mGoogleApiClient.isConnected()) {
            Log.d(getClass().getSimpleName(), "onDestroy: Disconnecting mGoogleApiClient");
            mGoogleApiClient.disconnect();
        }

        super.onDestroy();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        Log.i(getClass().getSimpleName(), "Data");
    }
}