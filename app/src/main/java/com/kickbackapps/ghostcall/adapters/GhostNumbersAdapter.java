package com.kickbackapps.ghostcall.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kickbackapps.ghostcall.GhostCallAPIInterface;
import com.kickbackapps.ghostcall.GhostCallDatabaseAdapter;
import com.kickbackapps.ghostcall.R;
import com.kickbackapps.ghostcall.extended.ScrollableListView;
import com.kickbackapps.ghostcall.objects.GhostNumbers;
import com.kickbackapps.ghostcall.ui.HistoryScreen;
import com.kickbackapps.ghostcall.ui.SMSActivity;

import java.sql.SQLException;
import java.util.List;

import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Ynott on 7/8/15.
 */
public class GhostNumbersAdapter extends BaseAdapter {

    RequestInterceptor requestInterceptor;
    RestAdapter restAdapter;
    private LayoutInflater layoutInflater;
    private List<GhostNumbers> ghostNumbersList;
    private Activity activity;
    private String apiKey;
    GhostCallAPIInterface service;

    public GhostNumbersAdapter(Activity activity, List<GhostNumbers> ghostNumbersList, String apiKey) {
        this.activity = activity;
        this.apiKey = apiKey;
        layoutInflater = LayoutInflater.from(activity);
        this.ghostNumbersList = ghostNumbersList;
    }

    @Override
    public int getCount() {
        return ghostNumbersList.size();
    }

    @Override
    public Object getItem(int position) {

        return ghostNumbersList.get(position);
    }

    @Override
    public long getItemId(int position) {

        return position;
    }

    public List<GhostNumbers> getData() {
        return ghostNumbersList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view;
        final ViewHolder holder;
        if (convertView == null) {
            view = layoutInflater.inflate(R.layout.ghost_number_row, parent, false);
            holder = new ViewHolder();
            holder.etUserName = (EditText) view.findViewById(R.id.etUserName);
            holder.etUserName.clearFocus();

            holder.edit = (TextView) view.findViewById(R.id.edit);
            holder.ghostNumber = (TextView) view.findViewById(R.id.ghost_number);
            holder.smsButton = (ImageView) view.findViewById(R.id.smsButton);
            holder.callButton = (ImageView) view.findViewById(R.id.callButton);
            holder.content = (RelativeLayout) view.findViewById(R.id.content);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }

        final GhostNumbers ghostNumbers = ghostNumbersList.get(position);
        holder.etUserName.setText(ghostNumbers.getGhostTitle());
        holder.ghostNumber.setText(ghostNumbers.getGhostNumber());

        holder.content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, HistoryScreen.class);
                intent.putExtra("ghostNumberExtra", ghostNumbers.getGhostNumber());
                intent.putExtra("ghostNameExtra", ghostNumbers.getGhostTitle());
                intent.putExtra("ghostIDExtra", ghostNumbers.getGhostID());
                intent.putExtra("ghostExpiration", ghostNumbers.getExpirationDate());
                intent.putExtra("ghostName", ghostNumbers.getGhostTitle());
                activity.startActivity(intent);
            }
        });

        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.edit.getText().toString().equals("EDIT")) {
                    holder.edit.setText("DONE");
                    holder.edit.setTextColor(activity.getResources().getColor(R.color.md_green_500));

                    holder.etUserName.requestFocus();
                    holder.etUserName.setCursorVisible(true);
                    holder.etUserName.setSelection(holder.etUserName.getText().toString().length());
                    InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(holder.etUserName, InputMethodManager.SHOW_IMPLICIT);
                } else {
                    holder.edit.setText("EDIT");
                    holder.edit.setTextColor(activity.getResources().getColor(R.color.md_red_500));
                    holder.etUserName.clearFocus();

                    //holder.etUserName.setCursorVisible(false);
                    InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(holder.edit.getWindowToken(), 0);

                    startEditProcess(ghostNumbers.getGhostID(), holder.etUserName.getText().toString().trim(), ghostNumbers.getGhostTitle(), holder.etUserName, holder.edit);
                }
            }

        });

        holder.smsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent(activity.getApplicationContext(), SMSActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                sendIntent.putExtra("ghostIDExtra", ghostNumbers.getGhostID());
                activity.startActivity(sendIntent);
            }
        });

        holder.callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callIntent = new Intent(activity.getApplicationContext(), com.kickbackapps.ghostcall.ui.CallScreenActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                callIntent.putExtra("callName", ghostNumbers.getGhostTitle());
                callIntent.putExtra("ghostIDExtra", ghostNumbers.getGhostID());
                activity.startActivity(callIntent);
            }
        });

        holder.etUserName.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    if (holder.edit.getText().toString().equals("DONE")){
                        holder.edit.setTextColor(activity.getResources().getColor(R.color.md_red_500));
                        holder.etUserName.clearFocus();
                        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(holder.edit.getWindowToken(), 0);
                        startEditProcess(ghostNumbers.getGhostID(), holder.etUserName.getText().toString().trim(), ghostNumbers.getGhostTitle(), holder.etUserName, holder.edit);
                    }
                    return true;
                }
                return false;
            }
        });

        return view;
    }

    private void startEditProcess(final String numberID, final String name, final String firstName, final EditText etUserName, final TextView edit) {

        requestInterceptor = new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade request) {
                request.addHeader("X-api-key", apiKey);
            }
        };

        restAdapter = new RestAdapter.Builder().setEndpoint("http://www.ghostcall.in/api")
                .setRequestInterceptor(requestInterceptor).build();
        service = restAdapter.create(GhostCallAPIInterface.class);

        service.changeNumberName(numberID, name, new Callback<Response>() {
            @Override
            public void success(Response result, Response response2) {
                Log.i("response: ", "success");

                edit.setText("EDIT");
                edit.setTextColor(activity.getResources().getColor(R.color.md_red_500));

                try {
                    GhostCallDatabaseAdapter databaseAdapter = new GhostCallDatabaseAdapter(activity);
                    databaseAdapter.open();
                    databaseAdapter.editNumberName(numberID, name);

                    ghostNumbersList = databaseAdapter.getUserNumbers();
                    databaseAdapter.close();
                    notifyDataSetChanged();

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void failure(final RetrofitError retrofitError) {
                Log.i("response: ", "failed" + retrofitError);
                etUserName.setText(firstName);
            }
        });
    }

    private class ViewHolder {
        public EditText etUserName;
        public TextView edit;
        public TextView ghostNumber;
        public RelativeLayout content;
        public ImageView smsButton, callButton;
    }

}
