/**
 * Module: EEE521 - Final Year Project
 * Project: Nutrimpact
 * File: ProgressViewModel.java
 * Description: ViewModel for the Progress screen. Holds which week is shown and loads that week's daily totals from the local database, surviving screen rotation.
 */

package com.example.nutrimpact;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.time.LocalDate;
import java.util.List;

/**
 * Keeps the last day of the week being shown and the daily totals for that week.
 */
public class ProgressViewModel extends AndroidViewModel {

    /** Reads the food log. */
    private final FoodLogRepository repository;

    /** Last day of the week shown, as days since 1970-01-01. Starts on today. */
    private final MutableLiveData<Long> endDay = new MutableLiveData<>(LocalDate.now().toEpochDay());

    /** Daily totals for the week; updates when the week or the log changes. */
    private final LiveData<List<DayTotal>> weekTotals;

    /**
     * Creates the ViewModel. Called by ViewModelProvider.
     *
     * @param application the app, used to open the database.
     */
    public ProgressViewModel(@NonNull Application application) {
        super(application);
        repository = new FoodLogRepository(application);
        weekTotals = Transformations.switchMap(endDay,
                day -> repository.getDayTotals(day - (WeekSummary.DAYS - 1), day));
    }

    /**
     * Gets the last day of the week shown.
     *
     * @return live epoch day.
     */
    public LiveData<Long> getEndDay() {
        return endDay;
    }

    /**
     * Gets the week's daily totals (days with nothing logged are left out).
     *
     * @return live list of daily totals.
     */
    public LiveData<List<DayTotal>> getWeekTotals() {
        return weekTotals;
    }

    /**
     * Moves back one week.
     */
    public void previousWeek() {
        endDay.setValue(currentEndDay() - WeekSummary.DAYS);
    }

    /**
     * Moves forward one week, stopping at the week ending today.
     */
    public void nextWeek() {
        endDay.setValue(Math.min(currentEndDay() + WeekSummary.DAYS, LocalDate.now().toEpochDay()));
    }

    /**
     * Checks whether the week shown ends today.
     *
     * @return true if this is the latest week.
     */
    public boolean isLatestWeek() {
        return currentEndDay() >= LocalDate.now().toEpochDay();
    }

    /**
     * Gets the last day of the week shown.
     *
     * @return epoch day, or today if not set yet.
     */
    private long currentEndDay() {
        Long day = endDay.getValue();
        return (day != null) ? day : LocalDate.now().toEpochDay();
    }
}
