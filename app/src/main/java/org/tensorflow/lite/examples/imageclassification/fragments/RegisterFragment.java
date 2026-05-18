package org.tensorflow.lite.examples.imageclassification.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.tensorflow.lite.examples.imageclassification.ApiClient;
import org.tensorflow.lite.examples.imageclassification.ApiService;
import org.tensorflow.lite.examples.imageclassification.R;

import java.util.HashMap;
import java.util.Map;

// 🔥 FASTAPI ADD
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private TextInputEditText nameInput, emailInput, passwordInput;
    private MaterialButton registerBtn;

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri selectedImageUri;

    // 🔥 FASTAPI
    private ApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_register_fragment, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        nameInput = view.findViewById(R.id.nameInput);
        emailInput = view.findViewById(R.id.emailInput);
        passwordInput = view.findViewById(R.id.passwordInput);
        registerBtn = view.findViewById(R.id.registerBtn);
        ImageView profileImage = view.findViewById(R.id.profileImage);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) MaterialButton pickImageBtn = view.findViewById(R.id.pickImageBtn);

        // FASTAPI INIT
        apiService = ApiClient
                .getClient("http://192.168.1.19:8000/")
                .create(ApiService.class);

        registerBtn.setOnClickListener(v -> registerUser(view));

        TextView goLogin = view.findViewById(R.id.goLogin);
        goLogin.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_register_to_login)
        );

        pickImageBtn.setOnClickListener(v -> openGallery());

        return view;
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void registerUser(View view) {

        String name = nameInput.getText() != null ? nameInput.getText().toString().trim() : "";
        String email = emailInput.getText() != null ? emailInput.getText().toString().trim() : "";
        String password = passwordInput.getText() != null ? passwordInput.getText().toString().trim() : "";

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        //  FASTAPI CALL (NON-BREAKING)
        apiService.registerUser(name, email, password)
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        // just log success
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        // ignore errors (DO NOT BREAK APP)
                    }
                });

        // =========================
        // FIREBASE (UNCHANGED)
        // =========================
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful() && mAuth.getCurrentUser() != null) {

                        String uid = mAuth.getCurrentUser().getUid();

                        Map<String, Object> user = new HashMap<>();
                        user.put("name", name);
                        user.put("email", email);

                        if (selectedImageUri != null) {
                            user.put("imageUrl", selectedImageUri.toString());
                        } else {
                            user.put("imageUrl", "");
                        }

                        db.collection("users")
                                .document(uid)
                                .set(user)
                                .addOnSuccessListener(unused ->
                                        Navigation.findNavController(view)
                                                .navigate(R.id.action_register_to_camera)
                                );

                    } else {
                        Toast.makeText(getContext(),
                                task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && data != null && data.getData() != null) {
            selectedImageUri = data.getData();

            ImageView profileImage = getView().findViewById(R.id.profileImage);
            profileImage.setImageURI(selectedImageUri);
        }
    }
}