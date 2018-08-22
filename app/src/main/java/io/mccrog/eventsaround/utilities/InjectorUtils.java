package io.mccrog.eventsaround.utilities;

import android.content.Context;

import io.mccrog.eventsaround.data.AppExecutors;
import io.mccrog.eventsaround.data.AppPreferences;
import io.mccrog.eventsaround.data.Repository;
import io.mccrog.eventsaround.data.database.AppDatabase;
import io.mccrog.eventsaround.viewmodel.EventsViewModelFactory;

public class InjectorUtils {
    public static Repository provideRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context.getApplicationContext());
        AppPreferences appPreferences = provideAppPreferences(context);
        AppExecutors executors = AppExecutors.getInstance();
        return Repository.getInstance(database.eventDao(), appPreferences, executors);
    }

    private static AppPreferences provideAppPreferences(Context context) {
        return AppPreferences.getInstance(context);
    }

    public static EventsViewModelFactory provideEventsViewModelFactory(Context context) {
        Repository repository = provideRepository(context.getApplicationContext());
        return new EventsViewModelFactory(repository);
    }
}
