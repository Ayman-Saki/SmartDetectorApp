package org.tensorflow.lite.examples.imageclassification;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;

public class MainViewModel extends ViewModel {
    private final MutableLiveData<List<HistoryItem>> _historyItems = new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<HistoryItem>> getHistoryItems() { return _historyItems; }

    private final MutableLiveData<Float> _threshold = new MutableLiveData<>(0.7f); // 70% par défaut
    public LiveData<Float> getThreshold() { return _threshold; }

    private final MutableLiveData<Integer> _delegate = new MutableLiveData<>(0); // 0: CPU, 1: GPU
    public LiveData<Integer> getDelegate() { return _delegate; }

    public void addHistoryItem(HistoryItem item) {
        List<HistoryItem> currentList = _historyItems.getValue();
        List<HistoryItem> newList = new ArrayList<>(currentList != null ? currentList : new ArrayList<>());
        newList.add(0, item);
        _historyItems.setValue(newList);
    }

    public void setThreshold(float value) {
        _threshold.setValue(value);
    }

    public void setDelegate(int value) {
        _delegate.setValue(value);
    }
}
