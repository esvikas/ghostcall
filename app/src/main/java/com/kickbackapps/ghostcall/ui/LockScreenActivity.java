package com.kickbackapps.ghostcall.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.kickbackapps.ghostcall.R;

/**
 * Created by viks on 2/25/16.
 */
public class LockScreenActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String GHOST_PREF = "GhostPrefFile";
    private SharedPreferences settings;

    TextView codeField;
    Button verifyButton;
    String temp_code = "";
    String code = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitivity_set_pincode);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Lock App");

        settings = getSharedPreferences(GHOST_PREF, 0);
        //apiKey = settings.getString("api_key", "");

        codeField = (TextView) findViewById(R.id.smsCodeInput);
        verifyButton = (Button) findViewById(R.id.verifyButton);
        verifyButton.setText("Next");
    }

    private void setAppLockPref() {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("pin_code", temp_code);
        editor.apply();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.verifyButton:
                if (code.length() == 4) {
                    if (verifyButton.getText().toString().equals("Next")) {
                        temp_code = codeField.getText().toString();
                        code = "";
                        codeField.setText("Re-Enter pin code");
                        verifyButton.setText("Confirm");
                    } else if (verifyButton.getText().toString().equals("Confirm")){
                        if (code.equals(temp_code)){
                            setAppLockPref();
                            onBackPressed();
                        } else {
                            Toast.makeText(this, "Pin code did not match", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(this, "Pin code length should be 4 digits", Toast.LENGTH_SHORT).show();
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
