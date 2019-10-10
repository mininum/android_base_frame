package com.facebook.fresco;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import com.facebook.drawee.drawable.DrawableUtils;

import org.rdengine.log.DLOG;

public class DefProgressBarDrawable extends Drawable
{

    private final Paint mPaint;
    private int mBackgroundColor;
    private int mColor;
    private int mLevel;
    private boolean mHideWhenZero;
    /** 六边形 */
    private boolean isHexagon = false;

    public DefProgressBarDrawable()
    {
        this.mPaint = new Paint(1);
        this.mPaint.setAntiAlias(true);
        this.mBackgroundColor = 0x80ADADAD;
        this.mColor = 0X80FFFFFF;
        this.mLevel = 0;
        this.mHideWhenZero = false;
    }

    public void setColor(int color)
    {
        if (this.mColor != color)
        {
            this.mColor = color;
            invalidateSelf();
        }
    }

    public int getColor()
    {
        return this.mColor;
    }

    public void setBackgroundColor(int backgroundColor)
    {
        if (this.mBackgroundColor != backgroundColor)
        {
            this.mBackgroundColor = backgroundColor;
            invalidateSelf();
        }
    }

    public int getBackgroundColor()
    {
        return this.mBackgroundColor;
    }

    public void setHideWhenZero(boolean hideWhenZero)
    {
        this.mHideWhenZero = hideWhenZero;
    }

    public boolean getHideWhenZero()
    {
        return this.mHideWhenZero;
    }

    @Override
    protected boolean onLevelChange(int level)
    {
        DLOG.i("onLevelChange", this.hashCode() + "  level=" + level);
        this.mLevel = level;
        invalidateSelf();
        return true;
    }

    @Override
    public void setAlpha(int alpha)
    {
        this.mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf)
    {
        this.mPaint.setColorFilter(cf);
    }

    @Override
    public int getOpacity()
    {
        return DrawableUtils.getOpacityFromColor(this.mPaint.getColor());
    }

    public void draw(Canvas canvas)
    {
        if ((this.mHideWhenZero) && (this.mLevel == 0))
        {
            return;
        }

        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.FILTER_BITMAP_FLAG | Paint.ANTI_ALIAS_FLAG));

        Rect bounds = getBounds();
        int min = Math.min(bounds.width(), bounds.height());
        min = (int) (min / 3.0f);// 直径＝宽高最小值的三分之一
        int radius = min / 2;

        int center_point_x = bounds.left + bounds.width() / 2;
        int center_point_y = bounds.top + bounds.height() / 2;
        int rect_l = center_point_x - radius;
        int rect_t = center_point_y - radius;
        int rect_r = center_point_x + radius;
        int rect_b = center_point_y + radius;
        RectF oval = new RectF(rect_l, rect_t, rect_r, rect_b);

        if (isHexagon)
        {
            // 正六边形
            float _h = oval.height() * 0.075F;
            Path path = new Path();
            path.moveTo(rect_l + oval.width() / 4, rect_t + _h);
            path.lineTo(rect_l + oval.width() * 0.75F, rect_t + _h);
            path.lineTo(rect_r, rect_t + oval.width() / 2);
            path.lineTo(rect_l + oval.width() * 0.75F, rect_b - _h);
            path.lineTo(rect_l + oval.width() / 4, rect_b - _h);
            path.lineTo(rect_l, rect_t + oval.width() / 2);
            path.close();
            canvas.clipPath(path);
        }

        drawBar(canvas, 10000, this.mBackgroundColor, oval);
        drawBar(canvas, this.mLevel, this.mColor, oval);
    }

    /**
     * @param canvas
     * @param level
     *            0-10000 progress
     * @param color
     */
    private void drawBar(Canvas canvas, int level, int color, RectF oval)
    {
        this.mPaint.setColor(color);
        float sweepAngle = 360.0F * level / 10000;

        if (isHexagon)
        {
            // 正六边形
            canvas.drawArc(new RectF(getBounds()), -90, sweepAngle, true, this.mPaint);
        } else
        {
            // 圆形
            canvas.drawArc(oval, -90, sweepAngle, true, this.mPaint);
        }
    }
}
