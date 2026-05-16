package org.tensorflow.lite.examples.imageclassification.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import org.tensorflow.lite.examples.imageclassification.R;

public class PermissionsFragment extends Fragment {

    public static boolean hasPermission(Context context) {
        return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (!isAdded()) return;

                        if (isGranted) {
                            Toast.makeText(requireContext(),
                                    "Camera permission granted",
                                    Toast.LENGTH_SHORT).show();
                            navigateToCamera();
                        } else {
                            Toast.makeText(requireContext(),
                                    "Camera permission denied",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
            );

    @Override
    public void onStart() {
        super.onStart();

        if (!isAdded()) return;

        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            navigateToCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void navigateToCamera() {
        if (!isAdded()) return;

        Navigation.findNavController(requireActivity(), R.id.fragment_container)
                .navigate(PermissionsFragmentDirections.actionPermissionsToCamera());
    }
}