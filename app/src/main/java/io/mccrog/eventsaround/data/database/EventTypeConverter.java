package io.mccrog.eventsaround.data.database;

import android.arch.persistence.room.TypeConverter;

import io.mccrog.eventsaround.model.EventType;

public class EventTypeConverter {
    @TypeConverter
    public static EventType toEventType(int code) {
        if (code == EventType.HOLIDAY.getCode()) {
            return EventType.HOLIDAY;
        } else if (code == EventType.CONFERENCE.getCode()) {
            return EventType.CONFERENCE;
        } else if (code == EventType.MEETING.getCode()) {
            return EventType.MEETING;
        } else if (code == EventType.MUSIC.getCode()) {
            return EventType.MUSIC;
        } else if (code == EventType.MOVIE.getCode()) {
            return EventType.MOVIE;
        } else if (code == EventType.GAME.getCode()) {
            return EventType.GAME;
        } else if (code == EventType.WARNING.getCode()) {
            return EventType.WARNING;
        } else if (code == EventType.OTHER_EVENT.getCode()) {
            return EventType.OTHER_EVENT;
        } else {
            throw new IllegalArgumentException("Could not recognize event");
        }
    }

    @TypeConverter
    public static int toInt(EventType type) {
        return type.getCode();
    }
}
