package com.android.frame.dialog;

import android.content.Context;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.android.frame.R;

import org.rdengine.util.PhoneUtil;
import org.rdengine.util.UiUtil;

public class GuildRecommendDialog extends BaseDialog implements View.OnClickListener {
    // ----------------R.layout.dialog_guildrecommend_view-------------Start
    private TextView tv_ingeral_sum;
    private TextView tv_currency_ingeral;
    private TextView tv_cancel; // ----------------R.layout.dialog_guildrecommend_view-------------End

    public GuildRecommendDialog(Context context)
    {
        super(context, R.style.BaseDialog);

        setCancelable(false);
        setCanceledOnTouchOutside(false);

        setContentView(R.layout.dialog_guildrecommend_view);

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

        autoLoad_dialog_guildrecommend_view();
        tv_cancel.setOnClickListener(this);

    }

    /** auto load R.layout.dialog_guildrecommend_view */
    private void autoLoad_dialog_guildrecommend_view()
    {
        tv_ingeral_sum = (TextView) findViewById(R.id.tv_ingeral_sum);
        tv_currency_ingeral = (TextView) findViewById(R.id.tv_currency_ingeral);
        tv_cancel = (TextView) findViewById(R.id.tv_cancel);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.tv_cancel:
            {
                dismiss();
            }
            break;
        }
    }
}
