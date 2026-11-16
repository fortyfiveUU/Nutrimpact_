/**
 * Module: EEE521 - Final Year Project
 * Project: Nutrimpact
 * File: MealTypeTest.java
 * Description: JUnit tests for MealType's time-of-day suggestion and safe name lookup.
 */

package com.example.nutrimpact;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.time.LocalTime;

/**
 * Unit tests for MealType.
 */
public class MealTypeTest {

    /**
     * Checks the meal suggested at the edges of each time window.
     */
    @Test
    public void suggestFor_timeWindows() {
        assertEquals(MealType.SNACKS, MealType.suggestFor(LocalTime.of(4, 59)));
        assertEquals(MealType.BREAKFAST, MealType.suggestFor(LocalTime.of(5, 0)));
        assertEquals(MealType.BREAKFAST, MealType.suggestFor(LocalTime.of(10, 59)));
        assertEquals(MealType.LUNCH, MealType.suggestFor(LocalTime.of(11, 0)));
        assertEquals(MealType.LUNCH, MealType.suggestFor(LocalTime.of(14, 59)));
        assertEquals(MealType.SNACKS, MealType.suggestFor(LocalTime.of(16, 0)));
        assertEquals(MealType.DINNER, MealType.suggestFor(LocalTime.of(17, 0)));
        assertEquals(MealType.DINNER, MealType.suggestFor(LocalTime.of(21, 59)));
        assertEquals(MealType.SNACKS, MealType.suggestFor(LocalTime.of(22, 0)));
    }

    /**
     * Stored names map back to the right meal; unknown or null names fall back to SNACKS.
     */
    @Test
    public void fromName_handlesKnownAndUnknownNames() {
        assertEquals(MealType.LUNCH, MealType.fromName("LUNCH"));
        assertEquals(MealType.SNACKS, MealType.fromName("BRUNCH"));
        assertEquals(MealType.SNACKS, MealType.fromName(null));
    }
}
