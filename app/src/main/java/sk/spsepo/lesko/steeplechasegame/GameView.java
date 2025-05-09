package sk.spsepo.lesko.steeplechasegame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import sk.spsepo.lesko.steeplechasegame.GameEngine;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {
    private GameEngine engine;
    private Thread gameThread;
    private boolean running;
    private Paint textPaint = new Paint();
    private Paint barPaint = new Paint();
    private Paint borderPaint = new Paint();

    public GameView(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        getHolder().addCallback(this);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(48);
        barPaint.setColor(Color.GREEN);
        borderPaint.setColor(Color.BLACK);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(4);
    }

    public void setEngine(GameEngine engine) {
        this.engine = engine;
    }

    public void startLoop() {
        running = true;
        gameThread = new Thread(() -> {
            long last = System.nanoTime();
            while (running) {
                long now = System.nanoTime();
                double dt = (now - last) / 1_000_000_000.0;
                last = now;

                engine.update(dt);

                Canvas canvas = getHolder().lockCanvas();
                if (canvas != null) {
                    synchronized (getHolder()) {
                        drawGame(canvas);
                    }
                    getHolder().unlockCanvasAndPost(canvas);
                }
            }
        });
        gameThread.start();
    }

    private void drawGame(Canvas canvas) {
        // Clear background
        canvas.drawColor(Color.rgb(255, 198, 108)); // ORANGE

        // Draw terrain strips
        double offset = engine.getSpeed() * 0; // replace with actual posun logic if needed
        int segmentWidth = 62;
        int count = 15;
        double remaining = engine.getRemaining();
        String[] path = engine.getTerrainPath(); // add this method
        for (int i = 0; i < count; i++) {
            int index = i;
            if (index >= path.length) break;
            String type = path[index];
            int color;
            switch (type) {
                case "Napájadlo": color = Color.CYAN; break;
                case "Náročné pásmo": color = Color.DKGRAY; break;
                case "Šprintérske pásmo": color = Color.YELLOW; break;
                default: color = Color.rgb(160,82,45); break;
            }
            barPaint.setColor(color);
            int x = (int)(i * segmentWidth - offset % segmentWidth);
            canvas.drawRect(x, 220, x + segmentWidth, 420, barPaint);
            canvas.drawRect(x, 220, x + segmentWidth, 420, borderPaint);
        }

        // Draw horse sprite
        Bitmap horseFrame = engine.getHorse().getCurrentFrame();
        int hx = 70;
        int hy = 300;
        if (horseFrame != null) {
            canvas.drawBitmap(horseFrame, hx, hy, null);
        }

        // UI stats
        // Speed
        canvas.drawText("Rýchlosť: " + engine.getSpeed(), 20, 60, textPaint);
        // Stamina bar
        int barX = 20, barY = 80, barW = 300, barH = 30;
        int stamina = engine.getStamina();
        int fillW = barW * stamina / Horse.MAX_STAMINA;
        barPaint.setColor(stamina>66?Color.GREEN:stamina>33?Color.YELLOW:Color.RED);
        canvas.drawRect(barX, barY, barX + fillW, barY + barH, barPaint);
        canvas.drawRect(barX, barY, barX + barW, barY + barH, borderPaint);

        // Remaining meters centered
        String meters = (int)engine.getRemaining() + "m";
        float mw = textPaint.measureText(meters);
        canvas.drawText(meters, (getWidth()-mw)/2, 50, textPaint);

        // Time
        canvas.drawText(engine.getTimeString(), getWidth()-300, 60, textPaint);

        // Record and overload
        canvas.drawText("Rekord: " + engine.getBestRecord(), 580, 120, textPaint);
        canvas.drawText("Preťaženie: " + (int)engine.getOverloadPercent() + "%", 580, 160, textPaint);
    }

    @Override public void surfaceCreated(SurfaceHolder holder) {}
    @Override public void surfaceChanged(SurfaceHolder h, int f, int w, int ht) {}
    @Override public void surfaceDestroyed(SurfaceHolder h) { running = false; }
}
