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

public class UvodneMenu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_uvodne_menu);

        setBackground();

        // Získaj meno používateľa zo SharedPreferences
        SharedPreferences prefs = getSharedPreferences("userdata", MODE_PRIVATE);
        String username = prefs.getString("username", "Hráč");

        // Zobraz ho v TextView
        TextView usernameText = findViewById(R.id.usernameText);
        usernameText.setText(username);
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
