package com.android.frame.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.android.frame.R;
import com.android.frame.activity.MainActivity;

import org.rdengine.view.manager.BaseView;
import org.rdengine.view.manager.ViewParam;

public class SplashView extends BaseView
{
    public final static String TAG = "SplashView";
    // ----------------R.layout.splash_view-------------Start
    private ImageView iv_logo;
    private TextView tv_title;
    private RelativeLayout layout_bottom;

    /** auto load R.layout.splash_view */
    private void autoLoad_splash_view()
    {
        iv_logo = (ImageView) findViewById(R.id.iv_logo);
        tv_title = (TextView) findViewById(R.id.tv_title);
        layout_bottom = (RelativeLayout) findViewById(R.id.layout_bottom);
    }

    // ----------------R.layout.splash_view-------------End

    public SplashView(Context context, ViewParam param)
    {
        super(context, param);
    }

    @Override
    public String getTag()
    {
        return TAG;
    }

    @Override
    public void init()
    {
        setContentView(R.layout.splash_view);
        autoLoad_splash_view();
    }

    @Override
    public void refresh()
    {
        super.refresh();

    }

    public void openGo()
    {
        // 开始定时 可以点击
        // if (ad == null)
        // {
        // showGetCoinAnimation();
        mHandler.sendEmptyMessageDelayed(0, 3500);
        // } else
        // {
        // fiv_ad.setOnClickListener(adOnClickListener);
        // tv_ad_time.setOnClickListener(adOnClickListener);
        // tv_ad_time.setVisibility(View.VISIBLE);
        // // 定时倒数
        //
        // mHandler.sendEmptyMessage(1);
        // }
    }

    Handler mHandler = new Handler(Looper.getMainLooper())
    {
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);

            switch (msg.what)
            {
            case 0 :
            {
                Activity activity = (Activity) getContext();
                Intent intent = new Intent();
                intent.setClass(activity, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                activity.startActivity(intent);
                activity.overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
                activity.finish();
            }
                break;
            }
        }
    };

}
