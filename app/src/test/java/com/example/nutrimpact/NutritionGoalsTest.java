/**
 * Module: EEE521 - Final Year Project
 * Project: Nutrimpact
 * File: NutritionGoalsTest.java
 * Description: JUnit tests for NutritionGoals defaults, range checks and progress percentages.
 */

package com.example.nutrimpact;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit tests for NutritionGoals.
 */
public class NutritionGoalsTest {

    /**
     * Defaults match the UK Reference Intakes.
     */
    @Test
    public void defaults_areUkReferenceIntakes() {
        NutritionGoals goals = NutritionGoals.defaults();
        assertEquals(2000, goals.getCalories());
        assertEquals(50, goals.getProtein());
        assertEquals(260, goals.getCarbs());
        assertEquals(70, goals.getFat());
    }

    /**
     * Calorie goals must be between 500 and 10,000 kcal.
     */
    @Test
    public void isValidCalories_checksRange() {
        assertTrue(NutritionGoals.isValidCalories(500));
        assertTrue(NutritionGoals.isValidCalories(10000));
        assertFalse(NutritionGoals.isValidCalories(499));
        assertFalse(NutritionGoals.isValidCalories(10001));
        assertFalse(NutritionGoals.isValidCalories(0));
    }

    /**
     * Macro goals must be between 1 and 1,000 g.
     */
    @Test
    public void isValidMacro_checksRange() {
        assertTrue(NutritionGoals.isValidMacro(1));
        assertTrue(NutritionGoals.isValidMacro(1000));
        assertFalse(NutritionGoals.isValidMacro(0));
        assertFalse(NutritionGoals.isValidMacro(1001));
    }

    /**
     * Progress is a rounded percentage, capped at 100 when over the goal.
     */
    @Test
    public void progressPercent_roundsAndCaps() {
        assertEquals(0, NutritionGoals.progressPercent(0, 2000));
        assertEquals(8, NutritionGoals.progressPercent(152, 2000));
        assertEquals(50, NutritionGoals.progressPercent(1000, 2000));
        assertEquals(100, NutritionGoals.progressPercent(2000, 2000));
        assertEquals(100, NutritionGoals.progressPercent(3500, 2000));
    }

    /**
     * Negative consumed values and a zero goal are handled safely.
     */
    @Test
    public void progressPercent_edgeCases() {
        assertEquals(0, NutritionGoals.progressPercent(-5, 2000));
        assertEquals(100, NutritionGoals.progressPercent(10, 0));
    }
}
