/**
 * Module: EEE521 - Final Year Project
 * Project: Nutrimpact
 * File: FoodLogExportTest.java
 * Description: Instrumented test for the CSV export: writes the app's real database to a file and checks the file's contents and the reported count.
 */

package com.example.nutrimpact;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.net.Uri;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Tests FoodLogRepository.exportCsv(). Android's "save as" screen can't be
 * driven by Espresso, so the export writes to a file in the app's cache
 * folder instead; the same code path is used once a file has been picked.
 */
@RunWith(AndroidJUnit4.class)
public class FoodLogExportTest {

    /** Where the test export is written. */
    private File file;

    /**
     * Empties the log and picks a fresh file.
     */
    @Before
    public void setUp() {
        TestHelpers.resetAppData();
        file = new File(TestHelpers.context().getCacheDir(), "export-test.csv");
        file.delete();
    }

    /**
     * Removes the test file and the foods added.
     */
    @After
    public void tearDown() {
        file.delete();
        TestHelpers.resetAppData();
    }

    /**
     * Runs an export and waits for it to finish.
     *
     * @param result filled with {success (1 or 0), entry count}.
     * @throws InterruptedException if interrupted while waiting.
     */
    private void export(int[] result) throws InterruptedException {
        CountDownLatch done = new CountDownLatch(1);
        new FoodLogRepository(TestHelpers.context()).exportCsv(
                TestHelpers.context().getContentResolver(), Uri.fromFile(file), (success, entryCount) -> {
                    result[0] = success ? 1 : 0;
                    result[1] = entryCount;
                    done.countDown();
                });
        assertTrue("Export never finished", done.await(5, TimeUnit.SECONDS));
    }

    /**
     * Every logged food, from any day, ends up in the file oldest first with portion-scaled values.
     *
     * @throws Exception if the file can't be read.
     */
    @Test
    public void export_writesEveryEntryOldestFirst() throws Exception {
        TestHelpers.addFood("Porridge, golden syrup", MealType.BREAKFAST, TestHelpers.today(), 40, 379);
        TestHelpers.addFood("Older food", MealType.DINNER, TestHelpers.today() - 2, 200, 50);

        int[] result = new int[2];
        export(result);

        assertEquals("Export failed", 1, result[0]);
        assertEquals(2, result[1]);
        String csv = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        String[] lines = csv.split("\r\n");
        assertEquals(3, lines.length);
        assertEquals(FoodLogCsv.UTF8_BOM + FoodLogCsv.HEADER, lines[0]);
        assertTrue(lines[1], lines[1].contains(",Dinner,Older food,Test brand,200.0,100.0,"));
        assertTrue(lines[2], lines[2].contains(",Breakfast,\"Porridge, golden syrup\",Test brand,40.0,151.6,"));
    }

    /**
     * An empty log still exports successfully, with just the header.
     *
     * @throws Exception if the file can't be read.
     */
    @Test
    public void export_emptyLog_writesHeaderOnly() throws Exception {
        int[] result = new int[2];
        export(result);

        assertEquals(1, result[0]);
        assertEquals(0, result[1]);
        String csv = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        assertEquals(FoodLogCsv.UTF8_BOM + FoodLogCsv.HEADER + "\r\n", csv);
    }
}
