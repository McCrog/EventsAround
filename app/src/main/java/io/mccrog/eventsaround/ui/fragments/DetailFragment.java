package io.mccrog.eventsaround.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.mccrog.eventsaround.R;
import io.mccrog.eventsaround.model.Event;
import io.mccrog.eventsaround.utilities.DateTimeUtil;
import io.mccrog.eventsaround.utilities.EventTypeHelper;

import static io.mccrog.eventsaround.utilities.Constants.KEY_EVENT_DATA;

public class DetailFragment extends Fragment {

    @BindView(R.id.event_type_text_view)
    TextView mEventType;
    @BindView(R.id.event_title_text_view)
    TextView mEventTitle;
    @BindView(R.id.event_description_text_view)
    TextView mEventDescription;
    @BindView(R.id.event_location_text_view)
    TextView mEventLocation;
    @BindView(R.id.event_date_time_text_view)
    TextView mEventEndsDateTime;

    private OnAddressClickListener mListener;

    private Event mEvent = null;
    private long mNow;

    public DetailFragment() {
        // Required empty public constructor
    }

    public static DetailFragment newInstance(Event event) {
        DetailFragment fragment = new DetailFragment();
        Bundle args = new Bundle();
        args.putParcelable(KEY_EVENT_DATA, event);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mEvent = getArguments().getParcelable(KEY_EVENT_DATA);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        ButterKnife.bind(this, rootView);

        mNow = DateTimeUtil.getCurrentDateTimeInMillis();

        initUI();

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mListener = (OnAddressClickListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    @OnClick(R.id.event_location_text_view)
    void onEventLocationTextClick() {
        mListener.onLocationClicked();
    }

    /**
     * Initializing UI view
     */
    private void initUI() {
        mEventTitle.setText(mEvent.getTitle());
        mEventDescription.setText(mEvent.getDescription());
        mEventLocation.setText(mEvent.getPlace().getAddress());

        String endsDateTimeText = getResources().getString(R.string.ends_date_time);
        long endsDateTime = mEvent.getEndsDateTime();
        if (endsDateTime < mNow) {
            endsDateTimeText = getResources().getString(R.string.ended_date_time);
            mEventEndsDateTime.setTextColor(ContextCompat.getColor(Objects.requireNonNull(getContext()), R.color.red));
        } else {
            mEventEndsDateTime.setTextColor(ContextCompat.getColor(Objects.requireNonNull(getContext()), R.color.grey));
        }
        mEventEndsDateTime.setText(DateTimeUtil.getFormattedDateTimeResourcesString(
                endsDateTimeText, endsDateTime));

        mEventType.setText(EventTypeHelper.getEventType(getContext(), mEvent.getType()));
    }

    public interface OnAddressClickListener {
        void onLocationClicked();
    }
}
