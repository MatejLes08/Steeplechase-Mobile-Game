package sk.spsepo.lesko.steeplechasegame;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.Locale;

public class Utils {
    private static final String PREFS = "steeple_prefs";
    private static final String KEY_BEST = "best_time";

    /** Prevod času vo formáte MM:SS:cc na celkové stotiny. */
    public static int timeToHundredths(String time) {
        String[] parts = time.split(":");
        int m = Integer.parseInt(parts[0]);
        int s = Integer.parseInt(parts[1]);
        int c = Integer.parseInt(parts[2]);
        return m * 6000 + s * 100 + c;
    }

    /** Prevod stotín na časový reťazec MM:SS:cc */
    public static String hundredthsToTime(int hund) {
        int m = hund / 6000;
        int rem = hund % 6000;
        int s = rem / 100;
        int c = rem % 100;
        return String.format(Locale.US, "%d:%02d:%02d", m, s, c);
    }

    /** Uloží najlepší čas, ak je lepší než existujúci */
    public static void saveBestTime(Context ctx, String newTime) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String old = prefs.getString(KEY_BEST, "N/A");
        if (old.equals("N/A") || timeToHundredths(newTime) < timeToHundredths(old)) {
            prefs.edit().putString(KEY_BEST, newTime).apply();
        }
    }

    /** Načíta najlepší čas alebo "N/A" */
    public static String loadBestTime(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return prefs.getString(KEY_BEST, "N/A");
    }
}
