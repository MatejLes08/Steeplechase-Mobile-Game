package sk.spsepo.lesko.steeplechasegame;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Utils {
    private static final String COLLECTION_USERS = "users";
    private static final String KEY_BEST_TIME = "best_time";

    /** Prevod času vo formáte MM:SS:cc na celkové stotiny. */
    public static int timeToHundredths(String time) {
        String[] parts = time.split(":");
        int m = Integer.parseInt(parts[0]);
        int s = Integer.parseInt(parts[1]);
        int c = Integer.parseInt(parts[2]);
        return m * 6000 + s * 100 + c;
    }

    /** Uloží najlepší čas do Firestore a vráti nový čas cez callback, ak je lepší než existujúci. */
    public static void saveBestTime(String uid, String newTime, FirebaseFirestore db, OnBestTimeSavedListener saveListener) {
        if (uid == null) {
            saveListener.onBestTimeSaved(null);
            return;
        }

        // Načítaj existujúci rekord
        db.collection(COLLECTION_USERS).document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String existing = documentSnapshot.getString(KEY_BEST_TIME);
                    if (existing == null || existing.equals("N/A") || timeToHundredths(newTime) < timeToHundredths(existing)) {
                        // Aktualizuj alebo vytvor dokument s novým časom
                        Map<String, Object> data = new HashMap<>();
                        data.put(KEY_BEST_TIME, newTime);
                        db.collection(COLLECTION_USERS).document(uid)
                                .set(data, SetOptions.merge())
                                .addOnSuccessListener(aVoid -> {
                                    // Vráti nový čas cez callback
                                    saveListener.onBestTimeSaved(newTime);
                                })
                                .addOnFailureListener(e -> {
                                    // Handle error (e.g., log or show toast)
                                    System.err.println("Failed to save best time: " + e.getMessage());
                                    saveListener.onBestTimeSaved(null);
                                });
                    } else {
                        // Ak čas nie je lepší, vráti existujúci čas
                        saveListener.onBestTimeSaved(existing);
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle error (e.g., log or show toast)
                    System.err.println("Failed to load best time: " + e.getMessage());
                    saveListener.onBestTimeSaved(null);
                });
    }

    /** Načíta najlepší čas z Firestore alebo vráti "N/A". */
    public static void loadBestTime(String uid, FirebaseFirestore db, OnBestTimeLoadedListener listener) {
        if (uid == null) {
            listener.onBestTimeLoaded("N/A");
            return;
        }

        db.collection(COLLECTION_USERS).document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String bestTime = documentSnapshot.getString(KEY_BEST_TIME);
                    listener.onBestTimeLoaded(bestTime != null ? bestTime : "N/A");
                })
                .addOnFailureListener(e -> {
                    listener.onBestTimeLoaded("N/A");
                    System.err.println("Failed to load best time: " + e.getMessage());
                });
    }

    /** Callback rozhranie pre asynchrónne uloženie času. */
    public interface OnBestTimeSavedListener {
        void onBestTimeSaved(String bestTime);
    }

    /** Callback rozhranie pre asynchrónne načítanie času. */
    public interface OnBestTimeLoadedListener {
        void onBestTimeLoaded(String bestTime);
    }

    /** Prevod stotín na časový reťazec MM:SS:cc */
    public static String hundredthsToTime(int hund) {
        int m = hund / 6000;
        int rem = hund % 6000;
        int s = rem / 100;
        int c = rem % 100;
        return String.format(Locale.US, "%d:%02d:%02d", m, s, c);
    }
}