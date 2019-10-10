package org.rdengine.widget.drawableview;

import android.graphics.Bitmap;

import java.io.Serializable;

public class DrawableViewConfig implements Serializable
{
    /* 打码，随手指移动路径画图片遮盖 */
    public static final int MODE_MOSAIC = 0;
    /* 画笔模式，随手指移动路径画线条 */
    public static final int MODE_PAINT = 1;
    /* 贴图模式，手指按下的位置贴一张图 */
    public static final int MODE_PATCH = 2;

    private int draw_mode = MODE_MOSAIC;

    private float strokeWidth;
    private int strokeColor;
    private int strokeColorOutglow;
    private Bitmap strokeBitmap;
    private int background_id;
    private int canvasWidth;
    private int canvasHeight;
    private float minZoom;
    private float maxZoom;
    private boolean showCanvasBounds;

    public float getMaxZoom()
    {
        return maxZoom;
    }

    public void setMaxZoom(float maxZoom)
    {
        this.maxZoom = maxZoom;
    }

    public float getMinZoom()
    {
        return minZoom;
    }

    public void setMinZoom(float minZoom)
    {
        this.minZoom = minZoom;
    }

    public int getCanvasHeight()
    {
        return canvasHeight;
    }

    public void setCanvasHeight(int canvasHeight)
    {
        this.canvasHeight = canvasHeight;
    }

    public int getCanvasWidth()
    {
        return canvasWidth;
    }

    public void setCanvasWidth(int canvasWidth)
    {
        this.canvasWidth = canvasWidth;
    }

    public float getStrokeWidth()
    {
        return strokeWidth;
    }

    public void setStrokeWidth(float strokeWidth)
    {
        this.strokeWidth = strokeWidth;
    }

    public int getStrokeColor()
    {
        return strokeColor;
    }

    public void setStrokeColor(int strokeColor)
    {
        this.strokeColor = strokeColor;
    }

    public int getStrokeColorOutglow()
    {
        return strokeColorOutglow;
    }

    public void setStrokeColorOutglow(int strokeColorOutglow)
    {
        this.strokeColorOutglow = strokeColorOutglow;
    }

    public Bitmap getStrokeBitmap()
    {
        return strokeBitmap;
    }

    public void setStrokeBitmap(Bitmap bitmap)
    {
        this.strokeBitmap = bitmap;
    }

    public boolean isShowCanvasBounds()
    {
        return showCanvasBounds;
    }

    public void setShowCanvasBounds(boolean showCanvasBounds)
    {
        this.showCanvasBounds = showCanvasBounds;
    }

    public void setBackgroundId(int resID)
    {
        this.background_id = resID;
    }

    public int getBackgroundId()
    {
        return this.background_id;
    }

    public void setDrawMode(int mode)
    {
        this.draw_mode = mode;
    }

    public int getDrawMode()
    {
        return this.draw_mode;
    }
}
