package org.rdengine.widget.cobe;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;


import com.android.frame.R;

import org.rdengine.widget.cobe.loadmore.LoadMoreGridViewContainer;
import org.rdengine.widget.cobe.ptr.PtrClassicFrameLayout;

/**
 * Gridview 刷新 、 加载更多 的整合控件<br>
 * 需要在xml中包裹住GridView<br>
 * &lt PtrGridLayout &gt<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt GridView /&gt<br>
 * &lt/ PtrGridLayout &gt
 * 
 * @author CCCMAX
 */
public class PtrGridLayout extends PtrClassicFrameLayout
{

    public LoadMoreGridViewContainer loadmoreContainer;

    private CodePullHandler pullListener;

    private GridViewWithHeaderAndFooter gridview;

    public PtrGridLayout(Context context)
    {
        super(context);
    }

    public PtrGridLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public PtrGridLayout(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate()
    {
        disableWhenHorizontalMove(true);
        int childcount = this.getChildCount();
        if (childcount > 0)
        {
            for (int i = 0; i < childcount; i++)
            {
                View v = getChildAt(i);
                if (v instanceof GridViewWithHeaderAndFooter)
                {
                    gridview = (GridViewWithHeaderAndFooter) v;
                    removeView(v);

                    loadmoreContainer = new LoadMoreGridViewContainer(getContext(), gridview);
                    ViewGroup.LayoutParams lmc_lp = new LayoutParams(v.getLayoutParams().width,
                            v.getLayoutParams().height);
                    loadmoreContainer.setLayoutParams(lmc_lp);
                    loadmoreContainer.setId(R.id.ptr_loadmore_container);

                    break;
                }
            }

            if (childcount > 2)
            {
                System.out.print("The PtrListLayou can only have one childview");
            }
        }
        addView(loadmoreContainer);

        super.onFinishInflate();
    }

    /**
     * 设置上拉下拉的监听
     * 
     * @param listener
     */
    public void setCodePullHandler(CodePullHandler listener)
    {
        this.pullListener = listener;

        setLastUpdateTimeRelateObject(listener);

        this.setPtrHandler(pullListener);// 下拉刷新监听

        loadmoreContainer.setLoadMoreHandler(pullListener);// 上拉加载更多监听

        // loadmoreContainer.useTransparentFooter();// 上拉加载的footerview 默认设置
        loadmoreContainer.useDefaultFooter();
    }

    public void setAutoLoadMore(boolean autoLoadMore)
    {
        loadmoreContainer.setAutoLoadMore(autoLoadMore);
    }

    /**
     * 结束加载更多
     * 
     * @param emptyResult
     * @param hasMore
     */
    public void loadMoreFinish(boolean emptyResult, boolean hasMore)
    {
        loadmoreContainer.loadMoreFinish(emptyResult, hasMore);
    }

    /**
     * 是否正在加载更多数据
     * 
     * @return
     */
    public boolean isLoading()
    {
        return loadmoreContainer.isLoading();
    }

}
