package org.rdengine.widget.drawableview.gestures.creator;

import android.graphics.RectF;
import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;

import org.rdengine.widget.drawableview.DrawableViewConfig;
import org.rdengine.widget.drawableview.draw.SerializablePath;

public class GestureCreator
{

    private SerializablePath currentDrawingPath = new SerializablePath();
    private GestureCreatorListener delegate;
    private DrawableViewConfig config;
    private boolean downAndUpGesture = false;
    private float scaleFactor = 1.0f;
    private RectF viewRect = new RectF();
    private RectF canvasRect = new RectF();

    /**
     * 画的时候 可以移动到屏幕外面
     */
    private boolean canMoveOutside = true;

    public GestureCreator(GestureCreatorListener delegate)
    {
        this.delegate = delegate;
    }

    public void onTouchEvent(MotionEvent event)
    {
        float touchX = (MotionEventCompat.getX(event, 0) + viewRect.left) / scaleFactor;
        float touchY = (MotionEventCompat.getY(event, 0) + viewRect.top) / scaleFactor;

        // Log.d("Drawer", "T[" + touchX + "," + touchY + "] V[" + viewRect.toShortString() + "] S[" + scaleFactor + "]");
        switch (MotionEventCompat.getActionMasked(event))
        {
        case MotionEvent.ACTION_DOWN :
            actionDown(touchX, touchY);
            break;
        case MotionEvent.ACTION_MOVE :
            actionMove(touchX, touchY);
            break;
        case MotionEvent.ACTION_UP :
            actionUp();
            break;
        case MotionEventCompat.ACTION_POINTER_DOWN :
            actionPointerDown();
            break;
        }
    }

    private void actionDown(float touchX, float touchY)
    {
        if (insideCanvas(touchX, touchY))
        {
            downAndUpGesture = true;

            if (config.getDrawMode() == DrawableViewConfig.MODE_PATCH)
            {
                SerializablePath path = SerializablePath.getInPatchRect(touchX, touchY, delegate.getAllPath());
                if (path != null)
                {
                    currentDrawingPath = path;
                    delegate.onCurrentGestureChanged(currentDrawingPath);
                    return;
                }
            }

            currentDrawingPath = new SerializablePath();
            if (config != null)
            {
                currentDrawingPath.setDrawMode(config.getDrawMode());
                currentDrawingPath.setColor(config.getStrokeColor());
                currentDrawingPath.setColor_outflow(config.getStrokeColorOutglow());
                currentDrawingPath.setWidth(config.getStrokeWidth());
                if (config.getStrokeBitmap() != null && !config.getStrokeBitmap().isRecycled())
                {
                    currentDrawingPath.setBitmap(config.getStrokeBitmap());
                }
            }
            currentDrawingPath.saveMoveTo(touchX, touchY);
            delegate.onCurrentGestureChanged(currentDrawingPath);
        }
    }

    private void actionMove(float touchX, float touchY)
    {
        if (canMoveOutside)
        {
            downAndUpGesture = false;
            if (currentDrawingPath != null)
            {
                currentDrawingPath.saveLineTo(touchX, touchY);
            }
        } else
        {
            if (insideCanvas(touchX, touchY))
            {
                downAndUpGesture = false;
                if (currentDrawingPath != null)
                {
                    currentDrawingPath.saveLineTo(touchX, touchY);
                }
            } else
            {
                actionUp();
            }
        }
    }

    private void actionUp()
    {
        if (currentDrawingPath != null)
        {
            if (downAndUpGesture)
            {
                currentDrawingPath.savePoint();
                downAndUpGesture = false;
            }

            currentDrawingPath.isTouchFinish = true;

            delegate.onGestureCreated(currentDrawingPath);
            currentDrawingPath = null;
            delegate.onCurrentGestureChanged(null);

        }
    }

    private void actionPointerDown()
    {
        currentDrawingPath = null;
        delegate.onCurrentGestureChanged(null);
    }

    private boolean insideCanvas(float touchX, float touchY)
    {
        return canvasRect.contains(touchX, touchY);
    }

    public void setConfig(DrawableViewConfig config)
    {
        this.config = config;
    }

    public void onScaleChange(float scaleFactor)
    {
        this.scaleFactor = scaleFactor;
    }

    public void onViewPortChange(RectF viewRect)
    {
        this.viewRect = viewRect;
    }

    public void onCanvasChanged(RectF canvasRect)
    {
        this.canvasRect.right = canvasRect.right / scaleFactor;
        this.canvasRect.bottom = canvasRect.bottom / scaleFactor;
    }

    public void setCanMoveOutside(boolean canMoveOutside)
    {
        this.canMoveOutside = canMoveOutside;
    }
}
