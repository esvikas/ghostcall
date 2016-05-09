package com.kickbackapps.ghostcall.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kickbackapps.ghostcall.Constants;
import com.kickbackapps.ghostcall.GhostCallAPIInterface;
import com.kickbackapps.ghostcall.GhostCallDatabaseAdapter;
import com.kickbackapps.ghostcall.InternetDialog;
import com.kickbackapps.ghostcall.R;
import com.kickbackapps.ghostcall.adapters.GhostNumbersAdapter;
import com.kickbackapps.ghostcall.extended.ScrollableListView;
import com.kickbackapps.ghostcall.objects.GhostNumbers;
import com.kickbackapps.ghostcall.pjsip.MyPJSIPService;
import com.kickbackapps.ghostcall.ui.AppLockActivity;
import com.kickbackapps.ghostcall.ui.MainScreenActivity;
import com.kickbackapps.ghostcall.ui.SelectPackageScreen;
import com.kickbackapps.ghostcall.user.UserData;
import com.melnykov.fab.FloatingActionButton;

import org.joda.time.DateTime;
import org.joda.time.Days;
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

import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Vikash on 1/18/2016.
 */
public class HomeFragment extends Fragment {

    //ImageView purchaseButton;
    public static final String GHOST_PREF = "GhostPrefFile";
    private static HomeFragment fragment;
    OnFabSelectedListener mCallback;

    ScrollableListView ghostNumberListView;
    GhostNumbersAdapter gNumberAdapter;
    GhostCallDatabaseAdapter nDatabaseAdapter;
    TextView userNumber;
    //TextView userRemainingText;
    String ownNumber = "";
    ArrayList<GhostNumbers> gNumberList;
    SharedPreferences.Editor editor;
    SharedPreferences settings;
    UserData userInfo;
    TextView tvCall;
    TextView tvSms;
    //Empty list_view
    RelativeLayout rlEmptyView;
    TextView tvEmptyText;
    private Activity mActivity;
    private String lastUpdatedTimestamp, apiKey;

    public static HomeFragment getInstance() {
        if (fragment == null) {
            fragment = new HomeFragment();
        }
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        userNumber = (TextView) rootView.findViewById(R.id.tvMyNumber);
        //userRemainingText = (TextView) rootView.findViewById(R.id.remainingText);
        //SharedPreferences settings = mActivity.getSharedPreferences(GHOST_PREF, 0);
        //String userSMS = settings.getString("userSMS", "0");
        //String userMins = settings.getString("userMins", "0");

        mActivity.startService(new Intent(mActivity, MyPJSIPService.class));

        //userRemainingText.setText(userSMS + " sms / " + userMins + " mins left");
        /*purchaseButton = (ImageView) rootView.findViewById(R.id.purchaseButton);
        purchaseButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    purchaseButton.setColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_ATOP);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    purchaseButton.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                }
                return false;
            }
        });
        purchaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent toPurchase = new Intent(mActivity, SelectPackageScreen.class);
                toPurchase.putExtra(Constants.PACKAGE_TYPE, "credits");
                startActivity(toPurchase);
            }
        });*/

        settings = mActivity.getSharedPreferences(GHOST_PREF, 0);

        String userSMS = settings.getString("userSMS", "0");
        String userMins = settings.getString("userMins", "0");

        tvCall = (TextView) rootView.findViewById(R.id.tvCall);
        tvSms = (TextView) rootView.findViewById(R.id.tvSms);
        tvCall.setText(userMins);
        tvSms.setText(userSMS);

        rlEmptyView = (RelativeLayout) rootView.findViewById(R.id.rlEmptyView);
        tvEmptyText = (TextView) rootView.findViewById(R.id.tvEmptyText);
        String empty_text = "It looks like you don't have any Ghost Numbers. Click the &quot;<font color=\"#F24C30\">+</font>&quot; button below to get started and hide your real number!";
        tvEmptyText.setText(Html.fromHtml(empty_text));

        ghostNumberListView = (ScrollableListView) rootView.findViewById(R.id.ghostNumberList);
        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fabGhostButton);
        //fab.attachToListView(ghostNumberListView);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(mActivity, GetGhostNumberScreen.class));
                try {
                    mCallback = (OnFabSelectedListener) mActivity;
                    mCallback.onFabSelected();
                } catch (ClassCastException e) {
                    throw new ClassCastException(mActivity.toString()
                            + " must implement OnHeadlineSelectedListener");
                }
            }
        });

        /*ImageView yourSMSButton = (ImageView) rootView.findViewById(R.id.yourSMS);
        yourSMSButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                sendIntent.setData(Uri.parse("sms:"));
                startActivity(sendIntent);
            }
        });*/

        ImageView yourCallButton = (ImageView) rootView.findViewById(R.id.ivCall);
        yourCallButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent callIntent = new Intent(mActivity, com.kickbackapps.ghostcall.ui.CallScreenActivity.class);
                callIntent.putExtra("callName", "Own Number");
                callIntent.putExtra("ghostIDExtra", "0");
                startActivity(callIntent);
            }
        });

        LinearLayout llPackage = (LinearLayout) rootView.findViewById(R.id.llPackage);
        llPackage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent toPurchase = new Intent(mActivity, SelectPackageScreen.class);
                toPurchase.putExtra(Constants.PACKAGE_TYPE, "credits");
                startActivity(toPurchase);
            }
        });

        /*Button getGhostNumbers = (Button) rootView.findViewById(R.id.getGhostButton);
        getGhostNumbers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mActivity, GetGhostNumberScreen.class));
            }
        });*/

        // set Option Menu for Fragment.
        setHasOptionsMenu(true);

        return rootView;
    }

    public void getUserData() {

        apiKey = settings.getString("api_key", "");
        userInfo = new UserData();

        RequestInterceptor requestInterceptor = new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade request) {
                request.addHeader("X-api-key", apiKey);
            }
        };

        RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint("http://www.ghostcall.in/api")
                .setRequestInterceptor(requestInterceptor).build();
        GhostCallAPIInterface service = restAdapter.create(GhostCallAPIInterface.class);

        service.getUserData(new Callback<UserData>() {
            @Override
            public void success(UserData userData, Response response) {
                userInfo.setBalance(userData.getBalance());
                tvCall.setText(userData.getBalance().getMinutes());
                tvSms.setText(userData.getBalance().getSms());

                SharedPreferences.Editor editor = settings.edit();
                editor.putString("userSMS", userData.getBalance().getSms());
                editor.putString("userMins", userData.getBalance().getMinutes());
                editor.commit();
            }

            @Override
            public void failure(RetrofitError retrofitError) {
            }
        });

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_lock, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_lock:
                Intent intent = new Intent(mActivity, AppLockActivity.class);
                startActivity(intent);
                return true;
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        /*GetUserInfo userInfo = new GetUserInfo(mActivity);
        userInfo.getUserData();*/
        getNumberFromSqlite();
        getUserData();
        if (InternetDialog.haveNetworkConnection(mActivity)) {
            new GetNumbersTask().execute();
        }
    }

    private void getNumberFromSqlite() {
        nDatabaseAdapter = new GhostCallDatabaseAdapter(mActivity);
        try {
            nDatabaseAdapter.open();
            ownNumber = nDatabaseAdapter.getUserNumber();
            userNumber.setText(ownNumber);
            MainScreenActivity.setUserNumber(ownNumber);

            gNumberList = nDatabaseAdapter.getUserNumbers();
            nDatabaseAdapter.close();
            Log.i("Array size ===", gNumberList.size() + "");
            if (gNumberList.size() != 0) {
                for (int i = 0; i < gNumberList.size(); i++) {
                    try {
                        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
                        DateTime dateTime = formatter.parseDateTime(gNumberList.get(i).getExpirationDate());
                        DateTime now = new DateTime();
                        LocalDate today = now.toLocalDate();
                        DateTime startOfToday = today.toDateTimeAtCurrentTime(now.getZone());
                        Days day = Days.daysBetween(startOfToday, dateTime);
                        int difference = day.getDays();
                        if (difference <= 0) {
                            Minutes minutes = Minutes.minutesBetween(startOfToday, dateTime);
                            difference = minutes.getMinutes();
                            if (difference <= 0) {
                                gNumberList.remove(i);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            gNumberAdapter = new GhostNumbersAdapter(mActivity, gNumberList, apiKey);
            ghostNumberListView.setAdapter(gNumberAdapter);

            if (gNumberList.size() >= 0) {
                ghostNumberListView.setVisibility(View.VISIBLE);
                rlEmptyView.setVisibility(View.GONE);
            } else {
                rlEmptyView.setVisibility(View.VISIBLE);
                ghostNumberListView.setVisibility(View.GONE);
            }

        } catch (SQLException e) {
            Toast.makeText(mActivity, "error", Toast.LENGTH_SHORT).show();
        }
    }

    // Container Activity must implement this interface
    public interface OnFabSelectedListener {
        public void onFabSelected();
    }

    public class GetNumbersTask extends AsyncTask<Void, Integer, Void> {

        HttpURLConnection urlConnection = null;
        URL url = null;
        String temp, response = "";
        Uri.Builder builderString;
        InputStream inStream = null;
        int progress_status;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

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
                        GhostCallDatabaseAdapter numberAdapter = new GhostCallDatabaseAdapter(mActivity);
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

                    } catch (JSONException | SQLException e) {
                        e.printStackTrace();
                        getNumberFromSqlite();
                    }
                }
            } catch (Exception e) {
                Log.i("Something fucked up", e.getMessage());
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
            Log.i("I'm running", "running..");
            try {
                nDatabaseAdapter.open();
                gNumberList = nDatabaseAdapter.getUserNumbers();
                nDatabaseAdapter.close();

                if (!gNumberList.isEmpty()) {
                    if (gNumberList.size() != 0) {
                        for (int i = 0; i < gNumberList.size(); i++) {
                            try {
                                DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
                                DateTime dateTime = formatter.parseDateTime(gNumberList.get(i).getExpirationDate());
                                DateTime now = new DateTime();
                                LocalDate today = now.toLocalDate();
                                DateTime startOfToday = today.toDateTimeAtCurrentTime(now.getZone());
                                Days day = Days.daysBetween(startOfToday, dateTime);
                                int difference = day.getDays();
                                if (difference <= 0) {
                                    Minutes minutes = Minutes.minutesBetween(startOfToday, dateTime);
                                    difference = minutes.getMinutes();
                                    if (difference <= 0) {
                                        gNumberList.remove(i);
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    if (gNumberList.size() != 1) {
                        gNumberAdapter.getData().clear();
                        gNumberAdapter.getData().addAll(gNumberList);
                        gNumberAdapter.notifyDataSetChanged();
                    } else {
                        gNumberAdapter.getData().clear();
                        gNumberAdapter.getData().addAll(gNumberList);
                        gNumberAdapter.notifyDataSetChanged();
                    }

                }


            } catch (Exception e) {
                Log.d("error in home screen", e.getMessage());
            }


        }
    }
}
