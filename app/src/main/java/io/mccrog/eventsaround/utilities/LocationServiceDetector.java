package io.mccrog.eventsaround.utilities;

import android.content.Context;
import android.location.LocationManager;

import java.util.Objects;

import static android.content.Context.LOCATION_SERVICE;

class LocationServiceDetector {

    private final Context mContext;

    LocationServiceDetector(Context context) {
        this.mContext = context;
    }

    /**
     * Checking location service is enabled
     **/
    public boolean isLocationServiceEnable() {
        LocationManager lm = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
        return Objects.requireNonNull(lm).isProviderEnabled(LocationManager.GPS_PROVIDER);
    }
}
