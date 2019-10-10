package org.rdengine.widget.cobe.ptr;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.android.frame.R;

import org.rdengine.log.DLOG;
import org.rdengine.widget.cobe.ptr.indicator.PtrIndicator;

/**
 * 下拉刷新 旋转剪头，跟随移动旋转
 */
public class PtrClassicRotateMoveHeader extends FrameLayout implements PtrUIHandler
{

    // ----------------R.layout.cube_ptr_classic_rotate_move_header-------------Start
    private ImageView ptr_classic_header_rotate_move;

    /**
     * auto load R.layout.cube_ptr_classic_rotate_move_header
     */
    private void autoLoad_cube_ptr_classic_rotate_move_header()
    {
        ptr_classic_header_rotate_move = (ImageView) findViewById(R.id.ptr_classic_header_rotate_move);
    }

    // ----------------R.layout.cube_ptr_classic_rotate_move_header-------------End

    public PtrClassicRotateMoveHeader(Context context)
    {
        super(context);
        initViews(null);
    }

    public PtrClassicRotateMoveHeader(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        initViews(attrs);
    }

    public PtrClassicRotateMoveHeader(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        initViews(attrs);
    }

    protected void initViews(AttributeSet attrs)
    {
        View header = LayoutInflater.from(getContext()).inflate(R.layout.cube_ptr_classic_rotate_move_header, this);

        autoLoad_cube_ptr_classic_rotate_move_header();

        int round_D = ptr_classic_header_rotate_move.getLayoutParams().width;// 圆形直径
        circumference = (float) (Math.PI * round_D);// 周长
        angle_length = circumference / 360.0F;
    }

    /** 周长像素 */
    float circumference = 0;
    /** 每一度对应的夹角弧线长度 */
    float angle_length = 1.0F;

    @Override
    protected void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();
    }

    ValueAnimator animation_rotate;

    private void stopAnimatable()
    {
        try
        {
            if (animation_rotate != null && animation_rotate.isRunning())
            {
                animation_rotate.end();
                animation_rotate = null;
                return;
            }
        } catch (Exception ex)
        {
            // ex.printStackTrace();
        }
    }

    private void startAnimatable()
    {
        try
        {
            if (animation_rotate != null && animation_rotate.isRunning())
            {
                animation_rotate.end();
                animation_rotate = null;
                return;
            }

            final float r = ptr_classic_header_rotate_move.getRotation();// 当前角度

            animation_rotate = ValueAnimator.ofFloat(0, -1);
            animation_rotate.setDuration((long) (500));
            animation_rotate.setRepeatCount(ValueAnimator.INFINITE);// 无限重复
            animation_rotate.setRepeatMode(ValueAnimator.RESTART);// 重新开始
            animation_rotate.setInterpolator(new LinearInterpolator());
            animation_rotate.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
            {
                public void onAnimationUpdate(ValueAnimator animation)
                {
                    float v = (Float) animation.getAnimatedValue();
                    // -1 ________ 0 ________ 1
                    // -359 ______ 0 ______ 359

                    float angle = 259 * v; // 旋转角度
                    float rrr = (r + angle) % 360;
                    ptr_classic_header_rotate_move.setRotation(rrr);
                }
            });
            animation_rotate.addListener(new Animator.AnimatorListener()
            {
                public void onAnimationStart(Animator animation)
                {}

                public void onAnimationRepeat(Animator animation)
                {}

                public void onAnimationEnd(Animator animation)
                {
                    animation_rotate = null;
                }

                public void onAnimationCancel(Animator animation)
                {
                    animation_rotate = null;
                }
            });
            animation_rotate.start();
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    @Override
    public void onUIReset(PtrFrameLayout frame)
    {
        // 当位置回到初始位置。
        DLOG.e("PtrClassicRotateMoveHeader", "onUIReset");
        stopAnimatable();
        ptr_classic_header_rotate_move.setRotation(0);// 0旋转角度 复位
        ptr_classic_header_rotate_move.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onUIRefreshPrepare(PtrFrameLayout frame)
    {
        DLOG.e("PtrClassicRotateMoveHeader", "onUIRefreshPrepare");
        ptr_classic_header_rotate_move.setVisibility(View.VISIBLE);
        // 当Header离开初始位置

        if (frame.isPullToRefresh())
        {
        } else
        {
        }
    }

    @Override
    public void onUIRefreshBegin(PtrFrameLayout frame)
    {
        DLOG.e("PtrClassicRotateMoveHeader", "onUIRefreshBegin");
        // Header开始刷新动画
        startAnimatable();
    }

    @Override
    public void onUIRefreshComplete(PtrFrameLayout frame)
    {
        DLOG.e("PtrClassicRotateMoveHeader", "onUIRefreshComplete");
        // Header刷新动画完成。Header刷新完成之后，开始回归初始位置。
        stopAnimatable();
    }

    @Override
    public void onUIPositionChange(PtrFrameLayout frame, boolean isUnderTouch, byte status, PtrIndicator ptrIndicator)
    {
        // DLOG.e("PtrClassicRotateMoveHeader", "onUIPositionChange");
        // Header位置发生变化时此方法通知UI更新

        final int mOffsetToRefresh = frame.getOffsetToRefresh();// 可以触发刷新的位置
        final int currentPos = ptrIndicator.getCurrentPosY();// 当前移动的点
        final int lastPos = ptrIndicator.getLastPosY();// 上一次移动的点

        if (currentPos < mOffsetToRefresh && lastPos >= mOffsetToRefresh)
        {
            // 当前点小于触发点 ， 上一次点大于触发点
            if (isUnderTouch && status == PtrFrameLayout.PTR_STATUS_PREPARE)
            {
                crossRotateLineFromBottomUnderTouch(frame);
            }
        } else if (currentPos > mOffsetToRefresh && lastPos <= mOffsetToRefresh)
        {
            if (isUnderTouch && status == PtrFrameLayout.PTR_STATUS_PREPARE)
            {
                crossRotateLineFromTopUnderTouch(frame);
            }
        }

        // 更随移动旋转
        if (!frame.isPullToRefresh())
        {
            // float r = 360 - (currentPos / angle_length) % 360;// 旋转角度 逆时针
            float r = (currentPos / angle_length) % 360;// 旋转角度 顺时针
            ptr_classic_header_rotate_move.setRotation(r);
        }
    }

    private void crossRotateLineFromTopUnderTouch(PtrFrameLayout frame)
    {
        if (!frame.isPullToRefresh())
        {
            // 释放刷新
            // startAnimatable();
        }
    }

    private void crossRotateLineFromBottomUnderTouch(PtrFrameLayout frame)
    {
        // 往下拉,提示下拉刷新

    }
}