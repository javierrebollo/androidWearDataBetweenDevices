package com.odigeo.wear;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

public class BookingListenerService extends WearableListenerService implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String WEARABLE_DATA_PATH = "/wearable_data";
    private static final String HANDHELD_DATA_PATH = "/handheld_data";
    GoogleApiClient googleClient;
    private SendToDataLayerThread s;

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
                    s = new SendToDataLayerThread(WEARABLE_DATA_PATH, dataMap);
                    s.run();
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

    class SendToDataLayerThread {
        String path;
        DataMap dataMap;

        // Constructor for sending data objects to the data layer
        SendToDataLayerThread(String p, DataMap data) {
            path = p;
            dataMap = data;
        }

        public void run() {
            //NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(googleClient);
            PendingResult<NodeApi.GetConnectedNodesResult> nodes = Wearable.NodeApi.getConnectedNodes(googleClient);
            nodes.setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                @Override
                public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                    for (Node node : getConnectedNodesResult.getNodes()) {

                        final Node node2 = node;

                        // Construct a DataRequest and send over the data layer
                        PutDataMapRequest putDMR = PutDataMapRequest.create(path);
                        putDMR.getDataMap().putAll(dataMap);
                        PutDataRequest request = putDMR.asPutDataRequest();
                        request.setUrgent();

                        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi.putDataItem(googleClient, request);
                        pendingResult.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                            @Override
                            public void onResult(DataApi.DataItemResult dataItemResult) {
                                if (dataItemResult.getStatus().isSuccess()) {
                                    Log.v(getClass().getSimpleName(), "DataMap: " + dataMap + " sent to: " + node2.getDisplayName());
                                } else {
                                    // Log an error
                                    Log.v(getClass().getSimpleName(), "ERROR: failed to send DataMap");
                                }
                            }
                        });
                    }
                }
            });
        }
    }
}