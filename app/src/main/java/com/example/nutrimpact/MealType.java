/**
 * Module: EEE521 - Final Year Project
 * Project: Nutrimpact
 * File: MealType.java
 * Description: The meal categories a logged food can belong to (Breakfast, Lunch, Dinner, Snacks), with a suggestion based on the time of day.
 */

package com.example.nutrimpact;

import java.time.LocalTime;

/**
 * Meal categories used when logging food. Stored in the database by name,
 * e.g. "BREAKFAST".
 */
public enum MealType {

    /** Morning meal. */
    BREAKFAST(R.string.meal_breakfast),
    /** Midday meal. */
    LUNCH(R.string.meal_lunch),
    /** Evening meal. */
    DINNER(R.string.meal_dinner),
    /** Anything eaten between meals. */
    SNACKS(R.string.meal_snacks);

    /** String resource ID for the meal's display name. */
    private final int labelResId;

    /**
     * Creates a meal type.
     *
     * @param labelResId string resource ID for the display name.
     */
    MealType(int labelResId) {
        this.labelResId = labelResId;
    }

    /**
     * Gets the string resource ID for this meal's display name.
     *
     * @return the label resource ID, e.g. R.string.meal_breakfast.
     */
    public int getLabelResId() {
        return labelResId;
    }

    /**
     * Suggests the most likely meal for a time of day:
     * Breakfast 05:00–10:59, Lunch 11:00–14:59, Dinner 17:00–21:59, otherwise Snacks.
     *
     * @param time the time to suggest a meal for.
     * @return the suggested meal.
     */
    public static MealType suggestFor(LocalTime time) {
        int hour = time.getHour();
        if (hour >= 5 && hour < 11) {
            return BREAKFAST;
        } else if (hour >= 11 && hour < 15) {
            return LUNCH;
        } else if (hour >= 17 && hour < 22) {
            return DINNER;
        }
        return SNACKS;
    }

    /**
     * Converts a stored name back to a MealType, falling back to SNACKS for
     * unknown or missing values so old or bad data can't crash the app.
     *
     * @param name the stored name, e.g. "LUNCH".
     * @return the matching meal, or SNACKS if there is no match.
     */
    public static MealType fromName(String name) {
        if (name != null) {
            for (MealType meal : values()) {
                if (meal.name().equals(name)) {
                    return meal;
                }
            }
        }
        return SNACKS;
    }
}
