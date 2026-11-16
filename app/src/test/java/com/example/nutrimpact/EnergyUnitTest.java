/**
 * Module: EEE521 - Final Year Project
 * Project: Nutrimpact
 * File: EnergyUnitTest.java
 * Description: JUnit tests for EnergyUnit's kcal/kJ conversion and safe name lookup.
 */

package com.example.nutrimpact;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit tests for EnergyUnit.
 */
public class EnergyUnitTest {

    /** Allowed rounding difference when comparing doubles. */
    private static final double DELTA = 0.0001;

    /**
     * kcal values are shown unchanged.
     */
    @Test
    public void kcal_isUnchanged() {
        assertEquals(152.0, EnergyUnit.KCAL.fromKcal(152), DELTA);
        assertEquals(152.0, EnergyUnit.KCAL.toKcal(152), DELTA);
    }

    /**
     * 100 kcal is 418.4 kJ, and converting back gives 100 kcal.
     */
    @Test
    public void kj_convertsBothWays() {
        assertEquals(418.4, EnergyUnit.KJ.fromKcal(100), DELTA);
        assertEquals(100.0, EnergyUnit.KJ.toKcal(418.4), DELTA);
        assertEquals(8368.0, EnergyUnit.KJ.fromKcal(2000), DELTA);
    }

    /**
     * Stored names map back to units; anything unknown falls back to kcal.
     */
    @Test
    public void fromName_fallsBackToKcal() {
        assertEquals(EnergyUnit.KJ, EnergyUnit.fromName("KJ"));
        assertEquals(EnergyUnit.KCAL, EnergyUnit.fromName("KCAL"));
        assertEquals(EnergyUnit.KCAL, EnergyUnit.fromName("joules"));
        assertEquals(EnergyUnit.KCAL, EnergyUnit.fromName(null));
    }
}
