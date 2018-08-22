package io.mccrog.eventsaround.data.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import io.mccrog.eventsaround.model.Event;

@Dao
public interface EventDao {
    // Returns a list of all events in the database
    @Query("SELECT * FROM events")
    LiveData<List<Event>> getAll();

    @Query("SELECT * FROM events")
    List<Event> getAllList();

    @Insert
    void insert(Event event);

    @Query("DELETE FROM events WHERE title = :title AND ends_date_time = :endsDateTime AND type = :typeCode")
    void deleteEvent(String title, long endsDateTime, int typeCode);

    @Query("DELETE FROM events WHERE ends_date_time < :endsDateTime")
    void deleteOld(long endsDateTime);

    @Query("DELETE FROM events")
    void deleteAll();

    @Query("SELECT * FROM events WHERE title = :title AND ends_date_time = :endsDateTime AND type = :typeCode")
    boolean isEventExist(String title, long endsDateTime, int typeCode);
}
