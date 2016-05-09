package com.kickbackapps.ghostcall.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.kickbackapps.ghostcall.InternetDialog;
import com.kickbackapps.ghostcall.PhoneNumberTextWatcher;
import com.kickbackapps.ghostcall.R;
import com.kickbackapps.ghostcall.services.RegistrationIntentService;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Vikash on 1/14/2016.
 */
public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    private Activity activity;
    private EditText codePhoneInput;
    private CircleProgressBar progressSpinner;
    private RelativeLayout spinnerLayout;

    private String deviceID;
    private String buildNumber;
    private String ePhoneNumber;

    public static final String GHOST_PREF = "GhostPrefFile";
    private SharedPreferences settings;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    boolean sentToken = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        activity = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Signup");

        codePhoneInput = (EditText) findViewById(R.id.phoneCodeField);
        codePhoneInput.addTextChangedListener(new PhoneNumberTextWatcher(codePhoneInput));

        codePhoneInput.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    startLoginProcess();
                    return true;
                }
                return false;
            }
        });

        settings = getSharedPreferences(GHOST_PREF, 0);

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }

        TelephonyManager tManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        deviceID = tManager.getDeviceId();
        buildNumber = Integer.toString(Build.VERSION.SDK_INT);

        spinnerLayout = (RelativeLayout) findViewById(R.id.spinnerLayout);
        progressSpinner = (CircleProgressBar) findViewById(R.id.progressBar);
        progressSpinner.setColorSchemeResources(android.R.color.holo_blue_dark);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                backPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        backPressed();
    }

    private void backPressed() {
        startActivity(new Intent(this, StartScreen.class));
        finish();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnSignUp:
                startLoginProcess();
                break;
        }
    }

    private void startLoginProcess() {

        spinnerLayout.setVisibility(View.VISIBLE);
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

        try {
            Phonenumber.PhoneNumber usaNumber = phoneUtil.parse(codePhoneInput.getText().toString(), "US");
            if (phoneUtil.isValidNumberForRegion(usaNumber, "US")) {
                ePhoneNumber = phoneUtil.format(usaNumber, PhoneNumberUtil.PhoneNumberFormat.E164);
                if (InternetDialog.haveNetworkConnection(activity)) {
                    new LoginCredentialsTask().execute();
                } else {
                    spinnerLayout.setVisibility(View.INVISIBLE);
                    InternetDialog.showInternetDialog(activity);
                }
            } else {
                Toast.makeText(getApplicationContext(), "Invalid Number", Toast.LENGTH_SHORT).show();
                spinnerLayout.setVisibility(View.INVISIBLE);
            }
        } catch (NumberParseException e) {
            Toast.makeText(getApplicationContext(), "Invalid Number", Toast.LENGTH_SHORT).show();
            spinnerLayout.setVisibility(View.INVISIBLE);
        }
    }

    public class LoginCredentialsTask extends AsyncTask<Void, Void, Void> {

        HttpURLConnection urlConnection = null;
        URL url = null;
        String temp, response = "";
        Uri.Builder builderString;
        InputStream inStream = null;
        StringBuilder phoneNumber;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            phoneNumber = new StringBuilder();
            phoneNumber.append("%2B").append(codePhoneInput.getText().toString());
            builderString = new Uri.Builder();
            builderString.scheme("http")
                    .authority("www.ghostcall.in")
                    .appendPath("api")
                    .appendPath("login")
                    .appendQueryParameter("phone_number", ePhoneNumber)
                    .appendQueryParameter("device_id", deviceID)
                    .appendQueryParameter("platform", "android")
                    .appendQueryParameter("platform_version", buildNumber)
                    .appendQueryParameter("app_version", "1.0.1");
            if (sentToken) {
                String GCMToken = settings.getString("GCMToken", "");
                builderString.appendQueryParameter("device_token", GCMToken);
            }

        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                url = new URL(builderString.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setDoInput(true);
                urlConnection.connect();
                inStream = urlConnection.getInputStream();
                BufferedReader bReader = new BufferedReader(new InputStreamReader(inStream));
                while ((temp = bReader.readLine()) != null) {
                    response += temp;
                }
            } catch (Exception e) {
                String error = e.getMessage();
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
                    String apiKey = jObject.getString("api_key");
                    settings = getSharedPreferences(GHOST_PREF, 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("api_key", apiKey);
                    editor.apply();
                    new sendVerifyCodeTask().execute();
                } catch (JSONException e) {
                    spinnerLayout.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(), "Fail to connect to server", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public class sendVerifyCodeTask extends AsyncTask<Void, Void, Void> {

        HttpURLConnection urlConnection = null;
        URL url = null;
        String temp, response = "";
        Uri.Builder builderString;
        InputStream inStream = null;
        String apiKey;
        int responseCode;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            builderString = new Uri.Builder();
            builderString.scheme("http")
                    .authority("www.ghostcall.in")
                    .appendPath("api")
                    .appendPath("verify");
            settings = getSharedPreferences(GHOST_PREF, 0);
            apiKey = settings.getString("api_key", "");
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (apiKey.equals("")) {
                // do something
            } else {
                try {
                    url = new URL(builderString.toString());
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setRequestProperty("X-api-key", apiKey);
                    urlConnection.setDoInput(true);
                    urlConnection.connect();
                    responseCode = urlConnection.getResponseCode();
                } catch (Exception e) {

                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            spinnerLayout.setVisibility(View.INVISIBLE);
            if (responseCode == 200 || responseCode == 201 || responseCode == 202) {
                Intent intent = new Intent(activity, CodeVerificationScreen.class);
                intent.putExtra("from", "signup");
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(getApplicationContext(), "Fail to connect to server", Toast.LENGTH_SHORT).show();
            }
            super.onPostExecute(aVoid);
        }
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i("HomeScreen", "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }
}
