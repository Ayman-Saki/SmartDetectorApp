package org.tensorflow.lite.examples.imageclassification.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import java.util.ArrayList;
import java.util.Locale;

import org.tensorflow.lite.examples.imageclassification.R;

public class MicFragment extends Fragment {

    private static final int REQ_CODE_SPEECH = 100;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_mic, container, false);

        view.findViewById(R.id.startMicBtn).setOnClickListener(v -> startListening());

        return view;
    }

    private void startListening() {

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault());

        try {
            startActivityForResult(intent, REQ_CODE_SPEECH);
        } catch (Exception e) {
            Toast.makeText(getContext(),
                    "Speech not supported on this device",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_CODE_SPEECH && data != null) {

            ArrayList<String> result =
                    data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            if (result != null && !result.isEmpty()) {
                handleCommand(result.get(0).toLowerCase());
            }
        }
    }

    private void handleCommand(String command) {

        NavController nav = Navigation.findNavController(requireView());

        command = command.toLowerCase().trim();

        // Normalize noise words (EN + FR)
        command = command.replace("open", "")
                .replace("go to", "")
                .replace("show", "")
                .replace("me", "")
                .replace("screen", "")
                .replace("page", "")
                .replace("ouvrir", "")
                .replace("aller", "")
                .replace("à", "")
                .replace("a", "")
                .replace("afficher", "");

        // CAMERA
        if (command.contains("camera") || command.contains("caméra") || command.contains("accueil")) {
            nav.navigate(R.id.camera_fragment);
        }

        // PROFILE
        else if (command.contains("profile") || command.contains("profil")) {
            nav.navigate(R.id.profileFragment);
        }

        // QR
        else if (command.contains("qr") || command.contains("code")) {
            nav.navigate(R.id.qrFragment);
        }

        // HISTORY
        else if (command.contains("history") || command.contains("historique")) {
            nav.navigate(R.id.history_fragment);
        }

        // SETTINGS
        else if (command.contains("settings") || command.contains("paramètres") || command.contains("parametres")) {
            nav.navigate(R.id.settings_fragment);
        }

        else {
            Toast.makeText(getContext(),
                    "Command not recognized: " + command,
                    Toast.LENGTH_SHORT).show();
        }
    }
}