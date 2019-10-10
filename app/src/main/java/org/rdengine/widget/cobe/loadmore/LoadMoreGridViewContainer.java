package org.rdengine.widget.cobe.loadmore;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;

import org.rdengine.widget.cobe.GridViewWithHeaderAndFooter;

/**
 * @author huqiu.lhq
 */
public class LoadMoreGridViewContainer extends LoadMoreContainerBase
{

    private GridViewWithHeaderAndFooter mGridView;

    public LoadMoreGridViewContainer(Context context)
    {
        super(context);
    }

    public LoadMoreGridViewContainer(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public LoadMoreGridViewContainer(Context context, GridViewWithHeaderAndFooter gridview)
    {
        super(context);
        addView(gridview);
        onFinishInflate();
    }

    @Override
    protected void addFooterView(View view)
    {
        mGridView.addFooterView(view);
    }

    @Override
    protected void removeFooterView(View view)
    {
        mGridView.removeFooterView(view);
    }

    @Override
    protected AbsListView retrieveAbsListView()
    {
        View view = getChildAt(0);
        mGridView = (GridViewWithHeaderAndFooter) view;
        return mGridView;
    }
}