/**
 * Module: EEE521 - Final Year Project
 * Project: Nutrimpact
 * File: SettingsActivity.java
 * Description: Settings screen for daily calorie and macronutrient goals, the energy unit (kcal/kJ) and the default portion unit (g/oz). Settings are saved only on the device. Also exports the food log as a CSV file.
 */

package com.example.nutrimpact;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.time.LocalDate;

/**
 * Lets the user change their goals and units. Nothing is saved until Save is tapped.
 */
public class SettingsActivity extends AppCompatActivity {

    /** Calorie goal box. */
    private TextInputLayout layoutCalorieGoal;

    /** Protein goal box. */
    private TextInputLayout layoutProteinGoal;

    /** Carbohydrate goal box. */
    private TextInputLayout layoutCarbsGoal;

    /** Fat goal box. */
    private TextInputLayout layoutFatGoal;

    /** kcal / kJ choice. */
    private MaterialButtonToggleGroup toggleEnergyUnit;

    /** g / oz choice. */
    private MaterialButtonToggleGroup togglePortionUnit;

    /** Where settings are stored. */
    private UserSettings settings;

    /**
     * Opens Android's "save as" screen for a CSV file, then exports to the file
     * the user picks. The app never needs storage permission: the user chooses
     * the place and Android grants access to that one file.
     */
    private final ActivityResultLauncher<String> exportLauncher = registerForActivityResult(
            new ActivityResultContracts.CreateDocument("text/csv"), this::onExportFileChosen);

    /**
     * Builds an Intent that opens the settings screen.
     *
     * @param context the screen opening settings.
     * @return an Intent ready for startActivity().
     */
    public static Intent createIntent(Context context) {
        return new Intent(context, SettingsActivity.class);
    }

    /**
     * Called when the screen is created. Fills the form with the saved
     * settings on first open; after rotation Android restores what was typed.
     *
     * @param savedInstanceState previously saved state, or null on first open.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        settings = new UserSettings(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbarSettings);
        toolbar.setNavigationOnClickListener(view -> finish());

        layoutCalorieGoal = findViewById(R.id.layoutCalorieGoal);
        layoutProteinGoal = findViewById(R.id.layoutProteinGoal);
        layoutCarbsGoal = findViewById(R.id.layoutCarbsGoal);
        layoutFatGoal = findViewById(R.id.layoutFatGoal);
        toggleEnergyUnit = findViewById(R.id.toggleEnergyUnit);
        togglePortionUnit = findViewById(R.id.togglePortionUnit);

        if (savedInstanceState == null) {
            showGoals(settings.getGoals());
            toggleEnergyUnit.check(settings.getEnergyUnit() == EnergyUnit.KJ
                    ? R.id.buttonUnitKj : R.id.buttonUnitKcal);
            togglePortionUnit.check(settings.isOuncesDefault()
                    ? R.id.buttonPortionOunces : R.id.buttonPortionGrams);
        }

        findViewById(R.id.buttonResetGoals).setOnClickListener(view -> showGoals(NutritionGoals.defaults()));
        findViewById(R.id.buttonSaveSettings).setOnClickListener(view -> save());
        findViewById(R.id.buttonExportCsv).setOnClickListener(view -> exportLauncher.launch(
                getString(R.string.settings_export_file_name, LocalDate.now().toString())));
    }

    /**
     * Writes the export once the user has picked where to save it.
     *
     * @param uri the chosen file, or null if the user backed out.
     */
    private void onExportFileChosen(Uri uri) {
        if (uri == null) {
            return;
        }
        Context appContext = getApplicationContext();
        new FoodLogRepository(appContext).exportCsv(getContentResolver(), uri, (success, entryCount) -> {
            String message = success
                    ? appContext.getResources().getQuantityString(R.plurals.settings_export_done, entryCount, entryCount)
                    : appContext.getString(R.string.settings_export_failed);
            Toast.makeText(appContext, message, Toast.LENGTH_LONG).show();
        });
    }

    /**
     * Puts goals into the four boxes and clears any errors.
     *
     * @param goals the goals to show.
     */
    private void showGoals(NutritionGoals goals) {
        setNumber(layoutCalorieGoal, goals.getCalories());
        setNumber(layoutProteinGoal, goals.getProtein());
        setNumber(layoutCarbsGoal, goals.getCarbs());
        setNumber(layoutFatGoal, goals.getFat());
    }

    /**
     * Checks the form and, if everything is valid, saves it and closes the screen.
     * Invalid boxes show an error and nothing is saved.
     */
    private void save() {
        int calories = readNumber(layoutCalorieGoal);
        int protein = readNumber(layoutProteinGoal);
        int carbs = readNumber(layoutCarbsGoal);
        int fat = readNumber(layoutFatGoal);

        boolean valid = check(layoutCalorieGoal, NutritionGoals.isValidCalories(calories),
                R.string.settings_error_calories);
        valid &= check(layoutProteinGoal, NutritionGoals.isValidMacro(protein), R.string.settings_error_macro);
        valid &= check(layoutCarbsGoal, NutritionGoals.isValidMacro(carbs), R.string.settings_error_macro);
        valid &= check(layoutFatGoal, NutritionGoals.isValidMacro(fat), R.string.settings_error_macro);
        if (!valid) {
            return;
        }

        settings.setGoals(new NutritionGoals(calories, protein, carbs, fat));
        settings.setEnergyUnit(toggleEnergyUnit.getCheckedButtonId() == R.id.buttonUnitKj
                ? EnergyUnit.KJ : EnergyUnit.KCAL);
        settings.setOuncesDefault(togglePortionUnit.getCheckedButtonId() == R.id.buttonPortionOunces);

        Toast.makeText(this, R.string.settings_saved, Toast.LENGTH_SHORT).show();
        finish();
    }

    /**
     * Shows or clears an error on a box.
     *
     * @param layout      the box.
     * @param ok          whether its value is valid.
     * @param errorResId  the message to show if not.
     * @return the same value as {@code ok}.
     */
    private boolean check(TextInputLayout layout, boolean ok, int errorResId) {
        layout.setError(ok ? null : getString(errorResId));
        return ok;
    }

    /**
     * Writes a whole number into a box.
     *
     * @param layout the box.
     * @param value  the number to show.
     */
    private static void setNumber(TextInputLayout layout, int value) {
        layout.setError(null);
        TextInputEditText edit = (TextInputEditText) layout.getEditText();
        if (edit != null) {
            edit.setText(String.valueOf(value));
        }
    }

    /**
     * Reads a whole number from a box.
     *
     * @param layout the box.
     * @return the number, or -1 if the box is empty or not a number.
     */
    private static int readNumber(TextInputLayout layout) {
        if (layout.getEditText() == null) {
            return -1;
        }
        Editable text = layout.getEditText().getText();
        try {
            return Integer.parseInt(text == null ? "" : text.toString().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
