package io.mccrog.eventsaround.ui.main;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import com.google.firebase.auth.FirebaseAuth;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.mccrog.eventsaround.R;
import io.mccrog.eventsaround.ui.fragments.AddFragment;
import io.mccrog.eventsaround.ui.fragments.FavoriteFragment;
import io.mccrog.eventsaround.utilities.InjectorUtils;
import io.mccrog.eventsaround.viewmodel.EventsViewModel;
import io.mccrog.eventsaround.viewmodel.EventsViewModelFactory;

import static io.mccrog.eventsaround.utilities.Constants.KEY_FRAGMENT;
import static io.mccrog.eventsaround.utilities.Constants.KEY_LIST;
import static io.mccrog.eventsaround.utilities.Constants.KEY_MAP;

public class FavoriteActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener {

    @BindView(R.id.add_fab)
    FloatingActionButton mFloatingActionButton;
    @BindView(R.id.bottom_navigation)
    BottomNavigationView mBottomNavigationView;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private GoogleApiClient mGoogleApiClient;

    private EventsViewModel mViewModel;
    private String mUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // BindView annotated fields and methods in that Activity.
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        EventsViewModelFactory factory = InjectorUtils.provideEventsViewModelFactory(this.getApplicationContext());
        mViewModel = ViewModelProviders.of(this, factory).get(EventsViewModel.class);

        // Set default username.
        mUsername = mViewModel.getUserName();

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(FavoriteFragment.ARG_ITEM_ID,
                    getIntent().getStringExtra(FavoriteFragment.ARG_ITEM_ID));
            FavoriteFragment mapFragment = new FavoriteFragment();
            mapFragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.master_list_container, mapFragment)
                    .commit();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        initNavigationItemSelectedListener();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.favorite_menu, menu);

        MenuItem userName = menu.findItem(R.id.user_name_menu);
        SpannableString spanString = new SpannableString(mUsername);
        spanString.setSpan(new ForegroundColorSpan(Color.GRAY),0, spanString.length(), 0);
        userName.setTitle(spanString);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                mFirebaseAuth.signOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                mViewModel.removeUserName();
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
                .addToBackStack(null)
                .commit();
    }

    private void initNavigationItemSelectedListener() {
        Menu menu = mBottomNavigationView.getMenu();
        MenuItem eventsList = menu.findItem(R.id.events_favorite);
        eventsList.setChecked(true);

        mBottomNavigationView.setOnNavigationItemSelectedListener(
                item -> {
                    switch (item.getItemId()) {
                        case R.id.events_list:
                            startEventActivity(KEY_LIST);
                            break;
                        case R.id.events_map:
                            startEventActivity(KEY_MAP);
                            break;
                        case R.id.events_favorite:
                            break;
                    }
                    return true;
                });
    }

    private void startEventActivity(String key) {
        Intent eventsIntent = new Intent(getApplicationContext(), MainActivity.class);
        eventsIntent.putExtra(KEY_FRAGMENT, key);
        eventsIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(eventsIntent);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
