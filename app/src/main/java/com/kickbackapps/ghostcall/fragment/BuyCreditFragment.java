package com.kickbackapps.ghostcall.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.kickbackapps.ghostcall.Constants;
import com.kickbackapps.ghostcall.GhostCallAPIInterface;
import com.kickbackapps.ghostcall.GhostCallDatabaseAdapter;
import com.kickbackapps.ghostcall.R;
import com.kickbackapps.ghostcall.adapters.PackageAdapter;
import com.kickbackapps.ghostcall.objects.GhostPackage;
import com.kickbackapps.ghostcall.ui.AppLockActivity;
import com.kickbackapps.ghostcall.util.IabHelper;
import com.kickbackapps.ghostcall.util.IabResult;
import com.kickbackapps.ghostcall.util.Inventory;
import com.kickbackapps.ghostcall.util.Purchase;

import java.util.List;

import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by viks on 2/1/16.
 */
public class BuyCreditFragment extends Fragment {

    private static BuyCreditFragment fragment;
    ListView packageListView;
    PackageAdapter packageAdapter;
    TextView selectLabel;
    GhostCallDatabaseAdapter nDatabaseAdapter;
    String skuID;
    IabHelper mHelper;
    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener;
    IabHelper.QueryInventoryFinishedListener mReceivedInventoryListener;
    RequestInterceptor requestInterceptor;
    RestAdapter restAdapter;
    GhostCallAPIInterface service;
    List<GhostPackage> ghostPackageList;
    private SharedPreferences settings;
    private String apiKey, productID;
    private Activity mActivity;

    public static BuyCreditFragment getInstance() {
        if (fragment == null) {
            fragment = new BuyCreditFragment();
        }
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        CalligraphyContextWrapper.wrap(mActivity);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.select_package_screen, container, false);

        // set Option Menu for Fragment.
        setHasOptionsMenu(true);

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

        mHelper = new IabHelper(mActivity, Constants.GOOGLE_ACCOUNT_BASE64);
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            @Override
            public void onIabSetupFinished(IabResult result) {
                if (result.isSuccess()) {
                    Log.i("IapHelper result", "isSuccess");
                } else {
                    Log.i("IapHelper result", "isFailure");
                }
            }
        });

        mReceivedInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
            @Override
            public void onQueryInventoryFinished(IabResult result, Inventory inv) {
                if (result.isFailure()) {
                    Log.d("query inventory", "isFailure");
                } else {
                    mHelper.consumeAsync(inv.getPurchase(skuID), mConsumeFinishedListener);
                }
            }
        };

        mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
            @Override
            public void onConsumeFinished(Purchase purchase, IabResult result) {
                if (result.isSuccess()) {

                } else {
                    Toast.makeText(mActivity, result.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        };

        final IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
            @Override
            public void onIabPurchaseFinished(IabResult result, final Purchase info) {
                if (result.isFailure()) {
                    Log.d("IapHelper result", "isFailure Listener");
                } else if (info.getSku().equals(skuID)) {
                    consumeItem();
                    service.purchaseCredits("credits", productID, info.getToken(), info.getOrderId(), new Callback<Response>() {
                        @Override
                        public void success(Response response, Response response2) {

                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (!mActivity.isFinishing()) {
                                        final AlertDialog.Builder alertBox = new AlertDialog.Builder(mActivity);
                                        alertBox.setTitle("Congratulations");
                                        alertBox.setMessage("You have more credits to send text and make calls now");
                                        alertBox.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                            }
                                        });
                                        alertBox.show();
                                    }
                                }
                            });
                        }

                        @Override
                        public void failure(final RetrofitError retrofitError) {
                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (!mActivity.isFinishing()) {
                                        final AlertDialog.Builder alertBox = new AlertDialog.Builder(mActivity);
                                        alertBox.setTitle("Purchase error");
                                        alertBox.setMessage("An error occurred during your purchase. Please contact support to get it resolved. We apologize for the inconvenience");
                                        alertBox.setPositiveButton("Contact Us", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Intent sendEmail = new Intent(Intent.ACTION_SENDTO);
                                                String uriText = "mailto:" + Uri.encode("support@prankdial.com") + "?subject=" + Uri.encode("GhostCall - Credits Purchase Error") +
                                                        "&body=" + Uri.encode(retrofitError.getMessage() + " \n \n Token ID: " + info.getToken() + "\n \n orderID: " + info.getOrderId());
                                                Uri uri = Uri.parse(uriText);
                                                sendEmail.setData(uri);
                                                startActivity(Intent.createChooser(sendEmail, "Send mail..."));
                                            }
                                        });
                                        alertBox.setNegativeButton("Close", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.cancel();
                                            }
                                        });
                                        alertBox.show();
                                    }
                                }
                            });
                        }
                    });
                }
            }
        };

        selectLabel = (TextView) rootView.findViewById(R.id.selectLabel);

        packageListView = (ListView) rootView.findViewById(R.id.packageListView);
        nDatabaseAdapter = new GhostCallDatabaseAdapter(mActivity);

        selectLabel.setText("Select a credits package");
        try {
            nDatabaseAdapter.open();
            ghostPackageList = nDatabaseAdapter.getCreditPackages();
            nDatabaseAdapter.close();
            packageAdapter = new PackageAdapter(mActivity, ghostPackageList);
            packageListView.setAdapter(packageAdapter);

        } catch (Exception e) {
            Log.d("Select Package - Error", e.getMessage());
        }

        packageListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                productID = ghostPackageList.get(position).getPackageID();
                skuID = ghostPackageList.get(position).getPackageAndroidID();
                mHelper.launchPurchaseFlow(mActivity, skuID, 10001, mPurchaseFinishedListener, "mytestpurchase");
            }
        });

        return rootView;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHelper != null) {
            mHelper.dispose();
            mHelper = null;
        }
    }

    public void consumeItem() {
        mHelper.queryInventoryAsync(mReceivedInventoryListener);
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

}
