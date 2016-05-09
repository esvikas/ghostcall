package com.kickbackapps.ghostcall.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kickbackapps.ghostcall.InternetDialog;
import com.kickbackapps.ghostcall.R;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class CodeVerificationScreen extends AppCompatActivity implements View.OnClickListener {

    //Button resendButton;
    public static final String GHOST_PREF = "GhostPrefFile";
    TextView smsCodeField;
    CircleProgressBar progressSpinner;
    RelativeLayout spinnerLayout;

    String code = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_verification_screen_two);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Verify number");

        spinnerLayout = (RelativeLayout) findViewById(R.id.spinnerLayout);
        progressSpinner = (CircleProgressBar) findViewById(R.id.progressBar);
        progressSpinner.setColorSchemeResources(android.R.color.holo_blue_dark);

        smsCodeField = (TextView) findViewById(R.id.smsCodeInput);

        showTermsConditions();

        /*resendButton = (Button) findViewById(R.id.resendButton);
        resendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent toPhoneVerification = new Intent(CodeVerificationScreen.this, VerificationScreen.class);
                startActivity(toPhoneVerification);
                finish();
            }
        });*/

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
    public void onBackPressed() {
        if (getIntent().getStringExtra("from").equals("login")) {
            startActivity(new Intent(CodeVerificationScreen.this, LoginActivity.class));
            finish();
        } else {
            startActivity(new Intent(CodeVerificationScreen.this, SignUpActivity.class));
            finish();
        }

    }

    private void showTermsConditions() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Terms and Conditions")
                .setMessage(R.string.terms_n_conditions)
                .setPositiveButton("ACCEPT", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setNegativeButton("DISAGREE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                    }
                })
                .create();
        dialog.show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.verifyButton:
                if (InternetDialog.haveNetworkConnection(CodeVerificationScreen.this)) {
                    spinnerLayout.setVisibility(View.VISIBLE);
                    new verifyCodeTask().execute();
                } else {
                    spinnerLayout.setVisibility(View.INVISIBLE);
                    InternetDialog.showInternetDialog(CodeVerificationScreen.this);
                }
                break;
            case R.id.ll_one:
                if (code.length() <= 3) {
                    code += "1";
                    smsCodeField.setText(code);
                }
                break;
            case R.id.ll_two:
                if (code.length() <= 3) {
                    code += "2";
                    smsCodeField.setText(code);
                }
                break;
            case R.id.ll_three:
                if (code.length() <= 3) {
                    code += "3";
                    smsCodeField.setText(code);
                }
                break;
            case R.id.ll_four:
                if (code.length() <= 3) {
                    code += "4";
                    smsCodeField.setText(code);
                }
                break;
            case R.id.ll_five:
                if (code.length() <= 3) {
                    code += "5";
                    smsCodeField.setText(code);
                }
                break;
            case R.id.ll_six:
                if (code.length() <= 3) {
                    code += "6";
                    smsCodeField.setText(code);
                }
                break;
            case R.id.ll_seven:
                if (code.length() <= 3) {
                    code += "7";
                    smsCodeField.setText(code);
                }
                break;
            case R.id.ll_eight:
                if (code.length() <= 3) {
                    code += "8";
                    smsCodeField.setText(code);
                }
                break;
            case R.id.ll_nine:
                if (code.length() <= 3) {
                    code += "9";
                    smsCodeField.setText(code);
                }
                break;
            case R.id.ll_zero:
                if (code.length() <= 3) {
                    code += "0";
                    smsCodeField.setText(code);
                }
                break;
            case R.id.ll_contact:
                break;
            case R.id.ll_delete:
                if (code.length() > 0)
                    code = code.substring(0, code.length() - 1);
                smsCodeField.setText(code);
                break;
        }
    }

    public class verifyCodeTask extends AsyncTask<Void, Void, Void> {

        HttpURLConnection urlConnection = null;
        URL url = null;
        String temp, response = "";
        Uri.Builder builderString;
        InputStream inStream = null;
        String apiKey;
        StringBuilder codeInput;
        SharedPreferences settings = getSharedPreferences(GHOST_PREF, 0);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            builderString = new Uri.Builder();
            builderString.scheme("http")
                    .authority("www.ghostcall.in")
                    .appendPath("api")
                    .appendPath("verify");
            apiKey = settings.getString("api_key", "");
            codeInput = new StringBuilder();
            codeInput.append("code=");
            codeInput.append(smsCodeField.getText().toString());
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                url = new URL(builderString.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("X-api-key", apiKey);
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
//                urlConnection.connect();
//                responseCode = urlConnection.getResponseCode();

                DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
                wr.writeBytes(codeInput.toString());
                wr.flush();
                wr.close();

                inStream = urlConnection.getInputStream();
                BufferedReader bReader = new BufferedReader(new InputStreamReader(inStream));
                while ((temp = bReader.readLine()) != null) {
                    response += temp;
                }
            } catch (Exception e) {

            } finally {
                if (inStream != null) {
                    try {
                        inStream.close();
                    } catch (IOException ignored) {
                    }
                }
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (response != null) {
                try {
                    JSONObject jObject = new JSONObject(response);
                    String verificationString = jObject.getString("verified");
                    if (verificationString.equals("true")) {
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putBoolean("verified", true);
                        editor.apply();
                        startActivity(new Intent(CodeVerificationScreen.this, StartScreen.class));
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), "Incorrect Pin", Toast.LENGTH_SHORT).show();
                    }
                    spinnerLayout.setVisibility(View.INVISIBLE);
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "An error occurred", Toast.LENGTH_SHORT).show();
                    spinnerLayout.setVisibility(View.INVISIBLE);
                }
            }
        }
    }
}
