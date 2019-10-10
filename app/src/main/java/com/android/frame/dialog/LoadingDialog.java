package com.android.frame.dialog;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.android.frame.R;

import org.rdengine.util.PhoneUtil;

public class LoadingDialog extends BaseDialog
{
    CharSequence message;

    public LoadingDialog(Context context, CharSequence msg)
    {
        super(context, R.style.BaseDialog);
        // setCancelable(false);
        // setCanceledOnTouchOutside(false);

        setContentView(R.layout.dialog_loading);

        Window w = getWindow();
        WindowManager.LayoutParams lp = w.getAttributes();

        lp.width = (int) (PhoneUtil.getScreenWidth(context) * 0.75);
        lp.gravity = Gravity.CENTER;
        onWindowAttributesChanged(lp);

        message = msg;

        autoLoad_dialog_loading();
    }

    // ----------------R.layout.dialog_loading-------------Start
    private org.rdengine.widget.ProgressBarCircular progressbar;
    private TextView tv_message;

    public void autoLoad_dialog_loading()
    {
        progressbar = (org.rdengine.widget.ProgressBarCircular) findViewById(R.id.progressbar);
        tv_message = (TextView) findViewById(R.id.tv_message);
    }

    // ----------------R.layout.dialog_loading-------------End

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setTipMessage(message);
    }

    public void setTipMessage(CharSequence msg)
    {
        message = msg;
        if (message != null && message.length() > 0)
        {
            tv_message.setText(message);
            tv_message.setVisibility(View.VISIBLE);
        } else
        {
            tv_message.setText("");
            tv_message.setVisibility(View.GONE);
        }
    }

    public void setTimedShutdown(long time)
    {
        if (time == 0)
        {
            cleanTimedShutdown();
        } else
        {
            mHandler.removeMessages(0);
            mHandler.sendEmptyMessageDelayed(0, time);
        }
    }

    public void cleanTimedShutdown()
    {
        mHandler.removeMessages(0);
    }

    private Handler mHandler = new Handler(Looper.getMainLooper())
    {
        public void handleMessage(Message msg)
        {
            try
            {
                super.handleMessage(msg);
                switch (msg.what)
                {
                case 0 :
                {
                    LoadingDialog.this.dismiss();
                }
                    break;
                }
            } catch (Exception e)
            {
            }
        }
    };
}
