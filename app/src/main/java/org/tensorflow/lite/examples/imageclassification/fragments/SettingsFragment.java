package org.tensorflow.lite.examples.imageclassification.fragments;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import org.tensorflow.lite.examples.imageclassification.MainViewModel;
import org.tensorflow.lite.examples.imageclassification.R;
import org.tensorflow.lite.examples.imageclassification.databinding.FragmentSettingsBinding;

import java.util.Locale;

public class SettingsFragment extends Fragment {
    private FragmentSettingsBinding binding;
    private MainViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        // Seuil de confiance
        viewModel.getThreshold().observe(getViewLifecycleOwner(), threshold -> {
            int progress = (int) (threshold * 100);
            binding.thresholdSeekbar.setProgress(progress);
            binding.thresholdValueText.setText(String.format(Locale.getDefault(), "%d%%", progress));
        });

        binding.thresholdSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    binding.thresholdValueText.setText(String.format(Locale.getDefault(), "%d%%", progress));
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                viewModel.setThreshold(seekBar.getProgress() / 100f);
            }
        });

        // Mode d'inférence (CPU/GPU)
        viewModel.getDelegate().observe(getViewLifecycleOwner(), this::updateDelegateButtons);

        binding.cpuButton.setOnClickListener(v -> viewModel.setDelegate(0));
        binding.gpuButton.setOnClickListener(v -> viewModel.setDelegate(1));

        // Thème
        String[] themes = {"Clair", "Sombre", "Système"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, themes);
        binding.themeSpinner.setAdapter(adapter);

        binding.saveButton.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Paramètres sauvegardés", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateDelegateButtons(int delegate) {
        int indigo = ContextCompat.getColor(requireContext(), R.color.primary);
        int white = ContextCompat.getColor(requireContext(), R.color.white);

        if (delegate == 0) { // CPU Sélectionné
            binding.cpuButton.setBackgroundTintList(ColorStateList.valueOf(indigo));
            binding.cpuButton.setTextColor(white);
            binding.cpuButton.setIconTint(ColorStateList.valueOf(white));
            
            binding.gpuButton.setBackgroundTintList(ColorStateList.valueOf(white));
            binding.gpuButton.setTextColor(indigo);
            binding.gpuButton.setIconTint(ColorStateList.valueOf(indigo));
            binding.gpuButton.setStrokeColor(ColorStateList.valueOf(indigo));
        } else { // GPU Sélectionné
            binding.gpuButton.setBackgroundTintList(ColorStateList.valueOf(indigo));
            binding.gpuButton.setTextColor(white);
            binding.gpuButton.setIconTint(ColorStateList.valueOf(white));
            
            binding.cpuButton.setBackgroundTintList(ColorStateList.valueOf(white));
            binding.cpuButton.setTextColor(indigo);
            binding.cpuButton.setIconTint(ColorStateList.valueOf(indigo));
            binding.cpuButton.setStrokeColor(ColorStateList.valueOf(indigo));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
