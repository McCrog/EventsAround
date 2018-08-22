package io.mccrog.eventsaround.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class EventsWidgetRemoteViewService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new EventsWidgetRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}
