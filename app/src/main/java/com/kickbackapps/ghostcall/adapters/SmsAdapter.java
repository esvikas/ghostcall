package com.kickbackapps.ghostcall.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.kickbackapps.ghostcall.R;
import com.kickbackapps.ghostcall.objects.SmsObject;

import java.util.List;

/**
 * Created by Ynott on 8/24/15.
 */
public class SmsAdapter extends BaseAdapter {

    public static final int IN_LAYOUT = 0;
    public static final int OUT_LAYOUT = 1;
    public static final int IN_LAYOUT_OLD = 2;
    public static final int OUT_LAYOUT_OLD = 3;

    Context context;
    private LayoutInflater layoutInflater;
    private List<SmsObject> smsObjectList;

    public SmsAdapter(Context context, List<SmsObject> smsObjectList) {
        this.context = context;
        this.smsObjectList = smsObjectList;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return smsObjectList.size();
    }

    @Override
    public Object getItem(int position) {
        return smsObjectList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public List<SmsObject> getData() {
        return smsObjectList;
    }

    @Override
    public int getItemViewType(int position) {
        if (smsObjectList.get(position).getMessageDirection().equals("out")) {
            if (smsObjectList.size() > position + 1)
                if (smsObjectList.get(position + 1).getMessageDirection().equals("out"))
                    return OUT_LAYOUT_OLD;
            return OUT_LAYOUT;
        } else {
            if (smsObjectList.size() > position + 1)
                if (!smsObjectList.get(position + 1).getMessageDirection().equals("out"))
                    return IN_LAYOUT_OLD;
            return IN_LAYOUT;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 4;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        int type = getItemViewType(position);

        if (convertView == null) {
            if (type == OUT_LAYOUT) {
                convertView = layoutInflater.inflate(R.layout.sms_out_layout, parent, false);
            } else if (type == IN_LAYOUT) {
                convertView = layoutInflater.inflate(R.layout.sms_in_layout, parent, false);
            } else if (type == OUT_LAYOUT_OLD) {
                convertView = layoutInflater.inflate(R.layout.sms_out_layout_old, parent, false);
            } else if (type == IN_LAYOUT_OLD) {
                convertView = layoutInflater.inflate(R.layout.sms_in_layout_old, parent, false);
            }

            holder = new ViewHolder();
            holder.messageText = (TextView) convertView.findViewById(R.id.message_view);
            holder.messageDate = (TextView) convertView.findViewById(R.id.date_view);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final SmsObject smsObject = smsObjectList.get(position);

        holder.messageDate.setText(smsObject.getMessageDate());

        holder.messageText.setText(smsObject.getMessageText());

        if (smsObject.getMessageDirection().equals("out")) {
            holder.messageText.getBackground().setColorFilter(context.getResources().getColor(R.color.bg_register), PorterDuff.Mode.SRC_ATOP);
            holder.messageText.setTextColor(context.getResources().getColor(R.color.white));
        } else if (smsObject.getMessageDirection().equals("in")) {
            holder.messageText.getBackground().setColorFilter(context.getResources().getColor(R.color.chat_bubble), PorterDuff.Mode.SRC_ATOP);
            holder.messageText.setTextColor(context.getResources().getColor(R.color.txt_dark));
        }

        return convertView;
    }

    private class ViewHolder {
        public TextView messageText, messageDate;
    }
}
