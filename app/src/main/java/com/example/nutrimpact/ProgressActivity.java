/**
 * Module: EEE521 - Final Year Project
 * Project: Nutrimpact
 * File: ProgressActivity.java
 * Description: Progress screen showing a week of energy intake as a bar chart against the daily goal, with weekly averages for energy and macronutrients.
 */

package com.example.nutrimpact;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.appbar.MaterialToolbar;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Weekly progress. Arrows move between weeks; the latest week ends today.
 */
public class ProgressActivity extends AppCompatActivity {

    /** Format for the dates in the week range, e.g. "10 Nov". */
    private static final String RANGE_PATTERN = "d MMM";

    /** Format for the labels under each bar, e.g. "Mon". */
    private static final String DAY_PATTERN = "EEE";

    /** Shows the week's dates. */
    private TextView textWeekRange;

    /** Moves to the next week; disabled on the latest week. */
    private Button buttonNextWeek;

    /** The bar chart. */
    private WeeklyChartView chartWeek;

    /** Average energy per logged day. */
    private TextView textAverage;

    /** How many days were logged. */
    private TextView textDaysLogged;

    /** How many logged days were within the goal. */
    private TextView textWithinGoal;

    /** Average protein vs goal. */
    private TextView textAverageProtein;

    /** Average carbohydrates vs goal. */
    private TextView textAverageCarbs;

    /** Average fat vs goal. */
    private TextView textAverageFat;

    /** Summary card, hidden when nothing is logged. */
    private View cardSummary;

    /** Shown when nothing is logged in the week. */
    private TextView textProgressEmpty;

    /** Holds the week and its totals. */
    private ProgressViewModel viewModel;

    /** Goals and units. */
    private UserSettings settings;

    /**
     * Builds an Intent that opens the Progress screen.
     *
     * @param context the screen opening this one.
     * @return an Intent ready for startActivity().
     */
    public static Intent createIntent(Context context) {
        return new Intent(context, ProgressActivity.class);
    }

    /**
     * Called when the screen is created, including after rotation.
     *
     * @param savedInstanceState previously saved state, or null on first open.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        settings = new UserSettings(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbarProgress);
        toolbar.setNavigationOnClickListener(view -> finish());

        textWeekRange = findViewById(R.id.textWeekRange);
        buttonNextWeek = findViewById(R.id.buttonNextWeek);
        chartWeek = findViewById(R.id.chartWeek);
        textAverage = findViewById(R.id.textAverage);
        textDaysLogged = findViewById(R.id.textDaysLogged);
        textWithinGoal = findViewById(R.id.textWithinGoal);
        textAverageProtein = findViewById(R.id.textAverageProtein);
        textAverageCarbs = findViewById(R.id.textAverageCarbs);
        textAverageFat = findViewById(R.id.textAverageFat);
        cardSummary = findViewById(R.id.cardSummary);
        textProgressEmpty = findViewById(R.id.textProgressEmpty);

        viewModel = new ViewModelProvider(this).get(ProgressViewModel.class);
        viewModel.getWeekTotals().observe(this, this::render);

        findViewById(R.id.buttonPreviousWeek).setOnClickListener(view -> viewModel.previousWeek());
        buttonNextWeek.setOnClickListener(view -> viewModel.nextWeek());
    }

    /**
     * Draws the chart and summary for the week.
     *
     * @param totals the week's daily totals from the database.
     */
    private void render(List<DayTotal> totals) {
        Long end = viewModel.getEndDay().getValue();
        long endDay = (end != null) ? end : LocalDate.now().toEpochDay();
        NutritionGoals goals = settings.getGoals();
        EnergyUnit unit = settings.getEnergyUnit();
        WeekSummary week = WeekSummary.of(totals, endDay, goals.getCalories());

        LocalDate first = LocalDate.ofEpochDay(endDay - (WeekSummary.DAYS - 1));
        LocalDate last = LocalDate.ofEpochDay(endDay);
        DateTimeFormatter rangeFormat = DateTimeFormatter.ofPattern(RANGE_PATTERN, Locale.getDefault());
        textWeekRange.setText(getString(R.string.progress_week_range,
                first.format(rangeFormat), last.format(rangeFormat)));
        buttonNextWeek.setEnabled(!viewModel.isLatestWeek());

        List<Double> values = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        DateTimeFormatter dayFormat = DateTimeFormatter.ofPattern(DAY_PATTERN, Locale.getDefault());
        for (DayTotal day : week.getDays()) {
            values.add(unit.fromKcal(day.getCalories()));
            labels.add(LocalDate.ofEpochDay(day.getLogDate()).format(dayFormat));
        }
        String goalText = getString(R.string.progress_goal_line, unit.format(this, goals.getCalories()));
        chartWeek.setData(values, labels, unit.fromKcal(goals.getCalories()), goalText);

        boolean empty = week.getDaysLogged() == 0;
        cardSummary.setVisibility(empty ? View.GONE : View.VISIBLE);
        textProgressEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);

        String average = getString(R.string.progress_average, unit.format(this, week.getAverageCalories()));
        textAverage.setText(average);
        textDaysLogged.setText(getString(R.string.progress_days_logged, week.getDaysLogged()));
        textWithinGoal.setText(getResources().getQuantityString(R.plurals.progress_within_goal,
                week.getDaysLogged(), week.getDaysWithinGoal(), week.getDaysLogged()));
        textAverageProtein.setText(getString(R.string.progress_macro_line,
                getString(R.string.macro_protein), week.getAverageProtein(), goals.getProtein()));
        textAverageCarbs.setText(getString(R.string.progress_macro_line,
                getString(R.string.macro_carbs), week.getAverageCarbs(), goals.getCarbs()));
        textAverageFat.setText(getString(R.string.progress_macro_line,
                getString(R.string.macro_fat), week.getAverageFat(), goals.getFat()));

        chartWeek.setContentDescription(getString(R.string.progress_chart_description,
                empty ? getString(R.string.progress_empty) : average));
    }
}
