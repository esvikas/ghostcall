package com.kickbackapps.ghostcall.adapters;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kickbackapps.ghostcall.R;

/**
 * Created by viks on 3/9/16.
 */
public class TutorialAdapter extends PagerAdapter {

    static int[] imageList = new int[]{R.drawable.mobile, R.drawable.mobile_lock,
            R.drawable.globe, R.drawable.globe_equalizer, R.drawable.mobile_hand};

    static String[] captionList = new String[]{"Give your calls privacy with plenty of options",
            "Keep your real number private",
            "Make excuses and be wherever you want to be",
            "Protect your identity even further with voice modulation",
            "Get started with trial Ghost number"};

    private final Context mContext;

    public TutorialAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return imageList.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.adapter_tutorial, container, false);

        ImageView tutorialImage = (ImageView) view.findViewById(R.id.tutorial_image);
        TextView tvTitle = (TextView) view.findViewById(R.id.tvTitle);

        tutorialImage.setImageResource(imageList[position]);
        tvTitle.setText(captionList[position]);

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((RelativeLayout) object);
    }
}
