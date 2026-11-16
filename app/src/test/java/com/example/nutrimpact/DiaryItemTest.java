/**
 * Module: EEE521 - Final Year Project
 * Project: Nutrimpact
 * File: DiaryItemTest.java
 * Description: JUnit tests for DiaryItem.buildList, checking foods are grouped under the right meal headings in the right order.
 */

package com.example.nutrimpact;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for DiaryItem.
 */
public class DiaryItemTest {

    /** Allowed rounding difference when comparing doubles. */
    private static final double DELTA = 0.0001;

    /**
     * Builds a test entry for a meal.
     *
     * @param meal  the meal.
     * @param grams portion in grams.
     * @param kcal  calories per 100g.
     * @return a food log entry.
     */
    private static FoodLogEntry entry(MealType meal, double grams, double kcal) {
        FoodLogEntry entry = DailyTotalsTest.entry(grams, kcal, 0, 0, 0);
        entry.setMealType(meal.name());
        return entry;
    }

    /**
     * Entries logged out of order end up under headings in
     * Breakfast, Lunch, Dinner, Snacks order, with meal subtotals.
     */
    @Test
    public void buildList_groupsByMealInOrder() {
        FoodLogEntry snack = entry(MealType.SNACKS, 30, 150);
        FoodLogEntry breakfast1 = entry(MealType.BREAKFAST, 40, 379);
        FoodLogEntry breakfast2 = entry(MealType.BREAKFAST, 200, 50);
        FoodLogEntry dinner = entry(MealType.DINNER, 100, 200);

        List<DiaryItem> items = DiaryItem.buildList(Arrays.asList(snack, breakfast1, dinner, breakfast2));

        assertEquals(7, items.size());
        assertHeader(items.get(0), MealType.BREAKFAST, 151.6 + 100);
        assertSame(breakfast1, items.get(1).getEntry());
        assertSame(breakfast2, items.get(2).getEntry());
        assertHeader(items.get(3), MealType.DINNER, 200);
        assertSame(dinner, items.get(4).getEntry());
        assertHeader(items.get(5), MealType.SNACKS, 45);
        assertSame(snack, items.get(6).getEntry());
    }

    /**
     * Meals with nothing logged get no heading.
     */
    @Test
    public void buildList_skipsEmptyMeals() {
        List<DiaryItem> items = DiaryItem.buildList(Arrays.asList(entry(MealType.LUNCH, 100, 100)));

        assertEquals(2, items.size());
        assertHeader(items.get(0), MealType.LUNCH, 100);
    }

    /**
     * No entries (or null) gives an empty list.
     */
    @Test
    public void buildList_emptyOrNull_givesEmptyList() {
        assertTrue(DiaryItem.buildList(null).isEmpty());
        assertTrue(DiaryItem.buildList(Arrays.asList()).isEmpty());
    }

    /**
     * An unknown stored meal name is shown under Snacks rather than lost.
     */
    @Test
    public void buildList_unknownMeal_goesToSnacks() {
        FoodLogEntry odd = DailyTotalsTest.entry(100, 10, 0, 0, 0);
        odd.setMealType("BRUNCH");

        List<DiaryItem> items = DiaryItem.buildList(Arrays.asList(odd));

        assertHeader(items.get(0), MealType.SNACKS, 10);
        assertEquals(MealType.SNACKS, items.get(1).getMeal());
    }

    /**
     * Checks that a row is a header for the given meal and subtotal.
     *
     * @param item     the row to check.
     * @param meal     the expected meal.
     * @param calories the expected meal subtotal.
     */
    private static void assertHeader(DiaryItem item, MealType meal, double calories) {
        assertEquals(DiaryItem.TYPE_HEADER, item.getType());
        assertEquals(meal, item.getMeal());
        assertEquals(calories, item.getMealCalories(), DELTA);
        assertNull(item.getEntry());
    }
}
