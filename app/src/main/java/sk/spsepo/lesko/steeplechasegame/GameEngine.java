package sk.spsepo.lesko.steeplechasegame;

import static sk.spsepo.lesko.steeplechasegame.Horse.TRACK_LENGTH;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.firestore.FirebaseFirestore;

public class GameEngine {
    private static final double TRACK = TRACK_LENGTH;
    private final Horse horse;
    private final Terrain terrain;
    private final FirebaseFirestore db;

    private double elapsedSec = 0;
    private int minutes = 0;
    private boolean running = false;
    private boolean victory = false;
    private boolean paused = false;

    // pre UI
    private double remaining = TRACK;
    private String timeString = "0:00:00";
    private String bestRecord = "N/A";
    private String currentType = "Cesta";
    private double distanceMeters = 0;

    private double pathOffset = 0;
    private double lastAccel = 1.0;

    String uid; // Firebase UID pre prihláseného používateľa

    public GameEngine(Horse horse, Terrain terrain, Context ctx) {
        this.horse = horse;
        this.terrain = terrain;
        this.db = FirebaseFirestore.getInstance();

        // získa UID z SharedPreferences (uložené po login/registrácii)
        SharedPreferences prefs = ctx.getSharedPreferences("userdata", Context.MODE_PRIVATE);
        uid = prefs.getString("uid", null);

        if (uid != null) {
            // Načíta najlepší čas z Firestore
            Utils.loadBestTime(uid, db, bestTime -> bestRecord = bestTime);
        }
    }

    public void startRace() {
        horse.reset();
        elapsedSec = 0;
        minutes = 0;
        running = true;
        victory = false;
        remaining = TRACK;
        pathOffset = 0;
    }

    public void update(double dt, Context ctx) {
        if (!running || paused) return;

        // čas
        elapsedSec += dt;
        if (elapsedSec >= 60) {
            elapsedSec -= 60;
            minutes++;
        }

        // logika trate a stamina
        Terrain.TerrainSegment seg = terrain.getSegment(remaining);
        lastAccel = seg.accel;
        horse.updateStamina(seg.rest, seg.accel, seg.difficulty, seg.bonus, dt);

        // stav koňa
        int spd = horse.getSpeed();
        remaining = horse.getRemaining();
        currentType = seg.type;

        // posun pozadia
        pathOffset += spd * dt * 11;

        // čas v formáte
        int hundredths = minutes * 6000
                + (int)elapsedSec * 100
                + (int)((elapsedSec - (int)elapsedSec) * 100);
        timeString = Utils.hundredthsToTime(hundredths);

        // dobeh do cieľa?
        if (remaining <= 0) {
            running = false;
            victory = true;

            if (uid != null) {
                String currentTime = timeString;
                Utils.saveBestTime(uid, currentTime, db, updatedBestTime -> {
                    if (updatedBestTime != null) {
                        bestRecord = updatedBestTime; // Okamžite aktualizuje bestRecord
                    }
                });
            }
        }
    }

    private boolean isFaster(String current, String previous) {
        // formát: M:SS:CC (napr. 1:23:45)
        String[] cur = current.split(":");
        String[] prev = previous.split(":");

        int curHundredths = Integer.parseInt(cur[0]) * 6000 + Integer.parseInt(cur[1]) * 100 + Integer.parseInt(cur[2]);
        int prevHundredths = Integer.parseInt(prev[0]) * 6000 + Integer.parseInt(prev[1]) * 100 + Integer.parseInt(prev[2]);

        return curHundredths < prevHundredths;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }


    // Gettery pre UI / GameView
    public boolean isRunning()         { return running; }
    public boolean isVictory()         { return victory; }
    public String getTimeString()      { return timeString; }
    public String getBestRecord()      { return bestRecord; }
    public int getSpeed()              { return horse.getSpeed(); }
    public double getStamina()         { return horse.getStamina(); }
    public double getOverloadPercent() { return horse.getOverload() * 100; }
    public String getCurrentTerrain()  { return currentType; }
    public double getPathOffset()      { return distanceMeters; }
    public Horse getHorse()            { return horse; }
    public double getRemaining()       { return remaining; }
    public double getDistanceMeters()  { return horse.getDistance(); }
    public String getUid()              {return uid; }
    public double getEffectiveSpeed() {
        return horse.getSpeed() * lastAccel;
    }

    public String[] getTerrainPath() {
        String[] path = new String[TRACK_LENGTH];
        for (int m = 0; m < TRACK_LENGTH; m++) {
            double rem = TRACK_LENGTH - m;
            Terrain.TerrainSegment seg = terrain.getSegment(rem);
            path[m] = seg.type;
        }
        return path;
    }
}