package org.rdengine.widget.drawableview.draw;

import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.graphics.RectF;

import org.rdengine.widget.drawableview.DrawableViewConfig;

import java.util.List;

public class PathDrawer
{

    private Paint gesturePaint;

    public PathDrawer()
    {
        initGesturePaint();
    }

    public void onDraw(Canvas canvas, SerializablePath currentDrawingPath, List<SerializablePath> paths)
    {
        drawGestures(canvas, paths);
        if (currentDrawingPath != null)
        {
            drawGesture(canvas, currentDrawingPath);
        }
    }

    public void drawGestures(Canvas canvas, List<SerializablePath> paths)
    {
        for (SerializablePath path : paths)
        {
            drawGesture(canvas, path);
        }
    }

    public Bitmap obtainBitmap(Bitmap createdBitmap, List<SerializablePath> paths)
    {
        Canvas composeCanvas = new Canvas(createdBitmap);
        drawGestures(composeCanvas, paths);
        return createdBitmap;
    }

    private void drawGesture(Canvas canvas, SerializablePath path)
    {
        gesturePaint.setStrokeWidth(path.getWidth());
        gesturePaint.setColor(path.getColor());
        switch (path.getDrawMode())
        {
        case DrawableViewConfig.MODE_MOSAIC :
        {
            try
            {
                Bitmap bitmap = path.getBitmap();
                PointF[] pointarray = getPointsWithSpace(path, bitmap.getWidth() / 2);
                for (PointF point : pointarray)
                {
                    RectF rectf = path.getBitmapRect(point.x, point.y);
                    canvas.drawBitmap(bitmap, rectf.left, rectf.top, gesturePaint);
                }
            } catch (Exception e)
            {
            }
        }
            break;
        case DrawableViewConfig.MODE_PATCH :
        {
            try
            {
                RectF rectf = path.getBitmapRectOnPatchMode();
                canvas.drawBitmap(path.getBitmap(), rectf.left, rectf.top, gesturePaint);
            } catch (Exception e)
            {
            }
        }
            break;
        case DrawableViewConfig.MODE_PAINT :
        default:
        {
            int color_outglow = path.getColor_outglow();
            if (color_outglow != 0)
            {
                if (!path.isTouchFinish)
                {
                    // DLOG.i("ggg","正常绘制"+path.hashCode());
                    BlurMaskFilter blurmaskfilter = new BlurMaskFilter(path.getWidth() * 2, BlurMaskFilter.Blur.SOLID);
                    Paint p = new Paint(gesturePaint);
                    p.setColor(color_outglow);
                    p.setMaskFilter(blurmaskfilter);
                    canvas.drawPath(path, p);
                } else if (path.cache != null && !path.cache.isRecycled())
                {
                    // DLOG.i("ggg","绘制缓存"+path.hashCode());
                    canvas.drawBitmap(path.cache, 0, 0, gesturePaint);
                } else
                {
                    try
                    {
                        // DLOG.i("ggg","生成缓存"+path.hashCode());
                        path.cache = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(),
                                Bitmap.Config.ARGB_8888);
                        Canvas tempcanvas = new Canvas(path.cache);

                        BlurMaskFilter blurmaskfilter = new BlurMaskFilter(path.getWidth() * 2,
                                BlurMaskFilter.Blur.SOLID);
                        Paint p = new Paint(gesturePaint);
                        p.setColor(color_outglow);
                        p.setMaskFilter(blurmaskfilter);
                        tempcanvas.drawPath(path, p);

                        canvas.drawBitmap(path.cache, 0, 0, gesturePaint);
                    } catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                }
            }
            canvas.drawPath(path, gesturePaint);
        }
            break;
        }
    }

    private void initGesturePaint()
    {
        gesturePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.FILTER_BITMAP_FLAG);
        gesturePaint.setStyle(Paint.Style.STROKE);
        gesturePaint.setStrokeJoin(Paint.Join.ROUND);
        gesturePaint.setStrokeCap(Paint.Cap.ROUND);
    }

    /**
     * 获取曲线路径中的点坐标数组 （矢量path抽样出点坐标） 根据间隔
     * 
     * @param path
     *            曲线路径
     * @param space
     *            抽样点的间隔
     * @return
     */
    private PointF[] getPointsWithSpace(SerializablePath path, int space)
    {
        if (space <= 0)
            space = 1;
        PathMeasure pm = new PathMeasure(path, false);
        float length = pm.getLength();
        int total = (int) (length / space);
        if (total == 0)
        {
            float[] p1 = path.getFristPoints();
            return new PointF[]
            { new PointF(p1[0], p1[1]) };
        } else
        {
            total += 1;
        }
        PointF[] pointArray = new PointF[total];
        float distance = 0f;
        int counter = 0;
        float[] aCoordinates = new float[2];
        while ((distance <= length) && (counter < total))
        {
            pm.getPosTan(distance, aCoordinates, null);
            pointArray[counter] = new PointF(aCoordinates[0], aCoordinates[1]);
            counter++;
            distance = distance + space;
        }

        // pm.getPosTan(length, aCoordinates, null);
        // pointArray[total - 1] = new PointF(aCoordinates[0], aCoordinates[1]);
        return pointArray;
    }
}
