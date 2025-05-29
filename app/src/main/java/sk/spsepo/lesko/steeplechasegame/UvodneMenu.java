package sk.spsepo.lesko.steeplechasegame;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
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
        // Nastaví orientáciu na šírku
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_uvodne_menu);

        // Inicializuje Firestore
        db = FirebaseFirestore.getInstance();

        // Nastaví vlastný layout do action baru
        LayoutInflater inflater = getLayoutInflater();
        View customActionBarView = inflater.inflate(R.layout.custom_action_bar, null);
        getSupportActionBar().setCustomView(customActionBarView);
        getSupportActionBar().setDisplayOptions(androidx.appcompat.app.ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);

        // Získa UID a načíta meno do action baru
        SharedPreferences prefs = getSharedPreferences("userdata", MODE_PRIVATE);
        String uid = prefs.getString("uid", null);
        TextView usernameText = customActionBarView.findViewById(R.id.usernameText);
        if (uid != null) {
            db.collection("users").document(uid)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        String username = documentSnapshot.getString("username");
                        usernameText.setText(username != null ? username : "Hráč");
                    })
                    .addOnFailureListener(e -> {
                        usernameText.setText("Hráč");
                    });
        } else {
            usernameText.setText("Hráč");
        }

        // Nastaví pozadie
        setBackground();
    }

    public void startGame(View view) {
        // Spustí hru a ukončí túto aktivitu
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void exitGame(View view) {
        finish();
    }

    public void showInfo(View view) {
        // Zobrazí informačný dialóg
        new AlertDialog.Builder(this)
                .setView(R.layout.custom_info_dialog)
                .setPositiveButton("EXIT", null)
                .show();
    }

    private void setBackground() {
        // Nastaví obrázok pozadia
        ImageView backgroundImage = findViewById(R.id.backgroundImage);
        backgroundImage.setImageResource(R.drawable.pozadie1);
    }
}