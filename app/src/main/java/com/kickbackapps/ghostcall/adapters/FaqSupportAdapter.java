package com.kickbackapps.ghostcall.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.kickbackapps.ghostcall.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Vikash on 1/21/2016.
 */
public class FaqSupportAdapter extends BaseAdapter{

    private Context context;
    private ViewHolder holder;
    private ArrayList<HashMap<String, String>> arrList;

    public FaqSupportAdapter(Context context, ArrayList<HashMap<String, String>> data) {
        this.context = context;
        this.arrList = data;
    }

    @Override
    public int getCount() {
        return arrList.size();
    }

    @Override
    public Object getItem(int i) {
        return arrList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (view == null) {
            view = inflater.inflate(R.layout.list_item_faq, null, false);
            holder = new ViewHolder();
            holder.tvTitle = (TextView) view.findViewById(R.id.tvTitle);
            holder.tvContent = (TextView) view.findViewById(R.id.tvContent);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        holder.tvTitle.setText(arrList.get(i).get("title"));
        holder.tvContent.setText(arrList.get(i).get("content"));

        return view;
    }

    static class ViewHolder {
        TextView tvTitle;
        TextView tvContent;
    }
}
