package io.mccrog.eventsaround.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.location.Location;

import java.util.List;

import io.mccrog.eventsaround.data.Repository;
import io.mccrog.eventsaround.model.Event;

import static io.mccrog.eventsaround.utilities.ConnectionHelper.OnConnectionListener;

public class EventsViewModel extends ViewModel {

    private final LiveData<List<Event>> mEvents;
    private final Repository mRepository;

    EventsViewModel(Repository repository) {
        mRepository = repository;
        mEvents = mRepository.getAllEvents();
    }

    public LiveData<List<Event>> getEvents() {
        return mEvents;
    }

    public void deleteEvent(Event event) {
        mRepository.deleteEvent(event);
    }

    public void saveEvent(Event event) {
        mRepository.saveEventInFirebase(event);
    }

    public void insertEvent(Event event) {
        mRepository.insertEvent(event);
    }

    public LiveData<Boolean> isEventExist(Event event) {
        return mRepository.isExist(event);
    }

    public String getUserName() {
        return mRepository.getUserName();
    }

    public int getSearchRadius() {
        return mRepository.getSearchRadius();
    }

    public void saveUserName(String userName) {
        mRepository.saveUserName(userName);
    }

    public void saveSearchRadius(int searchRadius) {
        mRepository.saveSearchRadius(searchRadius);
    }

    public void removeUserName() {
        mRepository.removeUserName();
    }

    public boolean startLocationTracker(Context context, OnConnectionListener onConnectionListener, int searchRadius) {
        return mRepository.startLocationTracker(context, onConnectionListener, searchRadius);
    }

    public void executeLocationTracker() {
        mRepository.executeLocationTracker();
    }

    public void stopLocationTracker() {
        mRepository.stopLocationTracker();
    }

    public void updateDataSourceLocation(Location lastKnownLocation, int searchRadius) {
        mRepository.updateRealtimeDataSourceLocation(lastKnownLocation, searchRadius);
    }

    public void unregisterDataSourceListener() {
        mRepository.unregisterRealtimeDataSourceListener();
    }

    public LiveData<Location> getCurrentLocation() {
        return mRepository.getCurrentLocation();
    }

    public LiveData<List<Event>> getDownloadedEvents() {
        return mRepository.getDownloadedEvents();
    }
}
