package sk.spsepo.lesko.steeplechasegame;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import sk.spsepo.lesko.steeplechasegame.R;
import sk.spsepo.lesko.steeplechasegame.GameEngine;
import sk.spsepo.lesko.steeplechasegame.Horse;
import sk.spsepo.lesko.steeplechasegame.Terrain;

public class MainActivity extends AppCompatActivity {
    private GameView gameView;
    private GameEngine engine;
    private Horse horse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);  // R is the generated resource class

        // Initialize model
        horse = new Horse();
        Terrain terrain = new Terrain(this, "mapa1.json");
        engine = new GameEngine(horse, terrain);

        // Wire up the view
        gameView = findViewById(R.id.game_view);
        gameView.setEngine(engine);

        // Control buttons
        Button btnAdd    = findViewById(R.id.btn_add);
        Button btnReduce = findViewById(R.id.btn_reduce);
        Button btnStart  = findViewById(R.id.btn_start);
        Button btnCancel = findViewById(R.id.btn_cancel);

        btnAdd   .setOnClickListener(v -> horse.addSpeed());
        btnReduce.setOnClickListener(v -> horse.reduceSpeed());
        btnStart .setOnClickListener(v -> engine.startRace());
        btnCancel.setOnClickListener(v -> finish());  // finish() closes Activity

        // Kick off the game loop
        gameView.post(gameView::startLoop);
    }
}
