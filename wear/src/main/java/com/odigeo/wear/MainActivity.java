package com.odigeo.wear;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, DataApi.DataListener{

    private static final String WEARABLE_DATA_PATH = "/wearable_data";
    private static final String HANDHELD_DATA_PATH = "/handheld_data";
    GoogleApiClient googleClient;
    private SendToDataLayerThread s;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initGoogleApiClient();
    }

    private void initGoogleApiClient() {
        if (googleClient == null) {
            Log.d(getClass().getSimpleName(), "Building google client id...");
            googleClient = new GoogleApiClient.Builder(this)
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            Log.d(getClass().getSimpleName(), "Google client id = " + googleClient.toString());
        }

        if (!googleClient.isConnected()) {
            googleClient.connect();
        }

        Log.d(getClass().getSimpleName(), "Google Client ID = " + googleClient.toString());
    }

    public void sendPing() {
        // Create a DataMap object and send it to the data layer
        DataMap dataMap = new DataMap();
        dataMap.putString("ping", "ping" + String.valueOf(System.currentTimeMillis()));
        dataMap.putLong("time", System.currentTimeMillis());
        //Requires a new thread to avoid blocking the UI

        s = new SendToDataLayerThread(HANDHELD_DATA_PATH, dataMap);
        s.run();
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
        if (null != googleClient && googleClient.isConnected()) {
            Log.d(getClass().getSimpleName(), "onDestroy: Disconnecting googleClient");
            googleClient.disconnect();
        }

        super.onDestroy();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        Log.i(getClass().getSimpleName(), "Data");
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
            Log.i(getClass().getSimpleName(), "Run!!");
            //NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(googleClient);
            PendingResult<NodeApi.GetConnectedNodesResult> nodes = Wearable.NodeApi.getConnectedNodes(googleClient);
            nodes.setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                @Override
                public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                    Log.i(getClass().getSimpleName(), "OnResult nodes");

                    for (Node node : getConnectedNodesResult.getNodes()) {
                        Log.i(getClass().getSimpleName(), "Node: " + node.getDisplayName());
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
                                    DataMap phoneDataMap = DataMapItem.fromDataItem(dataItemResult.getDataItem()).getDataMap();
                                    Log.v(getClass().getSimpleName(), "DataMap: " + dataMap + " sent to: " + node2.getDisplayName());
                                    Log.v(getClass().getSimpleName(), "PhoneDataMap: " + phoneDataMap + " sent to: " + node2.getDisplayName());
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
