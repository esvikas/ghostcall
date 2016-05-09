package com.kickbackapps.ghostcall.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kickbackapps.ghostcall.R;
import com.kickbackapps.ghostcall.objects.GhostPackage;

import java.util.List;


public class PackageAdapter extends BaseAdapter {

    private LayoutInflater layoutInflater;
    private List<GhostPackage> ghostPackage;


    public PackageAdapter(Context context, List<GhostPackage> ghostPackage) {

        layoutInflater = LayoutInflater.from(context);
        this.ghostPackage = ghostPackage;
    }

    @Override
    public int getCount() {

        return ghostPackage.size();
    }

    @Override
    public Object getItem(int position) {

        return ghostPackage.get(position);
    }

    @Override
    public long getItemId(int position) {

        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view;
        ViewHolder holder;
        if (convertView == null) {
            view = layoutInflater.inflate(R.layout.package_row, parent, false);
            holder = new ViewHolder();
            holder.tvPackageName = (TextView) view.findViewById(R.id.tvPackageName);

            holder.rlDate = (RelativeLayout) view.findViewById(R.id.rlDate);
            holder.tvDate = (TextView) view.findViewById(R.id.tvDate);

            holder.tvSms = (TextView) view.findViewById(R.id.tvSms);
            holder.rlSms = (RelativeLayout) view.findViewById(R.id.rlSms);

            holder.tvCall = (TextView) view.findViewById(R.id.tvCall);
            holder.rlCall = (RelativeLayout) view.findViewById(R.id.rlCall);

            holder.tvCredit = (TextView) view.findViewById(R.id.tvCredit);

            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }

        GhostPackage gPackage = ghostPackage.get(position);
        holder.tvPackageName.setText(gPackage.getPackageName());
        holder.tvCall.setText("");
        holder.tvCredit.setText("$" + gPackage.getPackagePrice());

        if (gPackage.getPackageType().equals("new")) {
            holder.tvDate.setText("Expires in " + gPackage.getPackageTime() + " days");
            holder.tvCall.setText(gPackage.getPackageCredits() + " Minutes Free");
            holder.rlSms.setVisibility(View.GONE);
        } else if (gPackage.getPackageType().equals("extend")) {
            holder.tvDate.setText(gPackage.getPackageTime() + " days");
            holder.tvCall.setText(gPackage.getPackageCredits() + " Minutes Free");
            holder.rlSms.setVisibility(View.GONE);
        } else if (gPackage.getPackageType().equals("credits")) {
            String currentText = gPackage.getPackageTime();
            String[] splitedText = currentText.split("/");
            String callText = splitedText[0];
            String smsText = splitedText[1];

            //Replace all non-digit with blank: the remaining string contains only digits.
            int call = Integer.parseInt(callText.replaceAll("[\\D]", ""));
            int sms = Integer.parseInt(smsText.replaceAll("[\\D]", ""));

            holder.tvSms.setText(String.valueOf(sms) + " SMS");
            holder.tvCall.setText(String.valueOf(call) + " Minutes");
            holder.rlDate.setVisibility(View.GONE);
        }


        return view;
    }

    private class ViewHolder {
        public TextView tvPackageName, tvDate, tvSms, tvCall, tvCredit;
        public RelativeLayout rlDate, rlSms, rlCall;
    }
}
