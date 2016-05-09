package com.kickbackapps.ghostcall.navigationdrawer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kickbackapps.ghostcall.Constants;
import com.kickbackapps.ghostcall.R;
import com.kickbackapps.ghostcall.extended.CustomAppCompact;

import java.util.LinkedList;
import java.util.List;

/**
 * Activity that implements ActionBarActivity with a Navigation Drawer with Material Design style
 */
@SuppressLint("InflateParams")
public abstract class MaterialNavigationDrawer extends CustomAppCompact implements MaterialSectionListener {

    public static final int BOTTOM_SECTION_START = 100;
    public static final int SECTION_START = 0;
    private static int indexFragment = 0;
    public boolean personalizeToggle = false;
    public boolean examTypeToggle = false;
    private DrawerLayout mDrawerLayout;
    private ActionBar actionBar;
    private ActionBarDrawerToggle mDrawerToggle;
    private Toolbar mToolbar;
    private RelativeLayout drawer;
    private ImageView userphoto;
    private ImageView usercover;
    private TextView username;
    private static TextView usermail;
    private LinearLayout sections;
    private LinearLayout bottomSections;
    private List<MaterialSection> sectionList;
    private List<MaterialSection> bottomSectionList;
    private CharSequence title;
    private float density;
    private int primaryColor;
    private int primaryColorDark;

    @Override
    /**
     * Do not Override this method!!! <br>
     * Use init() instead
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        // init toolbar
        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        // init drawer components
        drawer = (RelativeLayout) this.findViewById(R.id.drawer);
        username = (TextView) this.findViewById(R.id.user_nome);
        usermail = (TextView) this.findViewById(R.id.user_email);
        userphoto = (ImageView) this.findViewById(R.id.user_photo);
        usercover = (ImageView) this.findViewById(R.id.user_cover);
        sections = (LinearLayout) this.findViewById(R.id.sections);
        bottomSections = (LinearLayout) this.findViewById(R.id.bottom_sections);

        // init lists
        sectionList = new LinkedList<>();
        bottomSectionList = new LinkedList<>();

        //get density
        density = this.getResources().getDisplayMetrics().density;

        // get primary color
        Resources.Theme theme = this.getTheme();
        TypedValue tvPrimaryColor = new TypedValue();
        TypedValue tvPrimaryColorDark = new TypedValue();
        theme.resolveAttribute(R.attr.colorPrimary, tvPrimaryColor, true);
        theme.resolveAttribute(R.attr.colorPrimaryDark, tvPrimaryColorDark, true);
        primaryColor = tvPrimaryColor.data;
        primaryColorDark = tvPrimaryColorDark.data;

        init(savedInstanceState);

        // INIT ACTION BAR
        this.setSupportActionBar(mToolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        title = sectionList.get(indexFragment).getTitle();

        mDrawerLayout = (DrawerLayout) this.findViewById(R.id.drawer_layout);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.app_name, R.string.app_name) {

            public void onDrawerClosed(View view) {
                actionBar.setTitle(title);
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu();
            }

        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        setSelection();

    }

    public void setSelection() {
        MaterialSection section = sectionList.get(Constants.NAV_MENU_POSITION);
        section.select();
        setFragment(section.getTargetFragment(), section.getTitle());
    }

    public Toolbar getToolbar() {
        return mToolbar;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
            mDrawerLayout.closeDrawers();
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void setTitle(final CharSequence title) {
        this.title = title;
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                getSupportActionBar().setTitle(title);
            }
        });
    }

    private void setFragment(Fragment fragment, String title) {
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit();
        setTitle(title);
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                mDrawerLayout.closeDrawer(drawer);
            }
        });
    }

    @Override
    public void onClick(MaterialSection section) {
        switch (section.getTarget()) {
            case MaterialSection.TARGET_FRAGMENT:
                setFragment(section.getTargetFragment(), section.getTitle());

                // setting toolbar color if is setted
                if (section.hasToolbarColor()) {
                    this.getToolbar().setBackgroundColor(section.getToolbarPrimaryColor());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                        getWindow().setStatusBarColor(section.getToolbarPrimaryColorDark());
                } else {
                    this.getToolbar().setBackgroundColor(primaryColor);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                        getWindow().setStatusBarColor(primaryColorDark);
                }
                break;
            case MaterialSection.TARGET_ACTIVITY:
                this.startActivity(section.getTargetIntent());
                break;
            case MaterialSection.TARGET_LISTENER:
                section.getTargetListener().onClick(section.getView());
                break;
        }

        int position = section.getPosition();
        Constants.NAV_MENU_POSITION = position;

        for (MaterialSection mySection : sectionList) {
            if (position != mySection.getPosition()) {
                mySection.unSelect();
            } else {
                section.select();
            }
        }
        for (MaterialSection mySection : bottomSectionList) {
            if (position != mySection.getPosition())
                mySection.unSelect();
        }

    }

    private Bitmap convertToBitmap(Drawable drawable) {

        Bitmap mutableBitmap = Bitmap.createBitmap(drawable.getMinimumWidth(), drawable.getMinimumHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mutableBitmap);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        drawable.draw(canvas);

        return mutableBitmap;
    }

    private Bitmap getCroppedBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
        //return _bmp;
        return output;
    }

    // Method used for customize layout

    public static void setUserEmail(String email) {
        usermail.setText(email);
    }

    public void setUsername(String username) {
        this.username.setText(username);
    }

    public void setUserPhoto(Bitmap photo) {
        userphoto.setImageBitmap(photo);
        //userphoto.setImageBitmap(getCroppedBitmap(photo));
    }

    public void setUserPhoto(Drawable photo) {
        userphoto.setImageDrawable(photo);
        //userphoto.setImageBitmap(getCroppedBitmap(convertToBitmap(photo)));
    }

    public void setDrawerBackground(Bitmap background) {
        usercover.setImageBitmap(background);

        //setPalette();
    }

    public void setDrawerBackground(Drawable background) {
        usercover.setImageDrawable(background);

        //setPalette();
    }

    public void addSection(MaterialSection section, boolean showField) {

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (48 * density));
        sectionList.add(section);
        if (showField)
            sections.addView(section.getView(), params);
    }

    public void addBottomSection(MaterialSection section) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (48 * density));
        bottomSectionList.add(section);
        bottomSections.addView(section.getView(), params);
    }

    public void addDivisor() {
        View view = new View(this);
        view.setBackgroundColor(Color.parseColor("#e0e0e0"));
        // height 1 px
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1);
        params.setMargins(0, (int) (8 * density), 0, (int) (8 * density));

        sections.addView(view, params);
    }

    // create sections

    public MaterialSection LogoutSection(String title, Drawable icon, View.OnClickListener listener) {
        MaterialSection section = new MaterialSection(this, sectionList.size(), true, true, MaterialSection.TARGET_LISTENER);
        section.setOnClickListener(this);
        section.setTitle(title);
        section.setIcon(icon);
        section.setTarget(listener);
        return section;
    }

    public MaterialSection newSection(String title, Drawable icon, Fragment target) {
        MaterialSection section = new MaterialSection(this, sectionList.size(), true, false, MaterialSection.TARGET_FRAGMENT);
        section.setOnClickListener(this);
        section.setIcon(icon);
        section.setTitle(title);
        section.setTarget(target);

        return section;
    }

    public MaterialSection newSection(String title, Drawable icon, Intent target) {
        MaterialSection section = new MaterialSection(this, sectionList.size(), true, false, MaterialSection.TARGET_ACTIVITY);
        section.setOnClickListener(this);
        section.setIcon(icon);
        section.setTitle(title);
        section.setTarget(target);

        return section;
    }

    public MaterialSection newSection(String title, Bitmap icon, Fragment target) {
        MaterialSection section = new MaterialSection(this, sectionList.size(), true, false, MaterialSection.TARGET_FRAGMENT);
        section.setOnClickListener(this);
        section.setIcon(icon);
        section.setTitle(title);
        section.setTarget(target);

        return section;
    }

    public MaterialSection newSection(String title, Bitmap icon, Intent target) {
        MaterialSection section = new MaterialSection(this, sectionList.size(), true, false, MaterialSection.TARGET_ACTIVITY);
        section.setOnClickListener(this);
        section.setIcon(icon);
        section.setTitle(title);
        section.setTarget(target);

        return section;
    }

    public MaterialSection newSection(String title, Fragment target) {
        MaterialSection section = new MaterialSection(this, sectionList.size(), false, false, MaterialSection.TARGET_FRAGMENT);
        section.setOnClickListener(this);
        section.setTitle(title);
        section.setTarget(target);

        return section;
    }

    public MaterialSection newSection(String title, Intent target) {
        MaterialSection section = new MaterialSection(this, sectionList.size(), false, false, MaterialSection.TARGET_ACTIVITY);
        section.setOnClickListener(this);
        section.setTitle(title);
        section.setTarget(target);

        return section;
    }

    public MaterialSection newBottomSection(String title, Drawable icon, Fragment target) {
        MaterialSection section = new MaterialSection(this, BOTTOM_SECTION_START + bottomSectionList.size(), true, false, MaterialSection.TARGET_FRAGMENT);
        section.setOnClickListener(this);
        section.setIcon(icon);
        section.setTitle(title);
        section.setTarget(target);

        return section;
    }

    public MaterialSection newBottomSection(String title, Drawable icon, Intent target) {
        MaterialSection section = new MaterialSection(this, BOTTOM_SECTION_START + bottomSectionList.size(), true, false, MaterialSection.TARGET_ACTIVITY);
        section.setOnClickListener(this);
        section.setIcon(icon);
        section.setTitle(title);
        section.setTarget(target);

        return section;
    }

    public MaterialSection newBottomSection(String title, Bitmap icon, Fragment target) {
        MaterialSection section = new MaterialSection(this, BOTTOM_SECTION_START + bottomSectionList.size(), true, false, MaterialSection.TARGET_FRAGMENT);
        section.setOnClickListener(this);
        section.setIcon(icon);
        section.setTitle(title);
        section.setTarget(target);

        return section;
    }

    public MaterialSection newBottomSection(String title, Bitmap icon, Intent target) {
        MaterialSection section = new MaterialSection(this, BOTTOM_SECTION_START + bottomSectionList.size(), true, false, MaterialSection.TARGET_ACTIVITY);
        section.setOnClickListener(this);
        section.setIcon(icon);
        section.setTitle(title);
        section.setTarget(target);

        return section;
    }

    public MaterialSection newBottomSection(String title, Fragment target) {
        MaterialSection section = new MaterialSection(this, BOTTOM_SECTION_START + bottomSectionList.size(), false, false, MaterialSection.TARGET_FRAGMENT);
        section.setOnClickListener(this);
        section.setTitle(title);
        section.setTarget(target);

        return section;
    }

    public MaterialSection newBottomSection(String title, Intent target) {
        MaterialSection section = new MaterialSection(this, BOTTOM_SECTION_START + bottomSectionList.size(), false, false, MaterialSection.TARGET_ACTIVITY);
        section.setOnClickListener(this);
        section.setTitle(title);
        section.setTarget(target);

        return section;
    }

    // abstract methods

    public abstract void init(Bundle savedInstanceState);

    public MaterialSection newBottomSection(String title, Drawable icon, View.OnClickListener listener) {
        MaterialSection section = new MaterialSection(this, BOTTOM_SECTION_START + bottomSectionList.size(), true, false, MaterialSection.TARGET_LISTENER);
        section.setOnClickListener(this);
        section.setTitle(title);
        section.setIcon(icon);
        section.setTarget(listener);
        return section;
    }

    public MaterialSection newSection(String title, Drawable icon, Drawable rightIcon, View.OnClickListener listener) {
        MaterialSection section = new MaterialSection(this, sectionList.size(), true, true, MaterialSection.TARGET_LISTENER);
        section.setOnClickListener(this);
        section.setTitle(title);
        section.setIcon(icon);
        section.setRightIcon(rightIcon);
        section.setTarget(listener);
        return section;
    }

    public boolean clearAllSection() {
        sectionList.clear();
        bottomSectionList.clear();
        sections.removeAllViews();
        bottomSections.removeAllViews();
        Constants.NAV_MENU_POSITION = 0;
        return true;
    }

    public MaterialSection getSection(int pos) {
        if (sectionList != null && pos < sectionList.size()) {
            return sectionList.get(pos);
        }
        return null;
    }

}