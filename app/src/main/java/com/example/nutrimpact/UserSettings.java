/**
 * Module: EEE521 - Final Year Project
 * Project: Nutrimpact
 * File: UserSettings.java
 * Description: Reads and saves the user's settings (nutrition goals, energy unit and default portion unit) in on-device SharedPreferences.
 */

package com.example.nutrimpact;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Settings are stored only on this device. There are no accounts and
 * nothing is sent to a server.
 */
public class UserSettings {

    /** SharedPreferences file name. */
    private static final String PREFS_NAME = "nutrimpact_settings";

    /** Key: daily calorie goal in kcal. */
    private static final String KEY_CALORIE_GOAL = "calorie_goal";

    /** Key: daily protein goal in grams. */
    private static final String KEY_PROTEIN_GOAL = "protein_goal";

    /** Key: daily carbohydrate goal in grams. */
    private static final String KEY_CARBS_GOAL = "carbs_goal";

    /** Key: daily fat goal in grams. */
    private static final String KEY_FAT_GOAL = "fat_goal";

    /** Key: energy unit name ("KCAL" or "KJ"). */
    private static final String KEY_ENERGY_UNIT = "energy_unit";

    /** Key: true if portions should start in ounces. */
    private static final String KEY_OUNCES_DEFAULT = "ounces_default";

    /** The underlying preferences file. */
    private final SharedPreferences prefs;

    /**
     * Opens the app's settings.
     *
     * @param context any context; the application context is used.
     */
    public UserSettings(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Gets the saved goals, or the UK Reference Intake defaults if none are saved
     * (or a saved value is out of range).
     *
     * @return the daily goals.
     */
    public NutritionGoals getGoals() {
        int calories = prefs.getInt(KEY_CALORIE_GOAL, NutritionGoals.DEFAULT_CALORIES);
        int protein = prefs.getInt(KEY_PROTEIN_GOAL, NutritionGoals.DEFAULT_PROTEIN);
        int carbs = prefs.getInt(KEY_CARBS_GOAL, NutritionGoals.DEFAULT_CARBS);
        int fat = prefs.getInt(KEY_FAT_GOAL, NutritionGoals.DEFAULT_FAT);
        return new NutritionGoals(
                NutritionGoals.isValidCalories(calories) ? calories : NutritionGoals.DEFAULT_CALORIES,
                NutritionGoals.isValidMacro(protein) ? protein : NutritionGoals.DEFAULT_PROTEIN,
                NutritionGoals.isValidMacro(carbs) ? carbs : NutritionGoals.DEFAULT_CARBS,
                NutritionGoals.isValidMacro(fat) ? fat : NutritionGoals.DEFAULT_FAT);
    }

    /**
     * Saves new goals. Values should already have been checked as valid.
     *
     * @param goals the goals to save.
     */
    public void setGoals(NutritionGoals goals) {
        prefs.edit()
                .putInt(KEY_CALORIE_GOAL, goals.getCalories())
                .putInt(KEY_PROTEIN_GOAL, goals.getProtein())
                .putInt(KEY_CARBS_GOAL, goals.getCarbs())
                .putInt(KEY_FAT_GOAL, goals.getFat())
                .apply();
    }

    /**
     * Gets the unit energy is shown in.
     *
     * @return KCAL (default) or KJ.
     */
    public EnergyUnit getEnergyUnit() {
        return EnergyUnit.fromName(prefs.getString(KEY_ENERGY_UNIT, EnergyUnit.KCAL.name()));
    }

    /**
     * Sets the unit energy is shown in.
     *
     * @param unit KCAL or KJ.
     */
    public void setEnergyUnit(EnergyUnit unit) {
        prefs.edit().putString(KEY_ENERGY_UNIT, unit.name()).apply();
    }

    /**
     * Checks whether new portions should start in ounces.
     *
     * @return true for ounces, false for grams (default).
     */
    public boolean isOuncesDefault() {
        return prefs.getBoolean(KEY_OUNCES_DEFAULT, false);
    }

    /**
     * Sets whether new portions start in ounces.
     *
     * @param ounces true for ounces, false for grams.
     */
    public void setOuncesDefault(boolean ounces) {
        prefs.edit().putBoolean(KEY_OUNCES_DEFAULT, ounces).apply();
    }
}
