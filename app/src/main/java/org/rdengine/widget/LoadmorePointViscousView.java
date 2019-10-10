package org.rdengine.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;

import org.rdengine.util.PhoneUtil;

import java.util.ArrayList;

/**
 * Created by CCCMAX on 17/8/25.
 */

public class LoadmorePointViscousView extends View
{

    // 圆点半径
    int point_radius = 7;// 直径7 dp
    // 圆点颜色
    int point_color = 0xFFd8d8d8;
    // 圆点数量
    int point_count = 3;
    // 圆点间隔
    int point_space = 8;// dp

    // 运动圆点半径
    int viscous_radius = 8;// 直径8dp
    // 粘性部分颜色
    int viscous_color = 0xFF999999;

    // 运动方向 true：左向右， false 右向左
    boolean action_LeftToRight = false;

    Paint paint_point, paint_viscous;

    public LoadmorePointViscousView(Context context)
    {
        super(context);
        init();
    }

    public LoadmorePointViscousView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    public LoadmorePointViscousView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init();
    }

    /** 静态点对象 */
    ArrayList<RoundPoint> spList;
    RoundPoint runPoint;
    /** 运动点 起始位置、结束位置 */
    RoundPoint p_from, p_to;
    float run_progress = 0;
    float run_step = 0.025F;

    private void init()
    {
        point_radius = PhoneUtil.dipToPixel(point_radius / 2.0F, getContext());
        point_space = PhoneUtil.dipToPixel(point_space, getContext());

        viscous_radius = PhoneUtil.dipToPixel(viscous_radius / 2.0F, getContext());

        paint_point = new Paint();
        paint_point.setColor(point_color); // 设置圆环的颜色
        paint_point.setStyle(Paint.Style.FILL); // 设置实心
        paint_point.setAntiAlias(true); // 消除锯齿

        paint_viscous = new Paint();
        paint_viscous.setColor(viscous_color); // 设置圆环的颜色
        paint_viscous.setStyle(Paint.Style.FILL); // 设置实心
        paint_viscous.setAntiAlias(true); // 消除锯齿

        // 构造背景点
        spList = new ArrayList<RoundPoint>();
        for (int i = 0; i < point_count; i++)
        {
            RoundPoint sp = new RoundPoint();
            sp.setSize(point_radius * 2, point_radius * 2);
            spList.add(sp);
        }

        // 构造运动点
        runPoint = new RoundPoint();
        runPoint.setSize(viscous_radius * 2, viscous_radius * 2);
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

            // 总长 = 直径*点数 + 间隔*(点数-1)
            int ww = point_radius * 2 * point_count + point_space * (point_count - 1);
            // 居中的起始位置 (左)
            int left_start = (w - ww) / 2;
            // 居中的起始位置（上）
            int top_start = h / 2 - point_radius;

            // 静态点位置
            for (int i = 0; i < spList.size(); i++)
            {
                RoundPoint sp = spList.get(i);
                // x= 左侧起始位置 + 圆点宽度*位置 + 间隔*位置
                int x = left_start + sp.w * i + point_space * i;
                int y = top_start;
                sp.setPosition(x, y);
            }

            // 运动点位置（起始） 至少两个点 才能开始移动啊

            if (spList.size() >= 2)
            {
                run_progress = 0;
                if (isInEditMode())
                {
                    try
                    {
                        float ppp = Float.valueOf((String) getTag());
                        run_progress = ppp;
                    } catch (Exception ex)
                    {
                    }
                }
                if (action_LeftToRight)
                {
                    p_from = spList.get(0);
                    p_to = spList.get(1);

                } else
                {
                    p_from = spList.get(spList.size() - 1);
                    p_to = spList.get(spList.size() - 2);
                }
                Point point_f = p_from.getCenter();
                Point point_t = p_to.getCenter();
                int rp_x = (int) (point_f.x - (point_f.x - point_t.x) * run_progress);
                runPoint.setCenternPosition(rp_x, point_f.y);
            }

        }
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        // 绘制基础点
        for (RoundPoint sp : spList)
        {
            sp.draw(canvas, paint_point);
        }

        if (spList == null || spList.size() < 2)
            return;

        if (isInEditMode())
            paint_viscous.setColor(0x33ff0000);
        // 绘制运动点
        runPoint.draw(canvas, paint_viscous);

        Point point_r = runPoint.getCenter();// 移动点中心
        Point point_f = p_from.getCenter();// 起始点中心
        Point point_t = p_to.getCenter();// 结束点中心

        int index_from = spList.indexOf(p_from);
        int index_to = spList.indexOf(p_to);
        // 当前运动方向 true:从右向左; false:从左向右
        boolean direction = index_from > index_to;

        // 计算运动点后的粘性部分数据 并 绘制 ---------------------
        // 圆心距离
        int center_distance = Math.abs(point_r.x - point_f.x);
        // 拖拽结束的距离
        int distance_a = point_radius + viscous_radius;
        if (center_distance <= distance_a)
        {
            // 圆心距离 < 拖拽结束的距离 , 绘制拖拽

            RoundPoint smallPoint = new RoundPoint();
            smallPoint.setSize(point_radius * 2, point_radius * 2);

            // 判断收尾的距离
            int shrink = (int) (distance_a * 0.5F);// viscous_radius;

            // 移动距离== 大圆半径时 刚好大圆外边在小圆圆心上
            if (center_distance <= shrink)
            {
                // 小于移动园半径 拖拽尾部小圆不移动
                smallPoint.setCenternPosition(point_f.x, point_f.y);
            } else
            {
                // 大于移动园半径 尾部小圆向runPoint移动
                float pp = (center_distance - shrink) * 1.0F / (distance_a - shrink);// 收缩位移进度 1 完全收缩
                int x = (int) (point_r.x + (direction ? 1 : -1) * (1 - pp) * (distance_a - shrink));
                smallPoint.setCenternPosition(x, point_f.y);
            }
            if (isInEditMode())
                paint_viscous.setColor(0x3300ff00);
            smallPoint.draw(canvas, paint_viscous);// 绘制尾部粘性圆点

            // 补全两圆的上下四点区域，用四边形填充，如果拖拽远的话 正常应该用贝塞尔曲线
            Path path = new Path();
            path.moveTo(runPoint.x + viscous_radius, runPoint.y);// runPoint 上部
            path.lineTo(runPoint.x + viscous_radius, runPoint.y + runPoint.h);// runPoint 底部
            path.lineTo(smallPoint.x + point_radius, smallPoint.y + smallPoint.h);// p_from 底部
            path.lineTo(smallPoint.x + point_radius, smallPoint.y);// p_from 上部
            path.close();

            if (isInEditMode())
                paint_viscous.setColor(0x330000ff);
            canvas.drawPath(path, paint_viscous);
        }

        if (isInEditMode())
            return;

        // 计算运动点位移 ---------------------------------------

        boolean pause = false;

        // 位移步进
        run_progress += run_step;
        if (run_progress >= 1)
        {
            // 当前一段执行完毕，计算下一段路程目标
            // 重置 p_from、p_to、run_progress
            run_progress = 0;
            p_from = p_to;// 下一段的起始点 是当前的结束点
            if (direction)
            {
                pause = index_to == 0;

                // 当前 从右向左
                int index_to_next = index_to == 0 ? index_to + 1 : index_to - 1;
                p_to = spList.get(index_to_next);
            } else
            {
                pause = index_to == spList.size() - 1;

                // 当前 从左向右
                int index_to_next = index_to == spList.size() - 1 ? index_to - 1 : index_to + 1;
                p_to = spList.get(index_to_next);
            }
        }
        point_f = p_from.getCenter();// 起始点中心
        point_t = p_to.getCenter();// 结束点中心
        int rp_y = point_r.y;
        int rp_x = (int) (point_f.x - (point_f.x - point_t.x) * run_progress);
        runPoint.setCenternPosition(rp_x, rp_y);

        if (pause)
        {
            postInvalidateDelayed(200);
        } else
        {
            invalidate();
        }

    }

    /** 背景静态点 */
    private class RoundPoint
    {
        /** 左上角坐标 */
        int x, y;
        /** 宽高 */
        int w, h;

        public void setPosition(int x, int y)
        {
            this.x = x;
            this.y = y;
        }

        /** 设置中心位置 */
        public void setCenternPosition(int x, int y)
        {
            this.x = x - w / 2;
            this.y = y - h / 2;
        }

        public void setSize(int w, int h)
        {
            this.w = h;
            this.h = h;
        }

        public Point getCenter()
        {
            Point p = new Point(x + w / 2, y + h / 2);
            return p;
        }

        public void draw(Canvas canvas, Paint paint)
        {
            try
            {
                Point p = getCenter();
                canvas.drawCircle(p.x, p.y, w / 2, paint);
            } catch (Exception ex)
            {
            }
        }

    }

}
