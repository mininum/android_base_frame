package org.rdengine.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;


import com.android.frame.R;

import org.rdengine.log.DLOG;
import org.rdengine.util.PhoneUtil;
import org.rdengine.util.RandomUtil;

import java.util.ArrayList;

/**
 * Created by CCCMAX on 17/9/7.
 */

public class AudioSpectrumView extends View
{
    String TAG = "AudioSpectrumView";

    public AudioSpectrumView(Context context)
    {
        super(context);
        init(context, null);
    }

    public AudioSpectrumView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context, attrs);
    }

    public AudioSpectrumView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    Paint paint;

    /** 频谱条形的颜色 */
    int columnColor;
    /** 条形宽度 */
    float columnWidth;
    /** 频谱条形最大高度 */
    float columnMaxHeight;
    /** 频谱条形最小高度 */
    float columnMinHeight;
    /** 频谱条形之间的间隔 */
    float columnSpace;
    /** 频谱条形的数量 */
    int columnCount;

    ArrayList<SubColumn> scList;

    private void init(Context context, AttributeSet attrs)
    {

        TypedArray mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.audio_spectrum);

        // 获取自定义属性和默认值
        columnColor = mTypedArray.getColor(R.styleable.audio_spectrum_columnColor, Color.WHITE);
        columnWidth = mTypedArray.getDimension(R.styleable.audio_spectrum_columnWidth,
                PhoneUtil.dipToPixel(2, getContext()));
        columnMaxHeight = mTypedArray.getDimension(R.styleable.audio_spectrum_columnMaxHeight,
                PhoneUtil.dipToPixel(10, getContext()));
        columnMinHeight = mTypedArray.getDimension(R.styleable.audio_spectrum_columnMinHeight,
                PhoneUtil.dipToPixel(1, getContext()));
        columnSpace = mTypedArray.getDimension(R.styleable.audio_spectrum_columnSpace,
                PhoneUtil.dipToPixel(1.5F, getContext()));
        columnCount = mTypedArray.getInteger(R.styleable.audio_spectrum_columnCount, 4);
        mTypedArray.recycle();

        paint = new Paint();
        paint.setColor(columnColor); // 设置圆环的颜色
        paint.setStyle(Paint.Style.FILL); // 设置实心
        paint.setAntiAlias(true); // 消除锯齿

        // 构造元素
        scList = new ArrayList<SubColumn>();
        for (int i = 0; i < columnCount; i++)
        {
            SubColumn sc = new SubColumn();
            sc.width = columnWidth;
            sc.maxHeight = columnMaxHeight;
            sc.minHeight = columnMinHeight;
            scList.add(sc);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom)
    {
        super.onLayout(changed, left, top, right, bottom);
        if (changed)
        {
            // 改变背景点的坐标信息
            int w = right - left;
            int h = bottom - top;

            // 总长 = 宽*条数 + 间隔*(条数-1)
            float ww = columnWidth * columnCount + columnSpace * (columnCount - 1);
            // 居中的起始位置 (左)
            int left_start = (int) ((w - ww) / 2);
            // 居中的起始位置（下）
            int bottom_start = (int) (h / 2 + columnMaxHeight / 2);

            // 静态点位置
            for (int i = 0; i < scList.size(); i++)
            {
                SubColumn sc = scList.get(i);
                // x= 左侧起始位置 + 圆点宽度*位置 + 间隔*位置
                sc.x = (int) (left_start + columnWidth * i + columnSpace * i);
                sc.y = bottom_start;
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        for (int i = 0; i < scList.size(); i++)
        {
            SubColumn sc = scList.get(i);
            sc.draw(canvas, paint);
        }
        if (state != PAUSE)
            postInvalidateDelayed(15);
    }

    final int STOP = 0;
    final int PLAY = 1;
    final int PAUSE = 2;

    int state = PLAY;

    public void play()
    {
        // 开始随机循环
        if (state != PLAY)
        {
            state = PLAY;
            postInvalidate();
            DLOG.e(TAG, "play");
        }
    }

    public void pause()
    {
        // 暂停保持当前位置
        if (state == PLAY)
        {
            state = PAUSE;
            DLOG.e(TAG, "PAUSE");
        }
    }

    public void stop()
    {
        // 停止 所有条形自动下落到对消并保持在最小
        state = STOP;
        postInvalidate();
        DLOG.e(TAG, "stop");
    }

    class SubColumn
    {
        /** 条形左下角坐标 */
        int x, y;
        /** 最大高度 最小高度 */
        float maxHeight, minHeight;
        /** 宽 */
        float width;

        /** 目标高度比例 0～1 */
        float target_ratio = -1;

        /** 当前高度比例 0~1 */
        float current_ratio = 0;

        /** 比例步进 */
        float tick_ratio = 0.05F;

        public void draw(Canvas canvas, Paint paint)
        {
            int currentHeight = 0;

            // 计算高度
            if (state == PLAY)
            {
                // 运动中
                if (target_ratio < 0 || target_ratio == current_ratio)
                {
                    // 没有目标 或 前一个目标已达到， 确定一个目标
                    float newtarget = RandomUtil.getRandom(0, 100) / 100.0F;
                    if (newtarget == target_ratio)
                    {
                        newtarget = newtarget == 0 ? 1 : 0;
                    }
                    target_ratio = newtarget;
                }

                // 上升或下降
                if (current_ratio < target_ratio)
                {
                    current_ratio += tick_ratio;
                    if (current_ratio > target_ratio)
                        current_ratio = target_ratio;
                } else if (current_ratio > target_ratio)
                {
                    current_ratio -= tick_ratio;
                    if (current_ratio < target_ratio)
                        current_ratio = target_ratio;
                }

            } else if (state == STOP)
            {
                // 结束 不是最低就减到最低
                if (current_ratio > 0)
                {
                    current_ratio -= tick_ratio;
                }
                if (current_ratio < 0)
                    current_ratio = 0;

            } else if (state == PAUSE)
            {
                // 暂停 保持进度不变
            }

            // 保持在有小范围内
            current_ratio = Math.min(current_ratio, 1.0F);
            current_ratio = Math.max(current_ratio, 0.0F);

            // 计算要绘制的高度
            currentHeight = (int) (minHeight + (maxHeight - minHeight) * current_ratio);

            // 绘制
            RectF rect = new RectF();
            rect.left = x;
            rect.top = y - currentHeight;
            rect.right = x + width;
            rect.bottom = y;
            canvas.drawRect(rect, paint);
        }

    }
}
