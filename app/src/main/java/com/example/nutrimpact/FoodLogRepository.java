/**
 * Module: EEE521 - Final Year Project
 * Project: Nutrimpact
 * File: FoodLogRepository.java
 * Description: Single place for reading and writing the food log, running database writes on a background thread and reporting back on the main thread.
 */

package com.example.nutrimpact;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.util.List;

/**
 * Wraps FoodLogDao so screens don't deal with threads or the database directly.
 */
public class FoodLogRepository {

    /** Tag for log messages. */
    private static final String TAG = "FoodLogRepository";

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

    /**
     * Gets each day's totals between two days (inclusive). Updates automatically.
     *
     * @param startDay first day, as days since 1970-01-01.
     * @param endDay   last day, as days since 1970-01-01.
     * @return live list of daily totals, oldest first; days with nothing logged are left out.
     */
    public LiveData<List<DayTotal>> getDayTotals(long startDay, long endDay) {
        return foodLogDao.getDayTotals(startDay, endDay);
    }

    /**
     * Writes the whole food log as a CSV file to a place the user picked, in
     * the background, then reports back on the main thread.
     *
     * @param resolver used to open the file.
     * @param uri      the file the user picked or created.
     * @param listener told the result on the main thread.
     */
    public void exportCsv(ContentResolver resolver, Uri uri, ExportListener listener) {
        NutrimpactDatabase.DATABASE_WRITE_EXECUTOR.execute(() -> {
            List<FoodLogEntry> entries = foodLogDao.getAllEntries();
            String csv = FoodLogCsv.build(entries, ZoneId.systemDefault());
            boolean success;
            try (OutputStream out = resolver.openOutputStream(uri, "wt")) {
                if (out == null) {
                    throw new IOException("Could not open " + uri);
                }
                out.write(csv.getBytes(StandardCharsets.UTF_8));
                success = true;
            } catch (IOException | SecurityException e) {
                Log.e(TAG, "CSV export failed", e);
                success = false;
            }
            int count = entries.size();
            boolean finalSuccess = success;
            mainHandler.post(() -> listener.onExportFinished(finalSuccess, count));
        });
    }

    /**
     * Receives the result of a CSV export.
     */
    public interface ExportListener {

        /**
         * Called on the main thread when the export has finished.
         *
         * @param success    whether the file was written.
         * @param entryCount how many foods were in the log.
         */
        void onExportFinished(boolean success, int entryCount);
    }
}
