package sk.spsepo.lesko.steeplechasegame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import java.util.ArrayList;
import java.util.List;

public class Horse {
    public static final int TRACK_LENGTH = 2000;
    public static final double MAX_STAMINA = 100.0;
    public static final int MAX_SPEED = 60;
    public static final int BEH_DIFFICULTY_OFFSET = 2000;
    public static final int SPRINT_DIFFICULTY_OFFSET = 3000;

    private int speed = 0;
    private double stamina = MAX_STAMINA;
    private double distanceTravelled = 0;
    private double remaining = TRACK_LENGTH;
    private double overload = 0;

    // Animácie
    private final List<Bitmap> runFrames = new ArrayList<>();
    private final List<Bitmap> walkFrames = new ArrayList<>();
    private final List<Bitmap> idleFrames = new ArrayList<>();

    private double frameIndex = 0;
    private double animationSpeed = 0.1;
    private Bitmap currentBitmap;

    public Horse(Context ctx) {
        loadSpriteSheet(ctx);
        if (!idleFrames.isEmpty()) {
            currentBitmap = idleFrames.get(0);
        }
    }

    private void loadSpriteSheet(Context ctx) {
        loadFrameSequence(ctx, "horse_run_", 6, runFrames);
        loadFrameSequence(ctx, "horse_walk_", 8, walkFrames);
        loadFrameSequence(ctx, "horse_idle_", 13, idleFrames);
    }

    private void loadFrameSequence(Context ctx, String baseName, int count, List<Bitmap> frameList) {
        for (int i = 0; i < count; i++) {
            String frameName = baseName + i;
            int resId = ctx.getResources().getIdentifier(frameName, "drawable", ctx.getPackageName());
            if (resId != 0) {
                Bitmap frame = BitmapFactory.decodeResource(ctx.getResources(), resId);
                Bitmap scaled = Bitmap.createScaledBitmap(frame, frame.getWidth()*2, frame.getHeight()*2, true);
                frameList.add(scaled);
            }
        }
    }

    public void reset() {
        speed = 0;
        stamina = MAX_STAMINA;
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

    /**
     * Aktualizuje stamina a prejdenú vzdialenosť.
     * @param rest odpočinkový koeficient
     * @param accel zrýchlenie trate
     * @param difficulty náročnosť trate
     * @param bonus bonus z napájačky
     * @param dt čas od poslednej aktualizácie v sekundách
     */
    public void updateStamina(double rest, double accel, double difficulty, double bonus, double dt) {
        // vypočítaj overload (únavu za sekundu)
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

        stamina -= overload * 100.0 * dt;  //100

        // hranice
        if (stamina > MAX_STAMINA) stamina = MAX_STAMINA;
        if (stamina < 0) stamina = 0;


        // prejdená vzdialenosť v metroch
        distanceTravelled += speed * accel / 3.6 * dt;
        remaining = TRACK_LENGTH - distanceTravelled;
        if (remaining < 0) remaining = 0;

        System.out.println("DT: " + dt);
        System.out.println("Zrýchlenie: " + accel);
        System.out.println("Preťaženie: " + overload);
    }

    public void updateAnimation(double dt) {
        List<Bitmap> frames;
        if (speed <= 0) {
            frames = idleFrames;
            animationSpeed = 3;
        } else if (speed <= 12) {
            frames = walkFrames;
            animationSpeed = 1 * speed;
        } else {
            frames = runFrames;
            animationSpeed = 0.5 * speed;
        }
        if (frames.isEmpty()) return;

        frameIndex = (frameIndex + animationSpeed * dt) % frames.size();
        currentBitmap = frames.get((int)frameIndex);
    }

    // getters pre UI
    public int getSpeed() { return speed; }
    public double getStamina() { return stamina; }
    public double getDistance() { return distanceTravelled; }
    public int getRemaining() { return (int)Math.round(remaining); }
    public double getOverload() { return overload; }
    public Bitmap getCurrentBitmap() { return currentBitmap; }
}
