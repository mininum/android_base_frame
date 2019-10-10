package org.rdengine.widget.cobe.loadmore;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

/**
 * @author huqiu.lhq
 */
public class LoadMoreListViewContainer extends LoadMoreContainerBase
{

    private ListView mListView;

    public LoadMoreListViewContainer(Context context)
    {
        super(context);
    }

    public LoadMoreListViewContainer(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public LoadMoreListViewContainer(Context context, ListView listview)
    {
        super(context);
        addView(listview);
        onFinishInflate();
    }

    @Override
    protected void addFooterView(View view)
    {
        mListView.addFooterView(view);
    }

    @Override
    protected void removeFooterView(View view)
    {
        mListView.removeFooterView(view);
    }

    @Override
    protected AbsListView retrieveAbsListView()
    {
        mListView = (ListView) getChildAt(0);
        return mListView;
    }
}
