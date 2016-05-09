package com.kickbackapps.ghostcall.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Vikash on 1/18/2016.
 */
public class TestFragment extends Fragment {

    private static TestFragment fragment;

    public static TestFragment getInstance() {
        if (fragment == null) {
            fragment = new TestFragment();
        }
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
