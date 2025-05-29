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

public class LoginActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private Button loginButton, registerRedirectButton;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializuje Firebase Authentication
        auth = FirebaseAuth.getInstance();

        emailInput = findViewById(R.id.editTextEmail);
        passwordInput = findViewById(R.id.editTextPassword);
        loginButton = findViewById(R.id.buttonLogin);
        registerRedirectButton = findViewById(R.id.buttonRegisterRedirect);

        loginButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Zadajte email a heslo", Toast.LENGTH_SHORT).show();
                return;
            }

            // Prihlási používateľa cez Firebase
            auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            String uid = user.getUid();

                            // Uloží UID do SharedPreferences
                            SharedPreferences prefs = getSharedPreferences("userdata", MODE_PRIVATE);
                            prefs.edit().putString("uid", uid).apply();

                            // Prejde na úvodné menu
                            startActivity(new Intent(LoginActivity.this, UvodneMenu.class));
                            finish();
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Prihlásenie zlyhalo (nesprávny mail alebo heslo)", Toast.LENGTH_LONG).show()
                    );
        });

        registerRedirectButton.setOnClickListener(v -> {
            // Prejde na registračnú obrazovku
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }
}