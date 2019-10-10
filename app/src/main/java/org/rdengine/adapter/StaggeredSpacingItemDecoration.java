package org.rdengine.adapter;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

import com.android.frame.R;

/**
 * Recyclerview 瀑布流布局的间距 Created by CCCMAX on 18/11/1.
 */
public class StaggeredSpacingItemDecoration extends RecyclerView.ItemDecoration
{

    private int spanCount; // 列数
    private int spacing; // 各列中间的间隔
    private int start_spacing;// 第一列外部间距
    private int end_spacing;// 最后一列外部间距

    public StaggeredSpacingItemDecoration(int spanCount, int spacing, int start, int end)
    {
        this.spanCount = spanCount;
        this.spacing = spacing;
        this.start_spacing = start;
        this.end_spacing = end;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state)
    {
        // 用ID做标签 来控制某些view不修改间距 （例如header）
        if (view.getId() != R.id.cell)
            return;

        StaggeredGridLayoutManager.LayoutParams params = (StaggeredGridLayoutManager.LayoutParams) view
                .getLayoutParams();

        if (spanCount == 1)
        {
            outRect.left = start_spacing;
            outRect.right = end_spacing;
            return;
        }

        // 暂时写的两列
        /**
         * 根据params.getSpanIndex()来判断左右边确定分割线 第一列设置左边距为space，右边距为space/2 （第二列反之）
         */
        // if (params.getSpanIndex() % 2 == 0)
        if (params.getSpanIndex() == 0)
        {
            // 第一列
            outRect.left = start_spacing;
            outRect.right = spacing / 2;
        } else if (params.getSpanIndex() == spanCount - 1)
        {
            // 最后一列
            outRect.left = spacing / 2;
            outRect.right = end_spacing;
        } else
        {
            // 中间
            outRect.left = spacing / 2;
            outRect.right = outRect.left;
        }
    }
}
