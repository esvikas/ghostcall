package com.kickbackapps.ghostcall.ui;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.crashlytics.android.Crashlytics;
import com.kickbackapps.ghostcall.R;

import io.fabric.sdk.android.Fabric;

/**
 * Created by Vikash on 1/14/2016.
 */
public class SplashScreen extends AppCompatActivity {

    public static final String GHOST_PREF = "GhostPrefFile";
    private static final int time = 2000;
    SharedPreferences settings;
    String apiKey;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_splash_screen);

        settings = getSharedPreferences(GHOST_PREF, 0);
        apiKey = settings.getString("api_key", "");

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (apiKey.equals("")) {
                    gotoTutorialScreen();
                } else
                    gotoStartScreen();
            }
        }, time);

    }

    private void gotoTutorialScreen() {
        Intent intent = new Intent(this, TutorialActivity.class);
        startActivity(intent);
        finish();
    }

    private void gotoStartScreen() {
        Intent intent = new Intent(this, StartScreen.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ImageView imgLogo = (ImageView) findViewById(R.id.imgLogo);
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this, imgLogo, "logo");
            startActivityForResult(intent, 0, options.toBundle());
        } else {
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        finish();
    }

}
