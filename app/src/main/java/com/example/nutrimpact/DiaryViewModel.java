/**
 * Module: EEE521 - Final Year Project
 * Project: Nutrimpact
 * File: DiaryViewModel.java
 * Description: ViewModel for the diary screen. Holds which day is shown and loads that day's logged foods from the local database, surviving screen rotation.
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
 * Keeps the diary's selected day and its entries. Changing the day
 * automatically switches the entries LiveData to that day's query.
 */
public class DiaryViewModel extends AndroidViewModel {

    /** Reads and writes the food log. */
    private final FoodLogRepository repository;

    /** The day being shown, as days since 1970-01-01. */
    private final MutableLiveData<Long> selectedDay = new MutableLiveData<>(LocalDate.now().toEpochDay());

    /** The selected day's entries; updates when the day or the log changes. */
    private final LiveData<List<FoodLogEntry>> entries;

    /**
     * Creates the ViewModel. Called by ViewModelProvider.
     *
     * @param application the app, used to open the database.
     */
    public DiaryViewModel(@NonNull Application application) {
        super(application);
        repository = new FoodLogRepository(application);
        entries = Transformations.switchMap(selectedDay, repository::getEntriesForDay);
    }

    /**
     * Gets the day being shown.
     *
     * @return live epoch day.
     */
    public LiveData<Long> getSelectedDay() {
        return selectedDay;
    }

    /**
     * Gets the selected day's logged foods.
     *
     * @return live list of entries, oldest first.
     */
    public LiveData<List<FoodLogEntry>> getEntries() {
        return entries;
    }

    /**
     * Shows a particular day. Days after today are not allowed.
     *
     * @param epochDay the day, as days since 1970-01-01.
     */
    public void showDay(long epochDay) {
        long today = LocalDate.now().toEpochDay();
        selectedDay.setValue(Math.min(epochDay, today));
    }

    /**
     * Moves to the day before the one shown.
     */
    public void previousDay() {
        showDay(currentDay() - 1);
    }

    /**
     * Moves to the day after the one shown, stopping at today.
     */
    public void nextDay() {
        showDay(currentDay() + 1);
    }

    /**
     * Checks whether the day shown is today.
     *
     * @return true if today is shown.
     */
    public boolean isShowingToday() {
        return currentDay() >= LocalDate.now().toEpochDay();
    }

    /**
     * Removes a food from the log.
     *
     * @param entry the food to remove.
     */
    public void deleteEntry(FoodLogEntry entry) {
        repository.deleteEntry(entry);
    }

    /**
     * Puts back a food that was just removed (used by Undo). The entry keeps
     * its original ID and time, so it returns to the same place in the list.
     *
     * @param entry the food to put back.
     */
    public void restoreEntry(FoodLogEntry entry) {
        repository.addEntry(entry, null);
    }

    /**
     * Gets the day currently shown.
     *
     * @return epoch day, or today if not set yet.
     */
    private long currentDay() {
        Long day = selectedDay.getValue();
        return (day != null) ? day : LocalDate.now().toEpochDay();
    }
}
