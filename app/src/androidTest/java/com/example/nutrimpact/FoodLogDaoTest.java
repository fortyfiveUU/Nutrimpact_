/**
 * Module: EEE521 - Final Year Project
 * Project: Nutrimpact
 * File: FoodLogDaoTest.java
 * Description: Instrumented tests for the Room database queries in FoodLogDao, run against a temporary in-memory database.
 */

package com.example.nutrimpact;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

/**
 * Checks the real SQL in FoodLogDao: saving, reading by day, deleting,
 * restoring (Undo), and the daily totals used by the Progress screen.
 */
@RunWith(AndroidJUnit4.class)
public class FoodLogDaoTest {

    /** Allowed rounding difference when comparing doubles. */
    private static final double DELTA = 0.0001;

    /** An arbitrary day used as "today" in these tests. */
    private static final long DAY = 20000;

    /** Makes LiveData deliver values immediately. */
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    /** Fresh in-memory database for each test. */
    private NutrimpactDatabase database;

    /** The DAO under test. */
    private FoodLogDao dao;

    /**
     * Creates an empty in-memory database before each test.
     */
    @Before
    public void createDatabase() {
        Context context = ApplicationProvider.getApplicationContext();
        database = Room.inMemoryDatabaseBuilder(context, NutrimpactDatabase.class)
                .allowMainThreadQueries()
                .build();
        dao = database.foodLogDao();
    }

    /**
     * Closes the database after each test.
     */
    @After
    public void closeDatabase() {
        database.close();
    }

    /**
     * Builds an entry for the tests.
     *
     * @param name     product name.
     * @param meal     meal.
     * @param day      epoch day.
     * @param loggedAt time added, for ordering.
     * @param grams    portion in grams.
     * @param kcal     calories per 100g.
     * @param protein  protein per 100g.
     * @return a new, unsaved entry.
     */
    static FoodLogEntry entry(String name, MealType meal, long day, long loggedAt,
                              double grams, double kcal, double protein) {
        FoodLogEntry entry = new FoodLogEntry();
        entry.setProductName(name);
        entry.setBrands("Test brand");
        entry.setMealType(meal.name());
        entry.setLogDate(day);
        entry.setLoggedAt(loggedAt);
        entry.setPortionGrams(grams);
        entry.setCaloriesPer100g(kcal);
        entry.setProteinPer100g(protein);
        entry.setCarbsPer100g(10);
        entry.setFatPer100g(5);
        return entry;
    }

    /**
     * A saved entry can be read back for its day with every field intact.
     */
    @Test
    public void insert_thenReadBack() throws Exception {
        long id = dao.insert(entry("Oats", MealType.BREAKFAST, DAY, 1000, 40, 379, 8.9));

        List<FoodLogEntry> entries = LiveDataTestUtil.getOrAwaitValue(dao.getEntriesForDay(DAY));
        assertEquals(1, entries.size());
        FoodLogEntry saved = entries.get(0);
        assertEquals(id, saved.getId());
        assertEquals("Oats", saved.getProductName());
        assertEquals("BREAKFAST", saved.getMealType());
        assertEquals(40, saved.getPortionGrams(), DELTA);
        assertEquals(151.6, saved.getPortionCalories(), DELTA);
    }

    /**
     * Only the requested day is returned, oldest first.
     */
    @Test
    public void getEntriesForDay_filtersByDayAndOrdersByTime() throws Exception {
        dao.insert(entry("Second", MealType.LUNCH, DAY, 2000, 100, 100, 1));
        dao.insert(entry("Yesterday", MealType.LUNCH, DAY - 1, 1500, 100, 100, 1));
        dao.insert(entry("First", MealType.BREAKFAST, DAY, 1000, 100, 100, 1));

        List<FoodLogEntry> entries = LiveDataTestUtil.getOrAwaitValue(dao.getEntriesForDay(DAY));
        assertEquals(2, entries.size());
        assertEquals("First", entries.get(0).getProductName());
        assertEquals("Second", entries.get(1).getProductName());
    }

    /**
     * Deleting removes the entry; inserting it again with the same ID (as Undo does) brings it back.
     */
    @Test
    public void delete_thenRestore() throws Exception {
        FoodLogEntry entry = entry("Oats", MealType.BREAKFAST, DAY, 1000, 40, 379, 8.9);
        entry.setId(dao.insert(entry));

        dao.delete(entry);
        assertTrue(LiveDataTestUtil.getOrAwaitValue(dao.getEntriesForDay(DAY)).isEmpty());

        dao.insert(entry);
        List<FoodLogEntry> restored = LiveDataTestUtil.getOrAwaitValue(dao.getEntriesForDay(DAY));
        assertEquals(1, restored.size());
        assertEquals(entry.getId(), restored.get(0).getId());
    }

    /**
     * Daily totals scale each portion in SQL, add up per day, and skip days with nothing logged.
     */
    @Test
    public void getDayTotals_sumsScaledPortionsPerDay() throws Exception {
        dao.insert(entry("Oats", MealType.BREAKFAST, DAY, 1000, 40, 379, 8.9));    // 151.6 kcal, 3.56 g protein
        dao.insert(entry("Bran", MealType.SNACKS, DAY, 2000, 30, 150, 7));         // 45 kcal, 2.1 g protein
        dao.insert(entry("Older", MealType.LUNCH, DAY - 3, 1000, 200, 50, 1));     // 100 kcal
        dao.insert(entry("Too old", MealType.LUNCH, DAY - 10, 1000, 100, 999, 1)); // outside the range

        List<DayTotal> totals = LiveDataTestUtil.getOrAwaitValue(dao.getDayTotals(DAY - 6, DAY));
        assertEquals(2, totals.size());
        assertEquals(DAY - 3, totals.get(0).getLogDate());
        assertEquals(100, totals.get(0).getCalories(), DELTA);
        assertEquals(DAY, totals.get(1).getLogDate());
        assertEquals(196.6, totals.get(1).getCalories(), DELTA);
        assertEquals(5.66, totals.get(1).getProtein(), DELTA);
    }

    /**
     * SQL totals match the Java PortionCalculator and DailyTotals for the same entries.
     */
    @Test
    public void getDayTotals_matchesJavaCalculation() throws Exception {
        dao.insert(entry("A", MealType.BREAKFAST, DAY, 1, 12.5, 379, 8.9));
        dao.insert(entry("B", MealType.DINNER, DAY, 2, 333, 58, 0.6));

        List<FoodLogEntry> entries = LiveDataTestUtil.getOrAwaitValue(dao.getEntriesForDay(DAY));
        DailyTotals javaTotals = DailyTotals.of(entries);
        DayTotal sqlTotal = LiveDataTestUtil.getOrAwaitValue(dao.getDayTotals(DAY, DAY)).get(0);

        assertEquals(javaTotals.getCalories(), sqlTotal.getCalories(), DELTA);
        assertEquals(javaTotals.getProtein(), sqlTotal.getProtein(), DELTA);
        assertEquals(javaTotals.getFat(), sqlTotal.getFat(), DELTA);
    }
}
