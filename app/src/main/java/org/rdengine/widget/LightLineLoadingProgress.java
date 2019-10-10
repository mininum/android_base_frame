package org.rdengine.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import com.android.frame.R;


/**
 *
 */
public class LightLineLoadingProgress extends View
{
    /**
     * 画笔对象的引用
     */
    private Paint mPaint;

    /** view宽高 */
    int w, h;

    /** 光斑宽度 */
    int light_w;

    /** 路程 */
    int distance;

    /** 进度 */
    float progrss = 0;

    /** 光线颜色 */
    int color_light = 0xffffffff;
    int[] colors;
    float[] colors_positions;

    /** 光线占view长度的比例 */
    float light_w_ratio = 0.3F;

    public LightLineLoadingProgress(Context context)
    {
        this(context, null);
    }

    public LightLineLoadingProgress(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public LightLineLoadingProgress(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);

        TypedArray mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.LightLineLoadingProgress);
        color_light = mTypedArray.getColor(R.styleable.LightLineLoadingProgress_light_color, 0xffffffff);
        light_w_ratio = mTypedArray.getFloat(R.styleable.LightLineLoadingProgress_light_ratio, 0.5F);
        progrss = mTypedArray.getFloat(R.styleable.LightLineLoadingProgress_light_start_progress, 0F);
        mTypedArray.recycle();

        if (light_w_ratio < 0 || light_w_ratio > 1)
            light_w_ratio = 0.5F;

        initPaint();
        setColor(color_light);

    }

    public void initPaint()
    {
        this.mPaint = new Paint();
        this.mPaint.setAntiAlias(true);
    }

    public void setColor(int color)
    {
        color_light = color;
        int color_light_alpha = Color.argb(0, Color.red(color), Color.green(color), Color.blue(color));
        colors = new int[]
        { color_light_alpha, color_light, color_light_alpha };
        colors_positions = new float[]
        { 0, 0.5F, 1 };
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom)
    {
        super.onLayout(changed, left, top, right, bottom);
        // 改变背景点的坐标信息
        w = right - left;
        h = bottom - top;

        // 光斑宽度
        light_w = (int) (w * light_w_ratio);
        // 总长度
        distance = w + light_w;
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        // 设置渐变色

        int start_x = (int) (distance * progrss - light_w);
        int end_x = start_x + light_w;

        LinearGradient curve_shader = new LinearGradient(start_x, 0, end_x, 0, colors, colors_positions,
                Shader.TileMode.CLAMP);
        mPaint.setShader(curve_shader);

        canvas.drawRect(start_x, 0, end_x, h, mPaint);

        if (progrss >= 1)
            progrss = 0;
        else progrss += 0.01F;

        invalidate();
    }

    @Override
    public void setVisibility(int visibility)
    {
        super.setVisibility(visibility);
        if (visibility == View.VISIBLE)
        {
            onVisible();
        } else
        {
            onHide();
        }
    }

    private void onVisible()
    {
        // 重新显示
    }

    private void onHide()
    {
        // 隐藏
    }

    @Override
    protected void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();
        // 销毁
    }

}
