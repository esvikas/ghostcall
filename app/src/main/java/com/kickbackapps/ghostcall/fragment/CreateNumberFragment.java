package com.kickbackapps.ghostcall.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.kickbackapps.ghostcall.Constants;
import com.kickbackapps.ghostcall.GhostCallAPIInterface;
import com.kickbackapps.ghostcall.R;
import com.kickbackapps.ghostcall.objects.AreaCodeObject;
import com.kickbackapps.ghostcall.ui.SelectPackageScreen;
import com.kickbackapps.ghostcall.util.CSVFile;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;

import java.io.InputStream;
import java.util.List;
import java.util.Random;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;

/**
 * Created by Vikash on 1/20/2016.
 */
public class CreateNumberFragment extends Fragment implements View.OnClickListener {

    private static CreateNumberFragment fragment;
    SharedPreferences settings;
    String apiKey;
    RequestInterceptor requestInterceptor;
    RestAdapter restAdapter;
    GhostCallAPIInterface service;
    RelativeLayout rlHead;
    RelativeLayout rlTail;
    EditText etNumber;
    Button btnCreateNumber;
    Button btnRandom;
    RelativeLayout spinnerLayout;
    CircleProgressBar progressSpinner;
    private Activity mActivity;

    public static CreateNumberFragment getInstance() {
        if (fragment == null) {
            fragment = new CreateNumberFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_create_number, container, false);

        rlHead = (RelativeLayout) rootView.findViewById(R.id.rlHead);
        rlTail = (RelativeLayout) rootView.findViewById(R.id.rlTail);
        etNumber = (EditText) rootView.findViewById(R.id.etNumber);
        btnCreateNumber = (Button) rootView.findViewById(R.id.btnCreateNumber);
        btnRandom = (Button) rootView.findViewById(R.id.btnRandom);

        spinnerLayout = (RelativeLayout) rootView.findViewById(R.id.spinnerLayout);
        progressSpinner = (CircleProgressBar) rootView.findViewById(R.id.progressBar);
        progressSpinner.setColorSchemeResources(android.R.color.holo_blue_dark);

        btnCreateNumber.setOnClickListener(this);
        btnRandom.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnCreateNumber:
                if (btnCreateNumber.getText().toString().length() < 3) {
                    Toast.makeText(getActivity(), "Please insert a valid area code", Toast.LENGTH_SHORT).show();
                } else {
                    new checkAreaCodeTask().execute(etNumber.getText().toString());
                }
                break;
            case R.id.btnRandom:
                new checkAreaCodeTask().execute(generateNumber());
                break;
        }
    }


    public String generateNumber() {
        InputStream inputStream = getResources().openRawResource(R.raw.area_codes);
        CSVFile csvFile = new CSVFile(inputStream);
        List areaList = csvFile.read();

        Random r = new Random();
        int rand = r.nextInt(333);

        String areaCode = areaList.get(rand).toString();
        Log.i("Area code ===", areaCode);
        return areaCode;
    }

    public class checkAreaCodeTask extends AsyncTask<String, Void, Void> {
        String areaCode;
        String nickName;
        AreaCodeObject areaCodeObject;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            nickName = "Nick name";
            spinnerLayout.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(String... strings) {

            areaCode = strings[0];

            settings = mActivity.getSharedPreferences(Constants.GHOST_PREF, 0);
            apiKey = settings.getString("api_key", "");

            requestInterceptor = new RequestInterceptor() {
                @Override
                public void intercept(RequestFacade request) {
                    request.addHeader("X-api-key", apiKey);
                }
            };

            restAdapter = new RestAdapter.Builder().setEndpoint("http://www.ghostcall.in/api")
                    .setRequestInterceptor(requestInterceptor).build();
            service = restAdapter.create(GhostCallAPIInterface.class);

            try {
                areaCodeObject = service.getAreaCodeStatus(areaCode);
            } catch (Exception e) {
                //Log.e("GhostCall-Retrofit", e.getMessage() + "");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (areaCodeObject != null) {
                if (areaCodeObject.getAvailable() != null) {
                    if (areaCodeObject.getAvailable()) {
                        spinnerLayout.setVisibility(View.INVISIBLE);
                        Intent toGetNumberScreen = new Intent(mActivity, SelectPackageScreen.class);
                        toGetNumberScreen.putExtra("nickName", nickName);
                        toGetNumberScreen.putExtra(Constants.PACKAGE_TYPE, "new");
                        toGetNumberScreen.putExtra("areacode", areaCode);
                        startActivity(toGetNumberScreen);
                    }
                } else if (areaCodeObject.getCategory() != null) {
                    if (areaCodeObject.getCategory().equals("area_code_not_available")) {
                        Toast.makeText(mActivity, "This area code is not available, please try again", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Toast.makeText(mActivity, "This area code is not available, please try again", Toast.LENGTH_SHORT).show();
                spinnerLayout.setVisibility(View.INVISIBLE);
            }
        }
    }
}
