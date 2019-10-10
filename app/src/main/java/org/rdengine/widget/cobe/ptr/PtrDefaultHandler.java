package org.rdengine.widget.cobe.ptr;

import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AbsListView;

import org.rdengine.log.DLOG;

public abstract class PtrDefaultHandler implements PtrHandler
{

    public static boolean canChildScrollUp(View view)
    {
        // if (android.os.Build.VERSION.SDK_INT < 14) {
        if (view instanceof AbsListView)
        {
            final AbsListView absListView = (AbsListView) view;
            return absListView.getChildCount() > 0
                    && (absListView.getFirstVisiblePosition() > 0 || (absListView.getFirstVisiblePosition() == 0
                            && absListView.getChildAt(0).getTop() < absListView.getPaddingTop()));
        } else
        {
            return view.getScrollY() > 0;
        }
        // } else {
        // return view.canScrollVertically(-1);
        // }
    }

    /**
     * Default implement for check can perform pull to refresh
     * 
     * @param frame
     * @param content
     * @param header
     * @return
     */
    public static boolean checkContentCanBePulledDown(PtrFrameLayout frame, View content, View header)
    {
        return !canChildScrollUp(content);
    }

    @Override
    public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header)
    {
        return checkContentCanBePulledDown(frame, content, header);
    }

    public static boolean checkRecyclerCanBePulledDown(PtrFrameLayout frame, View content, View header)
    {
        boolean ret = false;
        if (content instanceof RecyclerView)
        {
            RecyclerView recyclerView = (RecyclerView) content;
            if (recyclerView.getChildAt(0) != null)
            {
                // 这个计算方法如果有ItemDecoration设置top边距 ， 会无法触发
                int top = recyclerView.getChildAt(0).getTop();
                // ret = top == 0;

                if (recyclerView.getChildCount() > 0)
                {
                    int currentPosition = ((RecyclerView.LayoutParams) recyclerView.getChildAt(0).getLayoutParams())
                            .getViewAdapterPosition();
                    // 当显示第0个item 同时top距离是0
                    ret = currentPosition == 0 && top == 0;
                }

                if (!ret)
                {
                    try
                    {
                        // 判断 第一个 完全显示的item 序号 是 0
                        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                        int firstCompletelyVisibleItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition();
                        // DLOG.i("cccmax", "firstCompletelyVisibleItemPosition: " + firstCompletelyVisibleItemPosition);
                        ret = firstCompletelyVisibleItemPosition == 0;
                    } catch (Exception ex)
                    {
                    }
                }

                if (!ret)
                {
                    // 判断 自一个子view 的ItemDecoration
                    Rect outRect = new Rect();
                    recyclerView.getLayoutManager().calculateItemDecorationsForChild(recyclerView.getChildAt(0),
                            outRect);
                    if (outRect.top == top)
                        ret = true;
                }
            } else
            {
                ret = checkContentCanBePulledDown(frame, content, header);
            }
        } else
        {
            ret = checkContentCanBePulledDown(frame, content, header);
        }
        DLOG.d("cccmax", "checkCanDoRefresh  " + ret);
        return ret;
    }
}