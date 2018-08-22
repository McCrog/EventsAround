package io.mccrog.eventsaround.data;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import io.mccrog.eventsaround.utilities.ConnectionHelper;

import static io.mccrog.eventsaround.utilities.ConnectionHelper.OnConnectionListener;

public class LocationTracker {

    // For Singleton instantiation
    private static final Object LOCK = new Object();
    private static LocationTracker sInstance;

    private final FusedLocationProviderClient mFusedLocationProviderClient;
    private final LocationRequest mLocationRequest;
    private final LocationCallback mLocationCallback;
    private final ConnectionHelper mConnectionHelper;

    private final float mSearchRadius;
    private final MutableLiveData<Location> mCurrentLocation;

    private LocationTracker(Context context, OnConnectionListener onConnectionListener, int searchRadius) {
        this.mConnectionHelper = new ConnectionHelper(context, onConnectionListener);
        this.mSearchRadius = searchRadius * 1000; // convert to meter

        // Construct a FusedLocationProviderClient.
        this.mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);

        mCurrentLocation = new MutableLiveData<>();

        this.mLocationRequest = new LocationRequest();
        this.mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    mCurrentLocation.postValue(location);
                }
            }
        };

        init();
    }

    public synchronized static LocationTracker getInstance(Context context,
                                                           OnConnectionListener onConnectionListener,
                                                           int searchRadius) {
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new LocationTracker(context, onConnectionListener, searchRadius);
            }
        }
        return sInstance;
    }

    private void init() {
        if (mConnectionHelper.checkLocationService()) {
            if (mConnectionHelper.checkInternetConnection()) {
                // Get the current location of the device and set the position of the map.
                execute();
            }
        }
    }

    public void execute() {
        createLocationRequest();
        startLocationUpdates();
    }

    public LiveData<Location> getCurrentLocation() {
        return mCurrentLocation;
    }

    /**
     * Creating location request object
     */
    private void createLocationRequest() {
        mLocationRequest.setInterval(0);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(mSearchRadius);
    }

    /**
     * Starting the location updates
     */
    private void startLocationUpdates() {
        try {
            mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    /**
     * Stopping location updates
     */
    public void stopLocationUpdates() {
        mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
    }
}
