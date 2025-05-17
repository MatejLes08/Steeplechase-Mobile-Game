package sk.spsepo.lesko.steeplechasegame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Horse {
    public static final int TRACK_LENGTH = 2000;
    public static final int MAX_STAMINA = 100;
    public static final int MAX_SPEED = 60;
    public static final int BEH_DIFFICULTY_OFFSET = 2000;
    public static final int SPRINT_DIFFICULTY_OFFSET = 3000;

    private int speed = 0;
    private int stamina = MAX_STAMINA;
    private double fatigue = 0;
    private double distanceTravelled = 0;
    private double remaining = TRACK_LENGTH;
    private double overload = 0;

    // animácie
    private List<Bitmap> runFrames = new ArrayList<>();
    private List<Bitmap> walkFrames = new ArrayList<>();
    private List<Bitmap> idleFrames = new ArrayList<>();

    private double frameIndex = 0;
    private double animationSpeed = 0.1;

    public Horse(Context ctx) {
        loadSpriteSheet(ctx);  // upravíme na decodeResource
    }

    private void loadSpriteSheet(Context ctx) {
        // Bežanie – horse_run.png
        Bitmap sheetRun = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.horse_run);
        int frameWRun = sheetRun.getHeight();
        int countRun = sheetRun.getWidth() / frameWRun;
        for (int i = 0; i < countRun; i++) {
            Bitmap frame = Bitmap.createBitmap(sheetRun, i * frameWRun, 0, frameWRun, frameWRun);
            runFrames.add(Bitmap.createScaledBitmap(frame, frameWRun * 2, frameWRun * 2, false));
        }

        // Chôdza – horse_walk.png
        Bitmap sheetWalk = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.horse_walk);
        int frameWWalk = sheetWalk.getHeight();
        int countWalk = sheetWalk.getWidth() / frameWWalk;
        for (int i = 0; i < countWalk; i++) {
            Bitmap frame = Bitmap.createBitmap(sheetWalk, i * frameWWalk, 0, frameWWalk, frameWWalk);
            walkFrames.add(Bitmap.createScaledBitmap(frame, frameWWalk * 2, frameWWalk * 2, false));
        }

        // Státie – horse_idle.png
        Bitmap sheetIdle = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.horse_idle);
        int frameWIdle = sheetIdle.getHeight();
        int countIdle = sheetIdle.getWidth() / frameWIdle;
        for (int i = 0; i < countIdle; i++) {
            Bitmap frame = Bitmap.createBitmap(sheetIdle, i * frameWIdle, 0, frameWIdle, frameWIdle);
            idleFrames.add(Bitmap.createScaledBitmap(frame, frameWIdle * 2, frameWIdle * 2, false));
        }
    }


    public void reset() {
        speed = 0;
        stamina = MAX_STAMINA;
        fatigue = 0;
        distanceTravelled = 0;
        remaining = TRACK_LENGTH;
        overload = 0;
        frameIndex = 0;
    }

    public void addSpeed() {
        if (stamina > 0 && speed < MAX_SPEED) {
            speed = Math.min(MAX_SPEED, speed + 4);
        }
    }

    public void reduceSpeed() {
        speed = Math.max(0, speed - 4);
    }

    public void updateStamina(double rest, double accel, double difficulty, double bonus, double dt) {
        if (speed == 0) {
            overload = (stamina < MAX_STAMINA) ? -rest * 2 * bonus : 0;
        } else if (stamina <= 0) {
            overload = 0;
            speed = Math.max(0, speed - 4);
        } else if (speed <= 12) {
            overload = (stamina < MAX_STAMINA) ? -rest : 0;
        } else if (speed <= 24) {
            overload = speed / difficulty;
        } else if (speed < 50) {
            overload = speed / (difficulty - BEH_DIFFICULTY_OFFSET);
        } else {
            overload = speed / (difficulty - SPRINT_DIFFICULTY_OFFSET);
        }

        fatigue += overload * dt;
        if (fatigue < 0) fatigue = 0;

        stamina = MAX_STAMINA - (int)fatigue;
        stamina = Math.max(0, Math.min(MAX_STAMINA, stamina));

        distanceTravelled += speed * accel / 3.6 * dt;
        remaining = TRACK_LENGTH - distanceTravelled;
        if (remaining < 0) remaining = 0;
    }

    public void updateAnimation(double dt) {
        frameIndex += animationSpeed * dt;
        List<Bitmap> frames;
        if (speed <= 0) {
            frames = idleFrames;
            animationSpeed = 0.1;
        } else if (speed <= 12) {
            frames = walkFrames;
            animationSpeed = 0.055 * speed / 24 * walkFrames.size();
        } else {
            frames = runFrames;
            animationSpeed = 0.001 * speed * runFrames.size();
        }
        if (frames.isEmpty()) return;
        if (frameIndex >= frames.size()) frameIndex = 0;
    }

    /**
     * Vráti aktuálny frame bitmapy pre vykreslenie.
     */
    public Bitmap getCurrentBitmap() {
        List<Bitmap> frames;
        if (speed <= 0) frames = idleFrames;
        else if (speed <= 12) frames = walkFrames;
        else frames = runFrames;
        if (frames.isEmpty()) return null;
        int idx = (int)frameIndex;
        return frames.get(idx);
    }

    // getters pre UI
    public int getSpeed() { return speed; }
    public int getStamina() { return stamina; }
    public double getDistance() { return distanceTravelled; }
    public int getRemaining() { return (int)Math.round(remaining); }
    public double getOverload() { return overload; }
}