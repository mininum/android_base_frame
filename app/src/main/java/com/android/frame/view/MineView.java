package com.android.frame.view;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.frame.R;
import com.android.frame.logic.UserMgr;
import com.android.frame.logic.bean.User;
import com.facebook.fresco.FrescoImageHelper;
import com.facebook.fresco.FrescoImageView;


import org.json.JSONObject;
import org.rdengine.http.ErrorCode;
import org.rdengine.http.JSONResponse;
import org.rdengine.util.ClickUtil;
import org.rdengine.util.StringUtil;
import org.rdengine.util.UiUtil;
import org.rdengine.view.manager.BaseView;
import org.rdengine.view.manager.ViewParam;
import org.rdengine.widget.ToastHelper;

public class MineView extends BaseView implements View.OnClickListener
{


    @Override
    public String getTag()
    {
        return "MineView";
    }

    public MineView(Context context, ViewParam param)
    {
        super(context, param);
    }

    @Override
    public void init()
    {

        setContentView(R.layout.layout_mine_view);


        UiUtil.setStatusBarDarkTheme((Activity) getContext(), true);


    }

    @Override
    public void onShow()
    {
        super.onShow();
        user = UserMgr.getInstance().getLoginUser();

    }

    User user;

    @Override
    public void refresh()
    {
        super.refresh();

    }

    @Override
    public void onClick(View view) {

    }

    public void onMainTabClick()
    {
        try
        {
        } catch (Exception ex)
        {
        }
    }
}
