package org.rdengine.view.manager;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.android.frame.R;

import org.rdengine.widget.cobe.ptr.PtrClassicFrameLayout;
import org.rdengine.widget.cobe.ptr.PtrDefaultHandler;
import org.rdengine.widget.cobe.ptr.PtrFrameLayout;
import org.rdengine.widget.cobe.ptr.PtrHandler;

/**
 * 基础recycler<br>
 * 配合BaseQuickAdapter使用 Created by CCCMAX on 18/11/19.
 */

public class BaseRecyclerView extends BaseView
        implements BaseQuickAdapter.OnItemClickListener, BaseQuickAdapter.RequestLoadMoreListener, PtrHandler
{
    public static final String TAG = "BaseRecyclerView";
    // ----------------R.layout.base_recycler_view-------------Start
    protected View statemask; // include R.layout.titlebar
    protected ImageView btn_back; // include R.layout.titlebar
    protected TextView titlebar_title_tv; // include R.layout.titlebar
    protected TextView titlebar_right_tv; // include R.layout.titlebar
    protected ImageView titlebar_right_iv; // include R.layout.titlebar
    protected LinearLayout titlebar; // include R.layout.titlebar
    protected RecyclerView recyclerview;
    protected PtrClassicFrameLayout ptrframelayout;
    protected RelativeLayout empty_view;
    protected FrameLayout layout_content;

    /** auto load R.layout.base_recycler_view */
    protected void autoLoad_base_recycler_view()
    {
        statemask = (View) findViewById(R.id.statemask); // include R.layout.titlebar
        btn_back = (ImageView) findViewById(R.id.btn_back); // include R.layout.titlebar
        titlebar_title_tv = (TextView) findViewById(R.id.titlebar_title_tv); // include R.layout.titlebar
        titlebar_right_tv = (TextView) findViewById(R.id.titlebar_right_tv); // include R.layout.titlebar
        titlebar_right_iv = (ImageView) findViewById(R.id.titlebar_right_iv); // include R.layout.titlebar
        titlebar = (LinearLayout) findViewById(R.id.titlebar); // include R.layout.titlebar
        recyclerview = (RecyclerView) findViewById(R.id.recyclerview);
        ptrframelayout = (PtrClassicFrameLayout) findViewById(R.id.ptrframelayout);
        empty_view = (RelativeLayout) findViewById(R.id.empty_view);
        layout_content = (FrameLayout) findViewById(R.id.layout_content);
    }
    // ----------------R.layout.base_recycler_view-------------End

    @Override
    public String getTag()
    {
        return TAG;
    }

    public BaseRecyclerView(Context context, ViewParam param)
    {
        super(context, param);
    }

    @Override
    public void init()
    {
        setContentView(R.layout.base_recycler_view);
        autoLoad_base_recycler_view();
        ptrframelayout.setPtrHandler(this);
    }

    public void refresh()
    {
        super.refresh();

    }

    /** 下拉刷新 判断是否可以下拉 */
    @Override
    public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header)
    {
        return PtrDefaultHandler.checkRecyclerCanBePulledDown(frame, content, header);
    }

    /** 下拉刷新 触发 */
    @Override
    public void onRefreshBegin(PtrFrameLayout frame)
    {}

    /** item点击监听 */
    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position)
    {}

    /** 上拉加载数据监听 */
    @Override
    public void onLoadMoreRequested()
    {}

    @Override
    public void autoRefreshOnShowAgain()
    {
        super.autoRefreshOnShowAgain();
        try
        {
            if (ptrframelayout != null)
            {
                ptrframelayout.autoRefresh();
            }
        } catch (Exception ex)
        {
        }
    }

}
