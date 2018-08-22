package io.mccrog.eventsaround.data;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import java.util.concurrent.ExecutionException;

import io.mccrog.eventsaround.data.database.EventDao;
import io.mccrog.eventsaround.data.network.RealtimeDataSource;
import io.mccrog.eventsaround.model.Event;

import static io.mccrog.eventsaround.utilities.ConnectionHelper.OnConnectionListener;
import static io.mccrog.eventsaround.utilities.Constants.EVENTS_CHILD;

public class Repository {

    // For Singleton instantiation
    private static final Object LOCK = new Object();
    private static Repository sInstance;

    private final EventDao mEventDao;
    private final AppPreferences mAppPreferences;
    private final AppExecutors mExecutors;

    private LocationTracker mLocationTracker;
    private RealtimeDataSource mRealtimeDataSource;

    private Repository(EventDao eventDao, AppPreferences appPreferences, AppExecutors executors) {
        mEventDao = eventDao;
        mAppPreferences = appPreferences;
        mExecutors = executors;
    }

    public synchronized static Repository getInstance(EventDao eventDao, AppPreferences appPreferences,
                                                      AppExecutors executors) {
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new Repository(eventDao, appPreferences, executors);
            }
        }
        return sInstance;
    }

    /**
     * Favorites. Returns all Events from the local database
     */
    public LiveData<List<Event>> getAllEvents() {
        return mEventDao.getAll();
    }

    /**
     * Widget. Returns all Events from the local database
     */
    public List<Event> getAllEventsList() {
        AsyncTask<Void, Void, List<Event>> task = new AsyncTask<Void, Void, List<Event>>() {
            @Override
            protected List<Event> doInBackground(Void... voids) {
                return mEventDao.getAllList();
            }

            @Override
            protected void onPostExecute(List<Event> events) {
                super.onPostExecute(events);
            }
        };

        task.execute();

        try {
            return task.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return null;

    }

    /**
     * Saving an Event to the Firebase Realtime Database
     * @param event new data
     */
    public void saveEventInFirebase(Event event) {
        DatabaseReference mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mFirebaseDatabaseReference.child(EVENTS_CHILD).push().setValue(event);
        insertEvent(event);
    }

    /**
     * Saving an Event to a Local Database
     * @param event new data
     */
    public void insertEvent(Event event) {
        mExecutors.diskIO().execute(() -> mEventDao.insert(event));
    }

    /**
     * Deleting an Event from a Local Database
     * @param event data
     */
    public void deleteEvent(Event event) {
        mExecutors.diskIO().execute(() ->
                mEventDao.deleteEvent(event.getTitle(), event.getEndsDateTime(), event.getType().getCode()));
    }

    /**
     * Check for an Event in the local database
     * @param event data
     */
    public LiveData<Boolean> isExist(Event event) {
        MutableLiveData<Boolean> isExist = new MutableLiveData<>();
        mExecutors.diskIO().execute(() ->
                isExist.postValue(mEventDao.isEventExist(event.getTitle(), event.getEndsDateTime(), event.getType().getCode())));

        return isExist;
    }

    public String getUserName() {
        return mAppPreferences.getUserName();
    }

    public int getSearchRadius() {
        return mAppPreferences.getSearchRadius();
    }

    public void saveUserName(String userName) {
        mAppPreferences.saveUserName(userName);
    }

    public void saveSearchRadius(int searchRadius) {
        mAppPreferences.saveSearchRadius(searchRadius);
    }

    public void removeUserName() {
        mAppPreferences.removeUserName();
    }

    public boolean startLocationTracker(Context context, OnConnectionListener onConnectionListener, int searchRadius) {
            mLocationTracker = LocationTracker.getInstance(context, onConnectionListener, searchRadius);
            initRealtimeDataSource();
            return true;
    }

    public void executeLocationTracker() {
        if (mLocationTracker != null) {
            mLocationTracker.execute();
        }
    }

    public void stopLocationTracker() {
        if (mLocationTracker != null) {
            mLocationTracker.stopLocationUpdates();
        }
    }

    private void initRealtimeDataSource() {
        mRealtimeDataSource = RealtimeDataSource.getInstance(getSearchRadius());
    }

    public void updateRealtimeDataSourceLocation(Location location, int searchRadius) {
        mRealtimeDataSource.updateLocation(location, searchRadius);
    }

    public void unregisterRealtimeDataSourceListener() {
        if (mRealtimeDataSource != null) {
            mRealtimeDataSource.unregister();
        }
    }

    public LiveData<Location> getCurrentLocation() {
        return mLocationTracker.getCurrentLocation();
    }

    public LiveData<List<Event>> getDownloadedEvents() {
        return mRealtimeDataSource.getDownloadedEvents();
    }
}
