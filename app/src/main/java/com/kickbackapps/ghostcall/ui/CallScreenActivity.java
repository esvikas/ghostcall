package com.kickbackapps.ghostcall.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.kickbackapps.ghostcall.Constants;
import com.kickbackapps.ghostcall.GetUserInfo;
import com.kickbackapps.ghostcall.GhostCallAPIInterface;
import com.kickbackapps.ghostcall.GhostCallDatabaseAdapter;
import com.kickbackapps.ghostcall.InternetDialog;
import com.kickbackapps.ghostcall.R;
import com.kickbackapps.ghostcall.adapters.BackgroundAdapter;
import com.kickbackapps.ghostcall.adapters.EffectsAdapter;
import com.kickbackapps.ghostcall.customview.dottedprogressbar.DottedProgressBar;
import com.kickbackapps.ghostcall.objects.CallData;
import com.kickbackapps.ghostcall.objects.backgroundeffects.BackgroundObject;
import com.kickbackapps.ghostcall.objects.soundeffects.EffectsObject;
import com.kickbackapps.ghostcall.pjsip.MyPJSIP;
import com.kickbackapps.ghostcall.user.CallStatus;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;
import com.melnykov.fab.FloatingActionButton;
import com.squareup.okhttp.OkHttpClient;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;
import org.pjsip.pjsua2.Account;
import org.pjsip.pjsua2.AudioMedia;
import org.pjsip.pjsua2.Call;
import org.pjsip.pjsua2.CallInfo;
import org.pjsip.pjsua2.CallMediaInfo;
import org.pjsip.pjsua2.CallMediaInfoVector;
import org.pjsip.pjsua2.CallOpParam;
import org.pjsip.pjsua2.Endpoint;
import org.pjsip.pjsua2.Media;
import org.pjsip.pjsua2.OnCallMediaStateParam;
import org.pjsip.pjsua2.OnCallStateParam;
import org.pjsip.pjsua2.OnIncomingCallParam;
import org.pjsip.pjsua2.pjmedia_type;
import org.pjsip.pjsua2.pjsip_inv_state;
import org.pjsip.pjsua2.pjsip_status_code;
import org.pjsip.pjsua2.pjsua_call_media_status;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by Vikash on 1/27/2016.
 */
public class CallScreenActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_CODE_PICK_CONTACTS = 11;
    private static final String GHOST_PREF = "GhostPrefFile";
    private static final String TAG = CallScreen.class.getSimpleName();

    static {
        System.loadLibrary("pjsua2");
        System.out.println("Library loaded");
    }

    LinearLayout vcHolder, bgHolder, effectsHolder;
    RelativeLayout dialpadHolder, spinnerLayout, vcLayout;
    DiscreteSeekBar voiceChangeBar;
    GridView bgGrid, effectsGrid;
    BackgroundAdapter backgroundAdapter;
    EffectsAdapter effectAdapter;
    ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;
    ScheduledFuture scheduledFuture;
    String toCallNumber;
    String resourceID = "";
    String currentCallStatus = "";
    TextView bgText, vcText, effectsText;
    TextView numberBox;
    Bundle extras;
    CardView cvCall;
    Button makeCallButton;
    CardView cvCloseButton;
    Button closeButton;
    FloatingActionButton hangupButton;
    RelativeLayout content_loading;
    MediaPlayer mediaPlayer;
    RequestInterceptor requestInterceptor;
    RestAdapter restAdapter;
    GhostCallAPIInterface service;
    CircleProgressBar progressSpinner;
    String sipUsername;
    String sipPassword;
    OkHttpClient client;
    SharedPreferences.Editor editor;
    int initiatedLimit = 0;
    Account acc;
    Endpoint ep;
    MyCall call;
    AudioManager audioManager;
    boolean isSpeakerOn = false;
    Button toggleSpeakerPhone;
    String credits;
    String toNumberBox;
    boolean incomingSipCall = false;
    int callStatusFailedLimit = 0;
    private Uri uriContact;
    private String contactID;
    private String contactNumber;
    private String apiKey;
    private String toNumber;
    private String numberID;
    private String verified;
    private String voiceChangerNumber = "0";
    private String backgroundID = "0";
    private String effectID = "0";
    private String callMethod, methodCheck;
    private boolean isRecording = false;
    private boolean isChangingVoice = false;
    private boolean isChangingBG = false;
    private boolean isViewingEffects = false;
    private ArrayList<BackgroundObject> backgroundList;
    private ArrayList<EffectsObject> effectsList;
    private SharedPreferences settings;
    private String code = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_call_screen);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.title_activity_call_screen);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        settings = getSharedPreferences(GHOST_PREF, 0);
        editor = settings.edit();
        apiKey = settings.getString("api_key", "");
        sipUsername = settings.getString(Constants.SIP_NAME, "");
        sipPassword = settings.getString(Constants.SIP_PASSWORD, "");
        callMethod = settings.getString(Constants.CALL_METHOD, "");
        methodCheck = settings.getString(Constants.CALL_METHOD_CHECK, "");

        requestInterceptor = new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade request) {
                request.addHeader("X-api-key", apiKey);
            }
        };

        client = new OkHttpClient();

        restAdapter = new RestAdapter.Builder().setEndpoint("http://www.ghostcall.in/api")
                .setRequestInterceptor(requestInterceptor).setClient(new OkClient(client)).build();
        service = restAdapter.create(GhostCallAPIInterface.class);

        GetUserInfo userInfo = new GetUserInfo(this);
        userInfo.getUserDataForCall();

        numberBox = (TextView) findViewById(R.id.tvInputNumber);
        numberBox.setKeyListener(null);

        content_loading = (RelativeLayout) findViewById(R.id.content_loading);

        extras = getIntent().getExtras();
        if (!(extras == null)) {
            numberID = extras.getString("ghostIDExtra");
            if (numberID.equals("0")) {
                verified = "true";
            } else {
                verified = "";
            }
            //numberName.setText(extras.getString("callName"));
            String toNumber = extras.getString("toNumber");
            if (toNumber != null) {
                numberBox.append(toNumber);
                code = toNumber;
            }

            toNumberBox = extras.getString("toNumberBox");
            if (toNumberBox != null) {
                numberBox.setText(toNumberBox);
                code = toNumberBox;
            }

            incomingSipCall = extras.getBoolean("incomingSipCall");

        }

        DottedProgressBar progressBar = (DottedProgressBar) findViewById(R.id.progress);
        progressBar.startProgress();

        dialpadHolder = (RelativeLayout) findViewById(R.id.dialpadLayout);
        spinnerLayout = (RelativeLayout) findViewById(R.id.spinnerLayout);
        progressSpinner = (CircleProgressBar) findViewById(R.id.progressBar);
        progressSpinner.setColorSchemeResources(android.R.color.holo_blue_dark);

        voiceChangeBar = (DiscreteSeekBar) findViewById(R.id.voiceSeekBar);
        voiceChangeBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {
                changeTextColor();
            }
        });

        cvCloseButton = (CardView) findViewById(R.id.cvCloseButton);
        closeButton = (Button) findViewById(R.id.closeVCButton);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                close();
            }
        });

        hangupButton = (FloatingActionButton) findViewById(R.id.hangupButton);
        hangupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                close();
            }
        });

        toggleSpeakerPhone = (Button) findViewById(R.id.speakerToggleButton);
        toggleSpeakerPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isSpeakerOn) {
                    isSpeakerOn = true;
                    audioManager.setSpeakerphoneOn(isSpeakerOn);
                    toggleSpeakerPhone.setText("Turn Speaker Off");
                } else if (isSpeakerOn) {
                    isSpeakerOn = false;
                    audioManager.setSpeakerphoneOn(isSpeakerOn);
                    toggleSpeakerPhone.setText("Turn Speaker On");
                }
            }
        });

        cvCall = (CardView) findViewById(R.id.cvCall);
        makeCallButton = (Button) findViewById(R.id.makeCallButton);
        makeCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                credits = settings.getString("userMins", "0");

                voiceChangerNumber = Integer.toString(voiceChangeBar.getProgress());
                PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
                try {

                    if (!credits.equals("0")) {
                        Phonenumber.PhoneNumber usaNumber = phoneUtil.parse(numberBox.getText().toString(), "US");
                        if (phoneUtil.isValidNumberForRegion(usaNumber, "US")) {

                            content_loading.setVisibility(View.VISIBLE);
                            dialpadHolder.setVisibility(View.GONE);
                            cvCall.setVisibility(View.GONE);
                            hangupButton.setClickable(false);
                            toggleSpeakerPhone.setVisibility(View.VISIBLE);
                            toNumber = phoneUtil.format(usaNumber, PhoneNumberUtil.PhoneNumberFormat.E164);


                            if (callMethod.equals("")) {
                                callMethod = "sip";
                            } else {
                                callMethod = settings.getString(Constants.CALL_METHOD, "");
                            }


                            if (haveNetworkConnection()) {
                                ((TextView) findViewById(R.id.tvNumber)).setText(toNumber);
                                service.makeCall(toNumber, numberID, backgroundID, voiceChangerNumber, Boolean.toString(isRecording), verified, callMethod, new Callback<CallData>() {
                                    @Override
                                    public void success(CallData callData, Response response) {
                                        toCallNumber = callData.getDial();
                                        resourceID = callData.getResourceId();
                                        String removePlus = toNumber.replace("+", "sip:");
                                        String sipCallAddress = removePlus + "@sip.ghostcall.in";

                                        if (callMethod.equals("sip")) {
                                            try {
                                                new makePJSIPCall().execute(toCallNumber);
                                                //numberName.setText("Calling " + numberBox.getText().toString());
                                            } catch (Exception e) {
                                                Log.d("TEST PJSIP ERROR", e.getMessage());
                                            }
                                        } else {
                                            resourceID = callData.getResourceId();
                                            Intent callIntent = new Intent(Intent.ACTION_CALL);
                                            callIntent.setData(Uri.parse("tel:" + toCallNumber));
                                            startActivity(callIntent);
                                            Toast.makeText(getApplicationContext(), "Making Call", Toast.LENGTH_SHORT).show();
                                            Log.d("STARTED CALL", "started call");
                                            initiatedLimit = 0;
                                            callStatusFailedLimit = 0;
                                            scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
                                            scheduledFuture = scheduledThreadPoolExecutor.scheduleAtFixedRate(new CheckCallStatus(), 0, 5, TimeUnit.SECONDS);
                                        }
                                    }

                                    @Override
                                    public void failure(RetrofitError retrofitError) {
                                        Toast.makeText(getApplicationContext(), "Call could not connect to server - Please try again", Toast.LENGTH_SHORT).show();
                                        dialpadHolder.setVisibility(View.VISIBLE);
                                        content_loading.setVisibility(View.GONE);
                                        cvCall.setVisibility(View.VISIBLE);
                                        makeCallButton.setText("Send Call");
                                        makeCallButton.setClickable(true);
                                        toggleSpeakerPhone.setVisibility(View.GONE);
                                    }
                                });
                            } else {
                                InternetDialog.showInternetDialog(CallScreenActivity.this);
                                toggleSpeakerPhone.setVisibility(View.GONE);
                                dialpadHolder.setVisibility(View.VISIBLE);
                                content_loading.setVisibility(View.GONE);
                                cvCall.setVisibility(View.VISIBLE);
                                makeCallButton.setText("Send Call");
                                makeCallButton.setClickable(true);
                            }


                        } else {
                            Toast.makeText(getApplicationContext(), "Invalid Number", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        AlertDialog.Builder alertBox = new AlertDialog.Builder(CallScreenActivity.this);
                        alertBox.setTitle("Insufficient Credits");
                        alertBox.setMessage("Not enough credits available to make this call");
                        alertBox.setPositiveButton("Got It", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        alertBox.setNegativeButton("Buy Credits", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent getCredits = new Intent(CallScreenActivity.this, SelectPackageScreen.class);
                                getCredits.putExtra(Constants.PACKAGE_TYPE, "credits");
                                startActivity(getCredits);
                                finish();
                            }
                        });
                        alertBox.show();
                    }

                } catch (NumberParseException e) {
                    Toast.makeText(getApplicationContext(), "Invalid Number", Toast.LENGTH_SHORT).show();
                }
            }
        });

        vcHolder = (LinearLayout) findViewById(R.id.vcHolder);
        vcLayout = (RelativeLayout) findViewById(R.id.vcLayout);
        vcText = (TextView) findViewById(R.id.vcText);

        bgHolder = (LinearLayout) findViewById(R.id.bgHolder);
        bgGrid = (GridView) findViewById(R.id.bgLayout);
        bgGrid.setChoiceMode(GridView.CHOICE_MODE_SINGLE);
        bgText = (TextView) findViewById(R.id.bgText);

        effectsHolder = (LinearLayout) findViewById(R.id.effectsHolder);
        effectsText = (TextView) findViewById(R.id.effectsText);
        effectsGrid = (GridView) findViewById(R.id.effectsLayout);
        effectsGrid.setChoiceMode(GridView.CHOICE_MODE_SINGLE);

        GhostCallDatabaseAdapter databaseAdapter = new GhostCallDatabaseAdapter(CallScreenActivity.this);

        try {
            databaseAdapter.open();
            backgroundList = databaseAdapter.getBackgroundObjects();
            backgroundAdapter = new BackgroundAdapter(this, backgroundList);
            bgGrid.setAdapter(backgroundAdapter);

            effectsList = databaseAdapter.getEffectsObjects();
            effectAdapter = new EffectsAdapter(this, effectsList);
            effectsGrid.setAdapter(effectAdapter);
            databaseAdapter.close();
        } catch (SQLException e) {

        }

        effectsGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                final int positionInt = position;
                for (int i = 0; i < effectsList.size(); i++) {
                    if (i != position) {
                        effectsList.get(i).setEffectsState("");
                        ensureMediaPlayerDeath();
                    }
                }

                if (!effectsList.get(position).getEffectsState().equals("selected")) {
                    effectsList.get(position).setEffectsState("selected");
                    effectID = effectsList.get(position).getEffectsID();
                    mediaPlayer = new MediaPlayer();

                    changeTextColor();

                    if (currentCallStatus.equals("connected")) {
                        service.sendEffects(effectsList.get(position).getEffectsID(), resourceID, new Callback<Response>() {
                            @Override
                            public void success(Response responseTwo, Response response) {
                                Log.d("EFFECTS RESPONSE", "i sent");
                            }

                            @Override
                            public void failure(RetrofitError retrofitError) {
                                Log.d("EFFECTS RESPONSE", "i failed " + retrofitError.getMessage());

                            }
                        });
                        Log.d("sending effects", "effects id = " + effectsList.get(position).getEffectsID() + " resourceID = " + resourceID);
                    }


                    try {
                        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        mediaPlayer.setDataSource(effectsList.get(position).getEffectsURL());
                        if (currentCallStatus.equals("connected")) {
                            float mute = 0;
                            mediaPlayer.setVolume(mute, mute);
                        }
                        mediaPlayer.prepareAsync();
                    } catch (IOException e) {
                        Toast.makeText(getApplicationContext(), "Fail to load sound", Toast.LENGTH_SHORT).show();
                    }

                    mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mp.start();
                        }
                    });

                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            effectsList.get(positionInt).setEffectsState("");
                            ensureMediaPlayerDeath();
                            effectAdapter.notifyDataSetChanged();
                        }
                    });
                    effectAdapter.notifyDataSetChanged();
                } else {
                    effectsList.get(position).setEffectsState("");
                    effectAdapter.notifyDataSetChanged();
                    effectID = "0";
                    changeTextColor();
                }
            }
        });

        bgGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                if (backgroundList.get(position).getBackgroundState() != null) {
                    for (int i = 0; i < backgroundList.size(); i++) {
                        if (i != position) {
                            backgroundList.get(i).setBackgroundState("");
                            if (mediaPlayer != null) {
                                mediaPlayer.stop();
                                mediaPlayer.release();
                                mediaPlayer = null;
                            }
                        }
                    }

                    if (!backgroundList.get(position).getBackgroundState().equals("selected")) {
                        backgroundList.get(position).setBackgroundState("selected");
                        backgroundID = backgroundList.get(position).getBackgroundID();
                        mediaPlayer = new MediaPlayer();

                        changeTextColor();

                        try {
                            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                            mediaPlayer.setDataSource(backgroundList.get(position).getBackgroundURL());
                            mediaPlayer.setLooping(true);
                            mediaPlayer.prepareAsync();
                        } catch (IOException e) {
                            Toast.makeText(getApplicationContext(), "Fail to load sound", Toast.LENGTH_SHORT).show();
                        }

                        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mp) {
                                mp.start();
                            }
                        });

                        backgroundAdapter.notifyDataSetChanged();
                    } else {
                        backgroundList.get(position).setBackgroundState("");
                        backgroundAdapter.notifyDataSetChanged();
                        backgroundID = "0";
                        changeTextColor();
                    }
                }

            }
        });

        vcHolder.setOnClickListener(this);
        bgHolder.setOnClickListener(this);
        effectsHolder.setOnClickListener(this);
    }

    private void close() {
        if (currentCallStatus.equals("connected") || currentCallStatus.equals("connecting") || currentCallStatus.equals("initiated")) {
            if (currentCallStatus.equals("connected")) {
                final AlertDialog.Builder alertBox = new AlertDialog.Builder(CallScreenActivity.this);
                alertBox.setTitle("Hang up call?");
                alertBox.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (currentCallStatus.equals("connected")) {
                            spinnerLayout.setVisibility(View.VISIBLE);
                            service.hangUpCall(resourceID, new Callback<Response>() {
                                @Override
                                public void success(Response response, Response response2) {

                                }

                                @Override
                                public void failure(RetrofitError retrofitError) {

                                }
                            });
                        } else {
                            dialog.cancel();
                        }
                    }
                });
                alertBox.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                alertBox.show();
            } else {
                spinnerLayout.setVisibility(View.VISIBLE);
                service.hangUpCall(resourceID, new Callback<Response>() {
                    @Override
                    public void success(Response response, Response response2) {

                    }

                    @Override
                    public void failure(RetrofitError retrofitError) {

                    }
                });
            }
        } else {
            removeAllViews();
            showDialpad();
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }
            changeTextColor();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (incomingSipCall) {
            incomingSipCall = false;
            if (MyPJSIP.call != null) {
                new answerPJSIPCall().execute();
                removeAllViews();
                isViewingEffects = true;
                effectsGrid.setVisibility(View.VISIBLE);
                cvCloseButton.setVisibility(View.GONE);
                closeButton.setText("Hang Up");
                hangupButton.setClickable(true);
                toggleSpeakerPhone.setVisibility(View.VISIBLE);
                effectsHolder.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                Log.d("This is answering", "yes");
            } else {
                Log.d("This is answering", "no");
                final AlertDialog.Builder missedCallBox = new AlertDialog.Builder(CallScreenActivity.this);
                missedCallBox.setTitle("Missed Call");
                missedCallBox.setMessage("Sorry, you missed the call.");
                missedCallBox.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        incomingSipCall = false;
                    }
                });
                missedCallBox.show();
            }

        }
        service.getCallStatus(null, new Callback<CallStatus>() {
            @Override
            public void success(CallStatus callStatus, Response response) {
                if (haveNetworkConnection()) {
                    if (callStatus != null) {
                        String status = callStatus.getStatus();
                        if (status.equals("connected")) {
                            removeAllViews();
                            isViewingEffects = true;
                            effectsGrid.setVisibility(View.VISIBLE);
                            cvCloseButton.setVisibility(View.VISIBLE);
                            toggleSpeakerPhone.setVisibility(View.VISIBLE);
                            effectsHolder.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                            resourceID = callStatus.getResourceId();
                            toNumber = callStatus.getTo();
                            //numberName.setText("Talking to " + toNumber);
                            initiatedLimit = 0;
                            if (scheduledThreadPoolExecutor == null) {
                                scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
                                scheduledFuture = scheduledThreadPoolExecutor.scheduleAtFixedRate(new CheckCallStatus(), 0, 5, TimeUnit.SECONDS);
                            }
                        }
                    }
                }


            }

            @Override
            public void failure(RetrofitError retrofitError) {
                String error = (retrofitError.getMessage() == null) ? "Call status error" : retrofitError.getMessage();
                Log.d("Call Status failed:", error);
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_one:
                if (code.length() <= 11) {
                    code += "1";
                    numberBox.setText(code);
                }
                ensureMediaPlayerDeath();
                mediaPlayer = mediaPlayer.create(this, R.raw.num_1);
                mediaPlayer.start();
                break;
            case R.id.ll_two:
                if (code.length() <= 11) {
                    code += "2";
                    numberBox.setText(code);
                }
                ensureMediaPlayerDeath();
                mediaPlayer = mediaPlayer.create(this, R.raw.num_2);
                mediaPlayer.start();
                break;
            case R.id.ll_three:
                if (code.length() <= 11) {
                    code += "3";
                    numberBox.setText(code);
                }
                ensureMediaPlayerDeath();
                mediaPlayer = mediaPlayer.create(this, R.raw.num_3);
                mediaPlayer.start();
                break;
            case R.id.ll_four:
                if (code.length() <= 11) {
                    code += "4";
                    numberBox.setText(code);
                }
                ensureMediaPlayerDeath();
                mediaPlayer = mediaPlayer.create(this, R.raw.num_4);
                mediaPlayer.start();
                break;
            case R.id.ll_five:
                if (code.length() <= 11) {
                    code += "5";
                    numberBox.setText(code);
                }
                ensureMediaPlayerDeath();
                mediaPlayer = mediaPlayer.create(this, R.raw.num_5);
                mediaPlayer.start();
                break;
            case R.id.ll_six:
                if (code.length() <= 11) {
                    code += "6";
                    numberBox.setText(code);
                }
                ensureMediaPlayerDeath();
                mediaPlayer = mediaPlayer.create(this, R.raw.num_6);
                mediaPlayer.start();
                break;
            case R.id.ll_seven:
                if (code.length() <= 11) {
                    code += "7";
                    numberBox.setText(code);
                }
                ensureMediaPlayerDeath();
                mediaPlayer = mediaPlayer.create(this, R.raw.num_7);
                mediaPlayer.start();
                break;
            case R.id.ll_eight:
                if (code.length() <= 11) {
                    code += "8";
                    numberBox.setText(code);
                }
                ensureMediaPlayerDeath();
                mediaPlayer = mediaPlayer.create(this, R.raw.num_8);
                mediaPlayer.start();
                break;
            case R.id.ll_nine:
                if (code.length() <= 11) {
                    code += "9";
                    numberBox.setText(code);
                }
                ensureMediaPlayerDeath();
                mediaPlayer = mediaPlayer.create(this, R.raw.num_9);
                mediaPlayer.start();
                break;
            case R.id.ll_contact:
                Intent intent = new Intent(getApplicationContext(), PhoneBookActivity.class);
                startActivityForResult(intent, REQUEST_CODE_PICK_CONTACTS);
                break;
            case R.id.ll_delete:
                if (code.length() > 0)
                    code = code.substring(0, code.length() - 1);
                numberBox.setText(code);
                ensureMediaPlayerDeath();
                mediaPlayer = mediaPlayer.create(this, R.raw.num_11);
                mediaPlayer.start();
                break;
            case R.id.ll_zero:
                if (code.length() <= 11) {
                    code += "0";
                    numberBox.setText(code);
                }
                ensureMediaPlayerDeath();
                mediaPlayer = mediaPlayer.create(this, R.raw.num_0);
                mediaPlayer.start();
                break;
            case R.id.vcHolder:
                if (isChangingVoice) {
                    removeAllViews();
                    isChangingVoice = false;
                    vcHolder.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                    showDialpad();
                } else {
                    removeAllViews();
                    isChangingVoice = true;
                    vcLayout.setVisibility(View.VISIBLE);
                    cvCloseButton.setVisibility(View.VISIBLE);
                    vcHolder.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                }
                changeTextColor();
                break;
            case R.id.bgHolder:
                if (isChangingBG) {
                    removeAllViews();
                    isChangingBG = false;
                    bgHolder.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                    showDialpad();
                    ensureMediaPlayerDeath();
                } else {
                    removeAllViews();
                    isChangingBG = true;
                    bgGrid.setVisibility(View.VISIBLE);
                    cvCloseButton.setVisibility(View.VISIBLE);
                    bgHolder.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                }
                changeTextColor();
                break;
            case R.id.effectsHolder:
                if (isViewingEffects) {
                    removeAllViews();
                    isViewingEffects = false;
                    effectsHolder.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                    showDialpad();
                    ensureMediaPlayerDeath();
                } else {
                    removeAllViews();
                    isViewingEffects = true;
                    effectsGrid.setVisibility(View.VISIBLE);
                    cvCloseButton.setVisibility(View.VISIBLE);
                    effectsHolder.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                }
                changeTextColor();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /*if (requestCode == REQUEST_CODE_PICK_CONTACTS && resultCode == RESULT_OK) {
            uriContact = data.getData();
            contactNumber = retrieveContactNumber();
            contactNumber = contactNumber.replaceAll("[^0-9]", "");
            if (contactNumber.startsWith("1")) {
                contactNumber = contactNumber.replaceFirst("1", "");
            }

            try {
                StringBuilder contactNumberBuilder = new StringBuilder(contactNumber);
                contactNumberBuilder.insert(3, '-');
                contactNumberBuilder.insert(7, '-');
                numberBox.setText(contactNumberBuilder.toString());
            } catch (Exception e) {
                numberBox.setText(contactNumber);
            }
        }*/
        switch (requestCode) {
            case REQUEST_CODE_PICK_CONTACTS:
                if (resultCode == RESULT_OK) {
                    Bundle res = data.getExtras();
                    String resultName = res.getString("name");
                    String resultContact = res.getString("contact");
                    Log.i("Contact: ", resultName + " : " + resultContact);
                    contactNumber = resultContact;
                    try {
                        if (contactNumber != null && !contactNumber.isEmpty())
                            numberBox.setText(contactNumber);
                    } catch (Exception e) {
                        numberBox.setText(contactNumber);
                    }
                }
                break;
        }

    }

    /*private String retrieveContactNumber() {

        String contactNumber;

        // getting contacts ID
        Cursor cursorID = getContentResolver().query(uriContact,
                new String[]{ContactsContract.Contacts._ID},
                null, null, null);

        if (cursorID.moveToFirst()) {

            contactID = cursorID.getString(cursorID.getColumnIndex(ContactsContract.Contacts._ID));
        }

        cursorID.close();

        Log.d(TAG, "Contact ID: " + contactID);

        // Using the contact ID now we will get contact phone number
        Cursor cursorPhone = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},

                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
                        ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,

                new String[]{contactID},
                null);

        if (cursorPhone.moveToFirst()) {
            contactNumber = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            return contactNumber;

        } else {
            cursorPhone.close();
            return "";
        }
    }*/

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onDestroy() {

        if (isSpeakerOn) {
            audioManager.setSpeakerphoneOn(false);
        }
        ensureMediaPlayerDeath();
        super.onDestroy();
    }

    @Override
    public void onPrepareSupportNavigateUpTaskStack(TaskStackBuilder builder) {
        super.onPrepareSupportNavigateUpTaskStack(builder);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_call_screen, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if (methodCheck.equals("")) {
            if (true) {
                menu.getItem(0).setChecked(true);
                editor.putString(Constants.CALL_METHOD, "sip");
                editor.apply();
            } else {
                menu.getItem(1).setChecked(true);
                editor.putString(Constants.CALL_METHOD, "gateway");
                editor.apply();
            }
        } else {
            if (methodCheck.equals("sipCheck")) {
                menu.getItem(0).setChecked(true);
            } else {
                menu.getItem(1).setChecked(true);
            }
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sip_menu_item:
                if (true) {
                    if (!item.isChecked()) {
                        item.setChecked(true);
                        editor.putString(Constants.CALL_METHOD, "sip");
                        editor.putString(Constants.CALL_METHOD_CHECK, "sipCheck");
                        methodCheck = "sipCheck";
                        callMethod = "sip";
                        editor.apply();
                    }
                } else {
                    Toast.makeText(CallScreenActivity.this, "Sorry, your phone does not support SIP calling.", Toast.LENGTH_SHORT).show();
                    item.setChecked(false);
                }
                break;
            case R.id.call_menu_item:
                TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                if (telephonyManager.getSimState() != TelephonyManager.SIM_STATE_READY) {
                    Toast.makeText(CallScreenActivity.this, "No sim card detected", Toast.LENGTH_SHORT).show();
                    item.setChecked(false);
                } else {
                    if (!item.isChecked()) {
                        item.setChecked(true);
                        editor.putString(Constants.CALL_METHOD, "gateway");
                        editor.putString(Constants.CALL_METHOD_CHECK, "simCheck");
                        methodCheck = "simCheck";
                        callMethod = "gateway";
                        editor.apply();
                    }
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void removeAllViews() {
        dialpadHolder.setVisibility(View.GONE);
        cvCall.setVisibility(View.GONE);
        vcLayout.setVisibility(View.GONE);
        bgGrid.setVisibility(View.GONE);
        effectsGrid.setVisibility(View.GONE);
        cvCloseButton.setVisibility(View.GONE);
        toggleSpeakerPhone.setVisibility(View.GONE);
        bgHolder.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        vcHolder.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        effectsHolder.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        spinnerLayout.setVisibility(View.GONE);
        content_loading.setVisibility(View.GONE);
        isChangingBG = false;
        isChangingVoice = false;
        isViewingEffects = false;
    }

    private void showDialpad() {
        dialpadHolder.setVisibility(View.VISIBLE);
        cvCall.setVisibility(View.VISIBLE);
        makeCallButton.setText("Send Call");
        makeCallButton.setClickable(true);
    }

    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    private void changeTextColor() {
        if (!backgroundID.equals("0")) {
            bgText.setTextColor(getResources().getColor(R.color.selectedyellow));
        } else {
            bgText.setTextColor(getResources().getColor(R.color.white));
        }

        if (!effectID.equals("0")) {
            effectsText.setTextColor(getResources().getColor(R.color.selectedyellow));
        } else {
            effectsText.setTextColor(getResources().getColor(R.color.white));
        }

        voiceChangerNumber = Integer.toString(voiceChangeBar.getProgress());
        if (!voiceChangerNumber.equals("0")) {
            vcText.setTextColor(getResources().getColor(R.color.selectedyellow));
        } else {
            vcText.setTextColor(getResources().getColor(R.color.white));
        }
    }

    private void ensureMediaPlayerDeath() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public void onBackPressed() {
        if (currentCallStatus.equals("connected")) {
            AlertDialog.Builder closeBox = new AlertDialog.Builder(CallScreenActivity.this);
            closeBox.setTitle("Leave Call?");
            closeBox.setMessage("Your call will be disconnected");
            closeBox.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            closeBox.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (currentCallStatus.equals("connected")) {
                        spinnerLayout.setVisibility(View.VISIBLE);
                        service.hangUpCall(resourceID, new Callback<Response>() {
                            @Override
                            public void success(Response response, Response response2) {
                                onBackPressed();
                                finish();
                            }

                            @Override
                            public void failure(RetrofitError retrofitError) {
                                onBackPressed();
                                finish();
                            }
                        });
                    } else {
                        dialog.cancel();
                    }
                }
            });

            closeBox.show();
        } else {
            finish();
        }
    }

    public class makePJSIPCall extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {

            try {
                if (!MyPJSIP.ep.libIsThreadRegistered()) {
                    MyPJSIP.ep.libRegisterThread("makeCall");
                }
            } catch (Exception e) {

            }

            String toSipAccount = params[0];
//            call = new MyCall(MyPJSIP.acc, -1);
//            CallOpParam prm = new CallOpParam(true);

            try {
                MyPJSIP.makeCall(toSipAccount);
                callStatusFailedLimit = 0;
                scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
                scheduledFuture = scheduledThreadPoolExecutor.scheduleAtFixedRate(new CheckCallStatus(), 0, 5, TimeUnit.SECONDS);
            } catch (Exception e) {
                Log.d("TEST PJSIP ERROR", e.getMessage());
            }

            return null;
        }
    }

    public class answerPJSIPCall extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            try {
                if (!MyPJSIP.ep.libIsThreadRegistered()) {
                    MyPJSIP.ep.libRegisterThread("incomingCall");
                }
            } catch (Exception e) {

            }

            try {
                MyPJSIP.answerCall();
                callStatusFailedLimit = 0;
                scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
                scheduledFuture = scheduledThreadPoolExecutor.scheduleAtFixedRate(new CheckCallStatus(), 0, 5, TimeUnit.SECONDS);

            } catch (Exception e) {

            }

            return null;
        }
    }

    public class MyAccount extends Account {

        @Override
        public void onIncomingCall(OnIncomingCallParam prm) {
            super.onIncomingCall(prm);
            Log.d("THERE'S A INCOMING CALL", "ANSWER ME!");
            call = new MyCall(acc, prm.getCallId());
            CallOpParam prmi = new CallOpParam(true);
            try {
                prmi.setStatusCode(pjsip_status_code.PJSIP_SC_OK);
                call.answer(prmi);
            } catch (Exception e) {
                Log.d("TEST PJSIP ERROR", e.getMessage());
            }
        }
    }

    public class MyCall extends Call {

        public MyCall(Account acc, int call_id) {
            super(acc, call_id);
        }

        @Override
        public void onCallState(OnCallStateParam prm) {
            try {
                CallInfo ci = getInfo();
                if (ci.getState() == pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED) {
                    this.delete();
                }
            } catch (Exception e) {
                System.out.print(e.getMessage());
                return;
            }
        }

        @Override
        public void onCallMediaState(OnCallMediaStateParam prm) {
            CallInfo ci;
            try {
                ci = getInfo();
            } catch (Exception e) {
                System.out.print(e.getMessage());
                return;
            }

            CallMediaInfoVector cmiv = ci.getMedia();
            for (int i = 0; i < cmiv.size(); i++) {
                CallMediaInfo cmi = cmiv.get(i);
                if (cmi.getType() == pjmedia_type.PJMEDIA_TYPE_AUDIO && (cmi.getStatus() == pjsua_call_media_status.PJSUA_CALL_MEDIA_ACTIVE ||
                        cmi.getStatus() == pjsua_call_media_status.PJSUA_CALL_MEDIA_REMOTE_HOLD)) {
                    Media m = getMedia(i);
                    AudioMedia am = AudioMedia.typecastFromMedia(m);
                    try {
                        ep.audDevManager().getCaptureDevMedia().startTransmit(am);
                        am.startTransmit(ep.audDevManager().getPlaybackDevMedia());
                    } catch (Exception e) {
                        System.out.print(e.getMessage());
                        continue;
                    }

                }
            }
        }
    }

    public class CheckCallStatus implements Runnable {

        @Override
        public void run() {

            service.getCallStatus(resourceID, new Callback<CallStatus>() {
                @Override
                public void success(CallStatus callStatus, Response response) {
                    if (callStatus != null) {
                        Log.d("GHOSTCALL CALL STATUS", callStatus.getStatus());
                        currentCallStatus = callStatus.getStatus();
                        bgHolder.setClickable(false);
                        vcHolder.setClickable(false);
                        effectsHolder.setClickable(false);

                        if (currentCallStatus.equals("initiated")) {
                            initiatedLimit++;
                            cvCall.setVisibility(View.INVISIBLE);
                            cvCloseButton.setVisibility(View.GONE);
                            closeButton.setText("Hang Up");
                            hangupButton.setClickable(true);
                            toggleSpeakerPhone.setVisibility(View.VISIBLE);
                            Log.d("initiated limit", Integer.toString(initiatedLimit));
                            if (initiatedLimit > 8) {
                                initiatedLimit = 0;
                                scheduledFuture.cancel(true);
                                scheduledThreadPoolExecutor.shutdownNow();
                                removeAllViews();
                                closeButton.setText("Close");
                                showDialpad();
                                if (extras != null) {
                                    if (extras.getString("callName") != null) {
                                        //numberName.setText(extras.getString("callName"));
                                    }
                                }
                                bgHolder.setClickable(true);
                                vcHolder.setClickable(true);
                                effectsHolder.setClickable(true);
                                Toast.makeText(CallScreenActivity.this, "Call took too long to connect", Toast.LENGTH_SHORT).show();
                                Log.d("SCHEDULED GOT SHUT DOWN", "SHUT DOWN");
                            }
                        }


                        if (currentCallStatus.equals("connected")) {
                            callStatusFailedLimit = 0;
                            content_loading.setVisibility(View.GONE);
                            removeAllViews();
                            toggleSpeakerPhone.setVisibility(View.VISIBLE);
                            cvCloseButton.setVisibility(View.GONE);
                            closeButton.setText("Hang Up");
                            hangupButton.setClickable(true);
                            effectsGrid.setVisibility(View.VISIBLE);
                            if (!numberBox.getText().equals("")) {
                                //numberName.setText("Talking to " + numberBox.getText().toString());
                            }

                        }

                        if (currentCallStatus.equals("connecting")) {
                            callStatusFailedLimit = 0;
                            cvCall.setVisibility(View.GONE);
                            toggleSpeakerPhone.setVisibility(View.VISIBLE);
                            cvCloseButton.setVisibility(View.GONE);
                            closeButton.setText("Hang Up");
                            hangupButton.setClickable(true);
                            bgHolder.setClickable(false);
                            vcHolder.setClickable(false);
                            effectsHolder.setClickable(false);
                        }

                        if (currentCallStatus.equals("hangup")) {
                            scheduledFuture.cancel(true);
                            scheduledThreadPoolExecutor.shutdownNow();

                            removeAllViews();
                            closeButton.setText("Close");
                            showDialpad();
                            bgHolder.setClickable(true);
                            vcHolder.setClickable(true);
                            effectsHolder.setClickable(true);
                            Log.d("SCHEDULED GOT SHUT DOWN", "SHUT DOWN");

                            GetUserInfo userInfo = new GetUserInfo(CallScreenActivity.this);
                            userInfo.getUserDataForCall();

                            if (extras != null) {
                                if (extras.getString("callName") != null) {
                                    //numberName.setText(extras.getString("callName"));
                                } else {
                                    if (extras.getString("ghostIDExtra") != null) {
                                        GhostCallDatabaseAdapter adapter = new GhostCallDatabaseAdapter(getApplicationContext());
                                        try {

                                            if (extras.getString("ghostIDExtra") != null) {
                                                adapter.open();
                                                //numberName.setText(adapter.getNumberName(extras.getString("ghostIDExtra")));
                                                adapter.close();
                                            }

                                        } catch (SQLException e) {

                                        }

                                    }
                                }
                            }


                        }
                    }


                }

                @Override
                public void failure(RetrofitError retrofitError) {
                    callStatusFailedLimit++;
                    Log.d("call status fail", Integer.toString(callStatusFailedLimit));
                    if (callStatusFailedLimit > 5) {

                        try {
                            if (!MyPJSIP.ep.libIsThreadRegistered()) {
                                MyPJSIP.ep.libRegisterThread("failCall");
                            }
                        } catch (Exception e) {

                        }

                        MyPJSIP.ep.hangupAllCalls();
                        callStatusFailedLimit = 0;
                        scheduledFuture.cancel(true);
                        scheduledThreadPoolExecutor.shutdownNow();

                        AlertDialog.Builder errorBox = new AlertDialog.Builder(CallScreenActivity.this);
                        errorBox.setTitle("Call Disconnected");
                        errorBox.setMessage("Your call has been disconnected");
                        errorBox.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });


                        try {
                            errorBox.show();
                        } catch (Exception e) {
                            Log.d("errorBox exception", e.getMessage());
                        }


                        removeAllViews();
                        closeButton.setText("Close");
                        showDialpad();
                        bgHolder.setClickable(true);
                        vcHolder.setClickable(true);
                        effectsHolder.setClickable(true);
                        Log.d("SCHEDULED GOT SHUT DOWN", "SHUT DOWN");

                        GetUserInfo userInfo = new GetUserInfo(CallScreenActivity.this);
                        userInfo.getUserDataForCall();

                        if (extras != null) {
                            if (extras.getString("callName") != null) {
                                //numberName.setText(extras.getString("callName"));
                            } else {
                                if (extras.getString("ghostIDExtra") != null) {
                                    GhostCallDatabaseAdapter adapter = new GhostCallDatabaseAdapter(getApplicationContext());
                                    try {

                                        if (extras.getString("ghostIDExtra") != null) {
                                            adapter.open();
                                            //numberName.setText(adapter.getNumberName(extras.getString("ghostIDExtra")));
                                            adapter.close();
                                        }

                                    } catch (SQLException e) {

                                    }

                                }
                            }
                        }
                    }
                }
            });
        }
    }
}
