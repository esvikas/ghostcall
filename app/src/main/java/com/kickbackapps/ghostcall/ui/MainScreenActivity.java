package com.kickbackapps.ghostcall.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Toast;

import com.kickbackapps.ghostcall.Constants;
import com.kickbackapps.ghostcall.GhostCallDatabaseAdapter;
import com.kickbackapps.ghostcall.InternetDialog;
import com.kickbackapps.ghostcall.R;
import com.kickbackapps.ghostcall.fragment.AboutUsFragment;
import com.kickbackapps.ghostcall.fragment.BuyCreditFragment;
import com.kickbackapps.ghostcall.fragment.CreateNumberFragment;
import com.kickbackapps.ghostcall.fragment.FaqSupportFragment;
import com.kickbackapps.ghostcall.fragment.HomeFragment;
import com.kickbackapps.ghostcall.fragment.PhonebookFragment;
import com.kickbackapps.ghostcall.navigationdrawer.MaterialNavigationDrawer;

import java.sql.SQLException;

/**
 * Created by Vikash on 1/18/2016.
 */
public class MainScreenActivity extends MaterialNavigationDrawer implements HomeFragment.OnFabSelectedListener {

    private static final String GHOST_PREF = "GhostPrefFile";
    Bundle mSavedInstanceState;

    public static void setUserNumber(String ownNumber) {
        setUserEmail(ownNumber);
    }

    @Override
    public void init(Bundle savedInstanceState) {

        View.OnClickListener logoutListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutDialog();
            }
        };

        mSavedInstanceState = savedInstanceState;
        this.clearAllSection();

        // User Information
        this.setDrawerBackground(this.getResources().getDrawable(R.drawable.bg_nav));
        this.setUserPhoto(this.getResources().getDrawable(R.drawable.nav_people));
        this.setUsername("YOUR NUMBER");
        // this.setUserEmail("9843 514804");

        this.addSection(this.newSection(getResources().getString(R.string.app_name),
                HomeFragment.getInstance()), false);
        this.addSection(this.newSection(getResources().getString(R.string.create_ghost_number),
                this.getResources().getDrawable(R.drawable.nav_create_ghost_number),
                CreateNumberFragment.getInstance()), true);
        this.addSection(this.newSection(getResources().getString(R.string.buy_credits),
                this.getResources().getDrawable(R.drawable.nav_buy_credit),
                BuyCreditFragment.getInstance()), true);
        this.addSection(this.newSection(getResources().getString(R.string.phone_book),
                this.getResources().getDrawable(R.drawable.nav_phonebook),
                PhonebookFragment.getInstance()), true);
        this.addSection(this.newSection(getResources().getString(R.string.faq_support),
                this.getResources().getDrawable(R.drawable.nav_faq),
                FaqSupportFragment.getInstance()), true);
        /*this.addSection(this.newSection(getResources().getString(R.string.about_us),
                this.getResources().getDrawable(R.drawable.nav_about_us),
                AboutUsFragment.getInstance()), true);*/
        this.addDivisor();
        this.addSection(this.LogoutSection(getResources().getString(R.string.logout),
                this.getResources().getDrawable(R.drawable.nav_logout),
                logoutListener), true);

        if (!InternetDialog.haveNetworkConnection(this)) {
            InternetDialog.showInternetDialog(this);
        }

    }

    private void showLogoutDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Do you want to logout from GhostCall?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        logout();
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        onBackPressed();
                    }
                })
                .create();
        dialog.show();
    }

    private void logout() {
        try {
            GhostCallDatabaseAdapter databaseAdapter = new GhostCallDatabaseAdapter(this);
            databaseAdapter.open();
            databaseAdapter.deleteAllData();
            databaseAdapter.close();

            getSharedPreferences(GHOST_PREF, 0).edit().clear().commit();
            startActivity(new Intent(this, StartScreen.class));
            finish();
        } catch (SQLException e) {
            e.printStackTrace();
            Toast.makeText(this, "Unable to logout", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (Constants.NAV_MENU_POSITION == 0) {
            showCloseConfirmDialog();
        } else if (Constants.NAV_MENU_POSITION > 0) {
            Constants.NAV_MENU_POSITION = 0;
            super.onClick(getSection(Constants.NAV_MENU_POSITION));
        } else {
            Constants.NAV_MENU_POSITION = 0;
            super.onBackPressed();
        }
    }

    private void showCloseConfirmDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Do you want to exit?")
                        //.setMessage("App will be closed if click yes")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Constants.NAV_MENU_POSITION = -1;
                        onBackPressed();
                        dialog.dismiss();
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

    @Override
    public void onFabSelected() {
        Constants.NAV_MENU_POSITION = 1;
        super.onClick(getSection(Constants.NAV_MENU_POSITION));
    }
}
