package sk.spsepo.lesko.steeplechasegame;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Utils {

    /** Prevod času vo formáte MM:SS:cc (stotiny) na celkové stotiny. */
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

    /** Výber minima z poľa časov (reťazcov formátu MM:SS:cc). */
    public static String getMinimalTime(String[] times) {
        int best = Integer.MAX_VALUE;
        String result = "N/A";
        for (String t : times) {
            int h = timeToHundredths(t);
            if (h < best) {
                best = h;
                result = t;
            }
        }
        return result;
    }
}
