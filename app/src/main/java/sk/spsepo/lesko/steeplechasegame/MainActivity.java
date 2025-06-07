package sk.spsepo.lesko.steeplechasegame;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private GameView gameView;
    private GameEngine engine;
    private Horse horse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        horse = new Horse(this);
        Terrain terrain = new Terrain(this, "mapa1.json");
        engine = new GameEngine(horse, terrain, this);
        gameView = findViewById(R.id.game_view);
        gameView.setEngine(engine);

        findViewById(R.id.btn_add).setOnClickListener(v -> horse.addSpeed());
        findViewById(R.id.btn_reduce).setOnClickListener(v -> horse.reduceSpeed());
        findViewById(R.id.btn_start).setOnClickListener(v -> engine.startRace());

        // 👉 TLAČIDLO PAUSE
        findViewById(R.id.btn_pause).setOnClickListener(v -> {
            engine.setPaused(true); // okamžité zastavenie hry
            showPauseDialog();
        });

        gameView.post(() -> gameView.startLoop(this));

        LayoutInflater inflater = getLayoutInflater();
        View customActionBarView = inflater.inflate(R.layout.custom_action_bar, null);
        getSupportActionBar().setCustomView(customActionBarView);
        getSupportActionBar().setDisplayOptions(androidx.appcompat.app.ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);

        String uid = engine.uid;
        TextView usernameText = customActionBarView.findViewById(R.id.usernameText);
        if (uid != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
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
    }

    public void restartGame() {
        Horse newHorse = new Horse(this);
        Terrain newTerrain = new Terrain(this, "mapa1.json");
        GameEngine newEngine = new GameEngine(newHorse, newTerrain, this);

        this.engine = newEngine;
        this.horse = newHorse;

        gameView.setEngine(newEngine);
        gameView.stopLoop(); // zabezpečí ukončenie predchádzajúceho vlákna

        // ✅ Spustíme loop správne ako v onCreate — cez post()
        gameView.post(() -> gameView.startLoop(this));

        // ✅ Opätovné pripojenie listenerov k novým objektom
        findViewById(R.id.btn_add).setOnClickListener(v -> horse.addSpeed());
        findViewById(R.id.btn_reduce).setOnClickListener(v -> horse.reduceSpeed());
        findViewById(R.id.btn_start).setOnClickListener(v -> engine.startRace());
        findViewById(R.id.btn_pause).setOnClickListener(v -> {
            engine.setPaused(true);
            showPauseDialog();
        });
    }



    // 👉 METÓDA: Zobrazí pauzovacie dialógové okno
    private void showPauseDialog() {
        runOnUiThread(() -> {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Hra pozastavená")
                    .setMessage("Čo chceš urobiť?")
                    .setCancelable(false)
                    .setPositiveButton("Pokračovať", (dialog, which) -> engine.setPaused(false))
                    .setNegativeButton("Ukončiť", (dialog, which) -> finish())
                    .show();
        });
    }

    public void showFinishDialog(String time, String bestTime, boolean isNewRecord) {
        runOnUiThread(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_finish, null);

            TextView tvTitle = dialogView.findViewById(R.id.tv_title);
            TextView tvTime = dialogView.findViewById(R.id.tv_time);
            TextView tvRecord = dialogView.findViewById(R.id.tv_record);

            tvTitle.setText(isNewRecord ? "Nový rekord!" : "Dokončené!");
            tvTime.setText("Tvoj čas: " + time);
            tvRecord.setText("Rekord: " + bestTime);

            builder.setView(dialogView);
            builder.setCancelable(false);

            // 👉 Tu vytvoríme dialog pred definíciou listenerov
            AlertDialog dialog = builder.create();

            dialogView.findViewById(R.id.btn_restart).setOnClickListener(v -> {
                restartGame();
                dialog.dismiss();
            });

            dialogView.findViewById(R.id.btn_exit).setOnClickListener(v -> {
                finish();
            });

            dialog.show();
        });
    }


}
