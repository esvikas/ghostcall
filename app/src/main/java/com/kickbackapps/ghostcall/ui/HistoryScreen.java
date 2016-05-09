package com.kickbackapps.ghostcall.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.kickbackapps.ghostcall.Constants;
import com.kickbackapps.ghostcall.GhostCallAPIInterface;
import com.kickbackapps.ghostcall.GhostCallDatabaseAdapter;
import com.kickbackapps.ghostcall.InternetDialog;
import com.kickbackapps.ghostcall.R;
import com.kickbackapps.ghostcall.adapters.HistoryAdapter;
import com.kickbackapps.ghostcall.objects.HistoryObject;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.LocalDate;
import org.joda.time.Minutes;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class HistoryScreen extends AppCompatActivity {

    private static final String GHOST_PREF = "GhostPrefFile";
    ListView historyList;
    HistoryAdapter historyAdapter;
    TextView userNumber, expireTimer;
    //ImageView purchaseButton;
    GhostCallDatabaseAdapter ghostDatabaseAdapter;
    //TextView userRemainingText;
    Bundle extras;
    ArrayList<HistoryObject> gHistoryList;
    MediaPlayer mediaPlayer;
    RequestInterceptor requestInterceptor;
    RestAdapter restAdapter;
    GhostCallAPIInterface service;
    SharedPreferences.Editor editor;
    private boolean isPlaying = false;
    private boolean isLoading = false;
    private String apiKey;
    private SharedPreferences settings;
    private String lastUpdatedTimestamp;
    private AudioManager audioManager;
    private int historyMax;
    private int currentAmount = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_screen);

        /*ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#1d375a")));
        actionBar.setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.home_actionbar_layout);*/

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("History");

        JodaTimeAndroid.init(this);

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.titleblue));
        }*/

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        settings = getSharedPreferences(GHOST_PREF, 0);
        apiKey = settings.getString("api_key", "");
        lastUpdatedTimestamp = settings.getString("lastUpdatedTimestamp", "");

        requestInterceptor = new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade request) {
                request.addHeader("X-api-key", apiKey);
            }
        };

        restAdapter = new RestAdapter.Builder().setEndpoint("http://www.ghostcall.in/api")
                .setRequestInterceptor(requestInterceptor).build();
        service = restAdapter.create(GhostCallAPIInterface.class);

        //userRemainingText = (TextView) findViewById(R.id.remainingText);
        SharedPreferences settings = getSharedPreferences(GHOST_PREF, 0);
        String userSMS = settings.getString("userSMS", "0");
        String userMins = settings.getString("userMins", "0");
        //userRemainingText.setText(userSMS + " sms / " + userMins + " mins left");

        userNumber = (TextView) findViewById(R.id.user_number);
        expireTimer = (TextView) findViewById(R.id.expire_timer);

        extras = getIntent().getExtras();
        if (!(extras == null)) {
            userNumber.setText(extras.getString("ghostNumberExtra"));
            try {
                DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
                DateTime dateTime = formatter.parseDateTime(extras.getString("ghostExpiration"));
                DateTime now = new DateTime();
                LocalDate today = now.toLocalDate();
                DateTime startOfToday = today.toDateTimeAtCurrentTime(now.getZone());
                Days day = Days.daysBetween(startOfToday, dateTime);
                int difference = day.getDays();
                if (difference > 1) {
                    expireTimer.setText("expires in " + Integer.toString(difference) + " days");
                } else if (difference == 1) {
                    expireTimer.setText("expires in " + Integer.toString(difference) + " day");
                } else if (difference == 0) {
                    Hours hours = Hours.hoursBetween(startOfToday, dateTime);
                    difference = hours.getHours();
                    if (difference > 1) {
                        expireTimer.setText("expires in " + Integer.toString(difference) + " hours");
                    } else if (difference == 1) {
                        expireTimer.setText("expires in " + Integer.toString(difference) + " hour");
                    } else if (difference == 0) {
                        Minutes minutes = Minutes.minutesBetween(startOfToday, dateTime);
                        difference = minutes.getMinutes();
                        if (difference == 1) {
                            expireTimer.setText("expires in " + Integer.toString(difference) + " minute");
                        } else if (difference <= 0) {
                            expireTimer.setText("This number has expired");
                        } else {
                            expireTimer.setText("expires in " + Integer.toString(difference) + " minutes");
                        }

                    }
                }

            } catch (IllegalArgumentException e) {
                expireTimer.setText("");
            }
        }

        /*purchaseButton = (ImageView) findViewById(R.id.purchaseButton);
        purchaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent toPurchase = new Intent(HistoryScreen.this, SelectPackageScreen.class);
                toPurchase.putExtra(Constants.PACKAGE_TYPE, "credits");
                startActivity(toPurchase);
            }
        });*/

        historyList = (ListView) findViewById(R.id.historyList);

        ImageView extendButton = (ImageView) findViewById(R.id.extendButton);
        extendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toPurchaseScreen = new Intent(getApplicationContext(), SelectPackageScreen.class);
                toPurchaseScreen.putExtra(Constants.PACKAGE_TYPE, "extend");
                toPurchaseScreen.putExtra("GhostID", extras.getString("ghostIDExtra"));
                startActivity(toPurchaseScreen);
                finish();
            }
        });

        ImageView ivSetting = (ImageView) findViewById(R.id.ivSetting);
        ivSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent settingScreen = new Intent(getApplicationContext(), SettingScreenActivity.class);
                String tempPeriod = expireTimer.getText().toString().trim();
                if (tempPeriod.equals("This number has expired")) {
                    settingScreen.putExtra("count", "0");
                    settingScreen.putExtra("time_unit", "0");
                } else if (tempPeriod.contains("expires in") && tempPeriod.contains("minute")) {
                    Scanner in = new Scanner(tempPeriod).useDelimiter("[^0-9]+");
                    settingScreen.putExtra("count", in.nextInt());
                    if (tempPeriod.contains("minutes"))
                        settingScreen.putExtra("time_unit", "minutes");
                    else settingScreen.putExtra("time_unit", "minute");
                } else if (tempPeriod.contains("expires in") && tempPeriod.contains("hour")) {
                    Scanner in = new Scanner(tempPeriod).useDelimiter("[^0-9]+");
                    settingScreen.putExtra("count", in.nextInt());
                    if (tempPeriod.contains("hours"))
                        settingScreen.putExtra("time_unit", "hours");
                    else settingScreen.putExtra("time_unit", "hour");
                } else if (tempPeriod.contains("expires in") && tempPeriod.contains("day")) {
                    Scanner in = new Scanner(tempPeriod).useDelimiter("[^0-9]+");
                    settingScreen.putExtra("count", in.nextInt());
                    if (tempPeriod.contains("days"))
                        settingScreen.putExtra("time_unit", "days");
                    else settingScreen.putExtra("time_unit", "day");
                }

                settingScreen.putExtra("number", userNumber.getText().toString().trim());
                settingScreen.putExtra("ghostIDExtra", extras.getString("ghostIDExtra"));
                settingScreen.putExtra("ghostNameExtra", extras.getString("ghostNameExtra"));

                startActivity(settingScreen);
                finish();
            }
        });

        ghostDatabaseAdapter = new GhostCallDatabaseAdapter(getApplicationContext());
        try {
            ghostDatabaseAdapter.open();
            gHistoryList = ghostDatabaseAdapter.getCallHistory(extras.getString("ghostIDExtra"), currentAmount);
            historyAdapter = new HistoryAdapter(this, gHistoryList);
            historyList.setAdapter(historyAdapter);
            ghostDatabaseAdapter.close();
        } catch (SQLException e) {
            // TODO DO SOMETHING
        }

        historyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int position, long id) {
                HistoryObject historyObject = (HistoryObject) adapterView.getItemAtPosition(position);
                if (historyObject.getHistoryType().equals("call")) {
                    if (historyObject.getHistoryRecord().equals("1")) {

                        for (int i = 0; i < gHistoryList.size(); i++) {
                            if (i != position) {
                                if (gHistoryList.get(i).getHistoryRecord().equals("1")) {
                                    gHistoryList.get(i).setHistoryState("not_playing");
                                    ensureMediaPlayerDeath();
                                }
                            }
                        }
                        if (!gHistoryList.get(position).getHistoryState().equals("playing")) {
                            gHistoryList.get(position).setHistoryState("loading");
                            historyAdapter.notifyDataSetChanged();
                            ensureMediaPlayerDeath();
                            mediaPlayer = new MediaPlayer();

                            service.getMP3(historyObject.getHistoryID(), apiKey, new Callback<Response>() {
                                @Override
                                public void success(Response response, Response response2) {
                                    Log.d("TESTING MP3 API", "SUCCESS");
                                    Uri url = Uri.parse("http://www.ghostcall.in/api/playback/call/" + gHistoryList.get(position).getHistoryID() + "/mp3?api_key=" + apiKey);
                                    Map<String, String> headers = new HashMap<String, String>();
                                    headers.put("X-api-key", apiKey);

                                    try {
                                        //     mediaPlayer.setDataSource(getApplicationContext(), url, headers);
                                        if (mediaPlayer != null) {
                                            mediaPlayer.reset();
                                            mediaPlayer.setDataSource(getApplicationContext(), url);
                                            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                            mediaPlayer.prepareAsync();
                                        }
                                    } catch (IOException e) {
                                        Toast.makeText(getApplicationContext(), "Fail to load recording", Toast.LENGTH_SHORT).show();
                                        gHistoryList.get(position).setHistoryState("not_playing");
                                        ensureMediaPlayerDeath();
                                        historyAdapter.notifyDataSetChanged();
                                    }
                                    if (mediaPlayer != null) {
                                        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                            @Override
                                            public void onPrepared(MediaPlayer mp) {
                                                Log.d("wired headset", Boolean.toString(audioManager.isWiredHeadsetOn()));
                                                if (!audioManager.isWiredHeadsetOn()) {
                                                    audioManager.setSpeakerphoneOn(true);
                                                } else {
                                                    audioManager.setSpeakerphoneOn(false);
                                                }
                                                mp.start();
                                                gHistoryList.get(position).setHistoryState("playing");
                                                historyAdapter.notifyDataSetChanged();
                                            }
                                        });
                                        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                            @Override
                                            public void onCompletion(MediaPlayer mp) {
                                                gHistoryList.get(position).setHistoryState("not_playing");
                                                ensureMediaPlayerDeath();
                                                historyAdapter.notifyDataSetChanged();
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void failure(RetrofitError retrofitError) {
                                    Log.d("TESTING MP3 API", "FAIL");
                                    gHistoryList.get(position).setHistoryState("not_playing");
                                    ensureMediaPlayerDeath();
                                    historyAdapter.notifyDataSetChanged();
                                    Toast.makeText(getApplicationContext(), "Recording failed to load", Toast.LENGTH_SHORT).show();
                                }
                            });
                            historyAdapter.notifyDataSetChanged();
                        } else {
                            gHistoryList.get(position).setHistoryState("not_playing");
                            ensureMediaPlayerDeath();
                            historyAdapter.notifyDataSetChanged();
                        }
                    } else {
                        Intent intent = new Intent(HistoryScreen.this, CallScreenActivity.class);
                        intent.putExtra("callName", extras.getString("ghostName"));
                        if (historyObject.getHistoryDescription().equals("in")) {
                            StringBuilder formatNumber = new StringBuilder(historyObject.getHistoryOutNumber());
                            /*formatNumber.delete(0, 1);
                            formatNumber.replace(3, 5, "-");*/
                            intent.putExtra("toNumber", formatNumber.toString());
                            intent.putExtra("ghostIDExtra", extras.getString("ghostIDExtra"));
                            startActivity(intent);
                        } else {
                            StringBuilder formatNumber = new StringBuilder(historyObject.getHistoryNumber());
                            /*formatNumber.delete(0, 1);
                            formatNumber.replace(3, 5, "-");*/
                            intent.putExtra("toNumber", formatNumber.toString());
                            intent.putExtra("ghostIDExtra", extras.getString("ghostIDExtra"));
                            startActivity(intent);
                        }

                    }
                } else if (historyObject.getHistoryType().equals("message")) {
                    Intent intent = new Intent(HistoryScreen.this, SMSActivity.class);
                    intent.putExtra("callName", extras.getString("ghostName"));
                    if (historyObject.getHistoryNumber().equals(extras.getString("ghostNumberExtra"))) {
                        StringBuilder formatNumber = new StringBuilder(historyObject.getHistoryOutNumber());
                        /*formatNumber.delete(0, 1);
                        formatNumber.replace(3, 5, "-");*/
                        intent.putExtra("toNumber", formatNumber.toString());
                        intent.putExtra("ghostIDExtra", extras.getString("ghostIDExtra"));
                    } else {
                        StringBuilder formatNumber = new StringBuilder(historyObject.getHistoryNumber());
                        /*formatNumber.delete(0, 1);
                        formatNumber.replace(3, 5, "-");*/
                        intent.putExtra("toNumber", formatNumber.toString());
                        intent.putExtra("ghostIDExtra", extras.getString("ghostIDExtra"));
                    }
                    startActivity(intent);
                }
            }
        });

        Button sendTextButton = (Button) findViewById(R.id.sendTextButton);
        sendTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent(HistoryScreen.this, SMSActivity.class);
                sendIntent.putExtra("ghostIDExtra", extras.getString("ghostIDExtra"));
                startActivity(sendIntent);
            }
        });

        Button sendCallButton = (Button) findViewById(R.id.sendCallButton);
        sendCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HistoryScreen.this, CallScreenActivity.class);
                intent.putExtra("callName", extras.getString("ghostName"));
                intent.putExtra("ghostIDExtra", extras.getString("ghostIDExtra"));
                startActivity(intent);
            }
        });

        historyList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int lastInScreen = firstVisibleItem + visibleItemCount;
                if (lastInScreen == totalItemCount) {
                    if (!isLoading) {
                        isLoading = true;
                        if (gHistoryList != null) {
                            if (currentAmount <= gHistoryList.size()) {
                                Log.d("max amount:", Integer.toString(gHistoryList.size()));
                                Log.d("current amount:", Integer.toString(currentAmount));
                                currentAmount += 10;
                                Log.d("current amount:", Integer.toString(currentAmount));
                                try {
                                    ghostDatabaseAdapter.open();
                                    gHistoryList = ghostDatabaseAdapter.getCallHistory(extras.getString("ghostIDExtra"), currentAmount);
                                    ghostDatabaseAdapter.close();

                                    if (!gHistoryList.isEmpty()) {
                                        if (gHistoryList.size() != 1) {
                                            historyAdapter.getData().clear();
                                            historyAdapter.getData().addAll(gHistoryList);
                                            historyAdapter.notifyDataSetChanged();
                                        } else {
                                            historyAdapter.getData().clear();
                                            historyAdapter.getData().addAll(gHistoryList);
                                            historyAdapter.notifyDataSetChanged();
                                        }
                                    }
                                    isLoading = false;
                                    Log.d("max amount:", Integer.toString(gHistoryList.size()));
                                } catch (Exception e) {
                                    isLoading = false;
                                    Log.d("error in history screen", e.getMessage());
                                }
                            }
                        }
                    }
                }

            }
        });
    }

    @Override
    protected void onDestroy() {
        audioManager.setSpeakerphoneOn(false);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        audioManager.setSpeakerphoneOn(false);
        for (int i = 0; i < gHistoryList.size(); i++) {
            if (gHistoryList.get(i).getHistoryRecord().equals("1")) {
                gHistoryList.get(i).setHistoryState("not_playing");
                ensureMediaPlayerDeath();
                historyAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*GetUserInfo userInfo = new GetUserInfo(this);
        userInfo.getUserData();*/
        if (InternetDialog.haveNetworkConnection(HistoryScreen.this)) {
            new getNumbersTask().execute();
        }

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void ensureMediaPlayerDeath() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
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

    public class getNumbersTask extends AsyncTask<Void, Integer, Void> {

        HttpURLConnection urlConnection = null;
        URL url = null;
        String temp, response = "";
        Uri.Builder builderString;
        InputStream inStream = null;
        int progress_status;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            extras = getIntent().getExtras();

            settings = getSharedPreferences(GHOST_PREF, 0);
            apiKey = settings.getString("api_key", "");
            lastUpdatedTimestamp = settings.getString("lastUpdatedTimestamp", "");
            editor = settings.edit();

            builderString = new Uri.Builder();
            builderString.scheme("http")
                    .authority("www.ghostcall.in")
                    .appendPath("api")
                    .appendPath("numbers");
            if (!lastUpdatedTimestamp.equals("")) {
                builderString.appendQueryParameter("last_timestamp", lastUpdatedTimestamp);
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                progress_status = 0;
                publishProgress(progress_status);
                url = new URL(builderString.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("X-api-key", apiKey);
                urlConnection.setDoInput(true);
                urlConnection.connect();

                inStream = urlConnection.getInputStream();
                BufferedReader bReader = new BufferedReader(new InputStreamReader(inStream));
                while ((temp = bReader.readLine()) != null) {
                    response += temp;
                }

                if (response != null) {
                    try {
                        GhostCallDatabaseAdapter numberAdapter = new GhostCallDatabaseAdapter(getApplicationContext());
                        numberAdapter.open();
                        JSONArray jsonArray = new JSONArray(response);
                        progress_status = 10;
                        publishProgress(progress_status);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jObject = jsonArray.getJSONObject(i);
                            String ghostID = jObject.getString("id");
                            String ghostNumber = jObject.getString("number");
                            String ghostName = jObject.getString("name");
                            String ghostExpire = jObject.getString("expire_on");
                            String ghostVoicemail = jObject.getString("voicemail");
                            String ghostDisableCalls = jObject.getString("disable_calls");
                            String ghostDisableMessages = jObject.getString("disable_messages");
                            JSONArray callArray = jObject.getJSONArray("calls");
                            progress_status = 20;
                            publishProgress(progress_status);
                            if (!(callArray.length() == 0)) {
                                for (int k = 0; k < callArray.length(); k++) {
                                    JSONObject jCall = callArray.getJSONObject(k);
                                    String callID = jCall.getString("id");
                                    String callUserID = jCall.getString("user_id");
                                    String callNumberID = jCall.getString("number_id");
                                    String callTo = jCall.getString("to");
                                    String callFrom = jCall.getString("from");
                                    String callDirection = jCall.getString("direction");
                                    String callStatus = jCall.getString("status");
                                    String callPitch = jCall.getString("pitch");
                                    String callBackgroundID = jCall.getString("background_item_id");
                                    String callDuration = jCall.getString("duration");
                                    String callResourceID = jCall.getString("resource_id");
                                    String callRecord = jCall.getString("record");
                                    String callCreatedAt = jCall.getString("created_at");
                                    String callUpdatedAt = jCall.getString("updated_at");
                                    numberAdapter.createCall(callID, callUserID, callNumberID, callTo, callFrom, callDirection, callStatus, callPitch, callBackgroundID, callDuration, callResourceID, callRecord, callCreatedAt, callUpdatedAt);
                                }
                            }
                            progress_status = 30;
                            publishProgress(progress_status);
                            JSONArray messageArray = jObject.getJSONArray("messages");
                            if (!(messageArray.length() == 0)) {
                                for (int j = 0; j < messageArray.length(); j++) {
                                    JSONObject jMessage = messageArray.getJSONObject(j);
                                    String messageID = jMessage.getString("id");
                                    String messageUserID = jMessage.getString("user_id");
                                    String messageNumberID = jMessage.getString("number_id");
                                    String messageTo = jMessage.getString("to");
                                    String messageFrom = jMessage.getString("from");
                                    String messageDirection = jMessage.getString("direction");
                                    String messageStatus = jMessage.getString("status");
                                    String messageResourceID = jMessage.getString("resource_id");
                                    String messageText = jMessage.getString("text");
                                    String messageCreatedAt = jMessage.getString("created_at");
                                    String messageupdatedAt = jMessage.getString("updated_at");
                                    String messageDeleted = jMessage.getString("deleted");
                                    numberAdapter.createMessage(messageID, messageUserID, messageNumberID, messageTo, messageFrom, messageDirection,
                                            messageStatus, messageResourceID, messageText, messageCreatedAt, messageupdatedAt, messageDeleted);


                                }
                            }
                            progress_status = 40;
                            publishProgress(progress_status);
                            JSONArray voicemailArray = jObject.getJSONArray("voicemails");
                            if (!(voicemailArray.length() == 0)) {
                                for (int m = 0; m < voicemailArray.length(); m++) {
                                    JSONObject jVoicemail = voicemailArray.getJSONObject(m);
                                    String voicemailID = jVoicemail.getString("id");
                                    String voicemailUserID = jVoicemail.getString("user_id");
                                    String voicemailNumberID = jVoicemail.getString("number_id");
                                    String voicemailCallID = jVoicemail.getString("call_id");
                                    String voicemailTo = jVoicemail.getString("to");
                                    String voicemailFrom = jVoicemail.getString("from");
                                    String voicemailDuration = jVoicemail.getString("duration");
                                    String voicemailResourceID = jVoicemail.getString("resource_id");
                                    String voicemailText = jVoicemail.getString("text");
                                    String voicemailCreatedAt = jVoicemail.getString("created_at");
                                    String voicemailUpdatedAt = jVoicemail.getString("updated_at");
                                    numberAdapter.createVoicemail(voicemailID, voicemailUserID, voicemailNumberID, voicemailCallID, voicemailTo, voicemailFrom, voicemailDuration, voicemailResourceID, voicemailText, voicemailCreatedAt, voicemailUpdatedAt);
                                }
                            }
                            if (!numberAdapter.numberExists(ghostID)) {
                                numberAdapter.createNumber(ghostID, ghostNumber, ghostName, ghostVoicemail, ghostDisableCalls, ghostDisableMessages, ghostExpire);
                            }
                        }

                        String theLatestTimestamp = numberAdapter.getLatestTimestamp();
                        editor.putString("lastUpdatedTimestamp", theLatestTimestamp);
                        editor.apply();
                        lastUpdatedTimestamp = settings.getString("lastUpdatedTimestamp", "");
                        numberAdapter.close();

                    } catch (JSONException e) {

                    } catch (SQLException e) {

                    }
                }
            } catch (Exception e) {
                Log.d("Something fucked up", e.getMessage().toString());
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
            try {
                ghostDatabaseAdapter.open();
                gHistoryList = ghostDatabaseAdapter.getCallHistory(extras.getString("ghostIDExtra"), currentAmount);
                ghostDatabaseAdapter.close();

                if (!gHistoryList.isEmpty()) {
                    if (gHistoryList.size() != 1) {
                        historyAdapter.getData().clear();
                        historyAdapter.getData().addAll(gHistoryList);
                        historyAdapter.notifyDataSetChanged();
                    } else {
                        historyAdapter.getData().clear();
                        historyAdapter.getData().addAll(gHistoryList);
                        historyAdapter.notifyDataSetChanged();
                    }
                }


            } catch (Exception e) {
                Log.d("error in history screen", e.getMessage());
            }
        }
    }
}
