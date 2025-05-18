package sk.spsepo.lesko.steeplechasegame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Locale;

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
        barPaint.setStyle(Paint.Style.FILL);
        borderPaint.setColor(Color.BLACK);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(4);
    }

    public void setEngine(GameEngine engine) {
        this.engine = engine;
    }

    public void startLoop(Context ctx) {
        running = true;
        gameThread = new Thread(() -> {
            long last = System.nanoTime();
            while (running) {
                long now = System.nanoTime();
                double dt = (now - last) / 1_000_000_000.0;
                last = now;

                engine.update(dt, ctx);

                // **Plynulá animácia**
                engine.getHorse().updateAnimation(dt);

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
        // pozadie
        canvas.drawColor(Color.rgb(255,198,108));

        // pásy trate
        String[] path = engine.getTerrainPath();    // celé 2000 prvkov
        double offset = engine.getPathOffset();
        int w = 100;
        int visible = getWidth() / w + 2;            // koľko pásikov vidíme + rezerva
        int start = (int)(offset / w);               // index prvej viditeľnej
        int x0 = - (int)(offset % w);                // začiatok posunu

        for (int i = 0; i < visible; i++) {
            int idx = start + i;
            if (idx < 0 || idx >= path.length) continue;
            String t = path[idx];
            int col;
            switch (t) {
                case "Napájadlo":      col = Color.CYAN;   break;
                case "Náročné pásmo":  col = Color.DKGRAY; break;
                case "Šprintérske pásmo": col = Color.YELLOW; break;
                default:               col = Color.rgb(160,82,45); break;
            }
            barPaint.setColor(col);
            int x = x0 + i * w;
            canvas.drawRect(x, 220, x + w, 420, barPaint);
            canvas.drawRect(x, 220, x + w, 420, borderPaint);
        }

        // kôň na mieste
        Bitmap horseBmp = engine.getHorse().getCurrentBitmap();
        if(horseBmp!=null){
            canvas.drawBitmap(horseBmp,70,300,null);
        }

        // UI: rýchlosť
        int eff = (int) Math.round(engine.getEffectiveSpeed());
        canvas.drawText("Rýchlosť: " + eff + " km/h", 20, 60, textPaint);

        // terén
        canvas.drawText("Terén: " + engine.getCurrentTerrain(), 20, 110, textPaint);                  // posuňte podľa potreby



        // stamina bar
        int st = engine.getStamina();
        int barX = 20, barY = 130, barW = 300, barH = 30;
        int filled = barW*st/ Horse.MAX_STAMINA;
        barPaint.setColor(st > 66 ? Color.GREEN : st > 33 ? Color.YELLOW : Color.RED);
        canvas.drawRect(barX, barY, barX + filled, barY + barH, barPaint);
        canvas.drawRect(barX, barY, barX + barW, barY + barH, borderPaint);

        // stamina vo vnútri baru
        String percent = st + "%";
        float tw = textPaint.measureText(percent);
        // zoraď na stred baru
        float tx = barX + (barW - tw) / 2;
        float ty = barY + barH - 6; // doladené vertikálne zarovnanie
        canvas.drawText(percent, tx, ty, textPaint);

        // metre
        String m = (int)engine.getRemaining()+"m";
        float mw=textPaint.measureText(m);
        canvas.drawText(m,(getWidth()-mw)/2,50,textPaint);

        // čas
        canvas.drawText(engine.getTimeString(),getWidth()-300,60,textPaint);

        // rekord
        canvas.drawText("Rekord: "+engine.getBestRecord(),580,120,textPaint);

        // preťaženie
        double overload = engine.getHorse().getOverload();        // napr. 0.012345
        String txt = String.format(Locale.US, "Preťaženie: %.2f", overload);
        canvas.drawText(txt, 580, 160, textPaint);


    }

    @Override public void surfaceCreated(SurfaceHolder h){}
    @Override public void surfaceChanged(SurfaceHolder h,int f,int w,int ht){}
    @Override public void surfaceDestroyed(SurfaceHolder h){running=false;}
}
