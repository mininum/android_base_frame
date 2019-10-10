package org.rdengine.widget.cobe.loadmore;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.android.frame.R;

public class LoadMorePointViscousFooterView extends RelativeLayout implements LoadMoreUIHandler
{

    private LinearLayout footer_layout;
    // private TextView mTextView;
    private View progressbar, bottom_view;

    public LoadMorePointViscousFooterView(Context context)
    {
        this(context, null);
    }

    public LoadMorePointViscousFooterView(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public LoadMorePointViscousFooterView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        setupViews();
    }

    private void setupViews()
    {
        LayoutInflater.from(getContext()).inflate(R.layout.cube_views_load_more_pointviscous_footer, this);
        // mTextView = (TextView) findViewById(R.id.cube_views_load_more_default_footer_text_view);
        footer_layout = (LinearLayout) findViewById(R.id.footer_layout);
        progressbar = findViewById(R.id.progressbar);
        bottom_view = findViewById(R.id.bottom_view);

        // 默认loading是隐藏的
        footer_layout.setVisibility(View.GONE);
    }

    @Override
    public void onLoading(LoadMoreContainer container)
    {
        // 加载中
        setVisibility(VISIBLE);
        // mTextView.setText(R.string.cube_views_load_more_loading);
        footer_layout.setVisibility(View.VISIBLE);
        progressbar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoadFinish(LoadMoreContainer container, boolean empty, boolean hasMore)
    {
        // 加载完成
        setVisibility(View.VISIBLE);
        progressbar.setVisibility(View.GONE);
        if (!hasMore)
        {
            // setVisibility(VISIBLE);
            // if (empty) {
            // mTextView.setText(R.string.cube_views_load_more_loaded_empty);
            // } else {
            // mTextView.setText(R.string.cube_views_load_more_loaded_no_more);
            // }

            footer_layout.setVisibility(View.GONE);
        } else
        {
            // footer_layout.setVisibility(View.VISIBLE);
            // setVisibility(INVISIBLE);
            footer_layout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onWaitToLoadMore(LoadMoreContainer container)
    {
        // 准备开始加载
        setVisibility(VISIBLE);
        // mTextView.setText(R.string.cube_views_load_more_click_to_load_more);

        footer_layout.setVisibility(View.VISIBLE);
        progressbar.setVisibility(View.GONE);
    }

    @Override
    public void onLoadError(LoadMoreContainer container, int errorCode, String errorMessage)
    {
        // mTextView.setText(R.string.cube_views_load_more_error);

        setVisibility(VISIBLE);
        footer_layout.setVisibility(View.VISIBLE);
        progressbar.setVisibility(View.GONE);
    }

    /** 设置底部占位空白是否显示 */
    public void setBottomViewVisibility(int visibility)
    {
        bottom_view.setVisibility(visibility);
    }

    /** 设置底部占位空白的高度 */
    public void setBottomViewHeight(int height)
    {
        ViewGroup.LayoutParams lp = bottom_view.getLayoutParams();
        lp.height = height;
        bottom_view.setLayoutParams(lp);
    }
}
