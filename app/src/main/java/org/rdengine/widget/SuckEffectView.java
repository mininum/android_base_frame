package org.rdengine.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;


import com.android.frame.R;

import java.util.ArrayList;

/**
 * 图片吸入变形动画控件,暂只支持向下吸入
 * 
 * @author CCCMAX
 */
public class SuckEffectView extends View
{

    public SuckEffectView(Context context)
    {
        super(context);
    }

    public SuckEffectView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    /** 控件自身宽度 */
    int wight = 0;
    /** 控件自身高度 */
    int height = 0;
    /** 需要展示变形特效的view */
    Bitmap bitmap = null;
    /** 进度 从0到MeshPath.all_frame_count(整体帧数) */
    int progress = 0;
    /** 动画是否执行中 */
    private boolean isExecuting = false;

    /** 吸入变形动画计算对象 */
    public MeshPath mp;

    public boolean isExecuting()
    {
        return isExecuting;
    }

    @Override
    protected void onFinishInflate()
    {
        super.onFinishInflate();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom)
    {
        super.onLayout(changed, left, top, right, bottom);
        wight = right - left;
        height = bottom - top;

        if (isInEditMode())
        {
            if (bitmap == null)
            {
                bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.ic_launcher);
            }
            if (mp == null && wight > 0 && height > 0)
            {
                buildMeshPathDebug();
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * 构造吸入动画
     * 
     * @param bitmap
     * @param imgRect
     * @param g
     *            暂只支持MP_Gravity.Bottom
     * @param endline_1
     * @param endline_2
     */
    public void buildMeshPath(Bitmap bitmap, Rect imgRect, MP_Gravity g, Point endline_1, Point endline_2)
    {
        this.bitmap = bitmap;
        int img_w = imgRect.right - imgRect.left;
        int img_h = imgRect.bottom - imgRect.top;
        float scale = getContext().getResources().getDisplayMetrics().density;
        int wight_dp = (int) (wight / scale);
        float e = wight_dp / 10.0F;// e是多少个dp一个格子
        int s_w = (int) (img_w / e);// 图片有多少个格子
        int s_h = (int) (img_h / e);

        if (endline_1.y < imgRect.bottom)
            endline_1.y = imgRect.bottom;
        if (endline_2.y < imgRect.bottom)
            endline_2.y = imgRect.bottom;

        mp = new MeshPath(getContext(), imgRect);
        mp.setEndLine(g, endline_1, endline_2);// End line
        mp.setImgMeshSize(s_w, s_h);
        mp.build();
        Log.i("cccmax", "s_w=" + s_w + " s_h=" + s_h);
    }

    private void buildMeshPathDebug()
    {
        int img_w = bitmap.getWidth();
        int img_h = bitmap.getHeight();
        img_w *= 1F;
        img_h *= 1F;

        Rect imgRect = null;
        Point el_1 = null, el_2 = null;

        int imgRect_pos = 3;// 图片位置
        MP_Gravity el_direction = MP_Gravity.Bottom;// 1234上下左右某条边
        float el_pos = 0.3F;// 某条边上的比例位置
        int el_size = 80;// 长度

        switch (imgRect_pos)
        {
        case 5 :// 居中
            imgRect = new Rect((wight - img_w) / 2, (height - img_h) / 2, img_w + (wight - img_w) / 2,
                    img_h + (height - img_h) / 2);
            break;
        case 1 :// 左上
            imgRect = new Rect(0, 0, img_w, img_h);
            break;
        case 2 :// 中上
            imgRect = new Rect((wight - img_w) / 2, 0, img_w + (wight - img_w) / 2, img_h);
            break;
        case 3 :// 右上
            imgRect = new Rect(wight - img_w, 0, wight, img_h);
            break;
        case 4 :// 左中
            imgRect = new Rect(0, (height - img_h) / 2, img_w, img_h + (height - img_h) / 2);
            break;
        case 6 : // 右中
            imgRect = new Rect(wight - img_w, (height - img_h) / 2, wight, img_h + (height - img_h) / 2);
            break;
        case 7 :// 左下
            imgRect = new Rect(0, (height - img_h), img_w, height);
            break;
        case 8 :// 中下
            imgRect = new Rect((wight - img_w) / 2, img_w + (wight - img_w) / 2, img_w, height);
            break;
        case 9 :// 右下
            imgRect = new Rect(wight - img_w, (height - img_h), wight, height);
            break;
        }

        if (el_direction == MP_Gravity.Top)
        {// 上
            el_1 = new Point((int) (wight * el_pos), 0);
            el_2 = new Point((int) (wight * el_pos) + el_size, 0);
        } else if (el_direction == MP_Gravity.Bottom)
        {
            // 下
            el_1 = new Point((int) (wight * el_pos), height);
            el_2 = new Point((int) (wight * el_pos) + el_size, height);
        } else if (el_direction == MP_Gravity.Left)
        {
            // 左
            el_1 = new Point(0, (int) (height * el_pos));
            el_2 = new Point(0, (int) (height * el_pos));

        } else if (el_direction == MP_Gravity.Right)
        {
            // 右
            el_1 = new Point((int) (wight * el_pos), (int) (height * el_pos));
            el_2 = new Point((int) (wight * el_pos) + el_size, (int) (height * el_pos));
        }

        float scale = getContext().getResources().getDisplayMetrics().density;
        int wight_dp = (int) (wight / scale);
        float e = wight_dp / 10.0F;// e是多少个dp一个格子
        int s_w = (int) (img_w / e);// 图片有多少个格子
        int s_h = (int) (img_h / e);
        Log.i("cccmax", "s_w=" + s_w + " s_h=" + s_h);

        mp = new MeshPath(getContext(), imgRect);
        mp.setEndLine(el_direction, el_1, el_2);// End line
        mp.setImgMeshSize(s_w, s_h);
        mp.build();
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        if (mp == null)
            return;

        long a = System.currentTimeMillis();
        try
        {
            mp.draw(canvas, progress);
            // DEBUG------
            if (isInEditMode())
                drawDebugInfo(canvas, mp);
        } catch (Throwable e)
        {
            e.printStackTrace();
        }
        long b = System.currentTimeMillis();
        // Log.i("cccmax", "onDraw time=" + (b - a));
    }

    /**
     * 绘制debug辅助内容 坐标、参考线、贝塞尔曲线、图形矩阵
     * 
     * @param canvas
     * @param mp
     */
    private void drawDebugInfo(Canvas canvas, MeshPath mp)
    {
        // 屏幕尺寸
        Paint paint_img = new Paint();
        Paint paint_text = new Paint();
        paint_text.setColor(0xff000000);
        paint_text.setStrokeWidth(10);
        canvas.drawText("w=" + wight + "  h=" + height, 0, 20, paint_text);

        // 结束位置
        paint_img.setColor(0xff0000ff);
        paint_img.setStrokeWidth(5);
        canvas.drawLine(mp.endline_p1.x, mp.endline_p1.y, mp.endline_p2.x, mp.endline_p2.y, paint_img);

        // 贝塞尔曲线
        paint_img.setStyle(Style.STROKE);
        paint_img.setColor(0x9900ff00);
        canvas.drawPath(mp.bessel_1, paint_img);
        canvas.drawPath(mp.bessel_2, paint_img);

        // 控制点
        if (mp.ctrlPoints != null)
        {
            for (PointF p : mp.ctrlPoints)
            {
                if (p != null)
                    drawPointXYDebug(p.x, p.y, canvas);
            }
        }

        // 图形矩阵 绘制点
        paint_img.setStyle(Style.FILL_AND_STROKE);
        paint_img.setColor(0x99ff0000);
        for (int i = 0; i < mp.verts_path.length; i += 2)
        {
            float x = mp.verts_path[i];
            float y = mp.verts_path[i + 1];
            canvas.drawOval(new RectF(x - 1, y - 1, x + 1, y + 1), paint_img);
        }

        if (isInEditMode())
            return;

        // 贝塞尔曲线中抽取的点
        paint_img.setColor(0x990000ff);
        for (int i = 0; i < mp.pointarray_path1.length; i++)
        {
            float x = mp.pointarray_path1[i].x;
            float y = mp.pointarray_path1[i].y;
            canvas.drawOval(new RectF(x - 1, y - 1, x + 1, y + 1), paint_img);
        }
        for (int i = 0; i < mp.pointarray_path2.length; i++)
        {
            float x = mp.pointarray_path2[i].x;
            float y = mp.pointarray_path2[i].y;
            canvas.drawOval(new RectF(x - 1, y - 1, x + 1, y + 1), paint_img);
        }
    }

    /**
     * 绘制一个点 带xy坐标参、考线
     * 
     * @param x
     * @param y
     * @param canvas
     */
    private void drawPointXYDebug(float x, float y, Canvas canvas)
    {
        Paint p = new Paint();
        p.setStrokeWidth(2);
        p.setColor(0x99000000);
        canvas.drawLine(0, y, canvas.getWidth(), y, p);
        canvas.drawLine(x, 0, x, canvas.getHeight(), p);
        canvas.drawText("" + x + "," + y, x, y, p);
    }

    public static enum MP_Gravity
    {
        Left, Top, Right, Bottom
    }

    /**
     * 吸入变形动画计算对象
     * 
     * @author CCCMAX
     */
    class MeshPath
    {
        Context mContext;
        float screen_scale = 2.0F;

        public MP_Gravity gravity = MP_Gravity.Bottom;

        // 图片宽高尺寸
        int img_w = 0;
        int img_h = 0;

        /** 图片原始位置矩形 */
        Rect img_Rect;

        // 网格宽高格数
        int img_mesh_w = 40;
        int img_mesh_h = 40;

        /** 原始图片网格矩阵点数 */
        int img_mesh_count;

        // 单独一个网格的高度
        float oneMeshSize = 0;

        // 原始图片矩阵点坐标
        float[] verts_img = null;
        // 动画矩阵点坐标
        float[] verts_path = null;

        /** 结束位置点 1 */
        Point endline_p1;
        /** 结束位置点 2 */
        Point endline_p2;

        /** 边界贝塞尔曲线 1 */
        Path bessel_1;
        /** 边界贝塞尔曲线 1 */
        Path bessel_2;

        /** 移动方向上 网格总数 点数需要+1 */
        int move_path_mesh_count = 0;

        /** 贝塞尔曲线1 抽取的点坐标序列 */
        PointF[] pointarray_path1;
        /** 贝塞尔曲线2 抽取的点坐标序列 */
        PointF[] pointarray_path2;

        /** 原始图片矩阵 到 动画矩阵 之间的补间动画(矩阵序列) */
        ArrayList<float[]> matrixVaryHeader = null;

        /** 贝塞尔曲线用过的控制点(拐点) */
        ArrayList<PointF> ctrlPoints = null;

        /** 矩阵中一行的偏移量 */
        int line_offsize = 0;

        /** 整体动画帧数 */
        int all_frame_count = 0;

        /** 两条贝塞尔曲线的抽样点 是否对齐 */
        boolean pathsAlign = false;

        /**
         * 设置图片在本控件中Rect 上下左右位置
         */
        public MeshPath(Context context, Rect imgRect)
        {
            this.img_w = imgRect.right - imgRect.left;
            this.img_h = imgRect.bottom - imgRect.top;
            img_Rect = imgRect;

            mContext = context;
            screen_scale = mContext.getResources().getDisplayMetrics().density;
        }

        /**
         * 两条贝塞尔曲线的抽样点 是否对齐, 对齐略微耗时40～50毫秒
         * 
         * @param isAlign
         */
        public void setPathsAlign(boolean isAlign)
        {
            pathsAlign = isAlign;
        }

        /**
         * 设置原始图片 宽高对应的网格数
         * 
         * @param mesh_w
         *            图片对应的网格数量 ， 点数＝mesh_w+1
         * @param mesh_h
         */
        public void setImgMeshSize(int mesh_w, int mesh_h)
        {
            this.img_mesh_w = mesh_w;
            this.img_mesh_h = mesh_h;
            img_mesh_count = (mesh_w + 1) * (mesh_h + 1);// 21*21个点
        }

        /**
         * 设置结束点、线 位置
         * 
         * @param gravity
         *            相对方向
         * @param p1
         * @param p2
         */
        public void setEndLine(MP_Gravity gravity, Point p1, Point p2)
        {
            endline_p1 = p1;
            endline_p2 = p2;
            this.gravity = gravity;
        }

        /**
         * 获取曲线路径中的点坐标数组 （矢量path抽样出点坐标）
         * 
         * @param path
         *            曲线路径
         * @param total
         *            获取点的总数
         * @return
         */
        private PointF[] getPoints(Path path, int total)
        {
            PointF[] pointArray = new PointF[total];
            PathMeasure pm = new PathMeasure(path, false);
            float length = pm.getLength();
            float distance = 0f;
            float speed = length / (total - 1);
            int counter = 0;
            float[] aCoordinates = new float[2];
            while ((distance < length) && (counter < total - 1))
            {
                pm.getPosTan(distance, aCoordinates, null);
                pointArray[counter] = new PointF(aCoordinates[0], aCoordinates[1]);
                counter++;
                distance = distance + speed;
            }

            pm.getPosTan(length, aCoordinates, null);
            pointArray[total - 1] = new PointF(aCoordinates[0], aCoordinates[1]);
            return pointArray;
        }

        /**
         * 获取曲线路径中的点坐标数组 ，但是可以在X、Y轴上与指定的路径保持平行，略微耗时 还可以接受30～40毫秒
         * 
         * @param path
         *            需要抽样的路径
         * @param pathsrc
         *            跟随对齐的路径
         * @param src
         *            跟随对齐路径的抽样
         * @param folowX
         *            跟随X轴 或 Y轴
         * @return
         */
        private PointF[] getPointsFollowParallel(Path path, Path pathsrc, PointF[] src, boolean folowX)
        {
            PathMeasure pmsrc = new PathMeasure(pathsrc, false);
            float lengthsrc = pmsrc.getLength();
            float speedsrc = lengthsrc / (src.length - 1);// 要跟随的原路径的步进长度;

            PointF[] pointArray = new PointF[src.length];
            PathMeasure pm = new PathMeasure(path, false);
            float length = pm.getLength();
            float distance = 0f;
            float speed = length / (src.length - 1);
            float[] aCoordinates = new float[2];
            findPointCount = 0;
            long time1 = System.currentTimeMillis();
            for (int i = 0; i < src.length; i++)
            {
                PointF srcPoint = src[i];
                distance = findPoint(pm, srcPoint, distance, aCoordinates, folowX);
                pointArray[i] = new PointF(aCoordinates[0], aCoordinates[1]);
            }
            long time2 = System.currentTimeMillis();
            Log.i("cccmax", "getPointsFollowParallel findPointCount=" + findPointCount + " time=" + (time2 - time1));
            return pointArray;
        }

        int findPointCount = 0;

        /**
         * 递归寻找比较对齐的点
         * 
         * @param pm
         * @param srcPoint
         * @param distance
         * @param aCoordinates
         * @param folowX
         * @return
         */
        private float findPoint(PathMeasure pm, PointF srcPoint, float distance, float[] aCoordinates, boolean folowX)
        {
            findPointCount++;
            pm.getPosTan(distance, aCoordinates, null);
            if (folowX)
            {
                // X轴平行
                float x = aCoordinates[0] - srcPoint.x;
                if (Math.abs(x) > screen_scale)
                {
                    findPoint(pm, srcPoint, distance + screen_scale, aCoordinates, folowX);
                }
            } else
            {
                // Y轴平行
                float y = aCoordinates[1] - srcPoint.y;
                if (Math.abs(y) > screen_scale)
                {
                    findPoint(pm, srcPoint, distance + screen_scale, aCoordinates, folowX);
                }
            }
            return distance;
        }

        /**
         * 生成贝塞尔曲线 曲线边界 运动轨迹的边界
         * 
         * @param start
         *            起始点 必要
         * @param ctrl1
         *            控制点1 必要
         * @param ctrl2
         *            控制点2 可选
         * @param end
         *            结束点 必要
         * @return
         */
        private Path makeBessel(PointF start, PointF ctrl1, PointF ctrl2, PointF end)
        {
            Path path = new Path();
            path.moveTo(start.x, start.y); // 贝赛尔曲线的起始点
            if (ctrl2 == null)
                ctrl2 = ctrl1;// 两控制点相同 会当作一个点
            path.cubicTo(ctrl1.x, ctrl1.y, ctrl2.x, ctrl2.y, end.x, end.y);
            return path;
        }

        /**
         * 计算两点之间直线距离
         * 
         * @param x1
         * @param y1
         * @param x2
         * @param y2
         * @return
         */
        private float getPointsDistance(float x1, float y1, float x2, float y2)
        {
            Log.i("cccmax", "getPointsDistance " + x1 + "," + y1 + "  -- " + x2 + "," + y2);
            float ret = 0;
            double a = Math.pow((x1 - x2), 2);
            double b = Math.pow((y1 - y2), 2);
            ret = (float) Math.abs(Math.sqrt(a + b));
            return ret;
        }

        /**
         * 生成动路径画第一祯与原始图片矩阵的补间矩阵list
         */
        private void makeVatrixVaryHeader()
        {
            // 变形动画的第一祯
            float vvvv[] = new float[line_offsize * (img_mesh_h + 1)];
            System.arraycopy(verts_path, 0, vvvv, 0, vvvv.length);
            int lt_x = 0;// 左上
            int lt_y = 1;
            int rt_x = line_offsize - 2;// 右上
            int rt_y = line_offsize - 1;
            int rb_x = vvvv.length - 2;// 右下
            int rb_y = vvvv.length - 1;
            int lb_x = vvvv.length - line_offsize;// 左下
            int lb_y = vvvv.length - line_offsize + 1;
            float a = getPointsDistance(vvvv[lt_x], vvvv[lt_y], verts_img[lt_x], verts_img[lt_y]);
            float b = getPointsDistance(vvvv[rt_x], vvvv[rt_y], verts_img[rt_x], verts_img[rt_y]);
            float c = getPointsDistance(vvvv[rb_x], vvvv[rb_y], verts_img[rb_x], verts_img[rb_y]);
            float d = getPointsDistance(vvvv[lb_x], vvvv[lb_y], verts_img[lb_x], verts_img[lb_y]);
            float max = Math.max(Math.max(a, b), Math.max(c, d));
            Log.i("cccmax", "makeVatrixVaryHeader a=" + a + " b=" + b + " c=" + c + " d=" + d);
            int meshcount = (int) (max / oneMeshSize) + 1;// 变形距离对应多少网格
            // 做meshcount个补间数组 verts_img-vvvv之间的渐变
            matrixVaryHeader = new ArrayList<float[]>();
            matrixVaryHeader.add(verts_img);// 第0个原图
            for (int i = 1; i < meshcount; i++)
            {
                float scale = 1.0F * i / meshcount;
                float[] vv = new float[verts_img.length];
                for (int m = 0; m < verts_img.length; m++)
                {
                    float element_a = verts_img[m];
                    float element_b = vvvv[m];
                    vv[m] = element_a + (element_b - element_a) * scale;
                }
                matrixVaryHeader.add(vv);
            }
        }

        /**
         * 新添加或者修改任何属性 都需要重新build （一个计算复杂的计算过程）
         */
        public void build()
        {
            line_offsize = (img_mesh_w + 1) * 2;

            ctrlPoints = new ArrayList<PointF>();

            // float x_max = Math.max(Math.max(img_Rect.left, img_Rect.right), Math.max(endline_p1.x, endline_p2.x));
            // float x_min = Math.min(Math.min(img_Rect.left, img_Rect.right), Math.min(endline_p1.x, endline_p2.x));

            // 轨迹左边
            // PointF ctrl1_1 = new PointF(img_Rect.left + (img_Rect.right - img_Rect.left) / 2, img_Rect.bottom);
            // PointF ctrl1_2 = new PointF(x_min + (x_max - x_min) / 2, (endline_p1.y - img_Rect.bottom) / 2
            // + img_Rect.bottom);
            PointF ctrl1_1 = new PointF(endline_p1.x, (endline_p1.y - img_Rect.top) / 2 + img_Rect.top);
            PointF ctrl1_2 = null;
            bessel_1 = makeBessel(new PointF(img_Rect.left, img_Rect.top), ctrl1_1, ctrl1_2,
                    new PointF(endline_p1.x, endline_p1.y));

            // 轨迹右边
            // PointF ctrl2_1 = new PointF(img_Rect.left + (img_Rect.right - img_Rect.left) / 2, img_Rect.bottom);
            // PointF ctrl2_2 = new PointF(x_min + (x_max - x_min) / 2, (endline_p1.y - img_Rect.bottom) / 2
            // + img_Rect.bottom);
            PointF ctrl2_1 = new PointF(endline_p2.x, (endline_p2.y - img_Rect.top) / 2 + img_Rect.top);
            PointF ctrl2_2 = null;
            bessel_2 = makeBessel(new PointF(img_Rect.right, img_Rect.top), ctrl2_1, ctrl2_2,
                    new PointF(endline_p2.x, endline_p2.y));

            ctrlPoints.add(ctrl1_1);
            ctrlPoints.add(ctrl1_2);
            ctrlPoints.add(ctrl2_1);
            ctrlPoints.add(ctrl2_2);

            // 原始图片矩阵
            verts_img = new float[img_mesh_count * 2];// xy两个坐标所以*2
            int index = 0;
            for (int y = 0; y <= img_mesh_h; y++)
            {
                float fy = img_h * y / img_mesh_h + img_Rect.top;// 增加原始图片位置的偏移量
                for (int x = 0; x <= img_mesh_w; x++)
                {
                    float fx = img_w * x / img_mesh_w + img_Rect.left;
                    verts_img[index * 2 + 0] = fx;
                    verts_img[index * 2 + 1] = fy;
                    index += 1;
                }
            }

            // 单独一个网格的高度
            oneMeshSize = img_h / img_mesh_h;
            // 图片底部距离endline的长度
            float imgBottom_endline = endline_p1.y - img_Rect.bottom;
            // 图片底部 到endline 需要补充多少行网格
            int imgBottom_endline_mesh_count = (int) (imgBottom_endline / oneMeshSize);
            // 整体路径运动方向的网格总数
            move_path_mesh_count = img_mesh_h + imgBottom_endline_mesh_count;
            // 从图片原始位置到结束位置的网格矩阵
            verts_path = new float[(img_mesh_w + 1) * (move_path_mesh_count + 1) * 2];

            if (isInEditMode())
                return;

            pointarray_path1 = getPoints(bessel_1, move_path_mesh_count + 1);
            if (pathsAlign)
            {
                pointarray_path2 = getPointsFollowParallel(bessel_2, bessel_1, pointarray_path1, false);
            } else
            {
                pointarray_path2 = getPoints(bessel_2, move_path_mesh_count + 1);
            }

            // 填充verts_path矩阵
            index = 0;
            for (int y = 0; y <= move_path_mesh_count; y++)
            {
                float y_top = pointarray_path1[y].y;
                float y_bottom = pointarray_path2[y].y;
                float y_offsize = 1.0F * (y_bottom - y_top) / (img_mesh_w);
                float x_left = pointarray_path1[y].x;
                float x_right = pointarray_path2[y].x;
                float x_offsize = 1.0F * (x_right - x_left) / (img_mesh_w);
                for (int x = 0; x <= img_mesh_w; x++)
                {
                    float fx = x_left + x_offsize * x;
                    float fy = y_top + y_offsize * x;
                    verts_path[index * 2 + 0] = fx;
                    verts_path[index * 2 + 1] = fy;
                    index += 1;
                }
            }
            makeVatrixVaryHeader();
            all_frame_count = matrixVaryHeader.size() + move_path_mesh_count;

            Log.i("cccmax", "framecount=" + all_frame_count + " headersize=" + matrixVaryHeader.size());
        }

        /**
         * 逐帧绘制
         * 
         * @param canvas
         * @param progress
         *            画哪一帧
         */
        public void draw(Canvas canvas, int progress)
        {
            Paint p = new Paint();
            p.setAntiAlias(true);
            progress = Math.max(progress, 0);
            progress = Math.min(progress, all_frame_count);

            if (matrixVaryHeader != null && matrixVaryHeader.size() > 0)
            {
                if (progress < matrixVaryHeader.size())
                {
                    // 绘制原始图片矩阵到路径动画第一帧矩阵之间的补间动画
                    canvas.drawBitmapMesh(bitmap, img_mesh_w, img_mesh_h, matrixVaryHeader.get(progress), 0, null, 0,
                            p);
                    return;
                } else
                {
                    progress = progress - matrixVaryHeader.size();
                    progress = Math.max(progress, 0);
                    progress = Math.min(progress, all_frame_count);
                }
            }

            int www = img_mesh_w;
            int hhh = img_mesh_h;
            int offsize = progress;
            float vvvv[] = new float[line_offsize * (hhh + 1)];
            if (offsize <= move_path_mesh_count - hhh)
            {
                System.arraycopy(verts_path, line_offsize * offsize, vvvv, 0, vvvv.length);
            } else
            {
                System.arraycopy(verts_path, line_offsize * offsize, vvvv, 0,
                        verts_path.length - line_offsize * offsize);
                for (int i = verts_path.length - line_offsize * offsize; i < vvvv.length; i += 2)
                {
                    vvvv[i] = verts_path[verts_path.length - 2];
                    vvvv[i + 1] = verts_path[verts_path.length - 1] + 1;
                }
            }

            canvas.drawBitmapMesh(bitmap, www, hhh, vvvv, 0, null, 0, p);
        }
    }

    public boolean start(boolean out)
    {
        if (isExecuting || mp == null)
            return false;

        if (out)
            progress = 0;
        else progress = mp.all_frame_count;

        if (progress == 0)
        {
            new Thread(animation_A).start();
        } else if (progress == mp.all_frame_count)
        {
            new Thread(animation_B).start();
        } else
        {
            progress = 0;
        }
        return true;

    }

    Handler mHandler = new Handler();

    Runnable animation_A = new Runnable()
    {
        public void run()
        {
            isExecuting = true;
            while (progress <= mp.all_frame_count)
            {
                try
                {
                    postInvalidate();
                    if (progress == 0 && mSuckListener != null)
                    {
                        mHandler.post(new Runnable()
                        {
                            public void run()
                            {
                                mSuckListener.onStart();
                            }
                        });
                    }
                    Thread.sleep(10);
                    progress += 2;
                } catch (Exception e)
                {
                }
            }
            progress = mp.all_frame_count;
            postInvalidate();
            isExecuting = false;
        }
    };

    Runnable animation_B = new Runnable()
    {
        public void run()
        {
            isExecuting = true;
            while (0 <= progress)
            {
                try
                {
                    postInvalidate();

                    Thread.sleep(10);
                    progress -= 2;
                } catch (InterruptedException e)
                {
                }
            }
            progress = 0;
            postInvalidate();
            isExecuting = false;

            if (mSuckListener != null)
            {
                mHandler.post(new Runnable()
                {
                    public void run()
                    {
                        mSuckListener.onEnd();
                    }
                });
            }
        }
    };

    SuckListener mSuckListener;

    public void setSuckListener(SuckListener mSuckListener)
    {
        this.mSuckListener = mSuckListener;
    }

    public static interface SuckListener
    {
        public void onStart();

        public void onEnd();
    }

}
