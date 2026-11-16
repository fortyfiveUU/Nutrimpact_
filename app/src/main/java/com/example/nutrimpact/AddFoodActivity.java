/**
 * Module: EEE521 - Final Year Project
 * Project: Nutrimpact
 * File: AddFoodActivity.java
 * Description: Screen for adding a searched product to the food log. The user picks a portion size (grams or ounces) and a meal, sees the scaled nutrition live, then saves it to the local database.
 */

package com.example.nutrimpact;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Locale;

/**
 * Lets the user choose how much of a product they ate and for which meal,
 * then adds it to the food log.
 */
public class AddFoodActivity extends AppCompatActivity {

    /** Intent extra: product name. */
    private static final String EXTRA_PRODUCT_NAME = "com.example.nutrimpact.PRODUCT_NAME";

    /** Intent extra: brand names. */
    private static final String EXTRA_BRANDS = "com.example.nutrimpact.BRANDS";

    /** Intent extra: calories per 100g. */
    private static final String EXTRA_CALORIES = "com.example.nutrimpact.CALORIES";

    /** Intent extra: protein per 100g. */
    private static final String EXTRA_PROTEIN = "com.example.nutrimpact.PROTEIN";

    /** Intent extra: carbohydrates per 100g. */
    private static final String EXTRA_CARBS = "com.example.nutrimpact.CARBS";

    /** Intent extra: fat per 100g. */
    private static final String EXTRA_FAT = "com.example.nutrimpact.FAT";

    /** Saved-state key for whether the amount is in ounces. */
    private static final String KEY_USING_OUNCES = "using_ounces";

    /** Portion shown when the screen first opens, in grams. */
    private static final double DEFAULT_PORTION_GRAMS = 100.0;

    /** Product name being logged. */
    private String productName;

    /** Brand names being logged. */
    private String brands;

    /** Calories per 100g. */
    private double caloriesPer100g;

    /** Protein per 100g. */
    private double proteinPer100g;

    /** Carbohydrates per 100g. */
    private double carbsPer100g;

    /** Fat per 100g. */
    private double fatPer100g;

    /** True when the amount box is in ounces, false for grams. */
    private boolean usingOunces;

    /** Outlined box around the amount field; shows the unit suffix and errors. */
    private TextInputLayout layoutAmount;

    /** Amount the user ate, in the selected unit. */
    private TextInputEditText editAmount;

    /** Meal choice chips. */
    private ChipGroup chipGroupMeal;

    /** Calories for the chosen portion. */
    private TextView textPortionCalories;

    /** Protein, carbs and fat for the chosen portion. */
    private TextView textPortionMacros;

    /** Saves the entry. */
    private Button buttonAddToLog;

    /** Saves entries to the local database. */
    private FoodLogRepository repository;

    /**
     * Builds an Intent that opens this screen for a product. Only products
     * with nutrition data should be passed in.
     *
     * @param context the screen opening this one.
     * @param product the product to log.
     * @return an Intent ready for startActivity().
     */
    public static Intent createIntent(Context context, FoodProduct product) {
        Nutriments nutriments = product.getNutriments();
        Intent intent = new Intent(context, AddFoodActivity.class);
        intent.putExtra(EXTRA_PRODUCT_NAME, product.getProductName());
        intent.putExtra(EXTRA_BRANDS, product.getBrands());
        intent.putExtra(EXTRA_CALORIES, nutriments.getCaloriesPer100g());
        intent.putExtra(EXTRA_PROTEIN, nutriments.getProteinPer100g());
        intent.putExtra(EXTRA_CARBS, nutriments.getCarbsPer100g());
        intent.putExtra(EXTRA_FAT, nutriments.getFatPer100g());
        return intent;
    }

    /**
     * Called when the screen is created, including after rotation. Reads the
     * product from the Intent and sets up the form.
     *
     * @param savedInstanceState previously saved state, or null on first open.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_food);

        Intent intent = getIntent();
        productName = intent.getStringExtra(EXTRA_PRODUCT_NAME);
        brands = intent.getStringExtra(EXTRA_BRANDS);
        caloriesPer100g = intent.getDoubleExtra(EXTRA_CALORIES, 0.0);
        proteinPer100g = intent.getDoubleExtra(EXTRA_PROTEIN, 0.0);
        carbsPer100g = intent.getDoubleExtra(EXTRA_CARBS, 0.0);
        fatPer100g = intent.getDoubleExtra(EXTRA_FAT, 0.0);

        repository = new FoodLogRepository(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbarAddFood);
        toolbar.setNavigationOnClickListener(view -> finish());

        layoutAmount = findViewById(R.id.layoutAmount);
        editAmount = findViewById(R.id.editAmount);
        chipGroupMeal = findViewById(R.id.chipGroupMeal);
        textPortionCalories = findViewById(R.id.textPortionCalories);
        textPortionMacros = findViewById(R.id.textPortionMacros);
        buttonAddToLog = findViewById(R.id.buttonAddToLog);
        MaterialButtonToggleGroup toggleUnit = findViewById(R.id.toggleUnit);

        ((TextView) findViewById(R.id.textAddProductName)).setText(productName);
        ((TextView) findViewById(R.id.textAddBrand)).setText(brands);
        ((TextView) findViewById(R.id.textAddPer100g)).setText(getString(R.string.add_food_per_100g,
                caloriesPer100g, proteinPer100g, carbsPer100g, fatPer100g));

        if (savedInstanceState == null) {
            // First open: 100g, and the meal that fits the current time.
            usingOunces = false;
            editAmount.setText(formatAmount(DEFAULT_PORTION_GRAMS));
            chipGroupMeal.check(chipIdFor(MealType.suggestFor(LocalTime.now())));
        } else {
            // After rotation, Android restores the text, chips and toggle itself.
            usingOunces = savedInstanceState.getBoolean(KEY_USING_OUNCES, false);
        }
        updateUnitSuffix();

        editAmount.addTextChangedListener(new TextWatcher() {

            /**
             * Not used.
             *
             * @param s     the text before the change.
             * @param start where the change starts.
             * @param count how many characters are being replaced.
             * @param after how many characters are replacing them.
             */
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            /**
             * Not used.
             *
             * @param s      the new text.
             * @param start  where the change starts.
             * @param before how many characters were replaced.
             * @param count  how many characters were added.
             */
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            /**
             * Recalculates the preview whenever the amount changes.
             *
             * @param s the text after the change.
             */
            @Override
            public void afterTextChanged(Editable s) {
                updatePreview();
            }
        });

        toggleUnit.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                onUnitChanged(checkedId == R.id.buttonOunces);
            }
        });

        buttonAddToLog.setOnClickListener(view -> saveEntry());
        updatePreview();
    }

    /**
     * Remembers the selected unit so it survives rotation.
     *
     * @param outState the bundle to save into.
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_USING_OUNCES, usingOunces);
    }

    /**
     * Refreshes the preview after Android restores the form on rotation.
     *
     * @param savedInstanceState the restored state.
     */
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        updatePreview();
    }

    /**
     * Switches between grams and ounces, converting the amount already typed
     * so the portion stays the same.
     *
     * @param toOunces true to switch to ounces, false for grams.
     */
    private void onUnitChanged(boolean toOunces) {
        if (toOunces == usingOunces) {
            return; // Same unit, e.g. when Android restores the toggle after rotation.
        }

        double grams = getPortionGrams();
        usingOunces = toOunces;
        updateUnitSuffix();

        if (!Double.isNaN(grams)) {
            double converted = usingOunces ? PortionCalculator.gramsToOunces(grams) : grams;
            editAmount.setText(formatAmount(converted));
            Editable text = editAmount.getText();
            editAmount.setSelection(text != null ? text.length() : 0);
        }
        updatePreview();
    }

    /**
     * Shows "g" or "oz" at the end of the amount box.
     */
    private void updateUnitSuffix() {
        layoutAmount.setSuffixText(getString(usingOunces
                ? R.string.add_food_unit_ounces : R.string.add_food_unit_grams));
    }

    /**
     * Reads the amount box and converts it to grams.
     *
     * @return the portion in grams, or NaN if the box is empty or not a number.
     */
    private double getPortionGrams() {
        Editable text = editAmount.getText();
        if (text == null) {
            return Double.NaN;
        }
        String raw = text.toString().trim().replace(',', '.');
        if (raw.isEmpty()) {
            return Double.NaN;
        }
        try {
            double amount = Double.parseDouble(raw);
            return usingOunces ? PortionCalculator.ouncesToGrams(amount) : amount;
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }

    /**
     * Recalculates the portion preview. Shows an error and disables the Add
     * button if the amount isn't valid.
     */
    private void updatePreview() {
        double grams = getPortionGrams();
        boolean valid = !Double.isNaN(grams) && PortionCalculator.isValidPortion(grams);

        if (valid) {
            layoutAmount.setError(null);
            textPortionCalories.setText(getString(R.string.add_food_portion_calories,
                    PortionCalculator.scale(caloriesPer100g, grams)));
            textPortionMacros.setText(getString(R.string.item_macros,
                    PortionCalculator.scale(proteinPer100g, grams),
                    PortionCalculator.scale(carbsPer100g, grams),
                    PortionCalculator.scale(fatPer100g, grams)));
        } else {
            // Don't nag while the box is empty; only flag amounts that are wrong.
            layoutAmount.setError(Double.isNaN(grams) ? null : getString(R.string.add_food_error_amount));
            textPortionCalories.setText("–");
            textPortionMacros.setText(R.string.add_food_portion_empty);
        }
        buttonAddToLog.setEnabled(valid);
    }

    /**
     * Saves the food to the log in the background, then confirms and closes
     * the screen.
     */
    private void saveEntry() {
        double grams = getPortionGrams();
        if (Double.isNaN(grams) || !PortionCalculator.isValidPortion(grams)) {
            updatePreview();
            return;
        }

        MealType meal = selectedMeal();

        FoodLogEntry entry = new FoodLogEntry();
        entry.setProductName(productName);
        entry.setBrands(brands);
        entry.setMealType(meal.name());
        entry.setLogDate(LocalDate.now().toEpochDay());
        entry.setLoggedAt(System.currentTimeMillis());
        entry.setPortionGrams(grams);
        entry.setCaloriesPer100g(caloriesPer100g);
        entry.setProteinPer100g(proteinPer100g);
        entry.setCarbsPer100g(carbsPer100g);
        entry.setFatPer100g(fatPer100g);

        // Stop double taps adding the same food twice.
        buttonAddToLog.setEnabled(false);

        repository.addEntry(entry, () -> {
            Toast.makeText(this, getString(R.string.add_food_saved, getString(meal.getLabelResId())),
                    Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    /**
     * Gets the meal the user picked.
     *
     * @return the selected meal, or SNACKS if somehow none is selected.
     */
    private MealType selectedMeal() {
        int checkedId = chipGroupMeal.getCheckedChipId();
        if (checkedId == R.id.chipBreakfast) {
            return MealType.BREAKFAST;
        } else if (checkedId == R.id.chipLunch) {
            return MealType.LUNCH;
        } else if (checkedId == R.id.chipDinner) {
            return MealType.DINNER;
        }
        return MealType.SNACKS;
    }

    /**
     * Gets the chip that represents a meal.
     *
     * @param meal the meal.
     * @return the chip's view ID.
     */
    private static int chipIdFor(MealType meal) {
        switch (meal) {
            case BREAKFAST:
                return R.id.chipBreakfast;
            case LUNCH:
                return R.id.chipLunch;
            case DINNER:
                return R.id.chipDinner;
            case SNACKS:
            default:
                return R.id.chipSnacks;
        }
    }

    /**
     * Formats an amount for the amount box: whole numbers without decimals,
     * otherwise one decimal place, always with a "." so it parses back reliably.
     *
     * @param amount the amount to format.
     * @return the formatted amount, e.g. "100" or "3.5".
     */
    private static String formatAmount(double amount) {
        double rounded = Math.round(amount * 10.0) / 10.0;
        if (rounded == Math.rint(rounded)) {
            return String.format(Locale.ROOT, "%.0f", rounded);
        }
        return String.format(Locale.ROOT, "%.1f", rounded);
    }
}
