package io.mccrog.eventsaround.ui.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.ButterKnife;
import io.mccrog.eventsaround.R;
import io.mccrog.eventsaround.model.Event;
import io.mccrog.eventsaround.ui.helper.CustomInfoWindowGoogleMap;
import io.mccrog.eventsaround.ui.main.DetailActivity;
import io.mccrog.eventsaround.ui.main.MainActivity;
import io.mccrog.eventsaround.utilities.InjectorUtils;
import io.mccrog.eventsaround.utilities.OnDataChangedListener;
import io.mccrog.eventsaround.viewmodel.EventsViewModel;
import io.mccrog.eventsaround.viewmodel.EventsViewModelFactory;

import static io.mccrog.eventsaround.utilities.Constants.DEFAULT_ZOOM;
import static io.mccrog.eventsaround.utilities.Constants.KEY_EVENT_DATA;

public class MapFragment extends Fragment implements OnDataChangedListener, OnMapReadyCallback,
        GoogleMap.OnInfoWindowClickListener {

    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_map_id";

    private GoogleMap mMap;

    private EventsViewModel mViewModel;
    private List<Event> mEvents;

    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((MainActivity) Objects.requireNonNull(getActivity())).setActivityListener(MapFragment.this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_map, container, false);

        ButterKnife.bind(this, rootView);

        // Build the map.
        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.events_map);
        supportMapFragment.getMapAsync(this);

        EventsViewModelFactory factory = InjectorUtils.provideEventsViewModelFactory(Objects.requireNonNull(getContext()));
        mViewModel = ViewModelProviders.of(this, factory).get(EventsViewModel.class);

        mEvents = new ArrayList<>();

        return rootView;
    }

    @Override
    public void onExploreChanged(boolean isExplore) {
        if (mMap != null) {
            setMyLocationEnabled(isExplore);
        }
    }

    /**
     * Manipulates the map when it's available.
     * This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        CustomInfoWindowGoogleMap customInfoWindow = new CustomInfoWindowGoogleMap(getContext());
        mMap.setInfoWindowAdapter(customInfoWindow);

        // Set a listener for info window events
        mMap.setOnInfoWindowClickListener(this);

        setMyLocationEnabled(true);

        init();
    }

    private void init() {
        mViewModel.getCurrentLocation().observe(this, this::moveMapCamera);

        mViewModel.getDownloadedEvents().observe(this, this::setData);
    }

    /**
     * A listener for info window events
     *
     * @param marker indicate single locations on the map
     */
    @Override
    public void onInfoWindowClick(Marker marker) {
        Event event = (Event) marker.getTag();
        Intent intent = new Intent(getContext(), DetailActivity.class);
        Bundle b = new Bundle();
        b.putParcelable(KEY_EVENT_DATA, event);
        intent.putExtras(b);
        startActivity(intent);
    }

    private void initMaker(Event event) {
        LatLng markerLatLng = new LatLng(event.getPlace().getLatitude(), event.getPlace().getLongitude());

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.title(event.getTitle())
                .position(markerLatLng)
                .snippet(event.getDescription());

        Marker marker = mMap.addMarker(markerOptions);
        marker.setTag(event);
    }

    private void moveMapCamera(Location location) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(location.getLatitude(),
                        location.getLongitude()), DEFAULT_ZOOM));
    }

    private void setMyLocationEnabled(boolean isEnabled) {
        try {
            mMap.setMyLocationEnabled(isEnabled);
            mMap.getUiSettings().setMyLocationButtonEnabled(isEnabled);
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void setData(List<Event> newEvents) {
        removeMarkers();
        mEvents.clear();
        mEvents.addAll(newEvents);
        for (Event event: mEvents) {
            initMaker(event);
        }
    }

    private void removeMarkers() {
        mMap.clear();
    }
}
