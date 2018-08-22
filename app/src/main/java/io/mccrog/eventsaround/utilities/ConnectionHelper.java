package io.mccrog.eventsaround.utilities;

import android.content.Context;

import io.mccrog.eventsaround.R;

public class ConnectionHelper {

    private final Context mContext;
    private final OnConnectionListener mListener;
    private AlertDialogManager mAlert;

    public ConnectionHelper(Context context, OnConnectionListener listener) {
        this.mContext = context;
        this.mListener = listener;
    }
    // Check GPS is enabled
    public boolean checkLocationService() {
        LocationServiceDetector ls = new LocationServiceDetector(mContext);

        if (!ls.isLocationServiceEnable()) {
            mAlert = new AlertDialogManager();
            // Internet Connection is not present
            mAlert.showAlertDialog(mContext, mContext.getString(R.string.location_error_title),
                    mContext.getString(R.string.location_error_message), false, (dialogInterface, i) ->
                            mListener.onLocationServiceFailed());
            return false;
        }
        return true;
    }

    // Check whether the Internet is available
    public boolean checkInternetConnection() {
        ConnectionDetector cd = new ConnectionDetector(mContext);
        // Check if Internet present
        if (!cd.isConnectingToInternet()) {
            mAlert = new AlertDialogManager();
            // Internet Connection is not present
            mAlert.showAlertDialog(mContext, mContext.getString(R.string.internet_error_title),
                    mContext.getString(R.string.internet_error_message), false, (dialogInterface, i) ->
                            mListener.onInternetConnectionFailed());
            return false;
        }
        return true;
    }

    public interface OnConnectionListener {
        void onLocationServiceFailed();
        void onInternetConnectionFailed();
    }
}
