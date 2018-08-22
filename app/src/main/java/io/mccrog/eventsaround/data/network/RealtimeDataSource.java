package io.mccrog.eventsaround.data.network;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.location.Location;
import android.support.annotation.NonNull;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.util.ArrayUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import io.mccrog.eventsaround.model.Event;

import static io.mccrog.eventsaround.utilities.Constants.EVENTS_CHILD;
import static io.mccrog.eventsaround.utilities.Constants.GEO_FIRE_CHILD;

public class RealtimeDataSource {

    // For Singleton instantiation
    private static final Object LOCK = new Object();
    private static RealtimeDataSource sInstance;

    private Location mLastKnownLocation;
    private double mSearchRadius;

    private DatabaseReference mDatabase;
    private GeoFire mGeoFire;
    private GeoQuery mGeoQuery;

    private final Map<String, Event> mEventsMap;
    private final Set<String> mEventIdsSetListeners;

    private ValueEventListener mEventValueListener;

    private boolean mInitialize = true;

    private final MutableLiveData<List<Event>> mDownloadedEvents;

    private RealtimeDataSource(int searchRadius) {
        this.mSearchRadius = searchRadius;
        mEventsMap = new HashMap<>();
        mEventIdsSetListeners = new HashSet<>();
        mDownloadedEvents = new MutableLiveData<>();
    }

    public synchronized static RealtimeDataSource getInstance(int searchRadius) {
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new RealtimeDataSource(searchRadius);
            }
        }
        return sInstance;
    }

    public void updateLocation(Location location, int searchRadius) {
        mLastKnownLocation = location;
        mSearchRadius = searchRadius;
        if (mInitialize) {
            init();
            mInitialize = false;
        } else {
            mGeoQuery.setLocation(new GeoLocation(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), mSearchRadius);
        }
    }

    public void unregister() {
        removeListeners();
    }

    private void init() {
        initFirebase();
        fetchData();
    }

    private void initFirebase() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mGeoFire = new GeoFire(mDatabase.child(GEO_FIRE_CHILD));

        initListeners();
    }

    private void fetchData() {
        GeoLocation currentLocation = new GeoLocation(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
        mGeoQuery = mGeoFire.queryAtLocation(currentLocation, mSearchRadius);
        mGeoQuery.addGeoQueryEventListener(mGeoQueryEventListener);
    }

    public LiveData<List<Event>> getDownloadedEvents() {
        return mDownloadedEvents;
    }

    private final GeoQueryEventListener mGeoQueryEventListener = new GeoQueryEventListener() {

        @Override
        public void onKeyEntered(String key, GeoLocation location) {
            addEventListener(key);
        }

        @Override
        public void onKeyExited(String key) {
            if (mEventIdsSetListeners.contains(key)) {
                mEventsMap.remove(key);
                removeListener(key);
                mEventIdsSetListeners.remove(key);

                mDownloadedEvents.postValue(ArrayUtils.toArrayList(mEventsMap.values()));
            }
        }

        @Override
        public void onKeyMoved(String key, GeoLocation location) {

        }

        @Override
        public void onGeoQueryReady() {

        }

        @Override
        public void onGeoQueryError(DatabaseError error) {

        }

        private void addEventListener(String eventKey) {
            mDatabase.child(EVENTS_CHILD).child(eventKey)
                    .addValueEventListener(mEventValueListener);

            mEventIdsSetListeners.add(eventKey);
        }
    };

    private void initListeners() {
        mEventValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Event event = dataSnapshot.getValue(Event.class);
                String eventKey = dataSnapshot.getKey();
                Objects.requireNonNull(event).setFirebaseId(eventKey);

                if (!mEventsMap.containsKey(eventKey)) {
                    newEventData(eventKey, event);
                }
            }

            private void newEventData(String eventKey, Event event) {
                mEventsMap.put(eventKey, event);
                mDownloadedEvents.postValue(ArrayUtils.toArrayList(mEventsMap.values()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }

    private void removeListeners() {
        if (mGeoQuery != null) {
            mGeoQuery.removeAllListeners();
        }

        for (String userId : mEventIdsSetListeners) {
            mDatabase.child(EVENTS_CHILD).child(userId)
                    .removeEventListener(mEventValueListener);
        }
    }

    private void removeListener(String eventKey) {
        mDatabase.child(EVENTS_CHILD).child(eventKey)
                .removeEventListener(mEventValueListener);
    }
}
