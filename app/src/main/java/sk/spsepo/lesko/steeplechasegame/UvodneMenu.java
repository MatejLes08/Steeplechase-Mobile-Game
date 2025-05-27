package sk.spsepo.lesko.steeplechasegame;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class UvodneMenu extends AppCompatActivity {

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_uvodne_menu);

        db = FirebaseFirestore.getInstance();

        setBackground();

        // Získaj UID zo SharedPreferences
        SharedPreferences prefs = getSharedPreferences("userdata", MODE_PRIVATE);
        String uid = prefs.getString("uid", null);

        // Zobraz meno používateľa z Firestore
        TextView usernameText = findViewById(R.id.usernameText);
        if (uid != null) {
            db.collection("users").document(uid)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        String username = documentSnapshot.getString("username");
                        usernameText.setText(username != null ? username : "Hráč");
                    })
                    .addOnFailureListener(e -> {
                        usernameText.setText("Hráč");
                        // Handle error (e.g., show toast)
                    });
        } else {
            usernameText.setText("Hráč");
        }
    }

    public void startGame(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void exitGame(View view) {
        finish();
    }

    public void showInfo(View view) {
        new AlertDialog.Builder(this)
                .setView(R.layout.custom_info_dialog)
                .setPositiveButton("EXIT", null)
                .show();
    }

    private void setBackground() {
        ImageView backgroundImage = findViewById(R.id.backgroundImage);
        backgroundImage.setImageResource(R.drawable.pozadie1);
    }
}