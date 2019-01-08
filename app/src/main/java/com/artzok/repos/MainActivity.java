package com.artzok.repos;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.nfc.Tag;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.artzok.repos.widgets.AutoViewPager;
import com.artzok.utils.AppUtils;

import java.lang.reflect.Method;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private AutoViewPager mAutoViewPager;
    private Random mRandom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAutoViewPager = findViewById(R.id.auto_view_pager);
        mRandom = new Random();
        mAutoViewPager.setPagerAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return 10;
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
                return view == object;
            }

            public Object instantiateItem(ViewGroup container, int position) {
                Context context = container.getContext();
                TextView item = new TextView(context);
                item.setGravity(Gravity.CENTER);
                item.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50);
                String msg = null;
                switch (position) {
                    case 0:
                        msg = "UUID:" + AppUtils.getUUID(context);
                        break;
                    case 1:
                        msg = "Phone:" + AppUtils.getPhoneNumber(context);
                        break;
                    case 2:
                        msg = "SimSN:" + AppUtils.getSimSerialNumber(context);
                        break;
                    case 3:
                        msg = "VersionName:" + AppUtils.getVersionName(context);
                        break;
                    case 4:
                        msg = "VersionCode:" + AppUtils.getVersionCode(context);
                        break;
                    case 5:
                        msg = "ProcessName:" + AppUtils.getProcessName(context);
                        break;
                    case 6:
                        msg = "Operator:" + AppUtils.getOperator(context);
                        break;
                    case 7:
                        msg = "ScreenWidth:" + AppUtils.getScreenWidth(context);
                        break;
                    case 8:
                        msg = "ScreenHeight:" + AppUtils.getScreenHeight(context);
                        break;
                    case 9:
                        msg = "Network:" + AppUtils.getNetworkType(context);
                        break;
                }
                if (!TextUtils.isEmpty(msg))
                    item.setText(msg);
                container.addView(item);
                item.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                item.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                return item;
            }

            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView((View) object);
            }
        });
        mAutoViewPager.start();
    }
}
