package org.tensorflow.lite.examples.imageclassification;

public class HistoryItem {
    private final String label;
    private final float confidence;
    private final long timestamp;
    private final long inferenceTime;

    public HistoryItem(String label, float confidence, long timestamp, long inferenceTime) {
        this.label = label;
        this.confidence = confidence;
        this.timestamp = timestamp;
        this.inferenceTime = inferenceTime;
    }

    public String getLabel() { return label; }
    public float getConfidence() { return confidence; }
    public long getTimestamp() { return timestamp; }
    public long getInferenceTime() { return inferenceTime; }
}
