package io.mccrog.eventsaround.ui.fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import butterknife.ButterKnife;
import io.mccrog.eventsaround.R;
import io.mccrog.eventsaround.model.Event;

import static io.mccrog.eventsaround.utilities.Constants.DEFAULT_ZOOM;
import static io.mccrog.eventsaround.utilities.Constants.KEY_EVENT_DATA;

public class DetailMapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Event mEvent;

    public DetailMapFragment() {
        // Required empty public constructor
    }

    public static DetailMapFragment newInstance(Event event) {
        DetailMapFragment fragment = new DetailMapFragment();
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
        final View rootView = inflater.inflate(R.layout.fragment_map, container, false);

        ButterKnife.bind(this, rootView);

        // Build the map.
        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.events_map);
        supportMapFragment.getMapAsync(this);

        return rootView;
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        initMaker();
        moveMapCamera();
    }

    private void initMaker() {
        LatLng markerLatLng = new LatLng(mEvent.getPlace().getLatitude(), mEvent.getPlace().getLongitude());

        mMap.addMarker(new MarkerOptions().position(markerLatLng));
    }

    private void moveMapCamera() {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(mEvent.getPlace().getLatitude(),
                        mEvent.getPlace().getLongitude()), DEFAULT_ZOOM));
    }
}
