package org.tensorflow.lite.examples.imageclassification.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import org.tensorflow.lite.examples.imageclassification.R;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private TextInputEditText nameText, emailText;
    private ImageView profileImage;

    private MaterialButton logoutBtn, updateBtn, changeImageBtn;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private Uri selectedImageUri;

    private static final int PICK_IMAGE_REQUEST = 100;

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

        profileImage = view.findViewById(R.id.profileImage);

        logoutBtn = view.findViewById(R.id.logoutBtn);
        updateBtn = view.findViewById(R.id.updateBtn);
        changeImageBtn = view.findViewById(R.id.changeImageBtn);

        loadUserData();

        changeImageBtn.setOnClickListener(v -> openGallery());

        updateBtn.setOnClickListener(v -> updateProfile());

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

    private void openGallery() {

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");

        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == Activity.RESULT_OK
                && data != null
                && data.getData() != null) {

            selectedImageUri = data.getData();

            Glide.with(requireContext())
                    .load(selectedImageUri)
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .into(profileImage);
        }
    }

    private void updateProfile() {

        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) return;

        String uid = currentUser.getUid();

        String updatedName = nameText.getText() != null
                ? nameText.getText().toString().trim()
                : "";

        String updatedEmail = emailText.getText() != null
                ? emailText.getText().toString().trim()
                : "";

        if (updatedName.isEmpty() || updatedEmail.isEmpty()) {

            Toast.makeText(getContext(),
                    "Fields cannot be empty",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        currentUser.updateEmail(updatedEmail)
                .addOnSuccessListener(unused -> {

                    Map<String, Object> updates = new HashMap<>();

                    updates.put("name", updatedName);
                    updates.put("email", updatedEmail);

                    if (selectedImageUri != null) {
                        updates.put("imageUrl",
                                selectedImageUri.toString());
                    }

                    db.collection("users")
                            .document(uid)
                            .update(updates)
                            .addOnSuccessListener(unused1 ->
                                    Toast.makeText(getContext(),
                                            "Profile updated",
                                            Toast.LENGTH_SHORT).show()
                            )
                            .addOnFailureListener(e ->
                                    Toast.makeText(getContext(),
                                            e.getMessage(),
                                            Toast.LENGTH_SHORT).show()
                            );

                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
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

                        nameText.setText(name != null ? name : "");
                        emailText.setText(email != null ? email : "");

                        if (imageUrl != null
                                && !imageUrl.isEmpty()
                                && !imageUrl.equals("DEFAULT")) {

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