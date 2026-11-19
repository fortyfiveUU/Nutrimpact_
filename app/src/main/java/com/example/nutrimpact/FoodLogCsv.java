/**
 * Module: EEE521 - Final Year Project
 * Project: Nutrimpact
 * File: FoodLogCsv.java
 * Description: Turns food log entries into a CSV file that opens in Excel, Google Sheets or Numbers, so users can keep their own copy of their data.
 */

package com.example.nutrimpact;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Builds the text of a CSV export. Pure Java (no Android classes) so it can be
 * unit tested.
 */
public final class FoodLogCsv {

    /** The first row of the file. Energy is always in kcal, whatever the display setting. */
    public static final String HEADER =
            "Date,Time,Meal,Food,Brand,Portion (g),Energy (kcal),Protein (g),Carbohydrate (g),Fat (g)";

    /**
     * Byte order mark. Without it, Excel guesses the wrong encoding and shows
     * accented names (e.g. "Crème fraîche") as garbled text.
     */
    public static final String UTF8_BOM = "\uFEFF";

    /** Line ending used between rows, as the CSV standard (RFC 4180) asks for. */
    private static final String LINE_END = "\r\n";

    /** Format for the time a food was added, e.g. "08:15". */
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm", Locale.ROOT);

    /**
     * Private constructor so the class can't be instantiated.
     */
    private FoodLogCsv() {
    }

    /**
     * Builds the whole CSV file: the byte order mark, the header, then one row
     * per entry in the order given.
     *
     * @param entries the entries to export, usually oldest first.
     * @param zone    the time zone to show the time added in.
     * @return the file's text.
     */
    public static String build(List<FoodLogEntry> entries, ZoneId zone) {
        StringBuilder csv = new StringBuilder(UTF8_BOM).append(HEADER).append(LINE_END);
        if (entries != null) {
            for (FoodLogEntry entry : entries) {
                csv.append(row(entry, zone)).append(LINE_END);
            }
        }
        return csv.toString();
    }

    /**
     * Builds one row for an entry, with the nutrition scaled to the portion eaten.
     *
     * @param entry the entry.
     * @param zone  the time zone to show the time added in.
     * @return the row, without a line ending.
     */
    static String row(FoodLogEntry entry, ZoneId zone) {
        String date = LocalDate.ofEpochDay(entry.getLogDate()).toString();
        String time = TIME_FORMAT.format(Instant.ofEpochMilli(entry.getLoggedAt()).atZone(zone));
        return String.join(",",
                date,
                time,
                mealLabel(entry.getMealType()),
                escape(entry.getProductName()),
                escape(entry.getBrands()),
                number(entry.getPortionGrams()),
                number(entry.getPortionCalories()),
                number(entry.getPortionProtein()),
                number(entry.getPortionCarbs()),
                number(entry.getPortionFat()));
    }

    /**
     * Makes a text value safe for a CSV cell.
     * <ul>
     *     <li>Values containing a comma, quote or line break are wrapped in quotes,
     *     with any quotes inside doubled.</li>
     *     <li>Values starting with =, +, - or @ get a leading apostrophe. Product
     *     names come from Open Food Facts, which anyone can edit, and a spreadsheet
     *     would otherwise run a name like "=HYPERLINK(...)" as a formula
     *     ("CSV injection").</li>
     * </ul>
     *
     * @param value the text; null is written as an empty cell.
     * @return the escaped cell.
     */
    static String escape(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        String safe = value;
        char first = safe.charAt(0);
        if (first == '=' || first == '+' || first == '-' || first == '@') {
            safe = "'" + safe;
        }
        if (safe.contains(",") || safe.contains("\"") || safe.contains("\n") || safe.contains("\r")) {
            safe = "\"" + safe.replace("\"", "\"\"") + "\"";
        }
        return safe;
    }

    /**
     * Writes a number with one decimal place and a full stop as the decimal
     * point, whatever the phone's language, so spreadsheets read it as a number.
     *
     * @param value the number.
     * @return e.g. "151.6".
     */
    static String number(double value) {
        return String.format(Locale.ROOT, "%.1f", value);
    }

    /**
     * Turns a stored meal name into a readable one, e.g. "BREAKFAST" into "Breakfast".
     *
     * @param mealName the stored MealType name.
     * @return the readable name.
     */
    static String mealLabel(String mealName) {
        String name = MealType.fromName(mealName).name();
        return name.charAt(0) + name.substring(1).toLowerCase(Locale.ROOT);
    }
}
