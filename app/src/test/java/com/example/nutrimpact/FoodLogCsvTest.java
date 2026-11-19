/**
 * Module: EEE521 - Final Year Project
 * Project: Nutrimpact
 * File: FoodLogCsvTest.java
 * Description: Unit tests for FoodLogCsv: the header, scaled values in each row, escaping of commas and quotes, and protection against spreadsheet formula injection.
 */

package com.example.nutrimpact;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;

/**
 * Tests the CSV export text.
 */
public class FoodLogCsvTest {

    /** A fixed time zone so the "Time" column doesn't depend on the computer running the tests. */
    private static final ZoneId UTC = ZoneOffset.UTC;

    /**
     * Builds a logged food at 08:15 UTC on 19 November 2026.
     *
     * @param name   product name.
     * @param brands brand text.
     * @return the entry.
     */
    private static FoodLogEntry entry(String name, String brands) {
        FoodLogEntry entry = DailyTotalsTest.entry(40, 379, 8.9, 68, 6.4);
        entry.setProductName(name);
        entry.setBrands(brands);
        entry.setMealType(MealType.BREAKFAST.name());
        entry.setLogDate(LocalDate.of(2026, 11, 19).toEpochDay());
        entry.setLoggedAt(LocalDateTime.of(2026, 11, 19, 8, 15).toInstant(ZoneOffset.UTC).toEpochMilli());
        return entry;
    }

    /**
     * An empty log still gives a valid file: the byte order mark and the header row.
     */
    @Test
    public void build_emptyLog_hasOnlyHeader() {
        assertEquals(FoodLogCsv.UTF8_BOM + FoodLogCsv.HEADER + "\r\n",
                FoodLogCsv.build(Collections.emptyList(), UTC));
        assertEquals(FoodLogCsv.UTF8_BOM + FoodLogCsv.HEADER + "\r\n", FoodLogCsv.build(null, UTC));
    }

    /**
     * A row has the date, time, readable meal name, and values scaled to the
     * portion: 40 g of 379 kcal/100g is 151.6 kcal.
     */
    @Test
    public void row_showsScaledValues() {
        assertEquals("2026-11-19,08:15,Breakfast,Oats,Tesco,40.0,151.6,3.6,27.2,2.6",
                FoodLogCsv.row(entry("Oats", "Tesco"), UTC));
    }

    /**
     * Commas and quotes in names are escaped so they don't split the row into extra columns.
     */
    @Test
    public void escape_handlesCommasAndQuotes() {
        assertEquals("\"Heinz, Kraft\"", FoodLogCsv.escape("Heinz, Kraft"));
        assertEquals("\"The \"\"best\"\" beans\"", FoodLogCsv.escape("The \"best\" beans"));
        assertEquals("", FoodLogCsv.escape(null));
        assertEquals("Plain", FoodLogCsv.escape("Plain"));
    }

    /**
     * Names starting with a formula character get an apostrophe so a spreadsheet
     * shows them as text instead of running them.
     */
    @Test
    public void escape_blocksFormulaInjection() {
        assertEquals("'=1+1", FoodLogCsv.escape("=1+1"));
        assertEquals("'+44 crisps", FoodLogCsv.escape("+44 crisps"));
        assertEquals("'@SUM(A1)", FoodLogCsv.escape("@SUM(A1)"));
        assertEquals("\"'=HYPERLINK(\"\"x\"\",\"\"y\"\")\"", FoodLogCsv.escape("=HYPERLINK(\"x\",\"y\")"));
    }

    /**
     * Every row has the same number of columns as the header, even with awkward names.
     */
    @Test
    public void build_rowsMatchHeaderColumns() {
        String csv = FoodLogCsv.build(Arrays.asList(entry("Oats", "Tesco"), entry("Beans, baked", "Heinz")), UTC);
        String[] lines = csv.split("\r\n");
        assertEquals(3, lines.length);
        assertTrue(lines[2].startsWith("2026-11-19,08:15,Breakfast,\"Beans, baked\",Heinz,"));
        // Removing the one quoted cell leaves 10 plain columns, like the header.
        assertEquals(FoodLogCsv.HEADER.split(",").length,
                lines[2].replace("\"Beans, baked\"", "x").split(",").length);
    }
}
