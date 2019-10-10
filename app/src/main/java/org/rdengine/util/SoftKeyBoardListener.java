package org.rdengine.util;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Build;
import android.view.View;
import android.view.ViewTreeObserver;

import org.rdengine.log.DLOG;

/**
 * 软键盘弹出监听， 原理是监听activity的布局高度改变 <br>
 * Created by CCCMAX on 18/6/15.
 */

public class SoftKeyBoardListener
{
    private View rootView;// activity的根视图
    int rootViewVisibleHeight;// 纪录根视图的显示高度
    int rootViewVisibleWidth;// 纪录根视图的显示宽度

    ViewTreeObserver.OnGlobalLayoutListener mOnGlobalLayoutListener;

    private OnSoftKeyBoardChangeListener onSoftKeyBoardChangeListener;

    public SoftKeyBoardListener(View view)
    {
        // 监听视图树中全局布局发生改变或者视图树中的某个视图的可视状态发生改变
        this.rootView = view;

        mOnGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener()
        {
            @Override
            public void onGlobalLayout()
            {
                // 获取当前根视图在屏幕上显示的大小
                Rect r = new Rect();
                rootView.getWindowVisibleDisplayFrame(r);

                int visibleHeight = r.height();
                int visibleWidth = r.width();
                // DLOG.d("SoftKeyBoardListener", "GlobalLayout height=" + visibleHeight);

                if (rootViewVisibleHeight == 0)
                {
                    rootViewVisibleHeight = visibleHeight;
                    rootViewVisibleWidth = visibleWidth;
                    return;
                }

                // 宽度改变 应该是切换了横竖屏幕 不处理
                if (rootViewVisibleWidth != visibleWidth)
                {
                    return;
                }

                // 根视图显示高度没有变化，可以看作软键盘显示／隐藏状态没有改变
                if (rootViewVisibleHeight == visibleHeight)
                {
                    return;
                }

                // 根视图显示高度变小超过200，可以看作软键盘显示了
                if (rootViewVisibleHeight - visibleHeight > 200)
                {
                    if (onSoftKeyBoardChangeListener != null)
                    {
                        onSoftKeyBoardChangeListener.keyBoardShow(rootViewVisibleHeight - visibleHeight);
                    }
                    rootViewVisibleHeight = visibleHeight;
                    return;
                }

                // 根视图显示高度变大超过200，可以看作软键盘隐藏了
                if (visibleHeight - rootViewVisibleHeight > 200)
                {
                    if (onSoftKeyBoardChangeListener != null)
                    {
                        onSoftKeyBoardChangeListener.keyBoardHide(visibleHeight - rootViewVisibleHeight);
                    }
                    rootViewVisibleHeight = visibleHeight;
                    return;
                }

            }
        };
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(mOnGlobalLayoutListener);
    }

    private void setOnSoftKeyBoardChangeListener(OnSoftKeyBoardChangeListener onSoftKeyBoardChangeListener)
    {
        this.onSoftKeyBoardChangeListener = onSoftKeyBoardChangeListener;
    }

    /**
     * 解除view上的监听绑定，销毁对象强引用
     */
    public void release()
    {
        try
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            {
                rootView.getViewTreeObserver().removeOnGlobalLayoutListener(mOnGlobalLayoutListener);
            } else
            {
                rootView.getViewTreeObserver().removeGlobalOnLayoutListener(mOnGlobalLayoutListener);
            }
            mOnGlobalLayoutListener = null;
            onSoftKeyBoardChangeListener = null;
            rootView = null;
            DLOG.d("SoftKeyBoardListener", "release + " + this.hashCode());
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public interface OnSoftKeyBoardChangeListener
    {
        void keyBoardShow(int height);

        void keyBoardHide(int height);
    }

    /**
     * 监听activity的rootview <br>
     * 用完应该调用SoftKeyBoardListener.release() 进行释放
     */
    public static SoftKeyBoardListener setListener(Activity activity,
                                                   OnSoftKeyBoardChangeListener onSoftKeyBoardChangeListener)
    {
        // 获取activity的根视图
        View rootView = activity.getWindow().getDecorView();
        SoftKeyBoardListener softKeyBoardListener = new SoftKeyBoardListener(rootView);
        softKeyBoardListener.setOnSoftKeyBoardChangeListener(onSoftKeyBoardChangeListener);
        return softKeyBoardListener;
    }

    /**
     * 监听具体view <br>
     * 用完应该调用SoftKeyBoardListener.release() 进行释放
     */
    public static SoftKeyBoardListener setListener(View rootView,
                                                   OnSoftKeyBoardChangeListener onSoftKeyBoardChangeListener)
    {
        SoftKeyBoardListener softKeyBoardListener = new SoftKeyBoardListener(rootView);
        softKeyBoardListener.setOnSoftKeyBoardChangeListener(onSoftKeyBoardChangeListener);
        return softKeyBoardListener;
    }
}
