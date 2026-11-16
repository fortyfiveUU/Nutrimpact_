/**
 * Module: EEE521 - Final Year Project
 * Project: Nutrimpact
 * File: EnergyUnit.java
 * Description: The units energy can be shown in (kilocalories or kilojoules), with conversion and formatting.
 */

package com.example.nutrimpact;

import android.content.Context;

/**
 * Energy display units. All values are stored in kcal and converted only
 * when shown on screen.
 */
public enum EnergyUnit {

    /** Kilocalories ("calories"), the default. */
    KCAL(R.string.energy_kcal),
    /** Kilojoules, as printed on UK and EU food labels alongside kcal. */
    KJ(R.string.energy_kj);

    /** Kilojoules in one kilocalorie. */
    public static final double KJ_PER_KCAL = 4.184;

    /** Format string resource, e.g. "%1$.0f kcal". */
    private final int formatResId;

    /**
     * Creates an energy unit.
     *
     * @param formatResId string resource used to show a value in this unit.
     */
    EnergyUnit(int formatResId) {
        this.formatResId = formatResId;
    }

    /**
     * Converts a value in kcal to this unit.
     *
     * @param kcal energy in kilocalories.
     * @return the energy in this unit.
     */
    public double fromKcal(double kcal) {
        return (this == KJ) ? kcal * KJ_PER_KCAL : kcal;
    }

    /**
     * Converts a value in this unit back to kcal.
     *
     * @param value energy in this unit.
     * @return the energy in kilocalories.
     */
    public double toKcal(double value) {
        return (this == KJ) ? value / KJ_PER_KCAL : value;
    }

    /**
     * Formats an energy value for display, e.g. "152 kcal" or "636 kJ".
     *
     * @param context used to read the string resource.
     * @param kcal    the energy in kilocalories.
     * @return the formatted text in this unit.
     */
    public String format(Context context, double kcal) {
        return context.getString(formatResId, fromKcal(kcal));
    }

    /**
     * Converts a stored name back to an EnergyUnit, falling back to kcal.
     *
     * @param name the stored name, e.g. "KJ".
     * @return the matching unit, or KCAL if there is no match.
     */
    public static EnergyUnit fromName(String name) {
        return "KJ".equals(name) ? KJ : KCAL;
    }
}
