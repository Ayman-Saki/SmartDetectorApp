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

import org.tensorflow.lite.examples.imageclassification.R;

public class LoginFragment extends Fragment {

    private FirebaseAuth mAuth;
    private TextInputEditText emailInput, passwordInput;
    private MaterialButton loginBtn;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_login_fragment, container, false);

        mAuth = FirebaseAuth.getInstance();

        // AUTO LOGIN
        if (mAuth.getCurrentUser() != null) {
            view.post(() ->
                    Navigation.findNavController(view)
                            .navigate(R.id.camera_fragment)
            );
        }

        emailInput = view.findViewById(R.id.emailInput);
        passwordInput = view.findViewById(R.id.passwordInput);
        loginBtn = view.findViewById(R.id.loginBtn);

        loginBtn.setOnClickListener(v -> loginUser(view));

        // go register
        TextView goRegister = view.findViewById(R.id.goRegister);
        goRegister.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_login_to_register)
        );

        return view;
    }

    private void loginUser(View view) {

        String email = emailInput.getText() != null ? emailInput.getText().toString().trim() : "";
        String password = passwordInput.getText() != null ? passwordInput.getText().toString().trim() : "";

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        Navigation.findNavController(view)
                                .navigate(R.id.action_login_to_camera);

                    } else {
                        Toast.makeText(getContext(),
                                task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}