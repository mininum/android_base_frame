package org.rdengine.widget.drawableview.draw;

import android.graphics.Bitmap;
import android.graphics.Path;
import android.graphics.RectF;

import org.rdengine.widget.drawableview.DrawableViewConfig;

import java.io.Serializable;
import java.util.ArrayList;

public class SerializablePath extends Path implements Serializable
{

    private ArrayList<float[]> pathPoints;
    private int color;
    private int color_outflow;
    private float width;
    private Bitmap strokeBitmap;
    private int draw_mode;

    public Bitmap cache;
    /** 用手势画完的标记 true */
    public boolean isTouchFinish = false;

    public SerializablePath()
    {
        super();
        pathPoints = new ArrayList<float[]>();
    }

    public SerializablePath(SerializablePath p)
    {
        super(p);
        pathPoints = p.pathPoints;
    }

    public void addPathPoints(float[] points)
    {
        this.pathPoints.add(points);
    }

    public float[] getFristPoints()
    {
        if (pathPoints == null && pathPoints.size() == 0)
            return null;
        return pathPoints.get(0);
    }

    public void saveMoveTo(float x, float y)
    {
        isTouchFinish = false;
        // DLOG.i("ggg", "down" + this.hashCode());

        super.moveTo(x, y);
        addPathPoints(new float[]
        { x, y });
    }

    public void saveLineTo(float x, float y)
    {
        if (draw_mode == DrawableViewConfig.MODE_PATCH)
        {
            pathPoints.clear();
            reset();
            saveMoveTo(x, y);
        } else
        {
            super.lineTo(x, y);
            addPathPoints(new float[]
            { x, y });
        }
    }

    public void saveReset()
    {
        isTouchFinish = false;

        super.reset();
        pathPoints.clear();

        if (cache != null)
        {
            if (!cache.isRecycled())
                cache.recycle();
            cache = null;
        }
    }

    public void savePoint()
    {
        isTouchFinish = true;
        // DLOG.i("ggg", "up" + this.hashCode());

        if (pathPoints.size() > 0)
        {
            float[] points = pathPoints.get(0);
            saveLineTo(points[0] + 1, points[1] + 1);
        }
    }

    public void loadPathPointsAsQuadTo()
    {
        float[] initPoints = pathPoints.get(0);
        this.moveTo(initPoints[0], initPoints[1]);
        for (int j = 1; j < pathPoints.size(); j++)
        {
            float[] pointSet = pathPoints.get(j);
            this.lineTo(pointSet[0], pointSet[1]);
        }
    }

    public int getColor()
    {
        return color;
    }

    public void setColor_outflow(int color_outflow)
    {
        this.color_outflow = color_outflow;
    }

    public int getColor_outglow()
    {
        return color_outflow;
    }

    public void setColor(int color)
    {
        this.color = color;
    }

    public float getWidth()
    {
        return width;
    }

    public void setWidth(float width)
    {
        this.width = width;
    }

    public Bitmap getBitmap()
    {
        return strokeBitmap;
    }

    public void setBitmap(Bitmap bitmap)
    {
        this.strokeBitmap = bitmap;
    }

    public void setDrawMode(int mode)
    {
        this.draw_mode = mode;
    }

    public int getDrawMode()
    {
        return this.draw_mode;
    }

    public RectF getBitmapRect(float x, float y)
    {
        RectF ret = new RectF();
        if (strokeBitmap != null)
        {
            int w = strokeBitmap.getWidth();
            int h = strokeBitmap.getHeight();
            ret.left = x - w / 2;
            ret.top = y - h / 2;
            ret.right = ret.left + w;
            ret.bottom = ret.top + h;
        }
        return ret;
    }

    public RectF getBitmapRectOnPatchMode()
    {
        try
        {
            float[] point = getFristPoints();
            return getBitmapRect(point[0], point[1]);
        } catch (Exception e)
        {
        }
        return null;
    }

    /**
     * 判断触摸点是否是patch模式中按中了图片矩形
     * 
     * @param x
     * @param y
     * @return
     */
    public static SerializablePath getInPatchRect(float x, float y, ArrayList<SerializablePath> allpath)
    {
        if (allpath != null)
        {
            for (int i = allpath.size() - 1; i >= 0; i--)
            {
                SerializablePath path = allpath.get(i);
                try
                {
                    if (path.draw_mode == DrawableViewConfig.MODE_PATCH)
                    {
                        RectF rect = path.getBitmapRectOnPatchMode();
                        if (rect.contains(x, y))
                        {
                            return path;
                        }
                    }
                } catch (Exception e)
                {
                }
            }
        }

        return null;
    }
}
