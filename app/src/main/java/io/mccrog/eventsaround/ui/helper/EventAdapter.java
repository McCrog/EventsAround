package io.mccrog.eventsaround.ui.helper;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.mccrog.eventsaround.R;
import io.mccrog.eventsaround.model.Event;
import io.mccrog.eventsaround.utilities.DateTimeUtil;
import io.mccrog.eventsaround.utilities.EventTypeHelper;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private final List<Event> mEvents;
    private final Context mContext;
    private final EventOnClickHandler mClickHandler;
    private final long mNow;

    /**
     * The interface that receives onClick messages.
     */
    public interface EventOnClickHandler {
        void onItemClick(int index);

        void onShareClick(int index);
    }

    public EventAdapter(Context context, EventOnClickHandler mClickHandler) {
        this.mContext = context;
        this.mClickHandler = mClickHandler;
        mEvents = new ArrayList<>();
        mNow = DateTimeUtil.getCurrentDateTimeInMillis();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = mEvents.get(position);

        holder.mEventTitle.setText(event.getTitle());
        holder.mEventDescription.setText(event.getDescription());
        holder.mEventLocation.setText(event.getPlace().getAddress());

        String endsDateTimeText = mContext.getResources().getString(R.string.ends_date_time);
        long endsDateTime = event.getEndsDateTime();
        if (endsDateTime < mNow) {
            endsDateTimeText = mContext.getResources().getString(R.string.ended_date_time);
            holder.mEventEndsDateTime.setTextColor(ContextCompat.getColor(mContext, R.color.red));
        } else {
            holder.mEventEndsDateTime.setTextColor(ContextCompat.getColor(mContext, R.color.grey));
        }
        holder.mEventEndsDateTime.setText(DateTimeUtil.getFormattedDateTimeResourcesString(
                endsDateTimeText, endsDateTime));

        holder.mEventType.setText(EventTypeHelper.getEventType(mContext, event.getType()));

        switch (event.getType()) {
            case HOLIDAY:
                holder.mEventTypeImage.setImageResource(R.drawable.fireworks);
                break;
            case CONFERENCE:
                holder.mEventTypeImage.setImageResource(R.drawable.conference_second);
                break;
            case MEETING:
                holder.mEventTypeImage.setImageResource(R.drawable.meeting);
                break;
            case MUSIC:
                holder.mEventTypeImage.setImageResource(R.drawable.music);
                break;
            case MOVIE:
                holder.mEventTypeImage.setImageResource(R.drawable.cinema);
                break;
            case GAME:
                holder.mEventTypeImage.setImageResource(R.drawable.game);
                break;
            case WARNING:
                holder.mEventTypeImage.setImageResource(R.drawable.speaker);
                break;
            case OTHER_EVENT:
                holder.mEventTypeImage.setImageResource(R.drawable.calendar);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mEvents.size();
    }

    public void setData(List<Event> newEvents) {
        mEvents.clear();
        mEvents.addAll(newEvents);
        notifyDataSetChanged();
    }

    public Event getItem(int position) {
        if (mEvents.size() > 0) {
            return mEvents.get(position);
        }
        return null;
    }

    public void removeItem(int position) {
        mEvents.remove(position);
        notifyItemRemoved(position);
    }

    public class EventViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.view_foreground)
        public ConstraintLayout mViewForeground;
        @BindView(R.id.event_type_image_card_view)
        ImageView mEventTypeImage;
        @BindView(R.id.event_title_card_view)
        TextView mEventTitle;
        @BindView(R.id.event_description_card_view)
        TextView mEventDescription;
        @BindView(R.id.event_location_card_view)
        TextView mEventLocation;
        @BindView(R.id.event_type_card_view)
        TextView mEventType;
        @BindView(R.id.event_date_time_card_view)
        TextView mEventEndsDateTime;
        @BindView(R.id.event_share_button_card_view)
        Button mEventShare;

        EventViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);
            mEventShare.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int adapterPosition = getAdapterPosition();
            if (view.getId() == R.id.event_share_button_card_view) {
                mClickHandler.onShareClick(adapterPosition);
            } else {
                mClickHandler.onItemClick(adapterPosition);
            }
        }
    }
}
