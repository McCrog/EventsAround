package io.mccrog.eventsaround.ui.fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.mccrog.eventsaround.R;
import io.mccrog.eventsaround.model.Event;
import io.mccrog.eventsaround.model.EventType;
import io.mccrog.eventsaround.model.PlaceEvent;
import io.mccrog.eventsaround.ui.main.DetailActivity;
import io.mccrog.eventsaround.utilities.DateTimeUtil;
import io.mccrog.eventsaround.utilities.InjectorUtils;
import io.mccrog.eventsaround.viewmodel.EventsViewModel;
import io.mccrog.eventsaround.viewmodel.EventsViewModelFactory;
import io.mccrog.eventsaround.widget.EventWidgetProvider;

import static io.mccrog.eventsaround.utilities.Constants.KEY_EVENT_DATA;
import static io.mccrog.eventsaround.utilities.Constants.KEY_EVENT_PLACE_DATA;

public class AddFragment extends Fragment
        implements AdapterView.OnItemSelectedListener, DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener {

    @BindView(R.id.event_type_spinner)
    Spinner mEventTypeSpinner;
    @BindView(R.id.event_type_spinner_error)
    TextView mEventTypeSpinnerError;
    @BindView(R.id.edit_text_event_title)
    EditText mEventTitle;
    @BindView(R.id.edit_text_event_description)
    EditText mEventDescription;
    @BindView(R.id.edit_text_event_location)
    EditText mEventLocation;
    @BindView(R.id.edit_text_event_date)
    EditText mEventEndsDate;
    @BindView(R.id.edit_text_event_time)
    EditText mEventEndsTime;

    @BindView(R.id.event_title_layout)
    TextInputLayout mEventTitleInputLayout;
    @BindView(R.id.event_description_layout)
    TextInputLayout mEventDescriptionInputLayout;
    @BindView(R.id.event_location_layout)
    TextInputLayout mEventLocationInputLayout;
    @BindView(R.id.event_date_layout)
    TextInputLayout mEventEndsDateInputLayout;
    @BindView(R.id.event_time_layout)
    TextInputLayout mEventEndsTimeInputLayout;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    // Code for request
    private static final int REQUEST_MAPS_ACTIVITY_PLACE = 1;

    // Data variables
    private EventType mEventType;
    private String mEventTitleText = "";
    private String mEventDescriptionText = "";
    private Place mPlace = null;
    private long mDateTime;

    private Calendar mCalendarDateAndTime;

    // keys for savedInstanceState
    private static final String KEY_TYPE = "type";
    private static final String KEY_TITLE = "title";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_PLACE = "place";
    private static final String KEY_DATE_TIME = "dateTime";

    public AddFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_add, container, false);

        ButterKnife.bind(this, rootView);

        mToolbar.setTitle(getResources().getString(R.string.title_add_event));
        ((AppCompatActivity) Objects.requireNonNull(getActivity())).setSupportActionBar(mToolbar);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setHomeAsUpIndicator(android.R.drawable.ic_menu_close_clear_cancel);
        }
        setHasOptionsMenu(true);

        // Retrieve data from saved instance state.
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(KEY_TYPE)) {
                mEventType = EventType.valueOf(savedInstanceState.getString(KEY_TYPE));
                mEventTypeSpinner.setSelection(mEventType.ordinal());
            }
            if (savedInstanceState.containsKey(KEY_TITLE)) {
                mEventTitleText = savedInstanceState.getString(KEY_TITLE);
                mEventTitle.setText(mEventTitleText);
            }
            if (savedInstanceState.containsKey(KEY_DESCRIPTION)) {
                mEventDescriptionText = savedInstanceState.getString(KEY_DESCRIPTION);
                mEventDescription.setText(mEventDescriptionText);
            }
            if (savedInstanceState.containsKey(KEY_PLACE)) {
                mPlace = savedInstanceState.getParcelable(KEY_PLACE);
            }
            if (savedInstanceState.containsKey(KEY_DATE_TIME)) {
                mDateTime = savedInstanceState.getLong(KEY_DATE_TIME);
            }
        }

        // Hides the bottom line of the EditText
        mEventDescription.setBackground(null);

        mEventTypeSpinnerError.setVisibility(View.INVISIBLE);

        initEventTypeSpinner();
        initDateTime();

        // Return the root view
        return rootView;
    }

    /**
     * Saves the state of the place when the activity is paused.
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mEventTitleText = mEventTitle.getText().toString();
        mEventDescriptionText = mEventDescription.getText().toString();

        if (mEventType != null) {
            outState.putString(KEY_TYPE, String.valueOf(mEventType));
        }
        if (!mEventTitleText.matches("")) {
            outState.putString(KEY_TITLE, mEventTitleText);
        }
        if (!mEventDescriptionText.matches("")) {
            outState.putString(KEY_DESCRIPTION, mEventDescriptionText);
        }
        if (mPlace != null) {
            outState.putParcelable(KEY_PLACE, (Parcelable) mPlace);
        }

        mDateTime = mCalendarDateAndTime.getTimeInMillis();
        outState.putLong(KEY_DATE_TIME, mDateTime);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.add_event_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.done_item:
                mEventTitleText = mEventTitle.getText().toString();
                mEventDescriptionText = mEventDescription.getText().toString();

                if (checkEnteredText(mEventTitleText, mEventDescriptionText)) {
                    PlaceEvent placeEvent = new PlaceEvent(
                            String.valueOf(mPlace.getAddress()),
                            mPlace.getLatLng().latitude,
                            mPlace.getLatLng().longitude);

                    mDateTime = mCalendarDateAndTime.getTimeInMillis();

                    Event event = new Event(mEventType, mEventTitleText, mEventDescriptionText, placeEvent, mDateTime);

                    EventWidgetProvider.sendRefreshBroadcast(getContext());

                    saveEvent(event);

                    Intent intent = new Intent(getContext(), DetailActivity.class);
                    Bundle b = new Bundle();
                    b.putParcelable(KEY_EVENT_DATA, event);
                    intent.putExtras(b);
                    startActivity(intent);
                    Objects.requireNonNull(getActivity()).getSupportFragmentManager().popBackStackImmediate();
                }
                return true;
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                Objects.requireNonNull(getActivity()).getSupportFragmentManager().popBackStackImmediate();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Start {@link AddMapFragment} clicking on EditText
     */
    @OnClick(R.id.edit_text_event_location)
    void onLocationEditTextClick() {
        FragmentManager fragmentManager = Objects.requireNonNull(getActivity()).getSupportFragmentManager();

        AddMapFragment addMapFragment = new AddMapFragment();
        addMapFragment.setTargetFragment(this, REQUEST_MAPS_ACTIVITY_PLACE);

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.add(android.R.id.content, addMapFragment)
                .addToBackStack(null)
                .commit();
    }

    @OnClick(R.id.edit_text_event_date)
    void onEventEndsDateEditTextClick() {
        setDate();
    }

    @OnClick(R.id.edit_text_event_time)
    void onEventEndsTimeEditTextClick() {
        setTime();
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        String date = DateTimeUtil.getFormattedDateString(year, month, day);
        mEventEndsDate.setText(date);
        mCalendarDateAndTime.set(year, month, day);
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
        String time = DateTimeUtil.getFormattedTimeString(hour, minute);

        mEventEndsTime.setText(time);
        mCalendarDateAndTime.set(Calendar.HOUR_OF_DAY, hour);
        mCalendarDateAndTime.set(Calendar.MINUTE, minute);
    }

    /**
     * Handles the result of the {@link AddMapFragment}
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_MAPS_ACTIVITY_PLACE) {
            if (resultCode == Activity.RESULT_OK) {
                mPlace = data.getParcelableExtra(KEY_EVENT_PLACE_DATA);
                mEventLocation.setText(mPlace.getAddress());
            } else if (resultCode == PlacePicker.RESULT_ERROR) {
                Toast.makeText(getContext(), R.string.places_api_failure,
                        Toast.LENGTH_LONG).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if (i > 0)
            mEventType = EventType.values()[i - 1];
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    /**
     * Setting the EventType spinner
     */
    private void initEventTypeSpinner() {
        String[] events = getResources().getStringArray(R.array.events_type);
        List<String> eventsList = new ArrayList<>(Arrays.asList(events));

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(Objects.requireNonNull(getContext()), android.R.layout.simple_spinner_item, eventsList) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    tv.setTextColor(Color.GRAY);
                } else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };

        // Specify the layout to use when the list of choices appears
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mEventTypeSpinner.setAdapter(spinnerArrayAdapter);

        mEventTypeSpinner.setOnItemSelectedListener(this);
    }

    /**
     * Setting the start date and time
     */
    private void initDateTime() {
        mCalendarDateAndTime = Calendar.getInstance();
        int year = mCalendarDateAndTime.get(Calendar.YEAR);
        int month = mCalendarDateAndTime.get(Calendar.MONTH);
        int day = mCalendarDateAndTime.get(Calendar.DAY_OF_MONTH);

        String date = DateTimeUtil.getFormattedDateString(year, month, day);

        mEventEndsDate.setText(date);
        mEventEndsTime.setText(R.string.default_time);
        mCalendarDateAndTime.set(year, month, day, 23, 59);
        mDateTime = mCalendarDateAndTime.getTimeInMillis();
    }

    /**
     * Saving the Event in the Firebase Realtime Database
     * @param event new data
     */
    private void saveEvent(Event event) {
        EventsViewModelFactory factory = InjectorUtils.provideEventsViewModelFactory(Objects.requireNonNull(getContext()));
        EventsViewModel viewModel = ViewModelProviders.of(this, factory).get(EventsViewModel.class);
        viewModel.saveEvent(event);
    }

    private boolean checkEnteredText(String eventTitle, String eventDescription) {
        boolean isCorrectTitle = checkText(eventTitle, mEventTitleInputLayout);
        boolean isCorrectDescription = checkText(eventDescription, mEventDescriptionInputLayout);
        boolean isCorrectPlace = checkPlace();
        boolean isCorrectEventType = checkSpinnerValue();

        return isCorrectTitle && isCorrectDescription && isCorrectPlace && isCorrectEventType;
    }

    private boolean checkText(String text, TextInputLayout textInputLayout) {
        if (text != null && !text.matches("")) {
            textInputLayout.setErrorEnabled(false);
            return true;
        } else {
            textInputLayout.setError(getString(R.string.required_field));
            textInputLayout.setErrorEnabled(true);
            return false;
        }
    }

    private boolean checkPlace() {
        if (mPlace != null) {
            mEventLocationInputLayout.setErrorEnabled(false);
            return true;
        } else {
            mEventLocationInputLayout.setError(getString(R.string.required_field));
            mEventLocationInputLayout.setErrorEnabled(true);
            return false;
        }
    }

    private boolean checkSpinnerValue() {
        if (mEventType != null) {
            mEventTypeSpinnerError.setVisibility(View.INVISIBLE);
            return true;
        } else {
            mEventTypeSpinnerError.setVisibility(View.VISIBLE);
            return false;
        }
    }

    /**
     * Displays a dialog for selecting the date
     */
    private void setDate() {
        new DatePickerDialog(Objects.requireNonNull(getContext()), this,
                mCalendarDateAndTime.get(Calendar.YEAR),
                mCalendarDateAndTime.get(Calendar.MONTH),
                mCalendarDateAndTime.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    /**
     * Displays a dialog for selecting the time
     */
    private void setTime() {
        new TimePickerDialog(getContext(), this,
                mCalendarDateAndTime.get(Calendar.HOUR_OF_DAY),
                mCalendarDateAndTime.get(Calendar.MINUTE), true)
                .show();
    }
}