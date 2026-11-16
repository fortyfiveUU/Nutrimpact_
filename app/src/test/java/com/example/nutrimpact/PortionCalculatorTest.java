/**
 * Module: EEE521 - Final Year Project
 * Project: Nutrimpact
 * File: PortionCalculatorTest.java
 * Description: JUnit tests for PortionCalculator's portion scaling, unit conversion and validation.
 */

package com.example.nutrimpact;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit tests for PortionCalculator.
 */
public class PortionCalculatorTest {

    /** Allowed rounding difference when comparing doubles. */
    private static final double DELTA = 0.0001;

    /**
     * 40g of a 379 kcal/100g food is 151.6 kcal.
     */
    @Test
    public void scale_partialPortion_scalesDown() {
        assertEquals(151.6, PortionCalculator.scale(379.0, 40), DELTA);
    }

    /**
     * 100g returns the per-100g value unchanged.
     */
    @Test
    public void scale_100g_returnsSameValue() {
        assertEquals(8.9, PortionCalculator.scale(8.9, 100), DELTA);
    }

    /**
     * 250g of a 6.4g/100g food is 16g.
     */
    @Test
    public void scale_largePortion_scalesUp() {
        assertEquals(16.0, PortionCalculator.scale(6.4, 250), DELTA);
    }

    /**
     * A missing (null) value scales to 0 instead of crashing.
     */
    @Test
    public void scale_nullValue_returnsZero() {
        assertEquals(0.0, PortionCalculator.scale(null, 40), DELTA);
    }

    /**
     * A zero or negative portion scales to 0.
     */
    @Test
    public void scale_zeroOrNegativePortion_returnsZero() {
        assertEquals(0.0, PortionCalculator.scale(379.0, 0), DELTA);
        assertEquals(0.0, PortionCalculator.scale(379.0, -50), DELTA);
    }

    /**
     * 1 ounce is 28.3495g, and converting back gives 1 ounce.
     */
    @Test
    public void ounceConversion_roundTrips() {
        assertEquals(28.349523125, PortionCalculator.ouncesToGrams(1), DELTA);
        assertEquals(1.0, PortionCalculator.gramsToOunces(PortionCalculator.ouncesToGrams(1)), DELTA);
    }

    /**
     * 100g is about 3.527 ounces.
     */
    @Test
    public void gramsToOunces_100g() {
        assertEquals(3.5274, PortionCalculator.gramsToOunces(100), DELTA);
    }

    /**
     * Portions above 0g and up to 5kg are valid; anything else isn't.
     */
    @Test
    public void isValidPortion_checksRange() {
        assertTrue(PortionCalculator.isValidPortion(0.5));
        assertTrue(PortionCalculator.isValidPortion(5000));
        assertFalse(PortionCalculator.isValidPortion(0));
        assertFalse(PortionCalculator.isValidPortion(-1));
        assertFalse(PortionCalculator.isValidPortion(5000.1));
    }
}
