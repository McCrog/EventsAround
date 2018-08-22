package io.mccrog.eventsaround.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;
import java.util.List;

import io.mccrog.eventsaround.R;
import io.mccrog.eventsaround.model.Event;
import io.mccrog.eventsaround.utilities.DateTimeUtil;
import io.mccrog.eventsaround.utilities.EventTypeHelper;
import io.mccrog.eventsaround.utilities.InjectorUtils;

import static io.mccrog.eventsaround.utilities.Constants.KEY_EVENT_DATA;

class EventsWidgetRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private final List<Event> mEvents = new ArrayList<>();
    private final Context mContext;

    EventsWidgetRemoteViewsFactory(Context mContext, Intent intent) {
        this.mContext = mContext;
        int mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {
        setData(InjectorUtils.provideRepository(mContext).getAllEventsList());
    }

    @Override
    public void onDataSetChanged() {
        setData(InjectorUtils.provideRepository(mContext).getAllEventsList());
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return mEvents.size();
    }

    @Override
    public RemoteViews getViewAt(int i) {
        Event event = mEvents.get(i);
        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.widget_list_item);

        String endsDateTimeText = mContext.getResources().getString(R.string.ends_date_time);
        long endsDateTime = event.getEndsDateTime();
        long now = DateTimeUtil.getCurrentDateTimeInMillis();
        if (endsDateTime < now) {
            endsDateTimeText = mContext.getResources().getString(R.string.ended_date_time);
        }
        String dateTimeFormatted = DateTimeUtil.getFormattedDateTimeResourcesString(
                endsDateTimeText, endsDateTime);

        views.setTextViewText(R.id.widget_title_item_tv, event.getTitle());
        views.setTextViewText(R.id.widget_type_item_tv, EventTypeHelper.getEventType(mContext, event.getType()));
        views.setTextViewText(R.id.widget_date_time_item_tv, dateTimeFormatted);
        views.setTextViewText(R.id.widget_description_item_tv, event.getDescription());
        views.setTextViewText(R.id.widget_location_item_tv, event.getPlace().getAddress());

        Bundle extras = new Bundle();
        extras.putParcelable(KEY_EVENT_DATA, event);
        Intent fillInIntent = new Intent();
        fillInIntent.putExtras(extras);
        views.setOnClickFillInIntent(R.id.widget_linear_layout, fillInIntent);

        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    private void setData(List<Event> newEvents) {
        mEvents.clear();
        mEvents.addAll(newEvents);
    }
}
