package org.rdengine.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

/**
 * 有多长 占多长 不复用 不滚动
 * 
 * @author CCCMAX
 */
public class CustomGridView extends GridView
{

    public CustomGridView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    public CustomGridView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public CustomGridView(Context context)
    {
        super(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }

}