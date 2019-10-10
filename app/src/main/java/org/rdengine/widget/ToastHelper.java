package org.rdengine.widget;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.android.frame.R;

import org.rdengine.runtime.RT;

public class ToastHelper
{

    private static Toast mToast;

    public static void showToast(String obj)
    {
        // if (RT.application != null && mHandler != null && obj != null)
        // {
        // mHandler.obtainMessage(0, obj).sendToTarget();
        // }
        if (obj != null)
        {
            if (RT.application != null)
            {
                handler.obtainMessage(0, obj).sendToTarget();
            }
        }

    }

    public static void showToastWithIcon(String obj, int icon)
    {
        if (obj != null)
        {
            if (RT.application != null)
            {
                Bundle bundle = new Bundle();
                bundle.putString("text", obj);
                bundle.putInt("icon", icon);
                handler.obtainMessage(1, bundle).sendToTarget();
            }
        }
    }

    static Handler handler = new Handler(RT.application.getMainLooper())
    {
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
            case 0 :
                if (mToast != null)
                {
                    mToast.cancel();
                    mToast = null;
                }

                if (mToast == null)
                {
                    mToast = new Toast(RT.application);
                    mToast.setDuration(1500);
                    mToast.setGravity(Gravity.CENTER, 0, 0);
                    View view = LayoutInflater.from(RT.application).inflate(R.layout.toast_layout, null);
                    mToast.setView(view);
                }
                if (msg.obj != null)
                {
                    View view = mToast.getView();
                    TextView tv = (TextView) view.findViewById(R.id.toast);
                    tv.setText(msg.obj.toString());
                    ImageView iv = (ImageView) view.findViewById(R.id.icon_tip);
                    iv.setVisibility(View.GONE);
                    mToast.show();

                }
                break;
            case 1 :
                if (mToast != null)
                {
                    mToast.cancel();
                    mToast = null;
                }

                if (mToast == null)
                {
                    mToast = new Toast(RT.application);
                    mToast.setDuration(1500);
                    mToast.setGravity(Gravity.CENTER, 0, 0);
                    View view = LayoutInflater.from(RT.application).inflate(R.layout.toast_layout, null);
                    mToast.setView(view);
                }
                if (msg.obj != null)
                {
                    // DLOG.d("toast", "msg:" + msg.obj.toString());
                    Bundle bundle = (Bundle) msg.obj;
                    String text = bundle.getString("text");
                    int icon = bundle.getInt("icon");
                    View view = mToast.getView();
                    TextView tv = (TextView) view.findViewById(R.id.toast);
                    tv.setText(text);
                    ImageView iv = (ImageView) view.findViewById(R.id.icon_tip);
                    iv.setImageResource(icon);
                    iv.setVisibility(View.VISIBLE);
                    // mToast.setText(msg.obj.toString());
                    mToast.show();
                }
                break;
            }

        };
    };

}
