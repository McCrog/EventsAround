package io.mccrog.eventsaround.viewmodel;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import io.mccrog.eventsaround.data.Repository;

public class EventsViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final Repository mRepository;

    public EventsViewModelFactory(Repository repository) {
        this.mRepository = repository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        //noinspection unchecked
        return (T) new EventsViewModel(mRepository);
    }
}
