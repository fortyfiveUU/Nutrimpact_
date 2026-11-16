/**
 * Module: EEE521 - Final Year Project
 * Project: Nutrimpact
 * File: LiveDataTestUtil.java
 * Description: Test helper that waits for a LiveData to produce a value, so database queries that return LiveData can be checked in tests.
 */

package com.example.nutrimpact;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Helpers for testing LiveData.
 */
public final class LiveDataTestUtil {

    /** How long to wait for a value before failing. */
    private static final long TIMEOUT_SECONDS = 5;

    /**
     * Private constructor so the class can't be instantiated.
     */
    private LiveDataTestUtil() {
    }

    /**
     * Observes a LiveData until it has a value, then returns it.
     *
     * @param liveData the LiveData to read.
     * @param <T>      the value type.
     * @return the first value produced.
     * @throws InterruptedException if interrupted while waiting.
     * @throws TimeoutException     if no value arrives within five seconds.
     */
    public static <T> T getOrAwaitValue(LiveData<T> liveData) throws InterruptedException, TimeoutException {
        final Object[] result = new Object[1];
        CountDownLatch latch = new CountDownLatch(1);
        Observer<T> observer = new Observer<T>() {

            /**
             * Stores the first value and stops observing.
             *
             * @param value the value produced.
             */
            @Override
            public void onChanged(T value) {
                result[0] = value;
                latch.countDown();
                liveData.removeObserver(this);
            }
        };
        liveData.observeForever(observer);
        if (!latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            liveData.removeObserver(observer);
            throw new TimeoutException("LiveData value was never set");
        }
        @SuppressWarnings("unchecked")
        T value = (T) result[0];
        return value;
    }
}
