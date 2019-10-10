package org.rdengine.widget;


import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.StringRes;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.frame.R;


/**
 * Created by CCCMAX on 2019/5/8.
 */

public class LoadingButton extends RelativeLayout
{
    final static String ANDROIDXML = "http://schemas.android.com/apk/res/android";

    public LoadingButton(Context context)
    {
        super(context);
        init(null);
    }

    public LoadingButton(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(attrs);
    }

    public LoadingButton(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    TextView textview;
    String text;
    int text_size;

    ColorStateList text_color;

    ProgressBar progressBar;
    ProgressBarCircular progressbarCc;
    int pb_color;
    int pb_size;

    private void init(AttributeSet attrs)
    {
        Drawable pb_drawable = null;
        Drawable tv_drawableLeft = null, tv_drawableRight = null, tv_drawableTop = null, tv_drawableBottom = null;

        if (attrs != null)
        {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.LoadingButton);
            text = typedArray.getString(R.styleable.LoadingButton_lb_text);
            text_color = typedArray.getColorStateList(R.styleable.LoadingButton_lb_textcolor);
            text_size = typedArray.getDimensionPixelSize(R.styleable.LoadingButton_lb_textsize, 20);

            pb_drawable = typedArray.getDrawable(R.styleable.LoadingButton_lb_progressbar_drawable);
            pb_color = typedArray.getColor(R.styleable.LoadingButton_lb_progressbar_color, 0xffffffff);
            pb_size = typedArray.getDimensionPixelSize(R.styleable.LoadingButton_lb_progressbar_size, 20);

            typedArray.recycle();

            try
            {
                int[] arr = new int[]
                { android.R.attr.drawableLeft, android.R.attr.drawableTop, android.R.attr.drawableRight,
                        android.R.attr.drawableBottom };
                TypedArray typedArray_text = getContext().obtainStyledAttributes(attrs, arr);
                tv_drawableLeft = typedArray_text.getDrawable(0);
                tv_drawableTop = typedArray_text.getDrawable(1);
                tv_drawableRight = typedArray_text.getDrawable(2);
                tv_drawableBottom = typedArray_text.getDrawable(3);
                typedArray_text.recycle();
            } catch (Exception ex)
            {
            }

        }

        textview = new TextView(getContext());
        textview.setText(text);
        textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, text_size);
        if (text_color != null)
            textview.setTextColor(text_color);
        else textview.setTextColor(0xff000000);

        textview.setCompoundDrawablesWithIntrinsicBounds(tv_drawableLeft, tv_drawableTop, tv_drawableRight,
                tv_drawableBottom);

        LayoutParams text_lp = new LayoutParams(-2, -2);
        text_lp.addRule(RelativeLayout.CENTER_IN_PARENT);
        addView(textview, text_lp);

        if (pb_drawable != null)
        {
            progressBar = new ProgressBar(getContext());
            progressBar.setIndeterminateDrawable(pb_drawable);
            LayoutParams pb_lp = new LayoutParams(pb_size, pb_size);
            pb_lp.addRule(RelativeLayout.CENTER_IN_PARENT);
            addView(progressBar, pb_lp);
        } else
        {
            progressbarCc = new ProgressBarCircular(getContext());
            progressbarCc.setNoZoom(true);
            progressbarCc.setBackgroundColor(pb_color);
            LayoutParams pb_lp = new LayoutParams(pb_size, pb_size);
            pb_lp.addRule(RelativeLayout.CENTER_IN_PARENT);
            addView(progressbarCc, pb_lp);
        }

        boolean enabled = attrs.getAttributeBooleanValue(ANDROIDXML, "enabled", true);
        setEnabled(enabled);

        endLoading();
    }

    @Override
    public void setEnabled(boolean enabled)
    {
        super.setEnabled(enabled);
        textview.setEnabled(enabled);
    }

    public final void setText(@StringRes int resid)
    {
        setText(getContext().getResources().getText(resid));
    }

    public void setText(CharSequence text)
    {
        textview.setText(text);
    }

    public void setTextSize(int unit, float size)
    {
        textview.setTextSize(unit, size);
    }

    public void setTextSizePx(float size)
    {
        setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
    }

    public void setTextColor(int color)
    {
        textview.setTextColor(color);
    }

    public void setTextColor(ColorStateList color)
    {
        text_color = color;
        textview.setTextColor(text_color);
    }

    public void setProgressbarSize(int unit, float size)
    {
        if (progressbarCc != null)
        {
            int size_px = (int) TypedValue.applyDimension(unit, size, getResources().getDisplayMetrics());
            LayoutParams pb_lp = (LayoutParams) progressbarCc.getLayoutParams();
            pb_lp.width = size_px;
            pb_lp.height = size_px;
            progressbarCc.setLayoutParams(pb_lp);
        } else
        {
            int size_px = (int) TypedValue.applyDimension(unit, size, getResources().getDisplayMetrics());
            LayoutParams pb_lp = (LayoutParams) progressBar.getLayoutParams();
            pb_lp.width = size_px;
            pb_lp.height = size_px;
            progressBar.setLayoutParams(pb_lp);
        }
    }

    public void setProgressbarSizePx(int size)
    {
        if (progressbarCc != null)
        {
            LayoutParams pb_lp = (LayoutParams) progressbarCc.getLayoutParams();
            pb_lp.width = size;
            pb_lp.height = size;
            progressbarCc.setLayoutParams(pb_lp);
        } else
        {
            LayoutParams pb_lp = (LayoutParams) progressBar.getLayoutParams();
            pb_lp.width = size;
            pb_lp.height = size;
            progressBar.setLayoutParams(pb_lp);
        }
    }

    public void startLoading()
    {
        textview.setVisibility(View.INVISIBLE);
        if (progressbarCc != null)
        {
            progressbarCc.setVisibility(View.VISIBLE);
        } else
        {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    public boolean isLoading()
    {
        if (progressbarCc != null)
        {
            return progressbarCc.getVisibility() == View.VISIBLE;
        } else
        {
            return progressBar.getVisibility() == View.VISIBLE;
        }
    }

    public void endLoading()
    {
        textview.setVisibility(View.VISIBLE);
        if (progressbarCc != null)
        {
            progressbarCc.setVisibility(View.INVISIBLE);
        } else
        {
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

}
