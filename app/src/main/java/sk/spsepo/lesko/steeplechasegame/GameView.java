package sk.spsepo.lesko.steeplechasegame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

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
                double dt = (now - last)/1_000_000_000.0;
                last = now;

                engine.update(dt, ctx);
                Canvas c = getHolder().lockCanvas();
                if (c != null) {
                    synchronized (getHolder()) {
                        drawGame(c);
                    }
                    getHolder().unlockCanvasAndPost(c);
                }
            }
        });
        gameThread.start();
    }

    private void drawGame(Canvas canvas) {
        // pozadie
        canvas.drawColor(Color.rgb(255,198,108));

        // pásy trate
        String[] path = engine.getTerrainPath();
        double offset = engine.getPathOffset();
        int w = 62, y1=220, y2=420;
        for(int i=0;i<path.length;i++){
            String t = path[i];
            int col=0;
            switch(t){
                case "Napájadlo": col=Color.CYAN; break;
                case "Náročné pásmo": col=Color.DKGRAY; break;
                case "Šprintérske pásmo": col=Color.YELLOW; break;
                default: col=Color.rgb(160,82,45);
            }
            barPaint.setColor(col);
            int x = (int)(i*w - offset%w);
            canvas.drawRect(x,y1,x+w,y2,barPaint);
            canvas.drawRect(x,y1,x+w,y2,borderPaint);
        }

        // kôň na mieste
        Bitmap horseBmp = engine.getHorse().getCurrentBitmap();
        if(horseBmp!=null){
            canvas.drawBitmap(horseBmp,70,300,null);
        }

        // UI: rýchlosť
        canvas.drawText("Rýchlosť: "+engine.getSpeed(),20,60,textPaint);

        // stamina bar
        int st = engine.getStamina();
        int barW=300, barH=30;
        int filled = barW*st/ Horse.MAX_STAMINA;
        barPaint.setColor(st>66?Color.GREEN:st>33?Color.YELLOW:Color.RED);
        canvas.drawRect(20,80,20+filled,80+barH,barPaint);
        canvas.drawRect(20,80,20+barW,80+barH,borderPaint);

        // metre
        String m = (int)engine.getRemaining()+"m";
        float mw=textPaint.measureText(m);
        canvas.drawText(m,(getWidth()-mw)/2,50,textPaint);

        // čas
        canvas.drawText(engine.getTimeString(),getWidth()-300,60,textPaint);

        // rekord a preťaženie
        canvas.drawText("Rekord: "+engine.getBestRecord(),580,120,textPaint);
        canvas.drawText("Preťaženie: "+(int)engine.getOverloadPercent()+"%",580,160,textPaint);

        // prípadné hlásky
        if(!engine.isRunning()){
            String msg = engine.isVictory()?"Vyhral si!":"Prehral si!";
            float tw=textPaint.measureText(msg);
            canvas.drawText(msg,(getWidth()-tw)/2, getHeight()/2,textPaint);
        }
    }

    @Override public void surfaceCreated(SurfaceHolder h){}
    @Override public void surfaceChanged(SurfaceHolder h,int f,int w,int ht){}
    @Override public void surfaceDestroyed(SurfaceHolder h){running=false;}
}
