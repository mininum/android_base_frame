package com.android.frame.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import com.android.frame.R;


import org.rdengine.view.manager.BaseView;
import org.rdengine.view.manager.ViewParam;

import java.util.ArrayList;
import java.util.List;

public class HomeView extends BaseView
{
    // ----------------R.layout.layout_home_view-------------Start
    private RecyclerView home_recycle; // ----------------R.layout.layout_home_view-------------End

    @Override
    public String getTag()
    {
        return "HomeView";
    }

    public HomeView(Context context, ViewParam param)
    {
        super(context, param);
    }



    private List<String> testData = new ArrayList<>();
    @Override
    public void init()
    {
        setContentView(R.layout.layout_home_view);
        autoLoad_layout_home_view();
      //  UiUtil.setTransparentStatus((Activity) getContext());

        for(int i = 0;i < 5;i++){

            testData.add("1");
        }


    }


    public void onMainTabClick()
    {
        try
        {
        } catch (Exception ex)
        {
        }
    }

    /** auto load R.layout.layout_home_view */
    private void autoLoad_layout_home_view()
    {
        home_recycle = (RecyclerView) findViewById(R.id.home_recycle);
    }

    @Override
    public void onShow() {
        super.onShow();
    }

    @Override
    public void refresh() {
        super.refresh();
    }
}
