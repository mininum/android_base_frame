package org.rdengine.widget;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ScrollView;

import org.rdengine.log.DLOG;
import org.rdengine.util.PhoneUtil;

/**
 * 有回弹的scrollview，并且可以添加上拉下拉事件，可以用于上拉下拉翻页
 */
public class BounceScrollView extends ScrollView
{
    public static final String TAG = "BounceScrollView";

    private View inner;// 孩子View

    private float y;// 点击时y坐标

    private Rect normal = new Rect();// 矩形(这里只是个形式，只是用于判断是否需要动画.)

    private boolean isCount = false;// 是否开始计算

    private boolean touchLock = false;

    private int frist_orientation = 0;

    // 拖拽偏移量 负数是向下移动 正数是向上移动
    int drag_offset = 0;

    private float lastX = 0;
    private float lastY = 0;
    private float currentX = 0;
    private float currentY = 0;
    private float distanceX = 0;
    private float distanceY = 0;
    private boolean upDownSlide = false; // 判断手势上下滑动方向的flag

    /** 滑动回弹 是否可用 */
    private boolean bounceScrollOperable = true;

    VelocityTracker mVelocityTracker;

    public BounceScrollView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        mVelocityTracker = VelocityTracker.obtain();
    }

    @Override
    protected void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();
        try
        {
            if (mVelocityTracker != null)
                mVelocityTracker.recycle();
        } catch (Exception ex)
        {
        }
    }

    public void setBounceScrollOperable(boolean isAble)
    {
        bounceScrollOperable = isAble;
    }

    /***
     * 根据 XML 生成视图工作完成.该函数在生成视图的最后调用，<br>
     * 在所有子视图添加完之后. 即使子类覆盖了 onFinishInflate 方法，<br>
     * 也应该调用父类的方法，使该方法得以执行.
     */
    @Override
    protected void onFinishInflate()
    {
        if (getChildCount() > 0)
        {
            inner = getChildAt(0);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev)
    {
        // TODO Auto-generated method stub

        // 关闭边界拖拽回弹功能
        if (!bounceScrollOperable)
            return super.dispatchTouchEvent(ev);

        mVelocityTracker.addMovement(ev);

        currentX = ev.getX();
        currentY = ev.getY();
        switch (ev.getAction())
        {
        case MotionEvent.ACTION_DOWN :

            break;
        case MotionEvent.ACTION_MOVE :
            distanceX = currentX - lastX;
            distanceY = currentY - lastY;
            if (Math.abs(distanceX) < Math.abs(distanceY) && Math.abs(distanceY) > 12)
            {

                upDownSlide = true;
            }
            break;
        case MotionEvent.ACTION_UP :
            isCount = false;
            touchLock = false;
            // frist_orientation = 0;

            break;
        default:
            break;
        }
        lastX = currentX;
        lastY = currentY;
        if (upDownSlide && inner != null)
            commOnTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev)
    {
        // 拖拽过程中 事件只分发给自己，避免 和子view的onclick冲突
        if (touchLock)
        {
            return true;
        } else
        {
            return super.onInterceptTouchEvent(ev);
        }
    }

    /***
     * 监听touch
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev)
    {
        // if (inner != null)
        // {
        // commOnTouchEvent(ev);
        // if (isCount) // 这里可能会影响scroll正常滑动
        // return true;
        // }

        if (touchLock)
        {
            return true;
        } else
        {
            return super.onTouchEvent(ev);
        }

    }

    /***
     * 触摸事件
     * 
     * @param ev
     */
    public void commOnTouchEvent(MotionEvent ev)
    {
        int action = ev.getAction();
        switch (action)
        {
        case MotionEvent.ACTION_DOWN :
            // DLOG.d(TAG, "ACTION_DOWN");
            break;
        case MotionEvent.ACTION_UP :
            // DLOG.d(TAG, "ACTION_UP");
            // 手指松开.
            if (isNeedAnimation())
            {
                try
                {
                    mVelocityTracker.computeCurrentVelocity(1000); // 1000毫秒内移动像素，计算出的像素速度
                    float velocity = mVelocityTracker.getYVelocity();

                    float velocityDip = (int) PhoneUtil.pixelToDip((int) velocity, getContext());// 转 dp速度
                    // DLOG.d("cccmax", "velocity=" + velocity +" dp="+velocityDip);

                    // 拖拽松开事件
                    if (mScrollDragListener != null)
                        mScrollDragListener.onDragUp(frist_orientation, velocityDip);
                    else defScrollDragListener.onDragUp(frist_orientation, velocityDip);
                } catch (Exception ex)
                {
                }

                animation();
                isCount = false;
            }
            touchLock = false;
            frist_orientation = 0;
            break;
        /***
         * 排除出第一次移动计算，因为第一次无法得知y坐标， 在MotionEvent.ACTION_DOWN中获取不到，<br>
         * 因为此时是MyScrollView的touch事件传递到到了LIstView的孩子item上面.所以从第二次计算开始.<br>
         * 然而我们也要进行初始化，就是第一次移动的时候让滑动距离归0. 之后记录准确了就正常执行.
         */
        case MotionEvent.ACTION_MOVE :
        {
            // DLOG.d(TAG, "ACTION_MOVE");
            final float preY = y;// 按下时的y坐标
            float nowY = ev.getY();// 时时y坐标
            int deltaY = (int) (preY - nowY);// 滑动距离
            if (!isCount)
            {
                deltaY = 0; // 在这里要归0.
            }

            y = nowY;
            // 当滚动到最上或者最下时就不会再滚动，这时移动布局

            // 初始方向 -1向下 ， 1向上
            if (frist_orientation == 0)
            {
                if (deltaY > 0)
                    frist_orientation = 1;
                else if (deltaY < 0)
                    frist_orientation = -1;
            }

            // -1顶部，1底部，0非边界
            int edge = isEdge();
            // 子view处于边界 ， 有初始滑动方向 ， 边界和滑动方向匹配
            boolean needMove = edge != 0 && frist_orientation != 0 && edge * frist_orientation > 0;

            // 子view和容器一样大，上下边界条件都满足
            if (edge == -2 && frist_orientation != 0)
                needMove = true;

            DLOG.d(TAG, "needMove " + needMove + "  frist_orientation＝" + frist_orientation);
            if (needMove)
            {
                if (!touchLock)
                    touchLock = true;

                // 初始化头部矩形
                if (normal.isEmpty())
                {
                    // 保存正常的布局位置
                    normal.set(inner.getLeft(), inner.getTop(), inner.getRight(), inner.getBottom());
                }
                // Log.e("jj", "矩形：" + inner.getLeft() + "," + inner.getTop()
                // + "," + inner.getRight() + "," + inner.getBottom());

                if (deltaY != 0)
                {
                    // 拖拽偏移量 负数是向下移动 正数是向上移动, 拖拽方向/2 增加阻力
                    int temp_drag_offset = (deltaY * frist_orientation < 0) ? deltaY : deltaY / 2;

                    drag_offset += temp_drag_offset;

                    if (mScrollDragListener != null)
                        mScrollDragListener.onDrag(frist_orientation, drag_offset, normal);
                    else defScrollDragListener.onDrag(frist_orientation, drag_offset, normal);
                }

            }
            isCount = true;
        }
            break;

        default:
            break;
        }
    }

    /***
     * 回缩动画
     */
    public void animation()
    {
        // // 开启移动动画
        // TranslateAnimation ta = new TranslateAnimation(0, 0, inner.getTop(), normal.top);
        // ta.setDuration(200);
        // inner.startAnimation(ta);
        // // 设置回到正常的布局位置
        // inner.layout(normal.left, normal.top, normal.right, normal.bottom);
        //
        // // Log.e("jj", "回归：" + normal.left + "," + normal.top + "," + normal.right
        // // + "," + normal.bottom);

        final Rect _normal = new Rect(normal);
        ValueAnimator animator = ValueAnimator.ofInt(drag_offset, 0).setDuration((long) (200));
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new AnimatorUpdateListener()
        {
            public void onAnimationUpdate(ValueAnimator animation)
            {
                drag_offset = (Integer) animation.getAnimatedValue();

                if (mScrollDragListener != null)
                    mScrollDragListener.onDrag(frist_orientation, drag_offset, _normal);
                else defScrollDragListener.onDrag(frist_orientation, drag_offset, _normal);
            }
        });
        animator.start();

        normal.setEmpty();
    }

    // 是否需要开启动画
    public boolean isNeedAnimation()
    {
        return !normal.isEmpty();
    }

    /***
     * 是否需要移动布局 inner.getMeasuredHeight():获取的是控件的总高度 getHeight()：获取的是屏幕的高度<br>
     * 判断是否在边界，顶部或者底部<br>
     *
     * @return -1顶部边界，1底部边界，0不在边界，-2子控件和容器一样大,上下都是边界,或者小于容器高度
     */
    public int isEdge()
    {
        // 子控件与容器的高度差
        int offset = inner.getMeasuredHeight() - getHeight();
        // 当前滑动的位置
        int scrollY = getScrollY();

        if (offset < 0 || (offset == 0 && scrollY == 0))// offset小于0说明内容没有撑满容器
        {
            return -2;
        }

        // 0是顶部，后面那个是底部
        if (scrollY == 0)
        {
            DLOG.d(TAG, "isEdge true , 在顶部 scrolly=" + scrollY + ", offset=" + offset);
            return -1;
        } else if (scrollY == offset)
        {
            DLOG.d(TAG, "isEdge true , 在底部 scrolly=" + scrollY + ", offset=" + offset);
            return 1;
        }
        DLOG.d(TAG, "isEdge false , scrolly=" + scrollY + ", offset=" + offset);
        return 0;
    }

    ScrollDragListener mScrollDragListener = null;

    public void setScrollDragListener(ScrollDragListener listener)
    {
        mScrollDragListener = listener;
    }

    public interface ScrollDragListener
    {
        /**
         * @param orientation
         *            初始方向, -1 下拉 ， 1 上拉
         * @param drag_offset
         *            总偏移量 负数向下 正数向上
         * @param normal
         *            拖拽前ScrollView的位置rect
         */
        void onDrag(int orientation, int drag_offset, Rect normal);

        /**
         * 拖拽松手
         */
        void onDragUp(int orientation, float velocityDp);
    }

    /**
     * 默认拖拽事件
     */
    ScrollDragListener defScrollDragListener = new ScrollDragListener()
    {
        public void onDrag(int orientation, int drag_offset, Rect normal)
        {
            // 移动布局
            inner.layout(inner.getLeft(), normal.top - drag_offset, inner.getRight(), normal.bottom - drag_offset);
        }

        @Override
        public void onDragUp(int orientation, float velocity)
        {

        }

    };

}