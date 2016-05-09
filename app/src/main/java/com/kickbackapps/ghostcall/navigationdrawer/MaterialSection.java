package com.kickbackapps.ghostcall.navigationdrawer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kickbackapps.ghostcall.R;

/**
 * Navigation Drawer section with Material Design style
 */
public class MaterialSection implements View.OnTouchListener {

    public static final int TARGET_FRAGMENT = 0;
    public static final int TARGET_ACTIVITY = 1;
    public static final int TARGET_LISTENER = 2;
    private Context mContext;

    private int position;
    private View view;
    private TextView text;
    private ImageView icon;
    private ImageView rightIcon;
    private MaterialSectionListener listener;
    private boolean isSelected;
    private int targetType;
    private boolean toolbarColor;

    private int colorPressed;
    private int colorUnpressed;
    private int colorSelected;

    private int iconColor;
    private int iconSelectedColor;

    private String title;
    private Fragment targetFragment;
    private Intent targetIntent;
    private View.OnClickListener targetListener;

    private int toolbarPrimaryColor;
    private int toolbarPrimaryColorDark;

    public MaterialSection(Context context, int position, boolean hasIcon, boolean hasRightIcon, int target) {

        mContext = context;
        if (!hasIcon) {
            //if(!hasRightIcon) {
            view = LayoutInflater.from(context).inflate(R.layout.layout_material_section, null);

            text = (TextView) view.findViewById(R.id.section_text);
            /*}else{
                view = LayoutInflater.from(context).inflate(R.layout.layout_material_section_righticon, null);

                text = (TextView) view.findViewById(R.id.section_text);
                rightIcon = (ImageView) view.findViewById(R.id.section_righticon);
            }*/
        } else {
            if (!hasRightIcon) {
                view = LayoutInflater.from(context).inflate(R.layout.layout_material_section_icon, null);

                text = (TextView) view.findViewById(R.id.section_text);
                icon = (ImageView) view.findViewById(R.id.section_icon);
            } else {
                view = LayoutInflater.from(context).inflate(R.layout.layout_material_section_icon_righticon, null);

                text = (TextView) view.findViewById(R.id.section_text);
                icon = (ImageView) view.findViewById(R.id.section_icon);
                rightIcon = (ImageView) view.findViewById(R.id.section_righticon);
            }
        }

        view.setOnTouchListener(this);


        colorPressed = Color.parseColor("#16000000");
        colorUnpressed = Color.parseColor("#00FFFFFF");
        colorSelected = Color.parseColor("#0A000000");
        iconColor = Color.rgb(98, 98, 98);
        iconSelectedColor = context.getResources().getColor(R.color.colorPrimary);
        this.position = position;
        isSelected = false;
        toolbarColor = false;
        targetType = target;

        text.setTextColor(iconColor);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (!isSelected) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                view.setBackgroundColor(colorPressed);
                return true;
            }
            if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                view.setBackgroundColor(mContext.getResources().getColor(android.R.color.transparent));
                return true;
            }

            if (event.getAction() == MotionEvent.ACTION_UP) {
                isSelected = true;
                view.setBackgroundColor(colorSelected);

                text.setTextColor(iconColor);

                if (icon != null) {
                    icon.setColorFilter(iconColor);
                }

                if (listener != null)
                    listener.onClick(this);

                return true;
            }
        }
        return false;
    }

    public void select() {
        isSelected = true;
        view.setBackgroundColor(colorSelected);

        /*if (sectionColor)
            text.setTextColor(iconSelectedColor);*/
        text.setTextColor(iconSelectedColor);

        if (icon != null)
            icon.setColorFilter(iconSelectedColor);

    }

    public void unSelect() {
        isSelected = false;
        view.setBackgroundColor(colorUnpressed);

        /*if (sectionColor)
            text.setTextColor(iconColor);*/
        text.setTextColor(iconColor);

        if (icon != null)
            icon.setColorFilter(iconColor);

        if (rightIcon != null)
            rightIcon.setColorFilter(iconColor);

    }

    public int getPosition() {
        return position;
    }

    public void setOnClickListener(MaterialSectionListener listener) {
        this.listener = listener;
    }

    public View getView() {
        return view;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        this.text.setText(Html.fromHtml(title));
    }

    public void setIcon(Drawable icon) {
        this.icon.setImageDrawable(icon);
        this.icon.setColorFilter(iconColor);
    }

    public void setRightIcon(Drawable icon) {
        this.rightIcon.setImageDrawable(icon);
        this.rightIcon.setColorFilter(iconColor);
    }

    public void setIcon(Bitmap icon) {
        this.icon.setImageBitmap(icon);
        this.icon.setColorFilter(iconColor);
    }

    public void setTarget(Fragment target) {
        this.targetFragment = target;
    }

    public void setTarget(Intent intent) {
        this.targetIntent = intent;
    }

    public int getTarget() {
        return targetType;
    }

    public void setTarget(View.OnClickListener listener) {
        this.targetListener = listener;
    }

    public Fragment getTargetFragment() {
        return targetFragment;
    }

    public Intent getTargetIntent() {
        return targetIntent;
    }

    public View.OnClickListener getTargetListener() {
        return targetListener;
    }

    /*public MaterialSection setSectionColor(int color) {
        sectionColor = true;
        iconColor = color;
        return this;
    }*/

    /*public boolean hasSectionColor() {
        return sectionColor;
    }*/

    public int getSectionColor() {
        return iconColor;
    }

    /*public MaterialSection setSectionColor(Resources.Theme theme) {
        sectionColor = true;
        // get primary color
        TypedValue tvPrimaryColor = new TypedValue();
        theme.resolveAttribute(R.attr.colorPrimary, tvPrimaryColor, true);
        iconColor = tvPrimaryColor.data;
        return this;
    }*/

    public MaterialSection setToolbarColor(int primaryColor, int primaryColorDark) {
        toolbarColor = true;
        toolbarPrimaryColor = primaryColor;
        toolbarPrimaryColorDark = primaryColorDark;
        return this;
    }

    public boolean hasToolbarColor() {
        return toolbarColor;
    }

    public int getToolbarPrimaryColor() {
        return toolbarPrimaryColor;
    }

    public int getToolbarPrimaryColorDark() {
        return toolbarPrimaryColorDark;
    }
}
