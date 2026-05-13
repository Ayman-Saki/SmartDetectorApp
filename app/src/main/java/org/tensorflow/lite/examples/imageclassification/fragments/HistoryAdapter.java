package org.tensorflow.lite.examples.imageclassification.fragments;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.tensorflow.lite.examples.imageclassification.HistoryItem;
import org.tensorflow.lite.examples.imageclassification.databinding.ItemHistoryBinding;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private List<HistoryItem> items = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public void setItems(List<HistoryItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHistoryBinding binding = ItemHistoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemHistoryBinding binding;

        ViewHolder(ItemHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(HistoryItem item) {
            binding.historyItemLabel.setText(item.getLabel());
            String timeStr = dateFormat.format(new Date(item.getTimestamp()));
            binding.historyItemInfo.setText(String.format("%s  •  %d ms", timeStr, item.getInferenceTime()));
            binding.historyItemConfidence.setText(String.format(Locale.getDefault(), "%d%%", (int)(item.getConfidence() * 100)));
        }
    }
}
