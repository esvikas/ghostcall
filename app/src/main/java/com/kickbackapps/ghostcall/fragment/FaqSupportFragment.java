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
import android.widget.ListView;
import android.widget.Toast;

import com.kickbackapps.ghostcall.R;
import com.kickbackapps.ghostcall.adapters.FaqSupportAdapter;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Vikash on 1/21/2016.
 */
public class FaqSupportFragment extends Fragment {

    private static FaqSupportFragment fragment;
    private Activity mActivity;

    private ListView lvFaq;
    private ArrayList<HashMap<String, String>> arrayList;

    private String[] title = {"Expired or Ghosted number", "Allowing GhostCall access to your contacts",
            "Ghost Number expiration, minutes and text messages", "How to delete call and text history"};
    private String[] content = {"Once a number expires the number is permanently removed from your account. Any unused voice minutes or text messages will remain.",
            "This setting is totally optional but allowing GhostCall access to your contacts makes it easier to call and text people in your contacts.",
            "When you create a new Ghost Number, you have several options for which Ghost Number type you buy, and each one has a different configuration of expiration, minutes and messages.",
            "To quickly delete a conversation or number from your history simply slide left on the number you want to delete."};

    public static FaqSupportFragment getInstance() {
        if (fragment == null) {
            fragment = new FaqSupportFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_faq_support, container, false);

        lvFaq = (ListView) rootView.findViewById(R.id.lvFaq);
        arrayList = new ArrayList<>();

        for (int i = 0; i < title.length; i++) {
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("title", title[i]);
            hashMap.put("content", content[i]);
            arrayList.add(hashMap);
        }

        lvFaq.setAdapter(new FaqSupportAdapter(getActivity(), arrayList));

        // set Option Menu for Fragment.
        //setHasOptionsMenu(true);

        return rootView;
    }

   /* @Override
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
    }*/

}
