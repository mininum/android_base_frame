package org.rdengine.widget.cobe.loadmore;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class LoadMoreTransparentFooterView extends RelativeLayout implements LoadMoreUIHandler
{

    private LinearLayout footer_layout;

    public LoadMoreTransparentFooterView(Context context)
    {
        this(context, null);
    }

    public LoadMoreTransparentFooterView(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public LoadMoreTransparentFooterView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        setupViews();
    }

    private void setupViews()
    {
        // initView
        footer_layout = new LinearLayout(getContext());
        float scale = getContext().getResources().getDisplayMetrics().density;
        int height = (int) (10 * scale + 0.5f);// view的高度
        addView(footer_layout, -1, height);
    }

    @Override
    public void onLoading(LoadMoreContainer container)
    {
        setVisibility(VISIBLE);
        footer_layout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoadFinish(LoadMoreContainer container, boolean empty, boolean hasMore)
    {
        if (!hasMore)
        {
            footer_layout.setVisibility(View.GONE);
        } else
        {
            footer_layout.setVisibility(View.VISIBLE);
            setVisibility(INVISIBLE);
        }
    }

    @Override
    public void onWaitToLoadMore(LoadMoreContainer container)
    {
        setVisibility(VISIBLE);
        footer_layout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoadError(LoadMoreContainer container, int errorCode, String errorMessage)
    {
        setVisibility(VISIBLE);
        footer_layout.setVisibility(View.VISIBLE);
    }

}
