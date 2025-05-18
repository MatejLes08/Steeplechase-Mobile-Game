package sk.spsepo.lesko.steeplechasegame;

import static sk.spsepo.lesko.steeplechasegame.Horse.TRACK_LENGTH;

import android.content.Context;

public class GameEngine {
    private static final double TRACK = TRACK_LENGTH;
    private final Horse horse;
    private final Terrain terrain;

    private double elapsedSec = 0;
    private int minutes = 0;
    private boolean running = false;
    private boolean victory = false;

    // pre UI
    private double remaining = TRACK;
    private String timeString = "0:00:00";
    private String bestRecord = "N/A";
    private String currentType = "Cesta";

    private double pathOffset = 0;
    private double lastAccel = 1.0;

    public GameEngine(Horse horse, Terrain terrain, Context ctx) {
        this.horse = horse;
        this.terrain = terrain;

        // načítaj rekord zo SharedPreferences
        bestRecord = Utils.loadBestTime(ctx);
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
        if (!running) return;

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
            Utils.saveBestTime(ctx, timeString);
            bestRecord = timeString;

        }
    }

    // Gettre pre UI / GameView
    public boolean isRunning()         { return running; }
    public boolean isVictory()         { return victory; }
    public String getTimeString()      { return timeString; }
    public String getBestRecord()      { return bestRecord; }
    public int getSpeed()              { return horse.getSpeed(); }
    public int getStamina()            { return horse.getStamina(); }
    public double getOverloadPercent() { return horse.getOverload() * 100; }
    public String getCurrentTerrain()  { return currentType; }
    public double getPathOffset()      { return pathOffset; }
    public Horse getHorse()            { return horse; }
    public double getRemaining()       { return remaining; }

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
