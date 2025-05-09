package sk.spsepo.lesko.steeplechasegame;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;

public class Terrain {
    // rozsahy
    public static final int CHALLENGING_RANGE = 300;
    public static final int SPRINT_RANGE       = 400;
    public static final int WATER_RANGE        = 20;

    // hodnoty
    public static final double BASE_REST       = 0.01;
    public static final double BASE_ACCEL      = 1.0;
    public static final double BASE_DIFFICULTY = 7000;
    public static final double BASE_BONUS      = 1;

    public static final double CHALLENGING_REST       = 0.005;
    public static final double CHALLENGING_DIFFICULTY = 5000;
    public static final double SPRINT_ACCEL           = 1.25;
    public static final double WATER_BONUS            = 10;

    private int challengingPos;
    private int sprintPos;
    private int[] waterPositions;

    public Terrain(Context ctx, String assetFileName) {
        try {
            InputStream is = ctx.getAssets().open(assetFileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, "UTF-8");
            JSONObject obj = new JSONObject(json);
            challengingPos = obj.getInt("miesto_narocneho_pasma");
            sprintPos      = obj.getInt("miesto_sprinterskeho_pasma");
            JSONArray arr   = obj.getJSONArray("napajadla");
            waterPositions = new int[arr.length()];
            for (int i = 0; i < arr.length(); i++) {
                waterPositions[i] = arr.getInt(i);
            }
        } catch (Exception e) {
            // fallback
            challengingPos = 850;
            sprintPos      = 1600;
            waterPositions = new int[]{400, 1200, 1900};
        }
    }

    /**
     * Vrací parametre trate podľa zostávajúcich metrov.
     * @return [rest, accel, difficulty, bonus, terrainType]
     */
    public TerrainSegment getSegment(double remaining) {
        double rest       = BASE_REST;
        double accel      = BASE_ACCEL;
        double difficulty = BASE_DIFFICULTY;
        double bonus      = BASE_BONUS;
        String type       = "cesta";

        if (remaining <= challengingPos && remaining >= challengingPos - CHALLENGING_RANGE) {
            type = "náročné";
            difficulty = CHALLENGING_DIFFICULTY;
            rest       = CHALLENGING_REST;
        } else if (remaining <= sprintPos && remaining >= sprintPos - SPRINT_RANGE) {
            type  = "šprintérske";
            accel = SPRINT_ACCEL;
        }
        for (int wp : waterPositions) {
            if (remaining <= wp && remaining >= wp - WATER_RANGE) {
                type  = "napájadlo";
                bonus = WATER_BONUS;
                break;
            }
        }
        return new TerrainSegment(rest, accel, difficulty, bonus, type);
    }

    public static class TerrainSegment {
        public final double rest, accel, difficulty, bonus;
        public final String type;
        public TerrainSegment(double r, double a, double d, double b, String t) {
            rest = r; accel = a; difficulty = d; bonus = b; type = t;
        }
    }
}
