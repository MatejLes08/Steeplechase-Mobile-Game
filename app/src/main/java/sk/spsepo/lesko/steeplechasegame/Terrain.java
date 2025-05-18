package sk.spsepo.lesko.steeplechasegame;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class Terrain {
    public static final int TRACK_LENGTH = Horse.TRACK_LENGTH;
    // rozsahy
    public static final int CHALLENGING_RANGE = 300;
    public static final int SPRINT_RANGE       = 400;
    public static final int WATER_RANGE        = 20;
    // hodnoty
    public static final double BASE_REST       = 0.01;
    public static final double BASE_ACCEL      = 1.0;
    public static final double BASE_DIFFICULTY = 6000;
    public static final double BASE_BONUS      = 1;
    public static final double CHALLENGING_REST       = 0.005;
    public static final double CHALLENGING_DIFFICULTY = 4000;
    public static final double SPRINT_ACCEL           = 1.25;
    public static final double WATER_BONUS            = 10;

    private int challengingPos;
    private int sprintPos;
    private int[] waterPositions;

    public Terrain(Context ctx, String assetFile) {
        try (InputStream is = ctx.getAssets().open(assetFile)) {
            byte[] buf = new byte[is.available()];
            is.read(buf);
            String json = new String(buf, StandardCharsets.UTF_8);
            JSONObject o = new JSONObject(json);
            challengingPos = o.getInt("miesto_narocneho_pasma");
            sprintPos      = o.getInt("miesto_sprinterskeho_pasma");
            JSONArray arr   = o.getJSONArray("napajadla");
            waterPositions = new int[arr.length()];
            for (int i = 0; i < arr.length(); i++)
                waterPositions[i] = arr.getInt(i);
        } catch (Exception e) {
            challengingPos = 850;
            sprintPos      = 1600;
            waterPositions = new int[]{400, 1200, 1900};
        }
    }

    public TerrainSegment getSegment(double rem) {
        double rest = BASE_REST;
        double accel = BASE_ACCEL;
        double diff = BASE_DIFFICULTY;
        double bonus = BASE_BONUS;
        String type = "Cesta";

        if (rem <= challengingPos && rem >= challengingPos - CHALLENGING_RANGE) {
            type = "Náročné pásmo";
            diff = CHALLENGING_DIFFICULTY;
            rest = CHALLENGING_REST;
        } else if (rem <= sprintPos && rem >= sprintPos - SPRINT_RANGE) {
            type = "Šprintérske pásmo";
            accel = SPRINT_ACCEL;
        }
        for (int wp : waterPositions) {
            if (rem <= wp && rem >= wp - WATER_RANGE) {
                type = "Napájadlo";
                bonus = WATER_BONUS;
                break;
            }
        }
        return new TerrainSegment(rest, accel, diff, bonus, type);
    }

    /**
     * Vráti pole typov pásiem pozdĺž celej trate (od štartu po cieľ).
     */
    public String[] getTerrainTypesAhead(double startRem, int count) {
        String[] result = new String[count];
        double step = TRACK_LENGTH / (double)count;
        for (int i = 0; i < count; i++) {
            double r = startRem - i * step;
            if (r < 0) r = 0;
            result[i] = getSegment(r).type;
        }
        return result;
    }

    public static class TerrainSegment {
        public final double rest, accel, difficulty, bonus;
        public final String type;
        public TerrainSegment(double r, double a, double d, double b, String t) {
            rest = r; accel = a; difficulty = d; bonus = b; type = t;
        }
    }
}
