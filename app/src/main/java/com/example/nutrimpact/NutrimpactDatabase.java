/**
 * Module: EEE521 - Final Year Project
 * Project: Nutrimpact
 * File: NutrimpactDatabase.java
 * Description: The app's Room (SQLite) database, stored only on the device. Provides a single shared instance and a background thread for writes.
 */

package com.example.nutrimpact;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Local database holding the user's food log. Nothing in it is sent to any
 * server, in line with the app's privacy aims.
 */
@Database(entities = {FoodLogEntry.class}, version = 1, exportSchema = false)
public abstract class NutrimpactDatabase extends RoomDatabase {

    /** File name of the database on the device. */
    private static final String DATABASE_NAME = "nutrimpact.db";

    /** Background thread for database writes, so the UI never freezes. */
    public static final ExecutorService DATABASE_WRITE_EXECUTOR = Executors.newSingleThreadExecutor();

    /** The single shared instance, created on first use. */
    private static volatile NutrimpactDatabase instance;

    /**
     * Gets the DAO for the food log. Room generates the implementation.
     *
     * @return the food log DAO.
     */
    public abstract FoodLogDao foodLogDao();

    /**
     * Gets the shared database, creating it on first use.
     *
     * @param context any context; the application context is used so activities aren't leaked.
     * @return the database.
     */
    public static NutrimpactDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (NutrimpactDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                                    NutrimpactDatabase.class, DATABASE_NAME)
                            .build();
                }
            }
        }
        return instance;
    }
}
