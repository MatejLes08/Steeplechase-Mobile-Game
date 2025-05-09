package sk.spsepo.lesko.steeplechasegame;


public class GameEngine {
    private static final double TRACK = Horse.TRACK_LENGTH;
    private final Horse horse;
    private final Terrain terrain;

    private double elapsedSec = 0;
    private int minutes = 0;
    private boolean running = false;

    // pre UI
    private double remaining;
    private String timeString = "0:00:00";
    private String bestRecord = "N/A";
    private String currentType;

    public GameEngine(Horse horse, Terrain terrain) {
        this.horse = horse;
        this.terrain = terrain;
        this.remaining = TRACK;
    }

    public void startRace() {
        horse.reset();
        elapsedSec = 0;
        minutes = 0;
        running = true;
        remaining = TRACK;
    }

    /** Volaj túto metódu v hernom cykle, dt = sekundy od posledného volania. */
    public void update(double dt) {
        if (!running) return;

        elapsedSec += dt;
        if (elapsedSec >= 60) {
            elapsedSec -= 60;
            minutes++;
        }

        Terrain.TerrainSegment seg = terrain.getSegment(remaining);
        horse.updateStamina(seg.rest, seg.accel, seg.difficulty, seg.bonus, dt);

        double spd = horse.getSpeed();
        remaining = horse.getRemaining();
        currentType = seg.type;

        // priprav string času
        int hundredths = (int)(minutes * 6000 + (int)elapsedSec * 100 + (int)((elapsedSec - (int)elapsedSec)*100));
        timeString = Utils.hundredthsToTime(hundredths);

        // TODO: callback do UI: speed, remaining, stamina, timeString, overload*100

        if (horse.getDistance() >= TRACK) {
            running = false;
            // TODO: ukladať record, volať updateRecord
        }
    }

    // gettre pre UI
    public String getTimeString()     { return timeString; }
    public String getBestRecord()     { return bestRecord; }
    public double getRemaining()      { return remaining; }
    public int getSpeed()             { return horse.getSpeed(); }
    public int getStamina()           { return horse.getStamina(); }
    public double getOverloadPercent(){ return horse.getOverload() * 100; }
    public String getCurrentTerrain() { return currentType; }
    public boolean isRunning()        { return running; }
}
