package com.kickbackapps.ghostcall.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.kickbackapps.ghostcall.R;
import com.kickbackapps.ghostcall.adapters.TutorialAdapter;
import com.kickbackapps.ghostcall.view.CirclePageIndicator;
import com.kickbackapps.ghostcall.viewpager.ZoomOutTransformer;

/**
 * Created by viks on 3/16/16.
 */
public class TutorialActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnStart;
    private Button btn;
    private ViewPager mPager;
    private TutorialAdapter mAdapter;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tutorial);

        mAdapter = new TutorialAdapter(getApplicationContext());
        mPager = (ViewPager) findViewById(R.id.container);
        btnStart = (Button) findViewById(R.id.btnStart);
        btn = (Button) findViewById(R.id.btn);

        mPager.setAdapter(mAdapter);
        try {
            mPager.setPageTransformer(true, new ZoomOutTransformer());
        } catch (Exception e) {
            e.printStackTrace();
        }

        CirclePageIndicator indicator = (CirclePageIndicator) findViewById(R.id.indicator);
        indicator.setViewPager(mPager);

        btnStart.setVisibility(View.VISIBLE);
        btnStart.setClickable(true);
        btn.setVisibility(View.INVISIBLE);

        indicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    btnStart.setText(R.string.skip);
                    btnStart.setVisibility(View.VISIBLE);
                    btn.setVisibility(View.INVISIBLE);
                    btnStart.setClickable(true);
                } else if (position == 4) {
                    btnStart.setText(R.string.start);
                    btnStart.setVisibility(View.VISIBLE);
                    btn.setVisibility(View.INVISIBLE);
                    btnStart.setClickable(true);
                } else {
                    btnStart.setVisibility(View.INVISIBLE);
                    btn.setVisibility(View.INVISIBLE);
                    btnStart.setClickable(false);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnStart:
                Intent intent = new Intent(this, StartScreen.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                break;
        }
    }
}
