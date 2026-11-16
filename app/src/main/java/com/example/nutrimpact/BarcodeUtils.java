/**
 * Module: EEE521 - Final Year Project
 * Project: Nutrimpact
 * File: BarcodeUtils.java
 * Description: Checks whether text is a valid product barcode (EAN-8, UPC-A, EAN-13 or GTIN-14), including its check digit.
 */

package com.example.nutrimpact;

/**
 * Barcode validation. Used to decide whether text in the search box is a
 * barcode to look up, and to ignore misreads from the camera.
 */
public final class BarcodeUtils {

    /**
     * Private constructor so the class can't be instantiated.
     */
    private BarcodeUtils() {
    }

    /**
     * Checks that text is a product barcode: only digits, 8, 12, 13 or 14 long,
     * with a correct GS1 check digit (the last digit).
     *
     * @param text the text to check; may be null.
     * @return true if it is a valid barcode.
     */
    public static boolean isValidBarcode(String text) {
        if (text == null) {
            return false;
        }
        String code = text.trim();
        int length = code.length();
        if (length != 8 && length != 12 && length != 13 && length != 14) {
            return false;
        }
        for (int i = 0; i < length; i++) {
            char c = code.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return calculateCheckDigit(code.substring(0, length - 1)) == code.charAt(length - 1) - '0';
    }

    /**
     * Calculates the GS1 check digit for the digits before it. Working from
     * the right, digits are weighted 3, 1, 3, 1, ...; the check digit makes
     * the weighted sum a multiple of 10.
     *
     * @param digitsWithoutCheck the barcode without its last digit.
     * @return the check digit, 0 to 9.
     */
    static int calculateCheckDigit(String digitsWithoutCheck) {
        int sum = 0;
        boolean weightThree = true;
        for (int i = digitsWithoutCheck.length() - 1; i >= 0; i--) {
            int digit = digitsWithoutCheck.charAt(i) - '0';
            sum += weightThree ? digit * 3 : digit;
            weightThree = !weightThree;
        }
        return (10 - (sum % 10)) % 10;
    }
}
