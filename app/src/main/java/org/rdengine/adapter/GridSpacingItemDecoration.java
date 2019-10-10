package org.rdengine.adapter;

import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.android.frame.R;

/**
 * 网格布局的间距 Created by CCCMAX on 18/11/1.
 */
public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration
{

    private int v_left;// 第一列外部间距
    private int v_middle; // 各列中间的间隔
    private int v_right;// 最后一列外部间距

    private int h_top;// 第一行外部间距
    private int h_middle; // 各行中间的间隔
    private int h_bottom;// 最后一行外部间距

    // 方向 默认竖着
    private int orientation = OrientationHelper.VERTICAL;

    // 列数
    int spanCount;

    public GridSpacingItemDecoration(int spanCount, int orientation, int v_left, int v_middle, int v_right, int h_top,
            int h_middle, int h_bottom)
    {
        this.spanCount = spanCount;
        this.orientation = orientation;

        this.v_left = v_left;
        this.v_middle = v_middle;
        this.v_right = v_right;

        this.h_top = h_top;
        this.h_middle = h_middle;
        this.h_bottom = h_bottom;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state)
    {
        // 用ID做标签 来控制某些view不修改间距 （例如header）
        if (view.getId() != R.id.cell)
            return;

        GridLayoutManager.LayoutParams params = (GridLayoutManager.LayoutParams) view.getLayoutParams();

        // 当前在第节列
        int spanindex = params.getSpanIndex();

        if (orientation == OrientationHelper.VERTICAL)
        {
            // 纵向列表

            // 左中右 间距
            if (spanCount == 1)
            {
                outRect.left = v_left;
                outRect.right = v_right;
            } else
            {
                if (spanindex == 0)
                {
                    // 第一列
                    outRect.left = v_left;
                    outRect.right = v_middle / 2;
                } else if (spanindex == spanCount - 1)
                {
                    // 最后一列
                    outRect.left = v_middle / 2;
                    outRect.right = v_right;
                } else
                {
                    // 中间
                    outRect.left = v_middle / 2;
                    outRect.right = outRect.left;
                }
            }

            if (params.getViewAdapterPosition() < spanCount)
            {
                // 第一行的item 添加top
                outRect.top = h_top;

                outRect.bottom = h_bottom;
            } else
            {
                outRect.top = 0;
                outRect.bottom = h_bottom;
            }
        } else
        {
            // 横向列表

            // 上中下 间距
            if (spanCount == 1)
            {
                outRect.top = h_top;
                outRect.bottom = h_bottom;
            } else
            {
                if (spanindex == 0)
                {
                    // 第一列
                    outRect.top = h_top;
                    outRect.bottom = h_middle / 2;
                } else if (spanindex == spanCount - 1)
                {
                    // 最后一列
                    outRect.top = h_middle / 2;
                    outRect.bottom = h_bottom;
                } else
                {
                    // 中间
                    outRect.top = h_middle / 2;
                    outRect.bottom = outRect.left;
                }
            }

            if (params.getViewAdapterPosition() < spanCount)
            {
                // 第一行的item 添加top
                outRect.left = v_left;
                outRect.right = v_right;
            } else
            {
                outRect.left = 0;
                outRect.right = v_right;
            }
        }

    }
}
