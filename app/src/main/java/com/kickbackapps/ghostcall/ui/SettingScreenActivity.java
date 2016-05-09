package com.kickbackapps.ghostcall.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.kickbackapps.ghostcall.Constants;
import com.kickbackapps.ghostcall.GhostCallAPIInterface;
import com.kickbackapps.ghostcall.GhostCallDatabaseAdapter;
import com.kickbackapps.ghostcall.R;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;

import java.sql.SQLException;

import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Vikash on 1/25/2016.
 */
public class SettingScreenActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private static final String GHOST_PREF = "GhostPrefFile";
    TextView tvValidateCount;
    TextView tvValidateText;
    TextView phone_no;
    RelativeLayout spinnerLayout;
    CircleProgressBar progressSpinner;
    EditText etUserName;
    TextView edit;
    Switch callSwitch;
    Switch messageSwitch;
    Switch voiceMailSwitch;
    GhostCallAPIInterface service;
    RequestInterceptor requestInterceptor;
    RestAdapter restAdapter;
    private SharedPreferences settings;
    private SharedPreferences.Editor editor;
    private String apiKey;
    private String ghostIDExtra;
    private String numberID;

    private boolean recall;
    private String tempName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Settings");

        settings = getSharedPreferences(GHOST_PREF, 0);
        apiKey = settings.getString("api_key", "");

        tvValidateCount = (TextView) findViewById(R.id.tvValidateCount);
        tvValidateText = (TextView) findViewById(R.id.tvValidateText);
        phone_no = (TextView) findViewById(R.id.phone_no);
        spinnerLayout = (RelativeLayout) findViewById(R.id.spinnerLayout);
        spinnerLayout.setVisibility(View.GONE);
        progressSpinner = (CircleProgressBar) findViewById(R.id.progressBar);
        progressSpinner.setColorSchemeResources(android.R.color.holo_blue_dark);

        etUserName = (EditText) findViewById(R.id.etUserName);
        edit = (TextView) findViewById(R.id.edit);
        callSwitch = (Switch) findViewById(R.id.callSwitch);
        messageSwitch = (Switch) findViewById(R.id.messageSwitch);
        voiceMailSwitch = (Switch) findViewById(R.id.voiceMailSwitch);

        Intent intent = getIntent();
        ghostIDExtra = intent.getExtras().getString("ghostIDExtra");
        numberID = intent.getExtras().getString("ghostIDExtra");
        etUserName.setText(intent.getExtras().getString("ghostNameExtra"));
        phone_no.setText(intent.getExtras().getString("number"));
        tvValidateCount.setText(intent.getExtras().getInt("count") + "");
        tvValidateText.setText(intent.getExtras().getString("time_unit") + " left");

        // set status for checkbox.
        callSwitch.setChecked(settings.getBoolean(numberID + "calls", true));
        messageSwitch.setChecked(settings.getBoolean(numberID + "messages", true));
        voiceMailSwitch.setChecked(settings.getBoolean(numberID + "voicemail", false));

        callSwitch.setOnCheckedChangeListener(this);
        messageSwitch.setOnCheckedChangeListener(this);
        voiceMailSwitch.setOnCheckedChangeListener(this);
        recall = true;

        tempName = etUserName.getText().toString();
        etUserName.clearFocus();
        etUserName.setCursorVisible(false);

        etUserName.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    startEditProcess();
                    return true;
                }
                return false;
            }
        });

        etUserName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    edit.setText("DONE");
                    edit.setTextColor(getResources().getColor(R.color.md_green_500));
                } else {
                    edit.setText("EDIT");
                    edit.setTextColor(getResources().getColor(R.color.md_red_500));
                }
            }
        });

    }

    private void startEditProcess() {
        if (edit.getText().toString().equals("DONE")) {
            // Hide keyboard
            etUserName.setCursorVisible(false);
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(etUserName.getWindowToken(), 0);

            updateAllStatus(settings.getBoolean(numberID + "calls", true),
                    settings.getBoolean(numberID + "messages", true),
                    settings.getBoolean(numberID + "voicemail", false), 4);
        } else {
            etUserName.setFocusable(true);
            etUserName.setCursorVisible(true);
            etUserName.setSelection(etUserName.getText().toString().length());
            // Show keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(etUserName, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public void setSwitchesValue(boolean call, boolean messages, boolean voicemail) {
        editor = settings.edit();
        editor.putBoolean(numberID + "calls", call);
        editor.putBoolean(numberID + "messages", messages);
        editor.putBoolean(numberID + "voicemail", voicemail);
        editor.apply();

        callSwitch.setChecked(call);
        messageSwitch.setChecked(messages);
        voiceMailSwitch.setChecked(voicemail);
        recall = true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_lock, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_lock:
                Intent intent = new Intent(getApplicationContext(), AppLockActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnExtendTime:
                Intent toPurchaseScreen = new Intent(getApplicationContext(), SelectPackageScreen.class);
                toPurchaseScreen.putExtra(Constants.PACKAGE_TYPE, "extend");
                toPurchaseScreen.putExtra("GhostID", ghostIDExtra);
                startActivity(toPurchaseScreen);
                break;
            case R.id.btnDelete:
                showLogoutDialog();
                break;
            case R.id.edit:
                startEditProcess();
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()) {
            case R.id.callSwitch:
                if (recall)
                    updateAllStatus(b, settings.getBoolean(numberID + "messages", true), settings.getBoolean(numberID + "voicemail", false), 0);
                break;
            case R.id.messageSwitch:
                if (recall)
                    updateAllStatus(settings.getBoolean(numberID + "calls", true), b, settings.getBoolean(numberID + "voicemail", false), 1);
                break;
            case R.id.voiceMailSwitch:
                if (recall)
                    updateAllStatus(settings.getBoolean(numberID + "calls", true), settings.getBoolean(numberID + "messages", true), b, 2);
                break;
        }
    }

    private void showLogoutDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Delete")
                .setMessage("Do you want to delete this number")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        deleteNumber();
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        dialog.show();
    }

    private void deleteNumber() {
        Log.i("Number Id: ", numberID);
        Log.i("API key: ", apiKey);

        spinnerLayout.setVisibility(View.VISIBLE);
        requestInterceptor = new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade request) {
                request.addHeader("X-api-key", apiKey);
            }
        };

        restAdapter = new RestAdapter.Builder().setEndpoint("http://www.ghostcall.in/api")
                .setRequestInterceptor(requestInterceptor).build();
        service = restAdapter.create(GhostCallAPIInterface.class);

        service.deleteNumber(numberID, numberID, new Callback<Response>() {
            @Override
            public void success(Response response, Response response2) {
                spinnerLayout.setVisibility(View.GONE);
                if (response != null && response.getStatus() == 200) {
                    try {
                        GhostCallDatabaseAdapter databaseAdapter = new GhostCallDatabaseAdapter(getApplicationContext());
                        databaseAdapter.open();
                        databaseAdapter.deleteNumber(numberID);
                        databaseAdapter.close();
                        Toast.makeText(getApplicationContext(), "Number deleted", Toast.LENGTH_SHORT).show();
                        finish();
                    } catch (SQLException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Error! Number not deleted", Toast.LENGTH_SHORT).show();
                    }
                }

            }

            @Override
            public void failure(final RetrofitError retrofitError) {
                spinnerLayout.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), "Sorry! Number not deleted", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateAllStatus(final boolean call, final boolean messages, final boolean voicemail, final int i) {
        spinnerLayout.setVisibility(View.VISIBLE);
        requestInterceptor = new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade request) {
                request.addHeader("X-api-key", apiKey);
            }
        };

        restAdapter = new RestAdapter.Builder().setEndpoint("http://www.ghostcall.in/api")
                .setRequestInterceptor(requestInterceptor).build();
        service = restAdapter.create(GhostCallAPIInterface.class);

        service.changeSettings(numberID, etUserName.getText().toString().trim(), call, messages, voicemail, new Callback<Response>() {
            @Override
            public void success(Response result, Response response2) {
                spinnerLayout.setVisibility(View.GONE);
                recall = true;

                setSwitchesValue(call, messages, voicemail);
                tempName = etUserName.getText().toString().trim();
                etUserName.setText(tempName);
                etUserName.setCursorVisible(false);
                edit.setText("EDIT");
                edit.setTextColor(getResources().getColor(R.color.md_red_500));

                try {
                    GhostCallDatabaseAdapter databaseAdapter = new GhostCallDatabaseAdapter(getApplicationContext());
                    databaseAdapter.open();
                    databaseAdapter.editNumberName(numberID, tempName);
                    databaseAdapter.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                //Try to get response body
                /*try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(result.getBody().in()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null)
                        sb.append(line);
                    Log.i("Response: ", sb.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }*/

            }

            @Override
            public void failure(final RetrofitError retrofitError) {
                spinnerLayout.setVisibility(View.GONE);
                Log.i("Response: ", retrofitError.toString());
                Toast.makeText(getApplicationContext(), "Sorry! could not change setting", Toast.LENGTH_SHORT).show();
                etUserName.setText(tempName);
                etUserName.setCursorVisible(false);
                edit.setText("EDIT");
                edit.setTextColor(getResources().getColor(R.color.md_red_500));
                switch (i) {
                    case 0:
                        recall = false;
                        setSwitchesValue(!call, messages, voicemail);
                        break;
                    case 1:
                        recall = false;
                        setSwitchesValue(call, !messages, voicemail);
                        break;
                    case 2:
                        recall = false;
                        setSwitchesValue(call, messages, !voicemail);
                        break;
                    case 4:
                        recall = false;
                        setSwitchesValue(call, messages, voicemail);
                        break;
                }
            }
        });
    }

}
