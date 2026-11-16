/**
 * Module: EEE521 - Final Year Project
 * Project: Nutrimpact
 * File: WeekSummary.java
 * Description: Turns a week of daily totals into seven chart-ready days (filling in days with nothing logged) and works out averages and days within the calorie goal.
 */

package com.example.nutrimpact;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Summary of the seven days ending on a given day. Has no Android
 * dependencies, so it can be unit tested on the computer.
 */
public final class WeekSummary {

    /** Number of days in the summary. */
    public static final int DAYS = 7;

    /** The seven days, oldest first. Days with nothing logged have zero totals. */
    private final List<DayTotal> days;

    /** Number of days with at least one food logged. */
    private final int daysLogged;

    /** Average calories over the days that were logged. */
    private final double averageCalories;

    /** Average protein over the days that were logged. */
    private final double averageProtein;

    /** Average carbohydrates over the days that were logged. */
    private final double averageCarbs;

    /** Average fat over the days that were logged. */
    private final double averageFat;

    /** Logged days whose calories were at or under the goal. */
    private final int daysWithinGoal;

    /**
     * Creates a summary. Use {@link #of(List, long, int)} to build one.
     *
     * @param days            the seven days, oldest first.
     * @param daysLogged      days with something logged.
     * @param averageCalories average calories over logged days.
     * @param averageProtein  average protein over logged days.
     * @param averageCarbs    average carbohydrates over logged days.
     * @param averageFat      average fat over logged days.
     * @param daysWithinGoal  logged days at or under the calorie goal.
     */
    private WeekSummary(List<DayTotal> days, int daysLogged, double averageCalories, double averageProtein,
                        double averageCarbs, double averageFat, int daysWithinGoal) {
        this.days = Collections.unmodifiableList(days);
        this.daysLogged = daysLogged;
        this.averageCalories = averageCalories;
        this.averageProtein = averageProtein;
        this.averageCarbs = averageCarbs;
        this.averageFat = averageFat;
        this.daysWithinGoal = daysWithinGoal;
    }

    /**
     * Builds the summary for the seven days ending on {@code endDay}.
     * Averages only count days where something was logged, so a day the
     * user forgot to log doesn't drag the average down.
     *
     * @param totals      daily totals from the database (any order; days outside the week are ignored).
     * @param endDay      the last day of the week, as days since 1970-01-01.
     * @param calorieGoal the daily calorie goal in kcal.
     * @return the week's summary.
     */
    public static WeekSummary of(List<DayTotal> totals, long endDay, int calorieGoal) {
        Map<Long, DayTotal> byDay = new HashMap<>();
        if (totals != null) {
            for (DayTotal total : totals) {
                if (total != null) {
                    byDay.put(total.getLogDate(), total);
                }
            }
        }

        List<DayTotal> days = new ArrayList<>();
        int logged = 0;
        int withinGoal = 0;
        double calories = 0;
        double protein = 0;
        double carbs = 0;
        double fat = 0;
        for (long day = endDay - (DAYS - 1); day <= endDay; day++) {
            DayTotal total = byDay.get(day);
            if (total == null) {
                days.add(DayTotal.of(day, 0, 0, 0, 0));
                continue;
            }
            days.add(total);
            logged++;
            calories += total.getCalories();
            protein += total.getProtein();
            carbs += total.getCarbs();
            fat += total.getFat();
            if (total.getCalories() <= calorieGoal) {
                withinGoal++;
            }
        }

        if (logged == 0) {
            return new WeekSummary(days, 0, 0, 0, 0, 0, 0);
        }
        return new WeekSummary(days, logged, calories / logged, protein / logged,
                carbs / logged, fat / logged, withinGoal);
    }

    /**
     * Gets the seven days, oldest first.
     *
     * @return read-only list of seven days.
     */
    public List<DayTotal> getDays() {
        return days;
    }

    /**
     * Gets how many days had something logged.
     *
     * @return 0 to 7.
     */
    public int getDaysLogged() {
        return daysLogged;
    }

    /**
     * Gets average calories over logged days.
     *
     * @return kcal per logged day, or 0 if nothing was logged.
     */
    public double getAverageCalories() {
        return averageCalories;
    }

    /**
     * Gets average protein over logged days.
     *
     * @return grams per logged day, or 0 if nothing was logged.
     */
    public double getAverageProtein() {
        return averageProtein;
    }

    /**
     * Gets average carbohydrates over logged days.
     *
     * @return grams per logged day, or 0 if nothing was logged.
     */
    public double getAverageCarbs() {
        return averageCarbs;
    }

    /**
     * Gets average fat over logged days.
     *
     * @return grams per logged day, or 0 if nothing was logged.
     */
    public double getAverageFat() {
        return averageFat;
    }

    /**
     * Gets how many logged days were at or under the calorie goal.
     *
     * @return 0 to 7.
     */
    public int getDaysWithinGoal() {
        return daysWithinGoal;
    }
}
