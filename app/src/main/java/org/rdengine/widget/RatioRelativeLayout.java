package org.rdengine.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.android.frame.R;


public class RatioRelativeLayout extends RelativeLayout
{

    // <declare-styleable name="RatioLayout">
    // <attr name="rl_aspectRatio" format="float" />
    // <attr name="rl_adjustWidth" format="boolean" />
    // </declare-styleable>

    public RatioRelativeLayout(Context context)
    {
        super(context);
    }

    public RatioRelativeLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(attrs);
    }

    float aspectRatio = 0;
    boolean adjustWidth = false;

    private void init(AttributeSet attrs)
    {
        Context context = getContext();
        if (attrs != null && context != null)
        {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RatioLayout);
            if (a != null)
            {
                aspectRatio = a.getFloat(R.styleable.RatioLayout_rl_aspectRatio, 0);
                adjustWidth = a.getBoolean(R.styleable.RatioLayout_rl_adjustWidth, false);
            }
        }
    }

    public float getAspectRatio()
    {
        return aspectRatio;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        final int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        if (aspectRatio > 0 && (widthSpecMode == MeasureSpec.AT_MOST || widthSpecMode == MeasureSpec.EXACTLY))
        {
            int widthSize;
            int heightSize;

            int pleft = getPaddingLeft();
            int pright = getPaddingRight();
            int ptop = getPaddingTop();
            int pbottom = getPaddingBottom();

            int w = 0;
            int h = 0;
            w += pleft + pright;
            h += ptop + pbottom;

            if (adjustWidth)
            {
                heightSize = resolveSizeAndStates(h, heightMeasureSpec, 0);
                widthSize = (int) ((heightSize - ptop - pbottom) * aspectRatio) + pleft + pright;
            } else
            {
                widthSize = resolveSizeAndStates(w, widthMeasureSpec, 0);
                heightSize = (int) ((widthSize - pleft - pright) * aspectRatio) + ptop + pbottom;
            }

            // setMeasuredDimension(widthSize, heightSize);
            int www = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
            int hhh = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY);
            super.onMeasure(www, hhh);
            // Log.e("cccc", "id " + getId() + " " + widthSize + " * " + heightSize);
        } else
        {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    public static int resolveSizeAndStates(int size, int measureSpec, int childMeasuredState)
    {
        int result = size;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (specMode)
        {
        case MeasureSpec.UNSPECIFIED :
            result = size;
            break;
        case MeasureSpec.AT_MOST :
            if (specSize < size)
            {
                result = specSize | 0x01000000;
            } else
            {
                result = size;
            }
            break;
        case MeasureSpec.EXACTLY :
            result = specSize;
            break;
        }
        return result | (childMeasuredState & 0xff000000);
    }

}
