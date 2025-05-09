package sk.spsepo.lesko.steeplechasegame;

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

    // Animácia
    private double frameIndex = 0;
    private double animationSpeed = 0.1;
    private String currentFrameSet = "idle"; // idle, walk, run
    private String imageName = "Horse_Idle_0"; // Zástupná hodnota za aktuálny obrázok

    public void addSpeed() {
        if (stamina > 0 && speed < MAX_SPEED) {
            speed = Math.min(MAX_SPEED, speed + 4);
        }
    }

    public void reduceSpeed() {
        speed = Math.max(0, speed - 4);
    }

    public void updateStamina(double rest, double accel, double difficulty, double bonus, double deltaTime) {
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

        fatigue += overload * deltaTime;
        if (fatigue < 0) fatigue = 0;

        stamina = MAX_STAMINA - (int)fatigue;
        stamina = Math.max(0, Math.min(MAX_STAMINA, stamina));

        distanceTravelled += speed * accel / 3.6 * deltaTime;
        remaining = TRACK_LENGTH - distanceTravelled;
        if (remaining < 0) remaining = 0;
    }

    public void updateAnimation(double deltaTime) {
        frameIndex += animationSpeed * deltaTime;

        if (speed <= 0) {
            currentFrameSet = "idle";
            animationSpeed = 0.1;
        } else if (speed <= 12) {
            currentFrameSet = "walk";
            animationSpeed = 0.055 * speed / 24 * 10; // 10 = počet snímok (príklad)
        } else {
            currentFrameSet = "run";
            animationSpeed = 0.001 * speed * 10; // 10 = počet snímok (príklad)
        }

        if (frameIndex >= 10) frameIndex = 0; // 10 = počet snímok

        imageName = "Horse_" + currentFrameSet + "_" + (int)frameIndex;
    }

    // Gettre pre UI
    public int getSpeed() { return speed; }
    public int getStamina() { return stamina; }
    public double getDistance() { return distanceTravelled; }
    public int getRemaining() { return (int)Math.round(remaining); }
    public double getOverload() { return overload; }

    // Získanie názvu obrázka (frame) pre vykreslenie
    public String getImageName() { return imageName; }
}
