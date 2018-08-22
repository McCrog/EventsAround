package io.mccrog.eventsaround.ui.main;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.mccrog.eventsaround.R;
import io.mccrog.eventsaround.ui.fragments.AddFragment;
import io.mccrog.eventsaround.ui.fragments.ListFragment;
import io.mccrog.eventsaround.ui.fragments.MapFragment;
import io.mccrog.eventsaround.utilities.AlertDialogManager;
import io.mccrog.eventsaround.utilities.ConnectionHelper;
import io.mccrog.eventsaround.utilities.InjectorUtils;
import io.mccrog.eventsaround.utilities.OnDataChangedListener;
import io.mccrog.eventsaround.viewmodel.EventsViewModel;
import io.mccrog.eventsaround.viewmodel.EventsViewModelFactory;

import static io.mccrog.eventsaround.utilities.Constants.KEY_FRAGMENT;
import static io.mccrog.eventsaround.utilities.Constants.KEY_IS_EXPLORE;
import static io.mccrog.eventsaround.utilities.Constants.KEY_LIST;
import static io.mccrog.eventsaround.utilities.Constants.KEY_LOCATION;
import static io.mccrog.eventsaround.utilities.Constants.KEY_MAP;
import static io.mccrog.eventsaround.utilities.Constants.KEY_SEARCH_RADIUS;
import static io.mccrog.eventsaround.utilities.Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,
        ConnectionHelper.OnConnectionListener {

    @BindView(R.id.add_fab)
    FloatingActionButton mFloatingActionButton;
    @BindView(R.id.bottom_navigation)
    BottomNavigationView mBottomNavigationView;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;

    private GoogleApiClient mGoogleApiClient;

    private boolean mLocationPermissionGranted;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;

    private EventsViewModel mViewModel;

    private String mUsername;
    private int mSearchRadius;
    private boolean mIsExplore = true;
    private boolean mIsLocationTrackerInit;

    private OnDataChangedListener mListener;

    private ListFragment mListFragment;
    private MapFragment mMapFragment;
    private String mFragmentKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // BindView annotated fields and methods in that Activity.
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        EventsViewModelFactory factory = InjectorUtils.provideEventsViewModelFactory(this.getApplicationContext());
        mViewModel = ViewModelProviders.of(this, factory).get(EventsViewModel.class);

        // Set default username and radius.
        mUsername = mViewModel.getUserName();
        mSearchRadius = mViewModel.getSearchRadius();

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser mFirebaseUser = mFirebaseAuth.getCurrentUser();
        // Obtain the FirebaseAnalytics instance
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        } else {
            mUsername = mFirebaseUser.getDisplayName();
            mViewModel.saveUserName(mUsername);
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        if (mIsExplore) {
            // Prompt the user for permission.
            getLocationPermission();

            init();
        }

        mListFragment = new ListFragment();
        mMapFragment = new MapFragment();

        mFragmentKey = KEY_LIST;
        if (savedInstanceState == null) {
            Intent fragmentIntent = getIntent();
            if (fragmentIntent.hasExtra(KEY_FRAGMENT)) {
                String key = fragmentIntent.getStringExtra(KEY_FRAGMENT);
                switch (key) {
                    case KEY_LIST:
                        startListFragment();
                        break;
                    case KEY_MAP:
                        startMapFragment();
                        mFragmentKey = KEY_MAP;
                        break;
                }
            } else {
                startListFragment();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mIsLocationTrackerInit)
            stopLocationTracker();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mIsExplore && mIsLocationTrackerInit)
            executeLocationTracker();

        initNavigationItemSelectedListener(mFragmentKey);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * Saves the state of the map when the activity is paused.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_SEARCH_RADIUS, mSearchRadius);
        outState.putBoolean(KEY_IS_EXPLORE, mIsExplore);
        outState.putString(KEY_FRAGMENT, mFragmentKey);
        if (mLastKnownLocation != null) {
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(KEY_LOCATION)) {
                mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            }
            if (savedInstanceState.containsKey(KEY_SEARCH_RADIUS)) {
                mSearchRadius = savedInstanceState.getInt(KEY_SEARCH_RADIUS);
            }
            if (savedInstanceState.containsKey(KEY_IS_EXPLORE)) {
                mIsExplore = savedInstanceState.getBoolean(KEY_IS_EXPLORE);
            }
            if (savedInstanceState.containsKey(KEY_FRAGMENT)) {
                mFragmentKey = savedInstanceState.getString(KEY_FRAGMENT);
            }
        }
    }

    public void setActivityListener(OnDataChangedListener listener) {
        this.mListener = listener;
    }

    private void init() {
        if (mLocationPermissionGranted) {

            startLocationTracker();

            mViewModel.getCurrentLocation().observe(this, location -> {
                mLastKnownLocation = location;
                mViewModel.updateDataSourceLocation(mLastKnownLocation, mSearchRadius);
            });
        }
    }

    private void startLocationTracker() {
        if (mLocationPermissionGranted) {
            mIsLocationTrackerInit = mViewModel.startLocationTracker(this, this, mSearchRadius);
        }
    }

    private void executeLocationTracker() {
        mViewModel.executeLocationTracker();

        if (mListener != null) {
            mListener.onExploreChanged(true);
        }
    }

    private void stopLocationTracker() {
        mViewModel.stopLocationTracker();

        if (mListener != null) {
            mListener.onExploreChanged(false);
        }
    }

    /**
     * Prompts the user for permission to use the device location.
     */
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                    init();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        initOptionsItems(menu);
        return true;
    }

    private void initOptionsItems(Menu menu) {
        MenuItem userName = menu.findItem(R.id.user_name_menu);
        SpannableString spanString = new SpannableString(mUsername);
        spanString.setSpan(new ForegroundColorSpan(Color.GRAY), 0, spanString.length(), 0);
        userName.setTitle(spanString);

        MenuItem explore = menu.findItem(R.id.explore_menu);
        if (!mIsExplore) {
            explore.setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_baseline_explore_off_24px));
            stopLocationTracker();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.explore_menu:
                if (mIsExplore) {
                    mIsExplore = false;
                    item.setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_baseline_explore_off_24px));
                    stopLocationTracker();
                } else {
                    mIsExplore = true;
                    item.setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_baseline_explore_24px));
                    if (mIsLocationTrackerInit) {
                        executeLocationTracker();
                    } else {
                        init();
                    }
                }
                return true;
            case R.id.settings_menu:
                AlertDialogManager alertDialog = new AlertDialogManager();
                // Internet Connection is not present
                alertDialog.showNumberPickerDialog(MainActivity.this, getString(R.string.setting_search_radius_title),
                        getString(R.string.setting_search_radius_message), (numberPicker, i, i1) -> {
                            mSearchRadius = numberPicker.getValue();
                            mViewModel.saveSearchRadius(mSearchRadius);
                            recreate();
                        });
                return true;
            case R.id.sign_out_menu:
                mFirebaseAuth.signOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                mViewModel.removeUserName();
                mViewModel.unregisterDataSourceListener();
                startActivity(new Intent(this, SignInActivity.class));
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Start {@link AddFragment} clicking on FloatingActionButton
     */
    @OnClick(R.id.add_fab)
    void onFloatingActionButtonClick() {
        FragmentManager fragmentManager = getSupportFragmentManager();

        AddFragment newFragment = new AddFragment();

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.add(android.R.id.content, newFragment)
                .addToBackStack(null).commit();
    }

    private void initNavigationItemSelectedListener(String key) {
        Menu menu = mBottomNavigationView.getMenu();
        MenuItem eventsList = menu.findItem(R.id.events_list);
        MenuItem eventsMap = menu.findItem(R.id.events_map);
        switch (key) {
            case KEY_LIST:
                eventsList.setChecked(true);
                break;
            case KEY_MAP:
                eventsMap.setChecked(true);
                break;
        }

        mBottomNavigationView.setOnNavigationItemSelectedListener(
                item -> {
                    switch (item.getItemId()) {
                        case R.id.events_list:
                            eventsList.setChecked(true);
                            mFragmentKey = KEY_LIST;
                            replaceListFragment();
                            break;
                        case R.id.events_map:
                            mIsLocationTrackerInit = false;
                            eventsMap.setChecked(true);
                            mFragmentKey = KEY_MAP;
                            replaceMapFragment();
                            break;
                        case R.id.events_favorite:
                            startFavoriteActivity();
                            break;
                    }
                    return true;
                });
    }

    private void startListFragment() {
        Bundle arguments = new Bundle();
        arguments.putString(ListFragment.ARG_ITEM_ID,
                getIntent().getStringExtra(ListFragment.ARG_ITEM_ID));
        mListFragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.master_list_container, mListFragment)
                .commit();
    }

    private void startMapFragment() {
        Bundle arguments = new Bundle();
        arguments.putString(MapFragment.ARG_ITEM_ID,
                getIntent().getStringExtra(MapFragment.ARG_ITEM_ID));
        mMapFragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.master_list_container, mMapFragment)
                .commit();
    }

    private void replaceListFragment() {
        Bundle arguments = new Bundle();
        arguments.putString(ListFragment.ARG_ITEM_ID,
                getIntent().getStringExtra(ListFragment.ARG_ITEM_ID));
        mListFragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.master_list_container, mListFragment)
                .commit();
    }

    private void replaceMapFragment() {
        Bundle arguments = new Bundle();
        arguments.putString(MapFragment.ARG_ITEM_ID,
                getIntent().getStringExtra(MapFragment.ARG_ITEM_ID));
        mMapFragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.master_list_container, mMapFragment)
                .commit();
    }

    private void startFavoriteActivity() {
        Intent eventsIntent = new Intent(getApplicationContext(), FavoriteActivity.class);
        eventsIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(eventsIntent);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationServiceFailed() {
        recreate();
    }

    @Override
    public void onInternetConnectionFailed() {
        recreate();
    }
}
