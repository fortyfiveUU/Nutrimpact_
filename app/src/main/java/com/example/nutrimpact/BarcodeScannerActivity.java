/**
 * Module: EEE521 - Final Year Project
 * Project: Nutrimpact
 * File: BarcodeScannerActivity.java
 * Description: Camera screen that scans product barcodes (EAN-13, EAN-8, UPC-A) using CameraX and on-device ML Kit, then returns the barcode to the search screen.
 */

package com.example.nutrimpact;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Scans a barcode and returns it with {@link #EXTRA_BARCODE}. All image
 * processing happens on the device; no camera images are uploaded.
 */
public class BarcodeScannerActivity extends AppCompatActivity {

    /** Result extra: the scanned barcode digits. */
    public static final String EXTRA_BARCODE = "com.example.nutrimpact.BARCODE";

    /** Logcat tag. */
    private static final String TAG = "NutrimpactScanner";

    /** Camera preview. */
    private PreviewView previewCamera;

    /** Message area shown if the camera can't be used. */
    private View layoutScannerMessage;

    /** Text inside the message area. */
    private TextView textScannerMessage;

    /** Runs barcode detection off the main thread. */
    private ExecutorService analysisExecutor;

    /** ML Kit barcode detector. */
    private BarcodeScanner scanner;

    /** Makes sure only the first good barcode is returned. */
    private final AtomicBoolean finished = new AtomicBoolean(false);

    /** Asks the user for camera permission. */
    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    startCamera();
                } else {
                    showMessage(R.string.scanner_permission_needed);
                }
            });

    /**
     * Builds an Intent that opens the scanner.
     *
     * @param context the screen opening the scanner.
     * @return an Intent for an activity-result launcher.
     */
    public static Intent createIntent(Context context) {
        return new Intent(context, BarcodeScannerActivity.class);
    }

    /**
     * Called when the screen is created. Starts the camera if permission has
     * already been given, otherwise asks for it.
     *
     * @param savedInstanceState previously saved state, or null on first open.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_scanner);

        previewCamera = findViewById(R.id.previewCamera);
        layoutScannerMessage = findViewById(R.id.layoutScannerMessage);
        textScannerMessage = findViewById(R.id.textScannerMessage);
        MaterialToolbar toolbar = findViewById(R.id.toolbarScanner);
        toolbar.setNavigationOnClickListener(view -> finish());
        findViewById(R.id.buttonScannerClose).setOnClickListener(view -> finish());

        analysisExecutor = Executors.newSingleThreadExecutor();
        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_EAN_13, Barcode.FORMAT_EAN_8, Barcode.FORMAT_UPC_A)
                .build();
        scanner = BarcodeScanning.getClient(options);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    /**
     * Releases the camera thread and the detector.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        analysisExecutor.shutdown();
        scanner.close();
    }

    /**
     * Connects the back camera to the preview and to barcode detection.
     * CameraX stops the camera automatically when this screen closes.
     */
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(this);
        future.addListener(() -> {
            try {
                ProcessCameraProvider provider = future.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewCamera.getSurfaceProvider());

                ImageAnalysis analysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();
                analysis.setAnalyzer(analysisExecutor, this::analyse);

                provider.unbindAll();
                provider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis);
            } catch (Exception e) {
                Log.e(TAG, "Camera could not be started", e);
                showMessage(R.string.scanner_camera_error);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    /**
     * Looks for a barcode in one camera frame. The frame must always be
     * closed so the camera can deliver the next one.
     *
     * @param imageProxy the camera frame.
     */
    @OptIn(markerClass = ExperimentalGetImage.class)
    private void analyse(@NonNull ImageProxy imageProxy) {
        if (imageProxy.getImage() == null || finished.get()) {
            imageProxy.close();
            return;
        }
        InputImage image = InputImage.fromMediaImage(imageProxy.getImage(),
                imageProxy.getImageInfo().getRotationDegrees());
        scanner.process(image)
                .addOnSuccessListener(barcodes -> {
                    for (Barcode barcode : barcodes) {
                        String value = barcode.getRawValue();
                        if (BarcodeUtils.isValidBarcode(value)) {
                            returnBarcode(value.trim());
                            break;
                        }
                    }
                })
                .addOnFailureListener(e -> Log.w(TAG, "Barcode detection failed", e))
                .addOnCompleteListener(task -> imageProxy.close());
    }

    /**
     * Sends the barcode back to the search screen and closes the scanner.
     * Only the first valid barcode is used.
     *
     * @param barcode the scanned digits.
     */
    private void returnBarcode(String barcode) {
        if (!finished.compareAndSet(false, true)) {
            return;
        }
        // CONFIRM was added in Android 11; older phones get a standard tap vibration.
        previewCamera.performHapticFeedback(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
                ? HapticFeedbackConstants.CONFIRM : HapticFeedbackConstants.VIRTUAL_KEY);
        Intent result = new Intent();
        result.putExtra(EXTRA_BARCODE, barcode);
        setResult(RESULT_OK, result);
        finish();
    }

    /**
     * Hides the camera and shows a message with a Close button.
     *
     * @param messageResId the message to show.
     */
    private void showMessage(int messageResId) {
        textScannerMessage.setText(messageResId);
        layoutScannerMessage.setVisibility(View.VISIBLE);
        findViewById(R.id.viewScanFrame).setVisibility(View.GONE);
        findViewById(R.id.textScannerHint).setVisibility(View.GONE);
    }
}
