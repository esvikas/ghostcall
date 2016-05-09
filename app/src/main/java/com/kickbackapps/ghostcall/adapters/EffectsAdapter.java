package com.kickbackapps.ghostcall.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kickbackapps.ghostcall.R;
import com.kickbackapps.ghostcall.objects.soundeffects.EffectsObject;

import java.util.List;

/**
 * Created by Ynott on 8/11/15.
 */
public class EffectsAdapter extends BaseAdapter {

    private LayoutInflater layoutInflater;
    private List<EffectsObject> effectsObjectList;
    private Context context;

    public EffectsAdapter(Context context, List<EffectsObject> effectsObjectList) {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        this.effectsObjectList = effectsObjectList;
    }

    @Override
    public int getCount() {
        return effectsObjectList.size();
    }

    @Override
    public Object getItem(int position) {
        return effectsObjectList.get(position);
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
            view = layoutInflater.inflate(R.layout.bg_item, parent, false);
            holder = new ViewHolder();
            holder.effectsImage = (ImageView) view.findViewById(R.id.bgImageGrid);
            holder.effectsName = (TextView) view.findViewById(R.id.background_name);
            holder.gridContainer = (RelativeLayout) view.findViewById(R.id.gridContainer);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }

        EffectsObject effectsObject = effectsObjectList.get(position);
        holder.effectsName.setText(effectsObject.getEffectsName());
        String effectsNameModified = effectsObject.getEffectsName().toLowerCase();
        if (effectsNameModified.contains(" "))
            effectsNameModified = effectsNameModified.replace(" ", "");
        int resID = context.getResources().getIdentifier(effectsNameModified, "drawable", context.getPackageName());
        holder.effectsImage.setImageResource(resID);

        if (effectsObject.getEffectsState() != null) {
            if (effectsObject.getEffectsState().equals("selected")) {
                holder.gridContainer.setPressed(true);
                holder.effectsImage.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                holder.effectsName.setTextColor(Color.WHITE);
            } else {
                holder.gridContainer.setPressed(false);
                holder.effectsImage.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
                holder.effectsName.setTextColor(Color.BLACK);
            }
        }

        return view;
    }

    private class ViewHolder {
        public TextView effectsName;
        public ImageView effectsImage;
        public RelativeLayout gridContainer;
    }
}
