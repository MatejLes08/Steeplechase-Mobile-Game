package sk.spsepo.lesko.steeplechasegame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import java.util.Locale;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {
    private volatile GameEngine engine;
    private Thread gameThread;
    private volatile boolean running;
    private Bitmap scaledBackground;

    // Paint pre texty
    private Paint paintLeft   = new Paint();
    private Paint paintCenter = new Paint();
    private Paint paintRight  = new Paint();

    // Pozadie
    private Bitmap background;

    // Terén
    private final Map<String, Bitmap> scaledBitmapCache = new HashMap<>();
    private Bitmap[] bmpCesta   = new Bitmap[3];
    private Bitmap[] bmpSprint  = new Bitmap[3];
    private Bitmap[] bmpNarocne = new Bitmap[3];
    private Bitmap[] bmpNapiadlo = new Bitmap[3];

    // Konštanty trate
    private static final int PATH_TOP    = 400;   // horný okraj trate
    private static final int PATH_HEIGHT = 250;   // výška pásikov
    private static final int HORSE_X     = 100;   // X pozícia koňa
    private static final int PIXELS_PER_METER = 100;

    public GameView(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        getHolder().addCallback(this);

        // --- Načítanie fontov a farieb ---
        paintLeft.setColor(Color.BLACK);
        paintLeft.setTextSize(52);
        paintCenter.setColor(Color.BLACK);
        paintCenter.setTextSize(72);
        paintRight.setColor(Color.BLACK);
        paintRight.setTextSize(52);

        // --- Načítanie obrázkov ---
        background = BitmapFactory.decodeResource(getResources(), R.drawable.pozadie);

        loadTerrainBitmaps(ctx);
    }

    private void loadTerrainBitmaps(Context ctx) {
        for (int i = 0; i < 3; i++) {
            bmpCesta[i]   = BitmapFactory.decodeResource(getResources(),
                    getResources().getIdentifier("cesta" + i, "drawable", ctx.getPackageName()));
            bmpSprint[i]  = BitmapFactory.decodeResource(getResources(),
                    getResources().getIdentifier("sprinterske" + i, "drawable", ctx.getPackageName()));
            bmpNarocne[i] = BitmapFactory.decodeResource(getResources(),
                    getResources().getIdentifier("narocne" + i, "drawable", ctx.getPackageName()));
            bmpNapiadlo[i] = BitmapFactory.decodeResource(getResources(),
                    getResources().getIdentifier("napajadlo" + i, "drawable", ctx.getPackageName()));
        }
        preloadScaledBitmaps();
    }

    private void preloadScaledBitmaps() {
        int width = PIXELS_PER_METER;
        int height = PATH_HEIGHT;

        for (int i = 0; i < 3; i++) {
            getScaledBitmap(bmpCesta[i], width, height, "cesta" + i);
            getScaledBitmap(bmpSprint[i], width, height, "sprinterske" + i);
            getScaledBitmap(bmpNarocne[i], width, height, "narocne" + i);
            getScaledBitmap(bmpNapiadlo[i], width, height, "napajadlo" + i);
        }
    }

    // Pomocná metóda na získanie škálovaného obrázka s kešovaním
    private Bitmap getScaledBitmap(Bitmap original, int width, int height, String cacheKey) {
        String key = cacheKey + "_" + width + "x" + height;
        Bitmap cached = scaledBitmapCache.get(key);
        if (cached != null) {
            return cached;
        }
        Bitmap scaled = Bitmap.createScaledBitmap(original, width, height, true);
        scaledBitmapCache.put(key, scaled);
        return scaled;
    }





    public void setEngine(GameEngine engine) {
        this.engine = engine;
    }

    public void startLoop(Context ctx) {
        running = true;
        gameThread = new Thread(() -> {
            final int targetFPS = 16;
            final long targetFrameTimeNanos = 1_000_000_000 / targetFPS;

            long last = System.nanoTime();

            while (running) {
                long startTime = System.nanoTime();
                double dt = (startTime - last) / 1_000_000_000.0;
                last = startTime;


                engine.update(dt, ctx);
                engine.getHorse().updateAnimation(dt);

                if (engine.isVictory() && running) {
                    running = false;

                    if (ctx instanceof MainActivity) {
                        MainActivity activity = (MainActivity) ctx;

                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        String uid = engine.getUid();
                        String finalTime = engine.getTimeString();

                        Utils.saveBestTime(uid, finalTime, db, new Utils.OnBestTimeSavedListener() {
                            @Override
                            public void onBestTimeSaved(String bestTime) {
                                boolean isNewRecord = false;
                                if (bestTime != null) {
                                    isNewRecord = Utils.timeToHundredths(finalTime) <= Utils.timeToHundredths(bestTime);
                                }

                                boolean finalIsNewRecord = isNewRecord;
                                activity.runOnUiThread(() -> {
                                    activity.showFinishDialog(finalTime, bestTime != null ? bestTime : "N/A", finalIsNewRecord);
                                });
                            }
                        });
                    }

                }


                Canvas canvas = getHolder().lockCanvas();
                if (canvas != null) {
                    synchronized (getHolder()) {
                        drawGame(canvas);
                    }
                    getHolder().unlockCanvasAndPost(canvas);
                }

                // FPS limiter (spánok, ak ideš rýchlejšie než 48 FPS)
                long frameDuration = System.nanoTime() - startTime;
                long sleepTime = targetFrameTimeNanos - frameDuration;
                if (sleepTime > 0) {
                    try {
                        Thread.sleep(sleepTime / 1_000_000, (int)(sleepTime % 1_000_000));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        });
        gameThread.start();
    }

    private void drawGame(Canvas canvas) {
        // Vykresli celé pozadie natiahnuté na veľkosť obrazovky
        if (scaledBackground != null) {
            canvas.drawBitmap(scaledBackground, 0, 0, null);
        }

        double trackLength = engine.getHorse().getTrackLength(); // v metroch

        // 2. Získaj pozíciu koňa (v metroch)
        double horseDistance = engine.getHorse().getDistance(); // v metroch

        // 3. Výpočet scrollX – horizontálny posun podľa vzdialenosti
        int backgroundWidth = background.getWidth(); // napr. 5000px
        int screenWidth = getWidth();

        // maxScroll = max počet pixelov, ktoré môžeš posunúť
        int maxScrollX = backgroundWidth - screenWidth;

        // koľko percent trate sme prešli
        double progress = horseDistance / trackLength;

        // pozícia pozadia: od 0 (začiatok) po maxScrollX (koniec)
        int scrollX = (int) (progress * maxScrollX);

        // 4. Vykresli výrez z pozadia
        Rect src = new Rect(scrollX, 0, scrollX + screenWidth, background.getHeight());
        Rect dst = new Rect(0, 0, screenWidth, getHeight());

        canvas.drawBitmap(background, src, dst, null);


        // Vypočítaj offset pásikov
        double offsetMeters = engine.getDistanceMeters() - 3.0;
        float offsetPx = (float)(offsetMeters * PIXELS_PER_METER);

        String[] path = engine.getTerrainPath();
        int visible = getWidth() / PIXELS_PER_METER + 2;
        int start   = (int)(offsetPx / PIXELS_PER_METER);
        float x0    = - (offsetPx % PIXELS_PER_METER);

        // Vykresli pásiky trate ako obrázky
        for (int i = 0; i < visible; i++) {
            int idx = start + i;
            if (idx < 0 || idx >= path.length) continue;

            // škáluj presne na meter x PATH_HEIGHT
            Bitmap tile;
            String terrain = path[idx];
            String cacheKey;
            switch (terrain) {
                case "Napájadlo":
                    tile = bmpNapiadlo[idx % 3];
                    cacheKey = "napajadlo" + (idx % 3);
                    break;
                case "Šprintérske pásmo":
                    tile = bmpSprint[idx % 3];
                    cacheKey = "sprinterske" + (idx % 3);
                    break;
                case "Náročné pásmo":
                    tile = bmpNarocne[idx % 3];
                    cacheKey = "narocne" + (idx % 3);
                    break;
                default:
                    tile = bmpCesta[idx % 3];
                    cacheKey = "cesta" + (idx % 3);
                    break;
            }

            Bitmap scaled = getScaledBitmap(tile, PIXELS_PER_METER, PATH_HEIGHT, cacheKey);
            float x = x0 + i * PIXELS_PER_METER;

            canvas.drawBitmap(scaled, x, PATH_TOP, null);
        }

        // Vykresli koňa uprostred trate
        Bitmap horseBmp = engine.getHorse().getCurrentBitmap();
        if (horseBmp != null) {
            int horseW = horseBmp.getWidth();
            int horseH = horseBmp.getHeight();
            int horseY = PATH_TOP + (PATH_HEIGHT - horseH) / 2 - 30; //posunie koňa trošku vyššie
            canvas.drawBitmap(horseBmp, HORSE_X, horseY, null);
        }

        // 5) UI texty – tri stĺpce
        // **ľavý stĺpec** (aligned left)
        float xL = 30;
        float y = 80;
        canvas.drawText("Rýchlosť: " + Math.round(engine.getEffectiveSpeed()) + " km/h", xL, y, paintLeft);
        y += 40;
        // energia bar s percentom
        int st = (int) engine.getStamina();
        int barW = 300, barH = 40;
        float barX = xL, barY = y;
        // pozadie baru
        paintLeft.setStyle(Paint.Style.STROKE);
        paintLeft.setStrokeWidth(4);
        canvas.drawRect(barX, barY, barX + barW, barY + barH, paintLeft);
        paintLeft.setStyle(Paint.Style.FILL);
        paintLeft.setColor(st>66?Color.GREEN:st>33?Color.YELLOW:Color.RED);
        canvas.drawRect(barX, barY, barX + barW * st / (float) Horse.MAX_STAMINA, barY + barH, paintLeft);
        // percent text
        paintLeft.setColor(Color.BLACK);
        canvas.drawText(st + "%", barX + barW/2 - paintLeft.measureText(st + "%")/2, barY + barH - 8, paintLeft);
        y += 80;
        canvas.drawText(
                String.format(Locale.US, "Preťaženie: %.2f%%", engine.getOverloadPercent()),
                xL, y, paintLeft
        );


        // **stredný stĺpec** (centered)
        float xC = getWidth()/2f;
        float yc = 80;
        String metres = (int)engine.getRemaining() + " m";
        paintCenter.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(metres, xC, yc, paintCenter);
        yc += 80;
        canvas.drawText(engine.getCurrentTerrain(), xC, yc, paintCenter);

        // **pravý stĺpec** (aligned right)
        paintRight.setTextAlign(Paint.Align.RIGHT);
        float xR = getWidth() - 30;
        float yr = 80;
        canvas.drawText("Rekord: " + engine.getBestRecord(), xR, yr, paintRight);
        yr += 60;
        canvas.drawText("Čas: " + engine.getTimeString(), xR, yr, paintRight);
    }

    public void stopLoop() {
        running = false;
        try {
            if (gameThread != null) {
                gameThread.join();  // čaká, kým sa thread bezpečne ukončí
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (background != null) {
            scaledBackground = Bitmap.createScaledBitmap(background, getWidth(), getHeight(), true);
        }
    }

    @Override public void surfaceChanged(SurfaceHolder h,int f,int w,int ht) {}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        running = false;
        try {
            if (gameThread != null) gameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
