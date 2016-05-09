package com.kickbackapps.ghostcall.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.kickbackapps.ghostcall.R;

/**
 * Created by Vikash on 1/26/2016.
 */
public class AppLockActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String GHOST_PREF = "GhostPrefFile";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_lock);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("GhostCall");

        Button btnUnlock = (Button) findViewById(R.id.btnUnlock);
        Button btnLock = (Button) findViewById(R.id.btnLock);

        SharedPreferences settings = getSharedPreferences(GHOST_PREF, 0);
        if (settings.getString("pin_code", "").length() == 4) {
            btnUnlock.setVisibility(View.VISIBLE);
            btnLock.setVisibility(View.GONE);
        } else {
            btnUnlock.setVisibility(View.GONE);
            btnLock.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnLock:
                Intent intent = new Intent(this, LockScreenActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.btnUnlock:
                Intent intent1 = new Intent(this, UnlockScreenActivity.class);
                intent1.putExtra("type", "unlock");
                startActivity(intent1);
                finish();
        }
    }
}
