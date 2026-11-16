/**
 * Module: EEE521 - Final Year Project
 * Project: Nutrimpact
 * File: WeeklyChartView.java
 * Description: Custom view that draws a seven-day bar chart of energy eaten, with a dashed line for the daily goal. Bars over the goal are drawn in the theme's error colour.
 */

package com.example.nutrimpact;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.color.MaterialColors;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple bar chart drawn directly on a Canvas, so no chart library is needed.
 */
public class WeeklyChartView extends View {

    /** Space above the tallest bar or goal line, as a fraction of the chart's maximum. */
    private static final float HEADROOM = 1.15f;

    /** Paint for bars within the goal. */
    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    /** Paint for bars over the goal. */
    private final Paint overPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    /** Paint for the dashed goal line. */
    private final Paint goalPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    /** Paint for day labels and values. */
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    /** Paint for the solid background behind the goal label. */
    private final Paint labelBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    /** Reused rectangle for drawing bars. */
    private final RectF barRect = new RectF();

    /** Energy for each day, in the display unit. */
    private final List<Double> values = new ArrayList<>();

    /** Label under each bar, e.g. "Mon". */
    private final List<String> labels = new ArrayList<>();

    /** Daily goal in the display unit. */
    private double goal;

    /** Label drawn on the goal line, e.g. "Goal 2,000 kcal". */
    private String goalLabel = "";

    /**
     * Creates the view from code.
     *
     * @param context the context.
     */
    public WeeklyChartView(@NonNull Context context) {
        this(context, null);
    }

    /**
     * Creates the view from a layout file.
     *
     * @param context the context.
     * @param attrs   layout attributes.
     */
    public WeeklyChartView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        barPaint.setColor(MaterialColors.getColor(this, com.google.android.material.R.attr.colorPrimary));
        overPaint.setColor(MaterialColors.getColor(this, com.google.android.material.R.attr.colorError));
        goalPaint.setColor(MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurfaceVariant));
        goalPaint.setStyle(Paint.Style.STROKE);
        goalPaint.setStrokeWidth(dp(1.5f));
        goalPaint.setPathEffect(new DashPathEffect(new float[]{dp(6), dp(4)}, 0));
        textPaint.setColor(MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurface));
        textPaint.setTextSize(sp(12));
        labelBackgroundPaint.setColor(MaterialColors.getColor(this, com.google.android.material.R.attr.colorSurface));
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    /**
     * Sets the data to draw and redraws the chart.
     *
     * @param dayValues  energy for each day in the display unit (seven values, oldest first).
     * @param dayLabels  label for each day, e.g. "Mon".
     * @param dailyGoal  the daily goal in the display unit.
     * @param goalText   text for the goal line, e.g. "Goal 2,000 kcal".
     */
    public void setData(List<Double> dayValues, List<String> dayLabels, double dailyGoal, String goalText) {
        values.clear();
        values.addAll(dayValues);
        labels.clear();
        labels.addAll(dayLabels);
        goal = dailyGoal;
        goalLabel = goalText;
        invalidate();
    }

    /**
     * Draws the bars, goal line and labels.
     *
     * @param canvas the canvas to draw on.
     */
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        if (values.isEmpty()) {
            return;
        }

        float left = getPaddingLeft();
        float right = getWidth() - getPaddingRight();
        float top = getPaddingTop() + sp(16);                 // room for the goal label
        float labelHeight = sp(20);
        float bottom = getHeight() - getPaddingBottom() - labelHeight;
        float chartHeight = bottom - top;

        double max = goal;
        for (double value : values) {
            max = Math.max(max, value);
        }
        max = Math.max(max, 1) * HEADROOM;

        int count = values.size();
        float slot = (right - left) / count;
        float barWidth = slot * 0.55f;
        float radius = dp(4);

        for (int i = 0; i < count; i++) {
            double value = values.get(i);
            float centreX = left + slot * i + slot / 2f;
            if (value > 0) {
                float barTop = bottom - (float) (value / max) * chartHeight;
                barRect.set(centreX - barWidth / 2f, barTop, centreX + barWidth / 2f, bottom);
                canvas.drawRoundRect(barRect, radius, radius, value > goal ? overPaint : barPaint);
            }
            if (i < labels.size()) {
                canvas.drawText(labels.get(i), centreX, bottom + sp(15), textPaint);
            }
        }

        if (goal > 0) {
            float goalY = bottom - (float) (goal / max) * chartHeight;
            canvas.drawLine(left, goalY, right, goalY, goalPaint);

            // Label on the left, on a solid background so bars behind it can't make it unreadable.
            textPaint.setTextAlign(Paint.Align.LEFT);
            float padding = dp(4);
            float textWidth = textPaint.measureText(goalLabel);
            float baseline = goalY - padding * 2;
            barRect.set(left, baseline + textPaint.ascent() - padding,
                    left + textWidth + padding * 2, baseline + textPaint.descent() + padding);
            canvas.drawRoundRect(barRect, radius, radius, labelBackgroundPaint);
            canvas.drawText(goalLabel, left + padding, baseline, textPaint);
            textPaint.setTextAlign(Paint.Align.CENTER);
        }
    }

    /**
     * Converts density-independent pixels to pixels.
     *
     * @param value size in dp.
     * @return size in pixels.
     */
    private float dp(float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }

    /**
     * Converts scale-independent pixels to pixels, respecting the user's font size.
     *
     * @param value size in sp.
     * @return size in pixels.
     */
    private float sp(float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value, getResources().getDisplayMetrics());
    }
}
