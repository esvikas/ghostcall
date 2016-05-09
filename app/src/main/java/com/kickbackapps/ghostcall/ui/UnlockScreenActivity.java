package com.kickbackapps.ghostcall.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.kickbackapps.ghostcall.R;

/**
 * Created by viks on 2/26/16.
 */
public class UnlockScreenActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String GHOST_PREF = "GhostPrefFile";
    TextView codeField;
    Button verifyButton;
    String code = "";
    String type;
    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitivity_set_pincode);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Unlock App");

        settings = getSharedPreferences(GHOST_PREF, 0);
        //apiKey = settings.getString("api_key", "");

        type = getIntent().getStringExtra("type");

        codeField = (TextView) findViewById(R.id.smsCodeInput);
        verifyButton = (Button) findViewById(R.id.verifyButton);
        verifyButton.setText("Unlock");
    }

    private void deleteAppLockPref() {
        SharedPreferences settings = getSharedPreferences(GHOST_PREF, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("pin_code", "");
        editor.apply();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.verifyButton:
                if (code.length() == 4) {
                    settings = getSharedPreferences(GHOST_PREF, 0);
                    Log.i("Code ===", settings.getString("pin_code", ""));
                    if (code.equals(settings.getString("pin_code", ""))) {
                        if (type.equals("lock"))
                            finish();
                        else {
                            deleteAppLockPref();
                            finish();
                        }
                    } else {
                        code = "";
                        codeField.setText("");
                        Toast.makeText(getApplicationContext(), "Please Enter a Valid Pin code", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    code = "";
                    codeField.setText("");
                }
                break;
            case R.id.ll_one:
                if (code.length() <= 3) {
                    code += "1";
                    codeField.setText(code);
                }
                break;
            case R.id.ll_two:
                if (code.length() <= 3) {
                    code += "2";
                    codeField.setText(code);
                }
                break;
            case R.id.ll_three:
                if (code.length() <= 3) {
                    code += "3";
                    codeField.setText(code);
                }
                break;
            case R.id.ll_four:
                if (code.length() <= 3) {
                    code += "4";
                    codeField.setText(code);
                }
                break;
            case R.id.ll_five:
                if (code.length() <= 3) {
                    code += "5";
                    codeField.setText(code);
                }
                break;
            case R.id.ll_six:
                if (code.length() <= 3) {
                    code += "6";
                    codeField.setText(code);
                }
                break;
            case R.id.ll_seven:
                if (code.length() <= 3) {
                    code += "7";
                    codeField.setText(code);
                }
                break;
            case R.id.ll_eight:
                if (code.length() <= 3) {
                    code += "8";
                    codeField.setText(code);
                }
                break;
            case R.id.ll_nine:
                if (code.length() <= 3) {
                    code += "9";
                    codeField.setText(code);
                }
                break;
            case R.id.ll_zero:
                if (code.length() <= 3) {
                    code += "0";
                    codeField.setText(code);
                }
                break;
            case R.id.ll_contact:
                break;
            case R.id.ll_delete:
                if (code.length() > 0)
                    code = code.substring(0, code.length() - 1);
                codeField.setText(code);
                break;
        }
    }

    @Override
    public void onBackPressed() {

    }
}
