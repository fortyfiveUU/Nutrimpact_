/**
 * Module: EEE521 - Final Year Project
 * Project: Nutrimpact
 * File: FoodLogDao.java
 * Description: Room Data Access Object defining the database queries for the food log.
 */

package com.example.nutrimpact;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

/**
 * Database operations for the "food_log" table. Room generates the code for
 * these methods at build time.
 */
@Dao
public interface FoodLogDao {

    /**
     * Adds a food to the log. Must be called off the main thread.
     *
     * @param entry the food to add.
     * @return the new entry's ID.
     */
    @Insert
    long insert(FoodLogEntry entry);

    /**
     * Removes a food from the log. Must be called off the main thread.
     *
     * @param entry the food to remove.
     */
    @Delete
    void delete(FoodLogEntry entry);

    /**
     * Gets every food logged on one day, oldest first. The LiveData updates
     * automatically whenever the log changes.
     *
     * @param epochDay the day, as days since 1970-01-01.
     * @return live list of that day's entries.
     */
    @Query("SELECT * FROM food_log WHERE log_date = :epochDay ORDER BY logged_at ASC")
    LiveData<List<FoodLogEntry>> getEntriesForDay(long epochDay);

    /**
     * Adds up each day's nutrition between two days (inclusive). Days with
     * nothing logged are not returned. Scaling from per-100g to the portion
     * is done in SQL, matching PortionCalculator.scale().
     *
     * @param startDay first day, as days since 1970-01-01.
     * @param endDay   last day, as days since 1970-01-01.
     * @return live list of daily totals, oldest first.
     */
    @Query("SELECT log_date,"
            + " SUM(calories_per_100g * portion_grams / 100.0) AS calories,"
            + " SUM(protein_per_100g * portion_grams / 100.0) AS protein,"
            + " SUM(carbs_per_100g * portion_grams / 100.0) AS carbs,"
            + " SUM(fat_per_100g * portion_grams / 100.0) AS fat"
            + " FROM food_log WHERE log_date BETWEEN :startDay AND :endDay"
            + " GROUP BY log_date ORDER BY log_date ASC")
    LiveData<List<DayTotal>> getDayTotals(long startDay, long endDay);
}
