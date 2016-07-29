package com.example.data;

import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

public class SendToDataLayerHelper {

    private String mPath;
    private DataMap mDataMap;
    private GoogleApiClient mGoogleApiClient;
    private DataLayerResultListener mDataLayerResultListener;

    // Constructor for sending data objects to the data layer
    public SendToDataLayerHelper(String p, DataMap data, GoogleApiClient googleApiClient, DataLayerResultListener dataLayerResultListener) {
        mPath = p;
        mDataMap = data;
        mGoogleApiClient = googleApiClient;
        mDataLayerResultListener = dataLayerResultListener;
    }

    public void run() {
        Log.i(getClass().getSimpleName(), "Run!!");
        //NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient);
        PendingResult<NodeApi.GetConnectedNodesResult> nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient);
        nodes.setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                Log.i(getClass().getSimpleName(), "OnResult nodes");

                for (Node node : getConnectedNodesResult.getNodes()) {
                    Log.i(getClass().getSimpleName(), "Node: " + node.getDisplayName());
                    final Node node2 = node;

                    // Construct a DataRequest and send over the data layer
                    PutDataMapRequest putDMR = PutDataMapRequest.create(mPath);
                    putDMR.getDataMap().putAll(mDataMap);
                    PutDataRequest request = putDMR.asPutDataRequest();
                    request.setUrgent();

                    PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi.putDataItem(mGoogleApiClient, request);
                    pendingResult.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                        @Override
                        public void onResult(DataApi.DataItemResult dataItemResult) {
                            if (dataItemResult.getStatus().isSuccess()) {
                                DataMap phoneDataMap = DataMapItem.fromDataItem(dataItemResult.getDataItem()).getDataMap();
                                Log.v(getClass().getSimpleName(), "DataMap: " + mDataMap + " sent to: " + node2.getDisplayName());
                                Log.v(getClass().getSimpleName(), "PhoneDataMap: " + phoneDataMap + " sent to: " + node2.getDisplayName());

                                mDataLayerResultListener.onSuccess();
                            } else {
                                // Log an error
                                Log.v(getClass().getSimpleName(), "ERROR: failed to send DataMap");

                                mDataLayerResultListener.onError();
                            }
                        }
                    });
                }
            }
        });
    }
}
