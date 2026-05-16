package org.tensorflow.lite.examples.imageclassification.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;

import com.google.android.material.button.MaterialButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.tensorflow.lite.examples.imageclassification.R;

public class ProfileFragment extends Fragment {

    private TextView nameText, emailText;
    private ImageView profileImage;
    private MaterialButton logoutBtn;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_profile_fragment, container, false);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        nameText = view.findViewById(R.id.nameText);
        emailText = view.findViewById(R.id.emailText);
        logoutBtn = view.findViewById(R.id.logoutBtn);

        profileImage = view.findViewById(R.id.profileImage);

        loadUserData();

        logoutBtn.setOnClickListener(v -> {

            FirebaseAuth.getInstance().signOut();

            androidx.navigation.NavOptions navOptions =
                    new androidx.navigation.NavOptions.Builder()
                            .setPopUpTo(R.id.camera_fragment, true)
                            .build();

            androidx.navigation.Navigation.findNavController(v)
                    .navigate(R.id.loginFragment, null, navOptions);
        });

        return view;
    }

    private void loadUserData() {

        if (auth.getCurrentUser() == null) return;

        String uid = auth.getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {

                    if (doc.exists()) {

                        String name = doc.getString("name");
                        String email = doc.getString("email");
                        String imageUrl = doc.getString("imageUrl");

                        nameText.setText(name != null ? name : "No name");
                        emailText.setText(email != null ? email : "No email");

                        // PROFILE IMAGE HANDLING
                        if (imageUrl != null && !imageUrl.isEmpty() && !imageUrl.equals("DEFAULT")) {

                            Glide.with(requireContext())
                                    .load(Uri.parse(imageUrl))
                                    .placeholder(R.drawable.ic_profile)
                                    .error(R.drawable.ic_profile)
                                    .into(profileImage);

                        } else {
                            profileImage.setImageResource(R.drawable.ic_profile);
                        }
                    }
                });
    }
}