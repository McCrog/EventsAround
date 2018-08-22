package io.mccrog.eventsaround.ui.helper;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.util.Objects;

import io.mccrog.eventsaround.R;
import io.mccrog.eventsaround.model.Event;
import io.mccrog.eventsaround.utilities.DateTimeUtil;
import io.mccrog.eventsaround.utilities.EventTypeHelper;

public class CustomInfoWindowGoogleMap implements GoogleMap.InfoWindowAdapter {

    private final Context mContext;

    public CustomInfoWindowGoogleMap(Context context){
        mContext = context;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        View view = ((Activity)mContext).getLayoutInflater()
                .inflate(R.layout.custom_info_contents, null);

        TextView title = view.findViewById(R.id.title);
        TextView description = view.findViewById(R.id.description);

        TextView type = view.findViewById(R.id.type);
        TextView location = view.findViewById(R.id.location);
        TextView endsDateTime = view.findViewById(R.id.ends_date_time);

        title.setText(marker.getTitle());
        description.setText(marker.getSnippet());

        Event event = (Event) marker.getTag();

        type.setText(EventTypeHelper.getEventType(mContext, Objects.requireNonNull(event).getType()));
        location.setText(event.getPlace().getAddress());

        endsDateTime.setText(DateTimeUtil.getFormattedDateTimeResourcesString(
                mContext.getResources().getString(R.string.ends_date_time),
                event.getEndsDateTime()));

        return view;
    }
}
