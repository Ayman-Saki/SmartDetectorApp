package org.tensorflow.lite.examples.imageclassification.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.tensorflow.lite.examples.imageclassification.R;

public class ProfileFragment extends Fragment {

    private TextView nameText, emailText;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    public ProfileFragment() {}

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // ✅ IMPORTANT FIX: correct XML name
        View view = inflater.inflate(R.layout.activity_profile_fragment, container, false);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        nameText = view.findViewById(R.id.nameText);
        emailText = view.findViewById(R.id.emailText);

        loadUserData();

        return view;
    }

    private void loadUserData() {

        String uid = auth.getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (documentSnapshot.exists()) {

                        String name = documentSnapshot.getString("name");
                        String email = documentSnapshot.getString("email");

                        nameText.setText(name);
                        emailText.setText(email);
                    }
                });
    }
}