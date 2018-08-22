package io.mccrog.eventsaround.model;

public enum EventType {
    HOLIDAY(0),
    CONFERENCE(1),
    MEETING(2),
    MUSIC(3),
    MOVIE(4),
    GAME(5),
    WARNING(6),
    OTHER_EVENT(7);

    private final int code;

    EventType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
