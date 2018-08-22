package io.mccrog.eventsaround.ui.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.mccrog.eventsaround.R;
import io.mccrog.eventsaround.model.Event;
import io.mccrog.eventsaround.ui.helper.EventAdapter;
import io.mccrog.eventsaround.ui.main.DetailActivity;
import io.mccrog.eventsaround.utilities.InjectorUtils;
import io.mccrog.eventsaround.utilities.RecyclerItemTouchHelper;
import io.mccrog.eventsaround.utilities.ShareHelper;
import io.mccrog.eventsaround.viewmodel.EventsViewModel;
import io.mccrog.eventsaround.viewmodel.EventsViewModelFactory;
import io.mccrog.eventsaround.widget.EventWidgetProvider;

import static io.mccrog.eventsaround.utilities.Constants.KEY_EVENT_DATA;

public class FavoriteFragment extends Fragment implements EventAdapter.EventOnClickHandler,
        RecyclerItemTouchHelper.RecyclerItemTouchHelperListener {

    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_favorite_id";

    @BindView(R.id.event_recycler_view)
    RecyclerView mEventRecyclerView;

    private EventAdapter mEventAdapter;
    private EventsViewModel mViewModel;

    public FavoriteFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_list, container, false);

        ButterKnife.bind(this, rootView);

        mEventRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mEventAdapter = new EventAdapter(getContext(), this);
        mEventRecyclerView.setAdapter(mEventAdapter);

        EventsViewModelFactory factory = InjectorUtils.provideEventsViewModelFactory(Objects.requireNonNull(getContext()));
        mViewModel = ViewModelProviders.of(this, factory).get(EventsViewModel.class);

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(mEventRecyclerView);

        // Fetching list of Events from the Database
        fetchData();

        return rootView;
    }

    /**
     * Fetching list of Events from the Database
     */
    private void fetchData() {
        mViewModel.getEvents().observe(this, events -> mEventAdapter.setData(events));
    }

    @Override
    public void onItemClick(int index) {
        startDetailActivity(index);
    }

    @Override
    public void onShareClick(int index) {
        Event event = mEventAdapter.getItem(index);
        ShareHelper shareHelper = new ShareHelper(getContext());
        shareHelper.shareEvent(event);
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof EventAdapter.EventViewHolder) {
            Event deletedEvent = mEventAdapter.getItem(position);

            // remove the item
            mEventAdapter.removeItem(viewHolder.getAdapterPosition());
            mViewModel.deleteEvent(deletedEvent);

            EventWidgetProvider.sendRefreshBroadcast(getContext());
        }
    }

    private void startDetailActivity(int position) {
        Event event = mEventAdapter.getItem(position);

        Intent intent = new Intent(getContext(), DetailActivity.class);
        Bundle b = new Bundle();
        b.putParcelable(KEY_EVENT_DATA, event);
        intent.putExtras(b);
        startActivity(intent);
    }
}
