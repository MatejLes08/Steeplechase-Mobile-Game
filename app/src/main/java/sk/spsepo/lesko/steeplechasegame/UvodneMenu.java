package sk.spsepo.lesko.steeplechasegame;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class UvodneMenu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uvodne_menu);

        // Set background (placeholder for your function)
        setBackground();
    }

    public void startGame(View view) {
        Intent intent = new Intent(this, MainActivity.class); // Replace GameActivity with MainActivity for now
        startActivity(intent);
        finish();
    }

    public void exitGame(View view) {
        finish();
    }

    public void showInfo(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Game Info");
        builder.setMessage("Staň sa rýchlym a odvážnym žrebcom! Ovládaj rýchlosť tlačidlami PRIDAJ A SPOMAĽ (o 4 km/h) až do maximálnej rýchlosti 60 km/h." +
                "V hre sú aj 3 pásma a napájadlá. Lúka - normálne pásmo, Šprintérske pásmo - zvyšuje rýchlosť, Náročné pásmo - skôr unaví koňa a Napájadlo - miesto, kde kôň rýchlejšie načerpe energiu. Je dôležité strážiť si ju, aby si kôň mohol udržať rýchlosť. Hra je na čas a najlepšie skóre sa ukladá!"); // Doplň vlastný text
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    private void setBackground() {
        // Placeholder for setting background image
        ImageView backgroundImage = findViewById(R.id.backgroundImage);
        backgroundImage.setImageResource(R.drawable.pozadie1);
    }
}