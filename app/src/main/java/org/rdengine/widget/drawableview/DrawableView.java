package org.rdengine.widget.drawableview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import org.rdengine.widget.drawableview.draw.CanvasDrawer;
import org.rdengine.widget.drawableview.draw.PathDrawer;
import org.rdengine.widget.drawableview.draw.SerializablePath;
import org.rdengine.widget.drawableview.gestures.creator.GestureCreator;
import org.rdengine.widget.drawableview.gestures.creator.GestureCreatorListener;
import org.rdengine.widget.drawableview.gestures.scale.GestureScaleListener;
import org.rdengine.widget.drawableview.gestures.scale.GestureScaler;
import org.rdengine.widget.drawableview.gestures.scale.ScalerListener;
import org.rdengine.widget.drawableview.gestures.scroller.GestureScrollListener;
import org.rdengine.widget.drawableview.gestures.scroller.GestureScroller;
import org.rdengine.widget.drawableview.gestures.scroller.ScrollerListener;

import java.util.ArrayList;

public class DrawableView extends View
        implements View.OnTouchListener, ScrollerListener, GestureCreatorListener, ScalerListener
{

    private final ArrayList<SerializablePath> paths = new ArrayList<SerializablePath>();

    private GestureScroller gestureScroller;
    private GestureScaler gestureScaler;
    private GestureCreator gestureCreator;
    private int canvasHeight;
    private int canvasWidth;
    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleGestureDetector;
    private PathDrawer pathDrawer;
    private CanvasDrawer canvasDrawer;
    private SerializablePath currentDrawingPath;
    private Drawable background_drawable;

    public DrawableView(Context context)
    {
        super(context);
        init();
    }

    public DrawableView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    public DrawableView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init()
    {
        gestureScroller = new GestureScroller(this);
        gestureDetector = new GestureDetector(getContext(), new GestureScrollListener(gestureScroller));
        gestureScaler = new GestureScaler(this);
        scaleGestureDetector = new ScaleGestureDetector(getContext(), new GestureScaleListener(gestureScaler));
        gestureCreator = new GestureCreator(this);
        pathDrawer = new PathDrawer();
        canvasDrawer = new CanvasDrawer();
        setOnTouchListener(this);
    }

    public void setConfig(DrawableViewConfig config)
    {
        if (config == null)
        {
            throw new IllegalArgumentException("Paint configuration cannot be null");
        }
        canvasWidth = config.getCanvasWidth();
        canvasHeight = config.getCanvasHeight();
        gestureCreator.setConfig(config);
        gestureScaler.setZooms(config.getMinZoom(), config.getMaxZoom());
        gestureScroller.setCanvasBounds(canvasWidth, canvasHeight);
        canvasDrawer.setConfig(config);
        if (config.getBackgroundId() != 0)
        {
            try
            {
                background_drawable = getResources().getDrawable(config.getBackgroundId());
                background_drawable.setBounds(0, 0, canvasWidth, canvasHeight);
            } catch (Exception e)
            {
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
        gestureScroller.setViewBounds(w, h);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        scaleGestureDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);
        gestureCreator.onTouchEvent(event);
        invalidate();
        return true;
    }

    public void undo()
    {
        if (paths.size() > 0)
        {
            paths.remove(paths.size() - 1);
            invalidate();
        }
    }

    @SuppressLint("WrongCall")
    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        canvasDrawer.onDraw(canvas);
        drawBackgroundBitmap(canvas);
        drawSourceBitmap(canvas);
        pathDrawer.onDraw(canvas, currentDrawingPath, paths);
    }

    public void clear()
    {
        paths.clear();
        invalidate();
    }

    public Bitmap obtainBitmapSourceimg()
    {
        Bitmap bbb = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888);
        Canvas composeCanvas = new Canvas(bbb);
        drawBackgroundBitmap(composeCanvas);
        drawSourceBitmap(composeCanvas);
        return obtainBitmap(bbb);
    }

    public Bitmap obtainBitmap(Bitmap createdBitmap)
    {
        return pathDrawer.obtainBitmap(createdBitmap, paths);
    }

    public Bitmap obtainBitmap()
    {
        return obtainBitmap(Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888));
    }

    @Override
    protected Parcelable onSaveInstanceState()
    {
        return super.onSaveInstanceState();
        // DrawableViewSaveState state = new DrawableViewSaveState(super.onSaveInstanceState());
        // state.setPaths(paths);
        // return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        if (!(state instanceof DrawableViewSaveState))
        {
            super.onRestoreInstanceState(state);
        } else
        {
            // DrawableViewSaveState ss = (DrawableViewSaveState) state;
            // super.onRestoreInstanceState(ss.getSuperState());
            // paths.addAll(ss.getPaths());
            super.onRestoreInstanceState(state);
        }
    }

    @Override
    public void onViewPortChange(RectF currentViewport)
    {
        gestureCreator.onViewPortChange(currentViewport);
        canvasDrawer.onViewPortChange(currentViewport);
    }

    @Override
    public void onCanvasChanged(RectF canvasRect)
    {
        gestureCreator.onCanvasChanged(canvasRect);
        canvasDrawer.onCanvasChanged(canvasRect);
    }

    @Override
    public void onGestureCreated(SerializablePath serializablePath)
    {
        paths.add(serializablePath);
    }

    @Override
    public void onCurrentGestureChanged(SerializablePath currentDrawingPath)
    {
        this.currentDrawingPath = currentDrawingPath;
    }

    @Override
    public ArrayList<SerializablePath> getAllPath()
    {
        return paths;
    }

    @Override
    public void onScaleChange(float scaleFactor)
    {
        gestureScroller.onScaleChange(scaleFactor);
        gestureCreator.onScaleChange(scaleFactor);
        canvasDrawer.onScaleChange(scaleFactor);
    }

    // --------------------增加背景原图
    Bitmap bitmap_source;
    Paint paint_sourcebitmap = new Paint();

    /**
     * 设置画布底图
     * 
     * @param bitmap
     */
    public void setSourceBitmap(Bitmap bitmap)
    {
        this.bitmap_source = bitmap;
    }

    /**
     * 绘制底图
     * 
     * @param canvas
     */
    public void drawSourceBitmap(Canvas canvas)
    {
        if (bitmap_source != null)
        {
            Rect src = new Rect(0, 0, bitmap_source.getWidth(), bitmap_source.getHeight());
            Rect dst = new Rect(0, 0, canvasWidth, canvasHeight);
            canvas.drawBitmap(bitmap_source, src, dst, paint_sourcebitmap);
        }
    }

    /**
     * 绘制背景
     * 
     * @param canvas
     */
    public void drawBackgroundBitmap(Canvas canvas)
    {
        if (background_drawable != null)
        {
            background_drawable.draw(canvas);
        }
    }
    // ---------------------

}
