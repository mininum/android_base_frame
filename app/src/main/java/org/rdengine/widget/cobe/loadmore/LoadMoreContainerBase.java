package org.rdengine.widget.cobe.loadmore;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import android.widget.LinearLayout;

/**
 * @author huqiu.lhq
 */
public abstract class LoadMoreContainerBase extends LinearLayout implements LoadMoreContainer
{

    private AbsListView.OnScrollListener mOnScrollListener;
    private LoadMoreUIHandler mLoadMoreUIHandler;
    private LoadMoreHandler mLoadMoreHandler;

    private boolean mIsLoading;
    private boolean mHasMore = false;
    private boolean mAutoLoadMore = true;
    private boolean mLoadError = false;

    private boolean mListEmpty = true;
    private boolean mShowLoadingForFirstPage = false;
    private View mFooterView;

    private AbsListView mAbsListView;

    private boolean onScrollLoad = false;

    public LoadMoreContainerBase(Context context)
    {
        super(context);
    }

    public LoadMoreContainerBase(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate()
    {
        super.onFinishInflate();
        mAbsListView = retrieveAbsListView();
        init();
    }

    /**
     * 滑动加载更多时 判断手指抬起时加载，还是滑动到指定位置直接加载
     * 
     * @param onScrollLoad
     *            true 滑动到底部时不用抬起手指直接加载 ，false 抬手开始加载
     */
    public void setOnScrollLoad(boolean onScrollLoad)
    {
        this.onScrollLoad = onScrollLoad;
    }

    public View getFooterView()
    {
        return mFooterView;
    }

    public void useDefaultFooter()
    {
        useDefaultFooter(false);
    }

    public void useDefaultFooter(boolean showBottomPadView)
    {
        LoadMoreDefaultFooterView footerView = new LoadMoreDefaultFooterView(getContext());
        footerView.setVisibility(GONE);
        setLoadMoreView(footerView);
        setLoadMoreUIHandler(footerView);

        footerView.setBottomViewVisibility(showBottomPadView ? View.VISIBLE : View.GONE);
    }

    public void usePointViscousFooter()
    {
        usePointViscousFooter(false);
    }

    public void usePointViscousFooter(boolean showBottomPadView)
    {
        LoadMorePointViscousFooterView footerView = new LoadMorePointViscousFooterView(getContext());
        footerView.setVisibility(GONE);
        setLoadMoreView(footerView);
        setLoadMoreUIHandler(footerView);

        footerView.setBottomViewVisibility(showBottomPadView ? View.VISIBLE : View.GONE);
    }

    /**
     * 上拉加载更多 纯透明的FooterView ，View高度很小 滑到listview底部直接load
     */
    public void useTransparentFooter()
    {
        LoadMoreTransparentFooterView footerView = new LoadMoreTransparentFooterView(getContext());
        footerView.setVisibility(GONE);
        setLoadMoreView(footerView);
        setLoadMoreUIHandler(footerView);
        setAutoLoadMore(true);
        setOnScrollLoad(true);
    }

    private void init()
    {

        if (mFooterView != null)
        {
            addFooterView(mFooterView);
        }

        mAbsListView.setOnScrollListener(new AbsListView.OnScrollListener()
        {

            private boolean mIsEnd = false;
            private int scroll_state = SCROLL_STATE_IDLE;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState)
            {
                scroll_state = scrollState;
                if (null != mOnScrollListener)
                {
                    mOnScrollListener.onScrollStateChanged(view, scrollState);
                }
                if (scrollState == SCROLL_STATE_IDLE)
                {
                    if (mIsEnd)
                    {
                        onReachBottom();
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
            {
                if (null != mOnScrollListener)
                {
                    mOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
                }
                if (firstVisibleItem + visibleItemCount >= totalItemCount - 1)
                {
                    mIsEnd = true;
                    if (onScrollLoad && (scroll_state != SCROLL_STATE_IDLE))
                    {
                        onReachBottom();// 滑动中不松手直接加载
                    }
                } else
                {
                    mIsEnd = false;
                }
            }
        });
    }

    public boolean isLoading()
    {
        return mIsLoading;
    }

    private void tryToPerformLoadMore()
    {
        if (mIsLoading)
        {
            return;
        }

        // no more content and also not load for first page
        if (!mHasMore && !(mListEmpty && mShowLoadingForFirstPage))
        {
            return;
        }

        mIsLoading = true;

        if (mLoadMoreUIHandler != null)
        {
            mLoadMoreUIHandler.onLoading(this);
        }
        if (null != mLoadMoreHandler)
        {
            mLoadMoreHandler.onLoadMore(this);
        }
    }

    private void onReachBottom()
    {
        // if has error, just leave what it should be
        if (mLoadError)
        {
            return;
        }
        if (mAutoLoadMore)
        {
            tryToPerformLoadMore();
        } else
        {
            if (mHasMore)
            {
                mLoadMoreUIHandler.onWaitToLoadMore(this);
            }
        }
    }

    @Override
    public void setShowLoadingForFirstPage(boolean showLoading)
    {
        mShowLoadingForFirstPage = showLoading;
    }

    @Override
    public void setAutoLoadMore(boolean autoLoadMore)
    {
        mAutoLoadMore = autoLoadMore;
    }

    @Override
    public void setOnScrollListener(AbsListView.OnScrollListener l)
    {
        mOnScrollListener = l;
    }

    @Override
    public void setLoadMoreView(View view)
    {
        // has not been initialized
        if (mAbsListView == null)
        {
            mFooterView = view;
            return;
        }
        // remove previous
        if (mFooterView != null && mFooterView != view)
        {
            removeFooterView(view);
        }

        // add current
        mFooterView = view;
        mFooterView.setOnClickListener(new OnClickListener()
        {

            @Override
            public void onClick(View view)
            {
                tryToPerformLoadMore();
            }
        });

        addFooterView(view);
    }

    @Override
    public void setLoadMoreUIHandler(LoadMoreUIHandler handler)
    {
        mLoadMoreUIHandler = handler;
    }

    @Override
    public void setLoadMoreHandler(LoadMoreHandler handler)
    {
        mLoadMoreHandler = handler;
    }

    /**
     * page has loaded
     * 
     * @param emptyResult
     * @param hasMore
     */
    @Override
    public void loadMoreFinish(boolean emptyResult, boolean hasMore)
    {
        mLoadError = false;
        mListEmpty = emptyResult;
        mIsLoading = false;
        mHasMore = hasMore;

        if (mLoadMoreUIHandler != null)
        {
            mLoadMoreUIHandler.onLoadFinish(this, emptyResult, hasMore);
        }
    }

    @Override
    public void loadMoreError(int errorCode, String errorMessage)
    {
        mIsLoading = false;
        mLoadError = true;
        if (mLoadMoreUIHandler != null)
        {
            mLoadMoreUIHandler.onLoadError(this, errorCode, errorMessage);
        }
    }

    protected abstract void addFooterView(View view);

    protected abstract void removeFooterView(View view);

    protected abstract AbsListView retrieveAbsListView();
}