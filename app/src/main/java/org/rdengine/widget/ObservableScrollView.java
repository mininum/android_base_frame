package org.rdengine.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

/**
 * Created by CCCMAX on 18/8/3.
 */

public class ObservableScrollView extends ScrollView
{
    private ScrollViewListener scrollViewListener = null;

    public ObservableScrollView(Context context)
    {
        super(context);
    }

    public ObservableScrollView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    public ObservableScrollView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public void setScrollViewListener(ScrollViewListener scrollViewListener)
    {
        this.scrollViewListener = scrollViewListener;
    }

    @Override
    protected void onScrollChanged(int x, int y, int oldx, int oldy)
    {
        super.onScrollChanged(x, y, oldx, oldy);
        if (scrollViewListener != null)
        {
            scrollViewListener.onScrollChange(this, x, y, oldx, oldy);
        }
    }

    public interface ScrollViewListener
    {
        void onScrollChange(ObservableScrollView scrollView, int x, int y, int oldx, int oldy);

    }

}