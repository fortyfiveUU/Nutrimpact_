/**
 * Module: EEE521 - Final Year Project
 * Project: Nutrimpact
 * File: MainActivity.java
 * Description: Launcher activity for Nutrimpact. Inflates the main layout and acts as the app's entry point.
 */

package com.example.nutrimpact;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    /**
     * Called when the activity is first created. Sets the main content view.
     *
     * @param savedInstanceState previously saved state, or null on first launch.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
