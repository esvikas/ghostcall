package com.kickbackapps.ghostcall.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.kickbackapps.ghostcall.R;

/**
 * Created by Vikash on 1/21/2016.
 */
public class AboutUsFragment extends Fragment {

    private static AboutUsFragment fragment;
    private Activity mActivity;

    private TextView tvAboutUs;

    public static AboutUsFragment getInstance() {
        if (fragment == null) {
            fragment = new AboutUsFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_about_us, container, false);

        tvAboutUs = (TextView) rootView.findViewById(R.id.tvAboutUs);
        tvAboutUs.setText(R.string.terms_message);

        // set Option Menu for Fragment.
        setHasOptionsMenu(true);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                Toast.makeText(mActivity, "Search clicked", Toast.LENGTH_SHORT).show();
                return true;
        }
        return false;
    }
}
