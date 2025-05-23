package sk.spsepo.lesko.steeplechasegame;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private EditText nameInput, emailInput, passwordInput;
    private Button registerButton, backToLoginButton;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();

        nameInput = findViewById(R.id.editTextName);
        emailInput = findViewById(R.id.editTextEmail);
        passwordInput = findViewById(R.id.editTextPassword);
        registerButton = findViewById(R.id.buttonRegister);
        backToLoginButton = findViewById(R.id.buttonBackToLogin);

        registerButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vyplňte všetky údaje", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(result -> {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            String uid = user.getUid();

                            // Ulož meno a UID
                            SharedPreferences prefs = getSharedPreferences("userdata", MODE_PRIVATE);
                            prefs.edit()
                                    .putString("uid", uid)
                                    .putString("username", name)
                                    .apply();

                            Toast.makeText(this, "Registrácia úspešná!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, UvodneMenu.class));
                            finish();
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Registrácia zlyhala: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
        });

        backToLoginButton.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}
