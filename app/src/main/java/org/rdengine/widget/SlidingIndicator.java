package org.rdengine.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.android.frame.R;


/**
 * 横向tiker指示器
 * 
 * @author CCCMAX
 */
public class SlidingIndicator extends LinearLayout
{
    private int mCount;
    private int mCurrSelectPositon;
    private LayoutInflater mLayoutInflater;
    private View[] indicators;
    // private final static LinearLayout.LayoutParams LAYOUT_PARAMS = new LinearLayout.LayoutParams(-2, -2);
    private Drawable mIndicatorDrawable = new ColorDrawable();
    private int margin = 0;

    private int item_w = 0;
    private int item_w_selected = 0;

    public SlidingIndicator(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.tiker_indicator);
        final Drawable d = a.getDrawable(R.styleable.tiker_indicator_tiker_indicator_drawable);
        if (d != null)
        {
            mIndicatorDrawable = d;
        }
        margin = (int) a.getDimension(R.styleable.tiker_indicator_tiker_indicator_margin, 0);
        mLayoutInflater = LayoutInflater.from(context);

        item_w = (int) a.getDimension(R.styleable.tiker_indicator_tiker_indicator_w, 0);
        item_w_selected = (int) a.getDimension(R.styleable.tiker_indicator_tiker_indicator_w_selected, 0);

        a.recycle();
        setOrientation(LinearLayout.HORIZONTAL);

        if (isInEditMode())
        {
            setCount(5);
            onItemSelect(2);
        }
    }

    public synchronized void setCount(int count)
    {
        this.mCount = count;
        removeAllViews();
        int w = mIndicatorDrawable.getIntrinsicWidth();
        int h = mIndicatorDrawable.getIntrinsicHeight();
        indicators = new View[this.mCount];

        for (int i = 0; i < this.mCount; i++)
        {
            // indicators[i] = mLayoutInflater.inflate(R.layout.custom_gallery_indicator, null);
            indicators[i] = new View(getContext());
            indicators[i].setBackgroundDrawable(mIndicatorDrawable.getConstantState().newDrawable());
            LayoutParams layout_params = new LayoutParams(-2, -2);
            layout_params.width = w;
            layout_params.height = h;
            layout_params.setMargins(margin, 0, margin, 0);
            layout_params.gravity = Gravity.CENTER_VERTICAL;
            addView(indicators[i], layout_params);

        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (item_w > 0 && item_w_selected > 0)
            return;

        int count = getChildCount();
        View child = null;

        int w = mIndicatorDrawable.getIntrinsicWidth();
        int h = mIndicatorDrawable.getIntrinsicHeight();
        for (int i = 0; i < count; i++)
        {
            child = getChildAt(i);
            child.measure(MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY));
        }
    }

    public synchronized void onItemSelect(int pos)
    {
        this.mCurrSelectPositon = pos;
        boolean select = false;
        for (int i = mCount - 1; i >= 0; i--)
        {

            if (this.mCurrSelectPositon == i)
            {
                select = true;
            } else
            {
                select = false;
            }
            indicators[i].setSelected(select);

            if (item_w > 0 && item_w_selected > 0)
            {
                MarginLayoutParams lp = (MarginLayoutParams) indicators[i].getLayoutParams();
                lp.width = select ? item_w_selected : item_w;
                indicators[i].setLayoutParams(lp);
            }

        }
    }

}
