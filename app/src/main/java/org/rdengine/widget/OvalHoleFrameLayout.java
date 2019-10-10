package org.rdengine.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Path.FillType;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * 椭圆型镂空 向内镂空 或者 向外镂空
 * 
 * @author CCCMAX
 */
public class OvalHoleFrameLayout extends FrameLayout
{

    public OvalHoleFrameLayout(Context context)
    {
        super(context);
        init(context, null, 0);
    }

    public OvalHoleFrameLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public OvalHoleFrameLayout(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    /** 画笔 */
    Paint pt = new Paint();

    public void init(Context context, AttributeSet attrs, int defStyle)
    {
        if (this.getBackground() == null)
        {
            setBackgroundColor(Color.TRANSPARENT);
        }

        // 创建画笔 颜色 抗锯齿
        pt.setColor(Color.WHITE);
        pt.setDither(true);
        pt.setAntiAlias(true);
        pt.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));

        if (isInEditMode())
        {
            setOvalHole(480, 760, 0, 0, true);
        }

    }

    @Override
    protected void drawableStateChanged()
    {
        super.drawableStateChanged();
    }

    /** 椭圆宽 */
    int oval_w = 0;
    /** 椭圆高 */
    int oval_h = 0;
    /** 椭圆X坐标 */
    int oval_x = 0;
    /** 椭圆Y坐标 */
    int oval_y = 0;
    /** true:椭圆内部镂空；false:椭圆外部镂空 */
    boolean holeInside = true;

    /**
     * 设置镂空椭圆
     * 
     * @param w
     *            宽
     * @param h
     *            高
     * @param x
     *            左上角的X坐标
     * @param y
     *            左上角的Y坐标
     * @param holeInside
     *            true:椭圆内部镂空；false:椭圆外部镂空
     */
    public void setOvalHole(int w, int h, int x, int y, boolean holeInside)
    {
        oval_w = w;
        oval_h = h;
        oval_x = x;
        oval_y = y;
        this.holeInside = holeInside;
        invalidate();
    }

    int w = 0;
    int h = 0;

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom)
    {
        super.onLayout(changed, left, top, right, bottom);
        w = getWidth();
        h = getHeight();
    }

    @SuppressLint("NewApi")
    @Override
    public void draw(Canvas canvas)
    {
        if (oval_w == 0 && oval_h == 0)
        {
            super.draw(canvas);
            return;
        }

        long t_a = System.currentTimeMillis();

        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.FILTER_BITMAP_FLAG | Paint.ANTI_ALIAS_FLAG));

        if (w == 0)
            w = canvas.getWidth();
        if (h == 0)
            h = canvas.getHeight();

        // 画布尺寸矩形
        RectF fullrect = new RectF(0, 0, w, h);
        // 必须创建一个新的layer层 否则会把整个页面布局的背景色扣掉。。。
        canvas.saveLayerAlpha(fullrect, 0xFF, 999);

        // 路径 从画布上删除的部分
        Path pth = new Path();

        // B 椭圆所在矩形
        RectF ovalRect = new RectF(oval_x, oval_y, oval_x + oval_w, oval_y + oval_h);

        if (holeInside)
        {
            // 椭圆内部镂空

            // 路径叠加部分相减 A-B 剩下的是圆角的范围
            pth.setFillType(FillType.EVEN_ODD);
            // pth.addRect(fullrect, Direction.CW);// A 矩形

            pth.addOval(ovalRect, Direction.CW);// B 椭圆
        } else
        {
            // 椭圆外部镂空

            // 路径叠加部分相减 A-B 剩下的是圆角的范围
            pth.setFillType(FillType.EVEN_ODD);
            pth.addRect(fullrect, Direction.CW);// A 矩形
            // B 椭圆所在矩形
            // pth.addOval(ovalRect, Direction.CCW);// B 椭圆

            float ra_x = (ovalRect.right - ovalRect.left) / 2;
            float ra_y = (ovalRect.bottom - ovalRect.top) / 2;
            float[] radii =
            { ra_x, ra_y, ra_x, ra_y, ra_x, ra_y, ra_x, ra_y };
            pth.addRoundRect(ovalRect, radii, Direction.CW);

        }

        super.draw(canvas);

        canvas.drawPath(pth, pt);// pt设置排除模式、抗锯齿

        // 恢复状态 之前绘制在layer上的内容全都合并到canvas上
        canvas.restore();

        if (isInEditMode())
        {
            Paint p = new Paint();
            p.setColor(0xff00ff00);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setAntiAlias(true);
            p.setStrokeWidth(2);
            canvas.drawPath(pth, p);

            Path oval = new Path();
            oval.addOval(ovalRect, Direction.CW);
            canvas.drawPath(oval, p);

            canvas.drawText("oval=" + ovalRect.toString(), 0, 100, p);
        }

        long t_b = System.currentTimeMillis();
        // if (RT.DEBUG)
        // {
        // DLOG.i("OvalHoleFrameLayout", "OvalHoleFrameLayout draw time=" + (t_b - t_a));
        // }
    }
}
