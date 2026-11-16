/**
 * Module: EEE521 - Final Year Project
 * Project: Nutrimpact
 * File: DailyTotalsTest.java
 * Description: JUnit tests for DailyTotals, checking that portion nutrition is added up correctly.
 */

package com.example.nutrimpact;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Unit tests for DailyTotals.
 */
public class DailyTotalsTest {

    /** Allowed rounding difference when comparing doubles. */
    private static final double DELTA = 0.0001;

    /**
     * Builds a test entry.
     *
     * @param grams    portion in grams.
     * @param kcal     calories per 100g.
     * @param protein  protein per 100g.
     * @param carbs    carbohydrates per 100g.
     * @param fat      fat per 100g.
     * @return a food log entry.
     */
    static FoodLogEntry entry(double grams, double kcal, double protein, double carbs, double fat) {
        FoodLogEntry entry = new FoodLogEntry();
        entry.setPortionGrams(grams);
        entry.setCaloriesPer100g(kcal);
        entry.setProteinPer100g(protein);
        entry.setCarbsPer100g(carbs);
        entry.setFatPer100g(fat);
        return entry;
    }

    /**
     * 40g of oats (379 kcal/100g) plus 30g of oat bran (150 kcal/100g)
     * is 151.6 + 45 = 196.6 kcal, and the macros add up the same way.
     */
    @Test
    public void of_addsUpScaledPortions() {
        DailyTotals totals = DailyTotals.of(Arrays.asList(
                entry(40, 379, 8.9, 68, 6.4),
                entry(30, 150, 7, 26, 2.5)));

        assertEquals(196.6, totals.getCalories(), DELTA);
        assertEquals(3.56 + 2.1, totals.getProtein(), DELTA);
        assertEquals(27.2 + 7.8, totals.getCarbs(), DELTA);
        assertEquals(2.56 + 0.75, totals.getFat(), DELTA);
    }

    /**
     * No entries gives all zeros, and null is handled the same way.
     */
    @Test
    public void of_emptyOrNull_returnsZeros() {
        DailyTotals empty = DailyTotals.of(Collections.emptyList());
        DailyTotals fromNull = DailyTotals.of(null);

        assertEquals(0.0, empty.getCalories(), DELTA);
        assertEquals(0.0, empty.getFat(), DELTA);
        assertEquals(0.0, fromNull.getCalories(), DELTA);
        assertEquals(0.0, fromNull.getProtein(), DELTA);
    }

    /**
     * Null entries inside the list are skipped instead of crashing.
     */
    @Test
    public void of_skipsNullEntries() {
        List<FoodLogEntry> entries = new ArrayList<>();
        entries.add(null);
        entries.add(entry(100, 50, 1, 2, 3));

        assertEquals(50.0, DailyTotals.of(entries).getCalories(), DELTA);
    }
}
