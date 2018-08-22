package io.mccrog.eventsaround.utilities;

public class Constants {
    public static final String APP_PREFERENCES = "APP_PREFERENCES";
    public static final String APP_SEARCH_RADIUS = "APP_SEARCH_RADIUS";
    public static final String APP_USER_NAME = "APP_USER_NAME";
    public static final String APP_MAIN_ACTIVITY_NUMBER = "APP_MAIN_ACTIVITY_NUMBER";

    // Keys for storing activity state.
    public static final String KEY_LOCATION = "location";
    public static final String KEY_SEARCH_RADIUS = "radius";
    public static final String KEY_IS_EXPLORE = "isExplore";
    public static final String KEY_EVENT = "event";
    public static final String KEY_EVENT_DATA = "EventData";
    public static final String KEY_EVENT_PLACE_DATA = "EventPaceData";
    public static final String KEY_FRAGMENT = "Fragment";
    public static final String KEY_LIST = "List";
    public static final String KEY_MAP = "Map";

    // Default user name
    public static final String ANONYMOUS = "Anonymous";

    // A  default zoom to use when location permission is not granted.
    public static final int DEFAULT_ZOOM = 15;
    // The code for the ACCESS_FINE_LOCATION permission request.
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    // Firebase Realtime Database child keys
    public static final String EVENTS_CHILD = "events";
    public static final String GEO_FIRE_CHILD = "geofire";
}
