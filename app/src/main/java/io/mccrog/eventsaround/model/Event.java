package io.mccrog.eventsaround.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;
import android.os.Parcel;
import android.os.Parcelable;

import io.mccrog.eventsaround.data.database.EventTypeConverter;

/**
 * Created by McCrog on 08/07/2018.
 */

@Entity(tableName = "events")
public class Event implements Parcelable {

    @PrimaryKey(autoGenerate = true)
    private int id;
    @TypeConverters(EventTypeConverter.class)
    private EventType type;
    private String title;
    private String description;
    @Embedded
    private PlaceEvent place;
    @ColumnInfo(name = "ends_date_time")
    private long endsDateTime;
    @Ignore
    private String firebaseId;

    @Ignore
    public Event() {
    }

    @Ignore
    public Event(EventType type, String title, String description, PlaceEvent place, long endsDateTime) {
        this.type = type;
        this.title = title;
        this.description = description;
        this.place = place;
        this.endsDateTime = endsDateTime;
        this.firebaseId = "0";
    }

    // Constructor used by Room to create Event
    public Event(int id, EventType type, String title, String description, PlaceEvent place, long endsDateTime) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.description = description;
        this.place = place;
        this.endsDateTime = endsDateTime;
        this.firebaseId = "0";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public EventType getType() {
        return type;
    }

    public void setType(EventType type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public PlaceEvent getPlace() {
        return place;
    }

    public void setPlace(PlaceEvent place) {
        this.place = place;
    }

    public long getEndsDateTime() {
        return endsDateTime;
    }

    public void setEndsDateTime(long endsDateTime) {
        this.endsDateTime = endsDateTime;
    }

    public String getFirebaseId() {
        return firebaseId;
    }

    public void setFirebaseId(String firebaseId) {
        this.firebaseId = firebaseId;
    }

    @Ignore
    protected Event(Parcel in) {
        type = EventType.valueOf(in.readString());
        title = in.readString();
        description = in.readString();
        place = in.readParcelable(PlaceEvent.class.getClassLoader());
        endsDateTime = in.readLong();
        firebaseId = in.readString();
    }


    public static final Creator<Event> CREATOR = new Creator<Event>() {
        @Override
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.type.name());
        dest.writeString(title);
        dest.writeString(description);
        dest.writeParcelable(place, 0);
        dest.writeLong(endsDateTime);
        dest.writeString(firebaseId);
    }

    public int describeContents() {
        return 0;
    }
}
