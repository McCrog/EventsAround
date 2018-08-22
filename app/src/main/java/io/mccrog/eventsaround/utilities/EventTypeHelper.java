package io.mccrog.eventsaround.utilities;

import android.content.Context;

import io.mccrog.eventsaround.R;
import io.mccrog.eventsaround.model.EventType;

public class EventTypeHelper {

    private EventTypeHelper() {
    }

    public static String getEventType(Context context, EventType type) {
        int eventOrdinal = type.ordinal() + 1;
        return context.getResources().getStringArray(R.array.events_type)[eventOrdinal];
    }
}
