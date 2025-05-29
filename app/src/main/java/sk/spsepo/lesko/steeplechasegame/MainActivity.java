package sk.spsepo.lesko.steeplechasegame;

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

        // 1) Inicializácia Horse s Contextom kvôli drawable
        horse = new Horse(this);

        // 2) Inicializácia terénu a engine
        Terrain terrain = new Terrain(this, "mapa1.json");
        engine = new GameEngine(horse, terrain, this);  // pridali sme Context

        // 3) Zobrazenie najlepšieho času (už sa načítava v GameEngine z Utils)
        gameView = findViewById(R.id.game_view);
        gameView.setEngine(engine);

        // 4) Ovládacie tlačidlá
        findViewById(R.id.btn_add).setOnClickListener(v -> horse.addSpeed());
        findViewById(R.id.btn_reduce).setOnClickListener(v -> horse.reduceSpeed());
        findViewById(R.id.btn_start).setOnClickListener(v -> engine.startRace());
        findViewById(R.id.btn_cancel).setOnClickListener(v -> finish());

        // 5) Spustenie hernej slučky s Contextom
        gameView.post(() -> gameView.startLoop(this));  // odovzdaj Context do loopu

        // 6) Nastavenie vlastného action baru
        LayoutInflater inflater = getLayoutInflater();
        View customActionBarView = inflater.inflate(R.layout.custom_action_bar, null);
        getSupportActionBar().setCustomView(customActionBarView);
        getSupportActionBar().setDisplayOptions(androidx.appcompat.app.ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);

        // 7) Získa UID a načíta meno do action baru
        String uid = engine.uid; // UID je už načítané v GameEngine
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
}