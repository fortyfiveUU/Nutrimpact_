/**
 * Module: EEE521 - Final Year Project
 * Project: Nutrimpact
 * File: BarcodeUtilsTest.java
 * Description: JUnit tests for BarcodeUtils, using real product barcodes and deliberately broken ones.
 */

package com.example.nutrimpact;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit tests for BarcodeUtils.
 */
public class BarcodeUtilsTest {

    /**
     * Real barcodes of each supported length are accepted.
     */
    @Test
    public void validBarcodes_areAccepted() {
        assertTrue(BarcodeUtils.isValidBarcode("3017620422003"));   // EAN-13: Nutella
        assertTrue(BarcodeUtils.isValidBarcode("5000159484695"));   // EAN-13: Twix
        assertTrue(BarcodeUtils.isValidBarcode("73513537"));        // EAN-8
        assertTrue(BarcodeUtils.isValidBarcode("036000291452"));    // UPC-A
        assertTrue(BarcodeUtils.isValidBarcode("15000159484692"));  // GTIN-14
        assertTrue(BarcodeUtils.isValidBarcode(" 3017620422003 ")); // surrounding spaces ignored
    }

    /**
     * A wrong last digit (a typo or camera misread) is rejected.
     */
    @Test
    public void wrongCheckDigit_isRejected() {
        assertFalse(BarcodeUtils.isValidBarcode("3017620422004"));
        assertFalse(BarcodeUtils.isValidBarcode("73513538"));
    }

    /**
     * Food names, wrong lengths, letters and empty input are not barcodes.
     */
    @Test
    public void nonBarcodes_areRejected() {
        assertFalse(BarcodeUtils.isValidBarcode("oats"));
        assertFalse(BarcodeUtils.isValidBarcode("1234567"));          // too short
        assertFalse(BarcodeUtils.isValidBarcode("301762042200"));     // 12 digits, wrong check digit
        assertFalse(BarcodeUtils.isValidBarcode("123456789012345"));  // too long
        assertFalse(BarcodeUtils.isValidBarcode("30176204220O3"));    // letter O, not zero
        assertFalse(BarcodeUtils.isValidBarcode(""));
        assertFalse(BarcodeUtils.isValidBarcode(null));
    }

    /**
     * The check digit calculation matches the GS1 example.
     */
    @Test
    public void calculateCheckDigit_matchesGs1() {
        assertEquals(3, BarcodeUtils.calculateCheckDigit("301762042200"));
        assertEquals(2, BarcodeUtils.calculateCheckDigit("03600029145"));
    }
}
