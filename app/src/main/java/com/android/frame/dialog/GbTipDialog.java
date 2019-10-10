package com.android.frame.dialog;

import android.content.Context;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.android.frame.R;

import org.rdengine.util.PhoneUtil;
import org.rdengine.util.UiUtil;

public class GbTipDialog extends BaseDialog implements OnClickListener
{

    public GbTipDialog(Context context)
    {
        super(context, R.style.BaseDialog);

        setCancelable(false);
        setCanceledOnTouchOutside(false);

        setContentView(R.layout.dialog_tip_a);

        Window w = getWindow();
        WindowManager.LayoutParams lp = w.getAttributes();

        lp.width = (int) (PhoneUtil.getScreenWidth(context) - 2 * PhoneUtil.dipToPixel(26, getContext()));
        lp.gravity = Gravity.CENTER;
        onWindowAttributesChanged(lp);

        // 沉浸模式 dialog全屏
        if (UiUtil.isOpenTransparentStatusbar())
        {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
            {
                w.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            } else
            {
                w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                        WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
                        WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            }
        }

        autoLoad_dialog_tip_gb();

        btn_cancel.setOnClickListener(this);
        btn_ok.setOnClickListener(this);

        tips_title.setVisibility(View.GONE);
        tips_message.setVisibility(View.GONE);
    }

    // ----------------R.layout.dialog_tip_gb-------------Start
    private TextView tips_title;
    private TextView tips_message;
    public TextView btn_ok;
    private TextView btn_cancel;

    public void autoLoad_dialog_tip_gb()
    {
        tips_title = (TextView) findViewById(R.id.tips_title);
        tips_message = (TextView) findViewById(R.id.tips_message);
        btn_ok = (TextView) findViewById(R.id.btn_ok);
        btn_cancel = (TextView) findViewById(R.id.btn_cancel);
    }

    // ----------------R.layout.dialog_tip_gb-------------End

    public void setTitle(CharSequence string)
    {
        tips_title.setText(string);
        tips_title.setVisibility(View.VISIBLE);
    }

    public void setContent(CharSequence string)
    {
        tips_message.setText(string);
        tips_message.setVisibility(View.VISIBLE);
    }

    public void setBtnCancelText(CharSequence txt)
    {
        btn_cancel.setText(txt);
    }

    public void setBtnOKText(CharSequence txt)
    {
        btn_ok.setText(txt);
    }

    public void setBaseDialogOnclicklistener(BaseDialogOnclicklistener listener)
    {
        this.listener = listener;
    }

    public View getBtnOK()
    {
        return btn_ok;
    }

    public View getBtnCancel()
    {
        return btn_cancel;
    }

    BaseDialogOnclicklistener listener;

    public boolean btnOnClickNeedDismiss = true;

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
        case R.id.btn_ok :
        {
            if (btnOnClickNeedDismiss)
                dismiss();
            if (listener != null)
                listener.onOkClick(this);
        }
            break;
        case R.id.btn_cancel :
        {
            if (btnOnClickNeedDismiss)
                dismiss();
            if (listener != null)
                listener.onCancleClick(this);
        }
            break;
        }
    }

}
