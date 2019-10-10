package com.android.frame.dialog;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;


import com.android.frame.R;

import org.rdengine.view.manager.BaseView;
import org.rdengine.view.manager.ViewManager;
import org.rdengine.view.manager.ViewParam;

import java.lang.ref.WeakReference;

public class FullScreenDialog extends BaseDialog
{
    public FullScreenDialog(Activity context)
    {
        this(context, R.style.dialog_fullscreen_pushup);
    }

    public FullScreenDialog(Activity context, int style)
    {
        super(context, style);
        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.height = -1;
        dialogWindow.setGravity(Gravity.LEFT | Gravity.BOTTOM);
        dialogWindow.setBackgroundDrawable(new ColorDrawable());
        dialogWindow.setAttributes(lp);
        setCancelable(true);
        setCanceledOnTouchOutside(true);
        setOwnerActivity(context);
    }

    @Override
    public void setContentView(int layoutResID)
    {
        super.setContentView(layoutResID);
        this.setTitleBar();
    }

    private WeakReference<BaseView> baseview;

    public void setBaseView(Class<? extends BaseView> clazz, ViewParam vp)
    {
        BaseView view = ViewManager.createView(clazz, vp, getOwnerActivity());
        setContentView(view);
        view.setParentObj(this);
        setTitleBar();
        view.refresh();
        view.onShow();

        baseview = new WeakReference<BaseView>(view);
    }

    @Override
    public void dismiss()
    {
        super.dismiss();
        if (baseview != null && baseview.get() != null)
        {
            baseview.get().onHide();
        }
    }

    /**
     * 设置titlebar信息
     */
    protected void setTitleBar()
    {
        View backBtn = (ImageView) findViewById(R.id.btn_back);
        if (backBtn != null)
        {
            backBtn.setOnClickListener(new View.OnClickListener()
            {

                @Override
                public void onClick(View v)
                {
                    FullScreenDialog.this.dismiss();
                }
            });
        }
    }

}
