/**
 * Module: EEE521 - Final Year Project
 * Project: Nutrimpact
 * File: DayTotal.java
 * Description: One day's nutrition totals, filled in by a Room query that adds up that day's logged foods.
 */

package com.example.nutrimpact;

import androidx.room.ColumnInfo;

/**
 * Totals for a single day. Returned by FoodLogDao.getDayTotals() and used by
 * the Progress screen's weekly chart.
 */
public class DayTotal {

    /** The day, as days since 1970-01-01. */
    @ColumnInfo(name = "log_date")
    private long logDate;

    /** Total calories in kcal. */
    @ColumnInfo(name = "calories")
    private double calories;

    /** Total protein in grams. */
    @ColumnInfo(name = "protein")
    private double protein;

    /** Total carbohydrates in grams. */
    @ColumnInfo(name = "carbs")
    private double carbs;

    /** Total fat in grams. */
    @ColumnInfo(name = "fat")
    private double fat;

    /**
     * Default constructor, required by Room.
     */
    public DayTotal() {
    }

    /**
     * Creates a day's totals directly (used for days with nothing logged, and in tests).
     *
     * @param logDate  the day, as days since 1970-01-01.
     * @param calories total calories in kcal.
     * @param protein  total protein in grams.
     * @param carbs    total carbohydrates in grams.
     * @param fat      total fat in grams.
     * @return the day's totals.
     */
    public static DayTotal of(long logDate, double calories, double protein, double carbs, double fat) {
        DayTotal total = new DayTotal();
        total.logDate = logDate;
        total.calories = calories;
        total.protein = protein;
        total.carbs = carbs;
        total.fat = fat;
        return total;
    }

    /**
     * Gets the day.
     *
     * @return days since 1970-01-01.
     */
    public long getLogDate() {
        return logDate;
    }

    /**
     * Sets the day. Used by Room.
     *
     * @param logDate days since 1970-01-01.
     */
    public void setLogDate(long logDate) {
        this.logDate = logDate;
    }

    /**
     * Gets total calories.
     *
     * @return calories in kcal.
     */
    public double getCalories() {
        return calories;
    }

    /**
     * Sets total calories. Used by Room.
     *
     * @param calories calories in kcal.
     */
    public void setCalories(double calories) {
        this.calories = calories;
    }

    /**
     * Gets total protein.
     *
     * @return protein in grams.
     */
    public double getProtein() {
        return protein;
    }

    /**
     * Sets total protein. Used by Room.
     *
     * @param protein protein in grams.
     */
    public void setProtein(double protein) {
        this.protein = protein;
    }

    /**
     * Gets total carbohydrates.
     *
     * @return carbohydrates in grams.
     */
    public double getCarbs() {
        return carbs;
    }

    /**
     * Sets total carbohydrates. Used by Room.
     *
     * @param carbs carbohydrates in grams.
     */
    public void setCarbs(double carbs) {
        this.carbs = carbs;
    }

    /**
     * Gets total fat.
     *
     * @return fat in grams.
     */
    public double getFat() {
        return fat;
    }

    /**
     * Sets total fat. Used by Room.
     *
     * @param fat fat in grams.
     */
    public void setFat(double fat) {
        this.fat = fat;
    }
}
