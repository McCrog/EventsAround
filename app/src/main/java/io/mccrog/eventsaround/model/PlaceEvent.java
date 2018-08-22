package io.mccrog.eventsaround.model;

import android.arch.persistence.room.Ignore;
import android.os.Parcel;
import android.os.Parcelable;

public class PlaceEvent implements Parcelable {

    private String address;
    private double latitude;
    private double longitude;

    @Ignore
    public PlaceEvent() {
    }

    public PlaceEvent(String address, double latitude, double longitude) {
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Ignore
    protected PlaceEvent(Parcel in) {
        address = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
    }


    public static final Creator<PlaceEvent> CREATOR = new Creator<PlaceEvent>() {
        @Override
        public PlaceEvent createFromParcel(Parcel in) {
            return new PlaceEvent(in);
        }

        @Override
        public PlaceEvent[] newArray(int size) {
            return new PlaceEvent[size];
        }
    };

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(address);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }

    public int describeContents() {
        return 0;
    }
}
