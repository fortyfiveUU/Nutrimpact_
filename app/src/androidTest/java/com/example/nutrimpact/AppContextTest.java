/**
 * Module: EEE521 - Final Year Project
 * Project: Nutrimpact
 * File: AppContextTest.java
 * Description: Instrumented sanity test confirming the tests are running against the Nutrimpact app package.
 */

package com.example.nutrimpact;

import static org.junit.Assert.assertEquals;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Replaces Android Studio's Kotlin template test with a Java version.
 */
@RunWith(AndroidJUnit4.class)
public class AppContextTest {

    /**
     * The app under test has the expected package name.
     */
    @Test
    public void appContext_hasNutrimpactPackage() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.example.nutrimpact", appContext.getPackageName());
    }
}
