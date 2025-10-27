package com.example.healthbuddy025;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class DataManager {
    private static final String PREF = "HealthBuddyPrefs";
    private static final String KEY_TODAY = "today_steps";
    private static final String KEY_LAST7 = "last7";
    private static final String KEY_SAVED_DATE = "saved_date";
    private static final String KEY_GOAL = "goal";

    private final SharedPreferences prefs;
    private final Gson gson;

    public DataManager(Context ctx) {
        prefs = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        gson = new Gson();
        // Ensure date rollover if needed
        ensureDate();
    }

    // ---------- Date helpers ----------

    private static String getTodayDateString() {
        Calendar c = Calendar.getInstance();
        int y = c.get(Calendar.YEAR);
        int m = c.get(Calendar.MONTH) + 1;
        int d = c.get(Calendar.DAY_OF_MONTH);
        return String.format("%04d-%02d-%02d", y, m, d);
    }

    /**
     * Ensure saved date matches today. If not, shift today's value into last7 and reset today.
     */
    private void ensureDate() {
        String saved = prefs.getString(KEY_SAVED_DATE, "");
        String today = getTodayDateString();
        if (!today.equals(saved)) {
            // rollover: push previous today into last7 if saved exists (non-empty)
            int prevToday = prefs.getInt(KEY_TODAY, 0);
            List<Integer> last7 = getLast7Internal(); // always returns 7 sized list
            if (last7.size() >= 7) {
                // remove oldest, append prevToday
                last7.remove(0);
            }
            last7.add(prevToday);
            saveLast7Internal(last7);

            // reset today to 0
            prefs.edit()
                    .putInt(KEY_TODAY, 0)
                    .putString(KEY_SAVED_DATE, today)
                    .apply();

            // reset goal-notified flag if you track that elsewhere (not in this class)
        }
    }

    // ---------- Today steps API ----------

    /**
     * Increment today's step count by 1 and persist.
     * Returns the updated today's step count.
     * Use this for accelerometer fallback step increments or when you want to persist step changes.
     */
    public synchronized long addStep() {
        ensureDate();
        int cur = prefs.getInt(KEY_TODAY, 0);
        cur = cur + 1;
        prefs.edit().putInt(KEY_TODAY, cur).apply();
        return cur;
    }

    /**
     * Add a given number of steps to today's count (useful if sensor provides delta >1).
     * Returns updated value.
     */
    public synchronized long addSteps(int delta) {
        if (delta <= 0) return getTodaySteps();
        ensureDate();
        int cur = prefs.getInt(KEY_TODAY, 0);
        cur = cur + delta;
        prefs.edit().putInt(KEY_TODAY, cur).apply();
        return cur;
    }

    /**
     * Get current today's steps (persisted).
     */
    public synchronized int getTodaySteps() {
        ensureDate();
        return prefs.getInt(KEY_TODAY, 0);
    }

    /**
     * Reset today's steps to zero (keeps history untouched).
     */
    public synchronized void resetTodaySteps() {
        prefs.edit().putInt(KEY_TODAY, 0).apply();
    }

    // ---------- Goal API ----------

    public void setGoal(int goal) {
        prefs.edit().putInt(KEY_GOAL, goal).apply();
    }

    public int getGoal() {
        return prefs.getInt(KEY_GOAL, 5000);
    }

    // ---------- Last7 history API (used by StatsActivity) ----------

    private List<Integer> getLast7Internal() {
        String json = prefs.getString(KEY_LAST7, null);
        if (json == null) {
            List<Integer> init = new ArrayList<>();
            for (int i = 0; i < 7; i++) init.add(0);
            return init;
        }
        Type type = new TypeToken<List<Integer>>() {}.getType();
        List<Integer> list = gson.fromJson(json, type);
        // Ensure size 7
        if (list == null) {
            list = new ArrayList<>();
        }
        while (list.size() < 7) list.add(0);
        // If longer than 7, keep the last 7
        if (list.size() > 7) {
            list = new ArrayList<>(list.subList(list.size() - 7, list.size()));
        }
        return list;
    }

    private void saveLast7Internal(List<Integer> list) {
        // Guarantee exactly 7 entries
        List<Integer> copy = new ArrayList<>(list);
        while (copy.size() < 7) copy.add(0);
        if (copy.size() > 7) copy = new ArrayList<>(copy.subList(copy.size() - 7, copy.size()));
        String json = gson.toJson(copy);
        prefs.edit().putString(KEY_LAST7, json).apply();
    }

    /**
     * Public getter for last7 (returns a copy).
     */
    public List<Integer> getLast7() {
        ensureDate();
        return new ArrayList<>(getLast7Internal());
    }

    /**
     * Save last7 explicitly (overwrites).
     */
    public void saveLast7(List<Integer> last7) {
        saveLast7Internal(last7);
    }

    // ---------- Reset & shift API (used by ResetReceiver) ----------

    /**
     * Reset daily and shift today's steps into last7.
     * Use this to perform a controlled reset (for example from midnight alarm).
     *
     * @param todaySteps the value to append to history (usually current today's steps)
     */
    public void resetDailyAndShift(int todaySteps) {
        // fetch current stored last7, append today's value (drop oldest)
        List<Integer> last7 = getLast7Internal();
        if (last7.size() >= 7) last7.remove(0);
        last7.add(Math.max(0, todaySteps));
        saveLast7Internal(last7);

        // reset today and update saved date
        prefs.edit()
                .putInt(KEY_TODAY, 0)
                .putString(KEY_SAVED_DATE, getTodayDateString())
                .apply();
    }

    // ---------- Utility: clear all (dev use) ----------

    public void clearAll() {
        prefs.edit().clear().apply();
    }
}
