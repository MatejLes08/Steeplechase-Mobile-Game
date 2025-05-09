package sk.spsepo.lesko.steeplechasegame;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private final int[] animaciaKona = {
            R.drawable.kon1, R.drawable.kon2, R.drawable.kon3,
            R.drawable.kon4, R.drawable.kon5, R.drawable.kon6,
            R.drawable.kon7, R.drawable.kon8, R.drawable.kon9,
            R.drawable.kon10, R.drawable.kon11, R.drawable.kon12, R.drawable.kon13
    };
    private final int[] pozadia = {
            R.drawable.pozadie1, R.drawable.pozadie2, R.drawable.pozadie3
    };

    private ImageView imageView;
    private ImageView backgroundImage;
    private Handler handler = new Handler();
    private int frameIndex = 0;
    private boolean animaciaBezi = false;

    private final int DRAHA = 2000;
    private final int KON_MAX_RYCHLOST = 60;
    private final int KON_VYDRZ = 100;
    private double prejdeneMetre = 0;
    private int konRychlost = 0;
    private double zataz = 0;
    private int sila = 100;
    private boolean stavHry = true;


    private double cas = 0;
    private int minuty = 0;

    private SharedPreferences settings;
    private String najkratsi;


    //Generácia terénu
    Random random = new Random();
    private int a = random.nextInt(600) + 400;
    private int b = random.nextInt(400) + 1400;

    private int [] udalost = {a, b};
    private int c = random.nextInt(2);
    private int mnp = udalost[c];
    private int msp = udalost[1 - c];

    int n1 = random.nextInt(400) + 1600;
    int n2 = random.nextInt(780) + 800;
    int n3 = random.nextInt(780) + 20;




    private TextView rychlost;
    private TextView neprejdenych;
    private TextView energia;
    private TextView aktualnaDraha;
    private TextView stopky;
    private TextView rekord;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settings = getSharedPreferences("GAME_DATA", Context.MODE_PRIVATE);

        rychlost = findViewById(R.id.textRychlost);
        neprejdenych = findViewById(R.id.textOstava);
        energia = findViewById(R.id.textEnergia);
        aktualnaDraha = findViewById(R.id.textTeren);
        stopky = findViewById(R.id.textCas);
        rekord = findViewById(R.id.textRekord);
        imageView = findViewById(R.id.obrKon);
        backgroundImage = findViewById(R.id.pozadie);

        stavHry = true;
        nacitatStavHry();

        System.out.println(stavHry);
        System.out.println(prejdeneMetre);

        if (prejdeneMetre > 1999) {
            vymazSharedPreferences();
            nacitatStavHry();
        }

        //-vymazSharedPreferences();
        najkratsi = getNajnizsiCas();
        rekord.setText("Rekord: " + getNajnizsiCas());

        if (savedInstanceState != null) {
            // Obnovenie stavu z Bundle pri otáčaní obrazovky
            prejdeneMetre = savedInstanceState.getDouble("prejdeneMetre");
            konRychlost = savedInstanceState.getInt("konRychlost");
            sila = savedInstanceState.getInt("sila");
            zataz = savedInstanceState.getDouble("zataz");
            cas = savedInstanceState.getDouble("cas");
            minuty = savedInstanceState.getInt("minuty");
            najkratsi = savedInstanceState.getString("najkratsi");
            stavHry = savedInstanceState.getBoolean("stavHry");

            mnp = savedInstanceState.getInt("mnp");
            msp = savedInstanceState.getInt("msp");
            n1 = savedInstanceState.getInt("n1");
            n2 = savedInstanceState.getInt("n2");
            n3 = savedInstanceState.getInt("n3");

            // Aktualizácia UI
            rychlost.setText("Aktuálna rýchlosť: " + konRychlost);
            neprejdenych.setText("Počet metrov do cieľa: " + (DRAHA - (int)prejdeneMetre));
            energia.setText("Energia koňa: " + sila);
            stopky.setText(String.format("Čas: %d:%02d", minuty, (int) cas));
            rekord.setText("Rekord: " + najkratsi);
        }

        if (stavHry) {
            tik();
        }


    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Uloženie stavu pri otáčaní obrazovky
        outState.putDouble("prejdeneMetre", prejdeneMetre);
        outState.putInt("konRychlost", konRychlost);
        outState.putInt("sila", sila);
        outState.putDouble("zataz", zataz);
        outState.putDouble("cas", cas);
        outState.putInt("minuty", minuty);
        outState.putString("najkratsi", najkratsi);
        outState.putBoolean("stavHry", stavHry);

        // Uloženie stavu terénu
        outState.putInt("mnp", mnp);
        outState.putInt("msp", msp);
        outState.putInt("n1", n1);
        outState.putInt("n2", n2);
        outState.putInt("n3", n3);
    }

    @Override
    protected void onDestroy() {
        ulozitStavHry();
        super.onDestroy();
        stavHry = false; // Zastav vlákno pri ukončení aktivity
        zastavAnimaciuKona();
    }


    public void pridaj(View view) {
        if (sila != 0 && konRychlost < KON_MAX_RYCHLOST) {
            konRychlost += 4;
        }
        rychlost.setText("Aktuálna rýchlosť: " + konRychlost);
    }

    public void spomal(View view) {
        if (konRychlost > 0) {
            konRychlost -= 4;
        }
        rychlost.setText("Aktuálna rýchlosť: " + konRychlost);
    }


    public void tik() {

        spustAnimaciuKona();

        stavHry = true;
        System.out.println(mnp + " = mnp, " + msp + " = msp, " + n1 + " = n1, " + n2 + " = n1, " + n3 + " = n3");
        new Thread(() -> {
            int ostava = DRAHA;
            while (ostava > 0 && stavHry) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                aktualnaDraha.setText("Terén: Lúka");

                cas += 0.01;
                if ((int) cas >= 60) {
                    cas -= 60;
                    minuty++;
                }


                Object[] info = zistiPasmo(ostava, mnp, msp, n1, n2, n3);
                double oddychCis = (double) info[0];
                double zrychlenie = (double) info[1];
                int narocnost = (int) info[2];
                int bonus = (int) info[3];


                if (konRychlost == 0 && sila < 100)
                    zataz -= oddychCis * 2 * bonus;

                else if (sila <= 0) {
                    konRychlost = 4;
                    rychlost.setText("Aktuálna rýchlosť: " + konRychlost);
                }

                else if (konRychlost <= 12) {
                    if (sila < 100)
                        zataz -= oddychCis;
                }
                else if (konRychlost <= 24) {
                    zataz += konRychlost / (double) narocnost;
                }

                else if (konRychlost > 24 && konRychlost < 50)
                    zataz += konRychlost / (double) (narocnost - 2000);

                else {
                    zataz += konRychlost / (double) (narocnost - 3000);
                }

                sila = (int) (KON_VYDRZ - zataz);
                prejdeneMetre += konRychlost*zrychlenie / 3.6 * 0.01;
                ostava = (int) Math.round(DRAHA - prejdeneMetre);

                rychlost.setText("Aktuálna rýchlosť: " + konRychlost * zrychlenie);
                neprejdenych.setText("Počet metrov do cieľa: " + ostava);
                energia.setText("Energia koňa: " + sila);
                stopky.setText(String.format("Čas: %d:%02d", minuty, (int) cas));
            }
            if (ostava <= 0) {
                String casStr = String.format("%d:%02d", minuty, (int) cas);
                ulozitCas(casStr, najkratsi);
                rekord.setText("Rekord: " + getNajnizsiCas());
                stavHry = false;
                zastavAnimaciuKona();
            }
        }).start();
    }

    public Object[] zistiPasmo(int ostava, int mnp, int msp, int n1, int n2, int n3) {
        double oddychCis = 0.01;
        double zrychlenie = 1;
        int narocnost = 7000;
        int bonus = 1;
        String napInf = "";

        if (n1 >= ostava && ostava >= n1 - 20 || n2 >= ostava && ostava >= n2 - 20 || n3 >= ostava && ostava >= n3 - 20) {
            napInf = ", Napájadlo";
            aktualnaDraha.setText("Terén: Napájadlo");
            bonus = 10;
        }

        if (mnp >= ostava && ostava >= (mnp - 300)) {
            aktualnaDraha.setText("Terén: Náročné pásmo" + napInf);
            narocnost = 5000;
            oddychCis = 0.005;
            backgroundImage.setImageResource(pozadia[2]);
        }
        else if (msp >= ostava && ostava >= (msp - 400)) {
            aktualnaDraha.setText("Terén: Šprintérske pásmo" + napInf);
            zrychlenie = 1.25;
            backgroundImage.setImageResource(pozadia[1]);
        }
        else {
            backgroundImage.setImageResource(pozadia[0]); // Nastaví pozadie1
        }

        return new Object[]{oddychCis, zrychlenie, narocnost, bonus};
    }

    public void ulozitCas(String cas, String rekord) {
        if (Objects.equals(rekord, "N/A") || cas_na_sekundy(rekord) > cas_na_sekundy(cas)) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("Rekord", cas);
            editor.apply();
        }
    }

    public String getNajnizsiCas() {
        //settings = getSharedPreferences("GAME_DATA", Context.MODE_PRIVATE);
        najkratsi = settings.getString("Rekord", "N/A");
        return najkratsi;
    }

    private void spustAnimaciuKona() {
        animaciaBezi = true;
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (konRychlost == 0){
                    imageView.setImageResource(animaciaKona[12]);

                    // Opakovanie po 100 ms
                    handler.postDelayed(this, 100);

                }
                else if (animaciaBezi) {

                    // Nastavenie aktuálneho obrázku
                    imageView.setImageResource(animaciaKona[frameIndex]);

                    // Posunutie na ďalší obrázok
                    frameIndex = (frameIndex + 1) % 12;

                    // Opakovanie po 100 ms
                    handler.postDelayed(this, 100 - konRychlost);

                }
            }
        });
    }

    private void zastavAnimaciuKona() {
        animaciaBezi = false;
        handler.removeCallbacksAndMessages(null);
    }

    public int cas_na_sekundy(String cas_str) {
        String minuty = cas_str.split(":")[0];
        String sekundy = cas_str.split(":")[1];

        int celkom = Integer.parseInt(minuty) * 60 + Integer.parseInt(sekundy);
        return celkom;
    }

    private void nacitatStavHry() {
        prejdeneMetre = settings.getFloat("prejdeneMetre",0);
        konRychlost = settings.getInt("konRychlost", 0);
        sila = settings.getInt("sila", KON_VYDRZ);
        zataz = settings.getFloat("zataz", 0);
        cas = settings.getFloat("cas", 0);
        minuty = settings.getInt("minuty", 0);
        stavHry = settings.getBoolean("stavHry", true);

        mnp = settings.getInt("mnp", a);
        msp = settings.getInt("msp", b);
        n1 = settings.getInt("n1", random.nextInt(400) + 1600);
        n2 = settings.getInt("n2", random.nextInt(780) + 800);
        n3 = settings.getInt("n3", random.nextInt(780) + 20);
    }

    private void ulozitStavHry() {
        SharedPreferences.Editor editor = settings.edit();
        editor.putFloat("prejdeneMetre", (float) prejdeneMetre);
        editor.putInt("konRychlost", konRychlost);
        editor.putInt("sila", sila);
        editor.putFloat("zataz", (float) zataz);
        editor.putFloat("cas", (float) cas);
        editor.putInt("minuty", minuty);
        editor.putBoolean("stavHry", stavHry);

        editor.putInt("mnp", mnp);
        editor.putInt("msp", msp);
        editor.putInt("n1", n1);
        editor.putInt("n2", n2);
        editor.putInt("n3", n3);

        editor.apply(); // Uloží údaje asynchrónne
    }

    public String sekundy_na_cas(int sekundy) {
        int minuty = sekundy / 60;
        sekundy = sekundy % 60;
        return String.format("%d:%02d", minuty, sekundy);
    }

    public void ukoncitHru(View view) {
        ulozitStavHry();
        finish();
    }


    public void vymazSharedPreferences() {
        settings = getSharedPreferences("GAME_DATA", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.remove("prejdeneMetre");  // Vymaže všetky údaje
        editor.remove("konRychlost");
        editor.remove("sila");
        editor.remove("zataz");
        editor.remove("cas");
        editor.remove("minuty");
        editor.remove("mnp");
        editor.remove("msp");
        editor.remove("n1");
        editor.remove("n2");
        editor.remove("n3");
        editor.remove("stavHry");
        editor.apply();   // Uloží zmeny
    }



}