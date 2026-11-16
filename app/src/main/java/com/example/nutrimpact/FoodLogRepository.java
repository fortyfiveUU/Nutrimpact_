/**
 * Module: EEE521 - Final Year Project
 * Project: Nutrimpact
 * File: FoodLogRepository.java
 * Description: Single place for reading and writing the food log, running database writes on a background thread and reporting back on the main thread.
 */

package com.example.nutrimpact;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;

import java.util.List;

/**
 * Wraps FoodLogDao so screens don't deal with threads or the database directly.
 */
public class FoodLogRepository {

    /** DAO used for all food log queries. */
    private final FoodLogDao foodLogDao;

    /** Posts results back to the main (UI) thread. */
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    /**
     * Creates a repository backed by the app's database.
     *
     * @param context any context.
     */
    public FoodLogRepository(Context context) {
        this.foodLogDao = NutrimpactDatabase.getInstance(context).foodLogDao();
    }

    /**
     * Saves a food to the log in the background.
     *
     * @param entry    the food to save.
     * @param onSaved  run on the main thread once saved; may be null.
     */
    public void addEntry(FoodLogEntry entry, Runnable onSaved) {
        NutrimpactDatabase.DATABASE_WRITE_EXECUTOR.execute(() -> {
            foodLogDao.insert(entry);
            if (onSaved != null) {
                mainHandler.post(onSaved);
            }
        });
    }

    /**
     * Removes a food from the log in the background.
     *
     * @param entry the food to remove.
     */
    public void deleteEntry(FoodLogEntry entry) {
        NutrimpactDatabase.DATABASE_WRITE_EXECUTOR.execute(() -> foodLogDao.delete(entry));
    }

    /**
     * Gets every food logged on one day. Updates automatically when the log changes.
     *
     * @param epochDay the day, as days since 1970-01-01.
     * @return live list of that day's entries.
     */
    public LiveData<List<FoodLogEntry>> getEntriesForDay(long epochDay) {
        return foodLogDao.getEntriesForDay(epochDay);
    }
}
