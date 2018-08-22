package io.mccrog.eventsaround.data;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;
import static io.mccrog.eventsaround.utilities.Constants.ANONYMOUS;
import static io.mccrog.eventsaround.utilities.Constants.APP_PREFERENCES;
import static io.mccrog.eventsaround.utilities.Constants.APP_SEARCH_RADIUS;
import static io.mccrog.eventsaround.utilities.Constants.APP_USER_NAME;

public class AppPreferences {

    private final SharedPreferences mSharedPreferences;

    // For Singleton instantiation
    private static final Object LOCK = new Object();
    private static AppPreferences sInstance;

    private AppPreferences(Context context) {
        mSharedPreferences = context.getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE);
    }

    /**
     * Get the singleton for this class
     */
    public static AppPreferences getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new AppPreferences(context);
            }
        }
        return sInstance;
    }

    public void saveSearchRadius(int preference) {
        SharedPreferences.Editor editor = getEditor();
        editor.putInt(APP_SEARCH_RADIUS, preference);
        editor.apply();
    }

    public int getSearchRadius() {
        return mSharedPreferences.getInt(APP_SEARCH_RADIUS, 1);
    }

    public void saveUserName(String name) {
        SharedPreferences.Editor editor = getEditor();
        editor.putString(APP_USER_NAME, name);
        editor.apply();
    }

    public String getUserName() {
        return mSharedPreferences.getString(APP_USER_NAME, ANONYMOUS);
    }

    public void removeUserName() {
        SharedPreferences.Editor editor = getEditor();
        editor.putString(APP_USER_NAME, ANONYMOUS);
        editor.apply();
    }

    private SharedPreferences.Editor getEditor() {
        return mSharedPreferences.edit();
    }
}
