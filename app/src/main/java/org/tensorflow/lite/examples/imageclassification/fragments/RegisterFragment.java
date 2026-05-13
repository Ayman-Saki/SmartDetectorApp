package org.tensorflow.lite.examples.imageclassification.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import org.tensorflow.lite.examples.imageclassification.R;

import java.util.HashMap;
import java.util.Map;

public class RegisterFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private TextInputEditText nameInput, emailInput, passwordInput;
    private MaterialButton registerBtn;

    public RegisterFragment() {}

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

        registerBtn.setOnClickListener(v -> registerUser(view));

        // 🔥 GO TO LOGIN
        TextView goLogin = view.findViewById(R.id.goLogin);
        goLogin.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.loginFragment)
        );

        return view;
    }

    private void registerUser(View view) {

        String name = nameInput.getText() != null
                ? nameInput.getText().toString().trim()
                : "";

        String email = emailInput.getText() != null
                ? emailInput.getText().toString().trim()
                : "";

        String password = passwordInput.getText() != null
                ? passwordInput.getText().toString().trim()
                : "";

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful() && mAuth.getCurrentUser() != null) {

                        String uid = mAuth.getCurrentUser().getUid();

                        Map<String, Object> user = new HashMap<>();
                        user.put("name", name);
                        user.put("email", email);
                        user.put("imageUrl", "DEFAULT"); // no Firebase Storage (as agreed)

                        db.collection("users")
                                .document(uid)
                                .set(user)
                                .addOnSuccessListener(unused -> {

                                    Toast.makeText(getContext(), "Account created", Toast.LENGTH_SHORT).show();

                                    view.post(() ->
                                            Navigation.findNavController(view)
                                                    .navigate(R.id.camera_fragment)
                                    );
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(getContext(),
                                                "Firestore error: " + e.getMessage(),
                                                Toast.LENGTH_SHORT).show()
                                );

                    } else {
                        Toast.makeText(getContext(),
                                task.getException() != null
                                        ? task.getException().getMessage()
                                        : "Registration failed",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}