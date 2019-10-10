package org.rdengine.view.manager;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.frame.R;

import org.rdengine.widget.cobe.CodePullHandler;
import org.rdengine.widget.cobe.loadmore.LoadMoreContainer;
import org.rdengine.widget.cobe.ptr.PtrDefaultHandler;
import org.rdengine.widget.cobe.ptr.PtrFrameLayout;

public class BaseListView extends BaseView implements CodePullHandler
{
    public BaseListView(Context context, ViewParam param)
    {
        super(context, param);
    }

    public static final String TAG = "BaseListView";

    @Override
    public String getTag()
    {
        return TAG;
    }

    // ----------------R.layout.base_list_view-------------Start
    public LinearLayout titlebar;// include R.layout.titlebar
    public ImageView btn_back;// include R.layout.titlebar
    public TextView titlebar_title_tv;// include R.layout.titlebar
    public TextView titlebar_right_tv;// include R.layout.titlebar
    public ImageView titlebar_right_iv;// include R.layout.titlebar
    public org.rdengine.widget.cobe.PtrListLayout ptrlistlayout;
    public ListView listview;
    public RelativeLayout empty_view;
    // public View titlebar_shadow;

    public void autoLoad_base_list_view()
    {
        titlebar = (LinearLayout) findViewById(R.id.titlebar);// titlebar
        btn_back = (ImageView) findViewById(R.id.btn_back);// titlebar
        titlebar_title_tv = (TextView) findViewById(R.id.titlebar_title_tv);// titlebar
        titlebar_right_tv = (TextView) findViewById(R.id.titlebar_right_tv);// titlebar
        titlebar_right_iv = (ImageView) findViewById(R.id.titlebar_right_iv);// titlebar
        ptrlistlayout = (org.rdengine.widget.cobe.PtrListLayout) findViewById(R.id.ptrlistlayout);
        listview = (ListView) findViewById(R.id.listview);
        empty_view = (RelativeLayout) findViewById(R.id.empty_view);
        // titlebar_shadow = findViewById(R.id.titlebar_shadow);
    }

    // ----------------R.layout.base_list_view-------------End

    public void init()
    {
        setContentView(R.layout.base_list_view);
        autoLoad_base_list_view();
        ptrlistlayout.setCodePullHandler(this);
    }

    public void refresh()
    {
        super.refresh();
    }

    @Override
    public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header)
    {
        return PtrDefaultHandler.checkContentCanBePulledDown(frame, listview, header);
    }

    @Override
    public void onRefreshBegin(PtrFrameLayout frame)
    {

    }

    @Override
    public void onLoadMore(LoadMoreContainer loadMoreContainer)
    {

    }

    @Override
    public void autoRefreshOnShowAgain()
    {
        super.autoRefreshOnShowAgain();
        try
        {
            if (ptrlistlayout != null)
            {
                ptrlistlayout.autoRefresh();
            }
        } catch (Exception ex)
        {
        }
    }

    public interface SubListRefreshListener
    {
        void onRefresh(ListView view);
    }
}
