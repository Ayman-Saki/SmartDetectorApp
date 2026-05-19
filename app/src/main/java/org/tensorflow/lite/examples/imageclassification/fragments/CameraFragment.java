package org.tensorflow.lite.examples.imageclassification.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;
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

    private Camera camera;
    private MainViewModel viewModel;

    private boolean isFrozen = false;
    private boolean isFlashOn = false;
    private Category lastResult;
    private long lastInferenceTime;

    private ExecutorService cameraExecutor;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCameraBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cameraExecutor = Executors.newSingleThreadExecutor();
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        imageClassifierHelper = ImageClassifierHelper.create(requireContext(), this);

        view.findViewById(R.id.micButton).setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.micFragment)
        );

        binding.btnHistory.setOnClickListener(v ->
                Navigation.findNavController(requireActivity(), R.id.fragment_container)
                        .navigate(R.id.history_fragment)
        );

        binding.viewFinder.post(this::setUpCamera);
    }

    private void setUpCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Camera init failed", e);
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void bindCameraUseCases(ProcessCameraProvider cameraProvider) {

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        Preview preview = new Preview.Builder().build();

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build();

        imageAnalysis.setAnalyzer(cameraExecutor, image -> {

            if (isFrozen) {
                image.close();
                return;
            }

            if (bitmapBuffer == null) {
                bitmapBuffer = Bitmap.createBitmap(
                        image.getWidth(),
                        image.getHeight(),
                        Bitmap.Config.ARGB_8888
                );
            }

            bitmapBuffer.copyPixelsFromBuffer(image.getPlanes()[0].getBuffer());

            int rotation = image.getImageInfo().getRotationDegrees();
            image.close();

            imageClassifierHelper.classify(bitmapBuffer, rotation);
        });

        cameraProvider.unbindAll();

        camera = cameraProvider.bindToLifecycle(
                getViewLifecycleOwner(),
                cameraSelector,
                preview,
                imageAnalysis
        );

        // ✅ CRITICAL FIX: prevents black screen on many devices
        binding.viewFinder.setImplementationMode(
                androidx.camera.view.PreviewView.ImplementationMode.COMPATIBLE
        );

        preview.setSurfaceProvider(binding.viewFinder.getSurfaceProvider());
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
            if (results != null && !results.isEmpty()
                    && !results.get(0).getCategories().isEmpty()) {

                Category category = results.get(0).getCategories().get(0);
                lastResult = category;
                lastInferenceTime = inferenceTime;

                binding.resultTitle.setText(category.getLabel());
                binding.resultConfidence.setText((int)(category.getScore() * 100) + "%");
                binding.confidenceProgress.setProgress((int)(category.getScore() * 100));
                binding.inferenceTime.setText("Inference Time: " + inferenceTime + " ms");

                binding.resultsCard.setVisibility(View.VISIBLE);
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