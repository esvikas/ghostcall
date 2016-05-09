package com.kickbackapps.ghostcall.extended;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;

import com.kickbackapps.ghostcall.ui.UnlockScreenActivity;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by viks on 2/26/16.
 */
public class CustomAppCompact extends AppCompatActivity {

    private static final String GHOST_PREF = "GhostPrefFile";
    SharedPreferences.Editor editor;
    private SharedPreferences settings;

    @Override
    protected void onPause() {

        try {
            settings = getSharedPreferences(GHOST_PREF, 0);
            editor = settings.edit();
            editor.putString("isAppOnForeground", String.valueOf(new ForegroundCheckTask().execute(this).get()));
            editor.commit();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        settings = getSharedPreferences(GHOST_PREF, 0);
        if (settings.getString("isAppOnForeground", "").equals("false"))
            if (settings.getString("pin_code", "").length() == 4){
                Intent intent1 = new Intent(this, UnlockScreenActivity.class);
                intent1.putExtra("type", "lock");
                startActivity(intent1);
            }

        try {
            editor = settings.edit();
            editor.putString("isAppOnForeground", String.valueOf(new ForegroundCheckTask().execute(this).get()));
            editor.commit();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

    }

    class ForegroundCheckTask extends AsyncTask<Context, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Context... params) {
            final Context context = params[0].getApplicationContext();
            return isAppOnForeground(context);
        }

        private boolean isAppOnForeground(Context context) {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
            if (appProcesses == null) {
                return false;
            }
            final String packageName = context.getPackageName();
            for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                    return true;
                }
            }
            return false;
        }
    }
}
