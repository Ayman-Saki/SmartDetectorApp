/*
 * Copyright 2022 The TensorFlow Authors. All Rights Reserved.
 */
package org.tensorflow.lite.examples.imageclassification.fragments;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.tensorflow.lite.examples.imageclassification.HistoryItem;
import org.tensorflow.lite.examples.imageclassification.ImageClassifierHelper;
import org.tensorflow.lite.examples.imageclassification.MainViewModel;
import org.tensorflow.lite.examples.imageclassification.R;
import org.tensorflow.lite.examples.imageclassification.databinding.FragmentCameraBinding;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.task.vision.classifier.Classifications;

public class CameraFragment extends Fragment
        implements ImageClassifierHelper.ClassifierListener {
    private static final String TAG = "CameraFragment";

    private FragmentCameraBinding binding;
    private ImageClassifierHelper imageClassifierHelper;
    private Bitmap bitmapBuffer;
    private ImageAnalysis imageAnalyzer;
    private ProcessCameraProvider cameraProvider;
    private Camera camera;
    private MainViewModel viewModel;
    
    private boolean isFrozen = false;
    private boolean isFlashOn = false;
    private Category lastResult;
    private long lastInferenceTime;

    private ExecutorService cameraExecutor;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCameraBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cameraExecutor = Executors.newSingleThreadExecutor();
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        imageClassifierHelper = ImageClassifierHelper.create(requireContext(), this);

        // 1. Bouton Flash
        binding.btnFlash.setOnClickListener(v -> {
            if (camera != null && camera.getCameraInfo().hasFlashUnit()) {
                isFlashOn = !isFlashOn;
                camera.getCameraControl().enableTorch(isFlashOn);
                binding.btnFlash.setImageResource(isFlashOn ? 
                    android.R.drawable.btn_star_big_on : R.drawable.ic_flash);
                Toast.makeText(requireContext(), isFlashOn ? "Flash Activé" : "Flash Désactivé", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Flash non disponible", Toast.LENGTH_SHORT).show();
            }
        });

        // 2. Bouton Freeze (Arrêt sur image)
        binding.btnFreeze.setOnClickListener(v -> {
            isFrozen = !isFrozen;
            binding.freezeOverlay.setVisibility(isFrozen ? View.VISIBLE : View.GONE);
            binding.liveBadgeContainer.setVisibility(isFrozen ? View.GONE : View.VISIBLE);
            
            if (isFrozen) {
                if (bitmapBuffer != null) {
                    binding.frozenImage.setImageBitmap(bitmapBuffer.copy(bitmapBuffer.getConfig(), false));
                    binding.frozenImage.setVisibility(View.VISIBLE);
                }
                binding.btnFreeze.setImageResource(android.R.drawable.ic_media_play);
                binding.freezeLabel.setText("Continuer\n(Mode Live)");
            } else {
                binding.frozenImage.setVisibility(View.GONE);
                binding.btnFreeze.setImageResource(R.drawable.ic_freeze);
                binding.freezeLabel.setText("Freeze\n(Arrêt sur image)");
            }
        });

        // 3. Bouton Capturer (Enregistrer dans l'historique)
        binding.btnCapture.setOnClickListener(v -> {
            if (lastResult != null) {
                HistoryItem item = new HistoryItem(
                        lastResult.getLabel(),
                        lastResult.getScore(),
                        System.currentTimeMillis(),
                        lastInferenceTime
                );
                viewModel.addHistoryItem(item);
                Toast.makeText(requireContext(), "Objet capturé : " + lastResult.getLabel(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Aucune détection en cours", Toast.LENGTH_SHORT).show();
            }
        });

        // 4. Bouton Historique (Navigation)
        binding.btnHistory.setOnClickListener(v -> {
            Navigation.findNavController(requireActivity(), R.id.fragment_container)
                    .navigate(R.id.history_fragment);
        });

        binding.viewFinder.post(this::setUpCamera);
    }

    private void setUpCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(requireContext());
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases();
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Camera initialization failed", e);
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void bindCameraUseCases() {
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK).build();

        Preview preview = new Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(binding.viewFinder.getDisplay().getRotation())
                .build();

        imageAnalyzer = new ImageAnalysis.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(binding.viewFinder.getDisplay().getRotation())
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build();

        imageAnalyzer.setAnalyzer(cameraExecutor, image -> {
            if (isFrozen) {
                image.close();
                return;
            }
            if (bitmapBuffer == null) {
                bitmapBuffer = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
            }
            bitmapBuffer.copyPixelsFromBuffer(image.getPlanes()[0].getBuffer());
            int rotation = image.getImageInfo().getRotationDegrees();
            image.close();
            imageClassifierHelper.classify(bitmapBuffer, rotation);
        });

        cameraProvider.unbindAll();
        try {
            camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer);
            preview.setSurfaceProvider(binding.viewFinder.getSurfaceProvider());
        } catch (Exception exc) {
            Log.e(TAG, "Use case binding failed", exc);
        }
    }

    @Override
    public void onError(String error) {
        if (!isAdded()) return;
        requireActivity().runOnUiThread(() -> 
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public void onResults(List<Classifications> results, long inferenceTime) {
        if (!isAdded() || isFrozen) return;

        requireActivity().runOnUiThread(() -> {
            if (results != null && !results.isEmpty() && !results.get(0).getCategories().isEmpty()) {
                Category category = results.get(0).getCategories().get(0);
                lastResult = category;
                lastInferenceTime = inferenceTime;

                binding.resultTitle.setText(category.getLabel());
                int confidencePercent = (int) (category.getScore() * 100);
                binding.resultConfidence.setText(confidencePercent + "%");
                binding.confidenceProgress.setProgress(confidencePercent);
                binding.inferenceTime.setText(String.format(Locale.US, "Inference Time: %d ms", inferenceTime));
                binding.resultsCard.setVisibility(View.VISIBLE);
            } else {
                lastResult = null;
                binding.resultTitle.setText("Recherche...");
                binding.resultConfidence.setText("0%");
                binding.confidenceProgress.setProgress(0);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cameraExecutor.shutdown();
        imageClassifierHelper.clearImageClassifier();
    }
}
