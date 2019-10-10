package com.android.frame.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;

public class BaseDialog extends Dialog
{

    public BaseDialog(Context context, int styleId)
    {
        super(context, styleId);
        setOwnerActivity((Activity) context);
    }

    public BaseDialog(Context context)
    {
        super(context);
        setOwnerActivity((Activity) context);
    }

    public interface OnActionSheetSelected
    {

        void onClick(int whichButton);
    }

    public interface BaseDialogOnclicklistener
    {

        void onOkClick(Dialog dialog);

        void onCancleClick(Dialog dialog);
    }
}
