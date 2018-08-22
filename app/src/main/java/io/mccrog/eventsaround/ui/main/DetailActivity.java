package io.mccrog.eventsaround.ui.main;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.mccrog.eventsaround.R;
import io.mccrog.eventsaround.model.Event;
import io.mccrog.eventsaround.ui.fragments.DetailFragment;
import io.mccrog.eventsaround.ui.fragments.DetailMapFragment;
import io.mccrog.eventsaround.utilities.InjectorUtils;
import io.mccrog.eventsaround.utilities.ShareHelper;
import io.mccrog.eventsaround.viewmodel.EventsViewModel;
import io.mccrog.eventsaround.viewmodel.EventsViewModelFactory;
import io.mccrog.eventsaround.widget.EventWidgetProvider;

import static io.mccrog.eventsaround.utilities.Constants.KEY_EVENT;
import static io.mccrog.eventsaround.utilities.Constants.KEY_EVENT_DATA;

public class DetailActivity extends AppCompatActivity implements DetailFragment.OnAddressClickListener {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    private MenuItem mFavoriteMenuButton;
    private Event mEvent = null;
    private EventsViewModel mViewModel;
    private boolean mIsFavorite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve event from saved instance state.
        if (savedInstanceState != null) {
            mEvent = savedInstanceState.getParcelable(KEY_EVENT);
        } else {
            Intent intent = getIntent();
            if (intent.hasExtra(KEY_EVENT_DATA)) {
                mEvent = intent.getParcelableExtra(KEY_EVENT_DATA);
            }
        }

        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_detail);

        // BindView annotated fields and methods in that Activity.
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        EventsViewModelFactory factory = InjectorUtils.provideEventsViewModelFactory(this.getApplicationContext());
        mViewModel = ViewModelProviders.of(this, factory).get(EventsViewModel.class);

        init();

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        if (savedInstanceState == null) {
            DetailFragment detailFragment = DetailFragment.newInstance(mEvent);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.detail_container, detailFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    /**
     * Saves the state of the event when the activity is paused.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mEvent != null) {
            outState.putParcelable(KEY_EVENT, mEvent);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.event_info_menu, menu);

        mFavoriteMenuButton = menu.findItem(R.id.favorite_menu);
        if (mIsFavorite) {
            mFavoriteMenuButton.setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_baseline_favorite_24px));
        } else {
            mFavoriteMenuButton.setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_baseline_favorite_border_24px));
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.favorite_menu:
                if (mIsFavorite) {
                    mViewModel.deleteEvent(mEvent);
                    mIsFavorite = false;
                    mFavoriteMenuButton.setIcon(ContextCompat.getDrawable(getApplicationContext(),
                            R.drawable.ic_baseline_favorite_border_24px));
                } else {
                    mViewModel.insertEvent(mEvent);
                    mIsFavorite = true;
                    mFavoriteMenuButton.setIcon(ContextCompat.getDrawable(getApplicationContext(),
                            R.drawable.ic_baseline_favorite_24px));
                }
                EventWidgetProvider.sendRefreshBroadcast(getApplicationContext());
                return true;
            case R.id.share_menu:
                ShareHelper shareHelper = new ShareHelper(this);
                shareHelper.shareEvent(mEvent);
                return true;
            case android.R.id.home:
                // Respond to the action bar's Up/Home button
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        backNavigation();
    }

    private void backNavigation() {
        final List<Fragment> fragment = getSupportFragmentManager().getFragments();

        if (fragment.size() == 0) {
            finish();
        }
    }

    private void init() {
        mViewModel.isEventExist(mEvent).observe(this, isExist -> mIsFavorite = isExist);
    }

    @Override
    public void onLocationClicked() {
        DetailMapFragment detailMapFragment = DetailMapFragment.newInstance(mEvent);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.detail_container, detailMapFragment)
                .addToBackStack(null)
                .commit();
    }
}
