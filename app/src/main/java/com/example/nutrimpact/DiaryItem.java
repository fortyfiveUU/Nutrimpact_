/**
 * Module: EEE521 - Final Year Project
 * Project: Nutrimpact
 * File: DiaryItem.java
 * Description: One row in the diary list: either a meal heading with that meal's calorie subtotal, or a single logged food.
 */

package com.example.nutrimpact;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * A row in the diary list. Built from a day's entries by {@link #buildList(List)},
 * which groups foods under meal headings in Breakfast, Lunch, Dinner, Snacks order.
 */
public final class DiaryItem {

    /** View type for a meal heading row. */
    public static final int TYPE_HEADER = 0;

    /** View type for a logged food row. */
    public static final int TYPE_ENTRY = 1;

    /** Which kind of row this is. */
    private final int type;

    /** The meal this row belongs to. */
    private final MealType meal;

    /** For headers: the meal's total calories. */
    private final double mealCalories;

    /** For food rows: the logged food. Null for headers. */
    private final FoodLogEntry entry;

    /**
     * Creates a row. Use the static factory methods instead.
     *
     * @param type         TYPE_HEADER or TYPE_ENTRY.
     * @param meal         the meal the row belongs to.
     * @param mealCalories the meal's calorie subtotal (headers only).
     * @param entry        the logged food (food rows only).
     */
    private DiaryItem(int type, MealType meal, double mealCalories, FoodLogEntry entry) {
        this.type = type;
        this.meal = meal;
        this.mealCalories = mealCalories;
        this.entry = entry;
    }

    /**
     * Creates a meal heading row.
     *
     * @param meal         the meal.
     * @param mealCalories the meal's total calories.
     * @return a header row.
     */
    public static DiaryItem header(MealType meal, double mealCalories) {
        return new DiaryItem(TYPE_HEADER, meal, mealCalories, null);
    }

    /**
     * Creates a logged food row.
     *
     * @param entry the logged food.
     * @return a food row.
     */
    public static DiaryItem entry(FoodLogEntry entry) {
        return new DiaryItem(TYPE_ENTRY, MealType.fromName(entry.getMealType()), 0, entry);
    }

    /**
     * Groups a day's entries by meal. Meals with nothing logged are left out.
     * Within each meal, entries keep the order they were given in.
     *
     * @param entries the day's logged foods; null or empty gives an empty list.
     * @return header and food rows, ready for the diary list.
     */
    public static List<DiaryItem> buildList(List<FoodLogEntry> entries) {
        Map<MealType, List<FoodLogEntry>> byMeal = new EnumMap<>(MealType.class);
        if (entries != null) {
            for (FoodLogEntry entry : entries) {
                if (entry == null) {
                    continue;
                }
                MealType meal = MealType.fromName(entry.getMealType());
                List<FoodLogEntry> mealEntries = byMeal.get(meal);
                if (mealEntries == null) {
                    mealEntries = new ArrayList<>();
                    byMeal.put(meal, mealEntries);
                }
                mealEntries.add(entry);
            }
        }

        // EnumMap iterates in declaration order: Breakfast, Lunch, Dinner, Snacks.
        List<DiaryItem> items = new ArrayList<>();
        for (Map.Entry<MealType, List<FoodLogEntry>> group : byMeal.entrySet()) {
            items.add(header(group.getKey(), DailyTotals.of(group.getValue()).getCalories()));
            for (FoodLogEntry entry : group.getValue()) {
                items.add(entry(entry));
            }
        }
        return items;
    }

    /**
     * Gets which kind of row this is.
     *
     * @return TYPE_HEADER or TYPE_ENTRY.
     */
    public int getType() {
        return type;
    }

    /**
     * Gets the meal this row belongs to.
     *
     * @return the meal.
     */
    public MealType getMeal() {
        return meal;
    }

    /**
     * Gets the meal's total calories (header rows only).
     *
     * @return calories in kcal, or 0 for food rows.
     */
    public double getMealCalories() {
        return mealCalories;
    }

    /**
     * Gets the logged food (food rows only).
     *
     * @return the entry, or null for header rows.
     */
    public FoodLogEntry getEntry() {
        return entry;
    }
}
