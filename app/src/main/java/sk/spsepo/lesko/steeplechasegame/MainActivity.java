package sk.spsepo.lesko.steeplechasegame;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

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
    }
}
