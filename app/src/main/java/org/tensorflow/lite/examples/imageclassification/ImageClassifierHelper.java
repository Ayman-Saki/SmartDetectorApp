/*
 * Copyright 2022 The TensorFlow Authors. All Rights Reserved.
 */

package org.tensorflow.lite.examples.imageclassification;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.tensorflow.lite.gpu.CompatibilityList;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.Rot90Op;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.task.core.BaseOptions;
import org.tensorflow.lite.task.vision.classifier.Classifications;
import org.tensorflow.lite.task.vision.classifier.ImageClassifier;

/** Helper class for wrapping Image Classification actions */
public class ImageClassifierHelper {
    private static final String TAG = "ImageClassifierHelper";
    private static final int DELEGATE_CPU = 0;
    private static final int DELEGATE_GPU = 1;
    private static final int DELEGATE_NNAPI = 2;

    private float threshold;
    private int numThreads;
    private int maxResults;
    private int currentDelegate;
    private int currentModel;
    private final Context context;
    private final ClassifierListener imageClassifierListener;
    private ImageClassifier imageClassifier;

    private List<String> customLabels = new ArrayList<>();

    public ImageClassifierHelper(Float threshold,
                                 int numThreads,
                                 int maxResults,
                                 int currentDelegate,
                                 int currentModel,
                                 Context context,
                                 ClassifierListener imageClassifierListener) {
        this.threshold = threshold;
        this.numThreads = numThreads;
        this.maxResults = maxResults;
        this.currentDelegate = currentDelegate;
        this.currentModel = currentModel;
        this.context = context;
        this.imageClassifierListener = imageClassifierListener;
        setupImageClassifier();
    }

    public static ImageClassifierHelper create(Context context, ClassifierListener listener) {
        return new ImageClassifierHelper(0.7f, 2, 3, 0, 0, context, listener);
    }

    public float getThreshold() { return threshold; }
    public void setThreshold(float threshold) { this.threshold = threshold; }
    public int getNumThreads() { return numThreads; }
    public void setNumThreads(int numThreads) { this.numThreads = numThreads; }
    public int getMaxResults() { return maxResults; }
    public void setMaxResults(int maxResults) { this.maxResults = maxResults; }
    public void setCurrentDelegate(int currentDelegate) { this.currentDelegate = currentDelegate; }
    public void setCurrentModel(int currentModel) { this.currentModel = currentModel; }

    public void setupImageClassifier() {
        customLabels.clear();
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(context.getAssets().open("labels.txt")));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.replaceAll("^\\d+\\s+", "");
                customLabels.add(line);
            }
            reader.close();
        } catch (IOException e) {
            Log.e(TAG, "Impossible de lire le fichier labels.txt", e);
        }

        ImageClassifier.ImageClassifierOptions.Builder optionsBuilder =
                ImageClassifier.ImageClassifierOptions.builder()
                        .setScoreThreshold(threshold)
                        .setMaxResults(maxResults);

        BaseOptions.Builder baseOptionsBuilder =
                BaseOptions.builder().setNumThreads(numThreads);

        switch (currentDelegate) {
            case DELEGATE_CPU:
                break;
            case DELEGATE_GPU:
                if (new CompatibilityList().isDelegateSupportedOnThisDevice()) {
                    baseOptionsBuilder.useGpu();
                } else {
                    imageClassifierListener.onError("GPU is not supported on this device");
                }
                break;
            case DELEGATE_NNAPI:
                baseOptionsBuilder.useNnapi();
        }

        optionsBuilder.setBaseOptions(baseOptionsBuilder.build());

        String modelName;
        switch (currentModel) {
            case 0: modelName = "mobilenetv1.tflite"; break;
            case 1: modelName = "efficientnet-lite0.tflite"; break;
            case 2: modelName = "efficientnet-lite1.tflite"; break;
            case 3: modelName = "efficientnet-lite2.tflite"; break;
            default: modelName = "mobilenetv1.tflite";
        }

        try {
            imageClassifier =
                    ImageClassifier.createFromFileAndOptions(
                            context,
                            modelName,
                            optionsBuilder.build());
        } catch (IOException e) {
            imageClassifierListener.onError("Failed to load model: " + e.getMessage());
        }
    }

    public void classify(Bitmap image, int imageRotation) {
        if (imageClassifier == null) {
            setupImageClassifier();
        }

        if (imageClassifier == null) return;

        long inferenceTime = SystemClock.uptimeMillis();

        ImageProcessor imageProcessor =
                new ImageProcessor.Builder().add(new Rot90Op(-imageRotation / 90)).build();

        TensorImage tensorImage =
                imageProcessor.process(TensorImage.fromBitmap(image));

        List<Classifications> results = imageClassifier.classify(tensorImage);

        if (results != null && !results.isEmpty() && !customLabels.isEmpty()) {
            List<Classifications> translatedResults = new ArrayList<>();
            for (Classifications classification : results) {
                List<Category> translatedCategories = new ArrayList<>();
                for (Category category : classification.getCategories()) {
                    try {
                        int index = -1;
                        try {
                            index = Integer.parseInt(category.getLabel());
                        } catch (NumberFormatException e) {
                            index = category.getIndex();
                        }
                        
                        if (index >= 0 && index < customLabels.size()) {
                            String realName = customLabels.get(index);
                            translatedCategories.add(Category.create(
                                    realName,
                                    category.getDisplayName(),
                                    category.getScore(),
                                    category.getIndex()
                            ));
                        } else {
                            translatedCategories.add(category);
                        }
                    } catch (Exception e) {
                        translatedCategories.add(category);
                    }
                }

                try {
                    Method createMethod = Classifications.class.getDeclaredMethod("create", List.class, int.class);
                    createMethod.setAccessible(true); 
                    Classifications newClassifications = (Classifications) createMethod.invoke(null, translatedCategories, classification.getHeadIndex());
                    translatedResults.add(newClassifications);
                } catch (Exception e) {
                    translatedResults.add(classification);
                }
            }
            results = translatedResults;
        }

        inferenceTime = SystemClock.uptimeMillis() - inferenceTime;
        imageClassifierListener.onResults(results, inferenceTime);
    }

    public void clearImageClassifier() {
        imageClassifier = null;
    }

    public interface ClassifierListener {
        void onError(String error);
        void onResults(List<Classifications> results, long inferenceTime);
    }
}
