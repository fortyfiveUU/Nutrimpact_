/**
 * Module: EEE521 - Final Year Project
 * Project: Nutrimpact
 * File: WeekSummaryTest.java
 * Description: JUnit tests for WeekSummary: filling in missing days, averaging only logged days, and counting days within the calorie goal.
 */

package com.example.nutrimpact;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for WeekSummary.
 */
public class WeekSummaryTest {

    /** Allowed rounding difference when comparing doubles. */
    private static final double DELTA = 0.0001;

    /** An arbitrary "today" for the tests. */
    private static final long END = 20000;

    /**
     * Seven days are always returned, oldest first, with zeros for days
     * that have nothing logged.
     */
    @Test
    public void of_fillsInMissingDays() {
        WeekSummary week = WeekSummary.of(Arrays.asList(
                DayTotal.of(END - 6, 1800, 60, 200, 50),
                DayTotal.of(END, 2200, 80, 250, 80)), END, 2000);

        List<DayTotal> days = week.getDays();
        assertEquals(7, days.size());
        assertEquals(END - 6, days.get(0).getLogDate());
        assertEquals(END, days.get(6).getLogDate());
        assertEquals(1800, days.get(0).getCalories(), DELTA);
        assertEquals(0, days.get(3).getCalories(), DELTA);
        assertEquals(2200, days.get(6).getCalories(), DELTA);
    }

    /**
     * Averages only count days with something logged.
     */
    @Test
    public void of_averagesLoggedDaysOnly() {
        WeekSummary week = WeekSummary.of(Arrays.asList(
                DayTotal.of(END - 6, 1800, 60, 200, 50),
                DayTotal.of(END, 2200, 80, 250, 80)), END, 2000);

        assertEquals(2, week.getDaysLogged());
        assertEquals(2000, week.getAverageCalories(), DELTA);
        assertEquals(70, week.getAverageProtein(), DELTA);
        assertEquals(225, week.getAverageCarbs(), DELTA);
        assertEquals(65, week.getAverageFat(), DELTA);
    }

    /**
     * A day exactly on the goal counts as within it; a day over doesn't.
     */
    @Test
    public void of_countsDaysWithinGoal() {
        WeekSummary week = WeekSummary.of(Arrays.asList(
                DayTotal.of(END - 2, 2000, 0, 0, 0),
                DayTotal.of(END - 1, 2001, 0, 0, 0),
                DayTotal.of(END, 1500, 0, 0, 0)), END, 2000);

        assertEquals(3, week.getDaysLogged());
        assertEquals(2, week.getDaysWithinGoal());
    }

    /**
     * Days outside the week are ignored.
     */
    @Test
    public void of_ignoresDaysOutsideTheWeek() {
        WeekSummary week = WeekSummary.of(Arrays.asList(
                DayTotal.of(END - 7, 9999, 0, 0, 0),
                DayTotal.of(END + 1, 9999, 0, 0, 0)), END, 2000);

        assertEquals(0, week.getDaysLogged());
        assertEquals(0, week.getAverageCalories(), DELTA);
    }

    /**
     * No data (or null) gives seven empty days and zero averages.
     */
    @Test
    public void of_emptyOrNull() {
        WeekSummary week = WeekSummary.of(null, END, 2000);
        assertEquals(7, week.getDays().size());
        assertEquals(0, week.getDaysLogged());
        assertEquals(0, week.getDaysWithinGoal());
    }
}
