package org.rdengine.widget.cropimage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PathEffect;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.android.frame.R;

import org.rdengine.util.PhoneUtil;

public class CropImageView extends ImageView
{
    protected Matrix mBaseMatrix = new Matrix();
    protected Matrix mSuppMatrix = new Matrix();
    private final Matrix mDisplayMatrix = new Matrix();
    private final float[] mMatrixValues = new float[9];

    float mMaxZoom;
    private int mWidth;
    private int mHeight;

    protected final RotateBitmap mBitmapDisplayed = new RotateBitmap(null);

    private Runnable mOnLayoutRunnable = null;

    static final float SCALE_RATE = 1.25F;

    public CropImageView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    @SuppressLint("NewApi")
    private void init()
    {

        try
        {
            if (PhoneUtil.hasHoneycomb())
            {
                setLayerType(LAYER_TYPE_SOFTWARE, null);

            }
        } catch (Error e)
        {
        }
        linePaint = new Paint();
        linePaint.setColor(Color.WHITE);
        linePaint.setStrokeWidth(lineWidth);

        dashPaint = new Paint();
        dashPaint.setStyle(Style.STROKE);
        PathEffect effect = new DashPathEffect(new float[]
        { 5, 5, 5, 5 }, 1);
        dashPaint.setAntiAlias(true);
        dashPaint.setPathEffect(effect);
        dashPaint.setColor(0xffffffff);
        dashPaint.setStrokeWidth(2.5f);

        setScaleType(ScaleType.MATRIX);

        setBackgroundResource(R.drawable.bg_pixel_matrix);
    }

    protected float maxZoom()
    {
        if (mBitmapDisplayed.getBitmap() == null)
        {
            return 1F;
        }

        float fw = (float) mBitmapDisplayed.getWidth() / (float) mWidth;
        float fh = (float) mBitmapDisplayed.getHeight() / (float) mHeight;
        float max = Math.max(fw, fh) * 10;
        return max;
    }

    protected float getValue(Matrix matrix, int whichValue)
    {
        matrix.getValues(mMatrixValues);
        return mMatrixValues[whichValue];
    }

    protected float getScale(Matrix matrix)
    {
        float scale = Math.abs(getValue(matrix, Matrix.MSCALE_X));
        if (scale == 0.0f)
        {
            scale = Math.abs(getValue(matrix, Matrix.MSKEW_X));
        }
        return scale;
    }

    public float getScale()
    {
        return getScale(mSuppMatrix);
    }

    protected Matrix getImageViewMatrix()
    {
        mDisplayMatrix.set(mBaseMatrix);
        mDisplayMatrix.postConcat(mSuppMatrix);
        return mDisplayMatrix;
    }

    public void setRoate(int degree)
    {
        if (mBitmapDisplayed.getBitmap() != null)
        {
            // mBitmapDisplayed.setRotation(degree);
            // mSuppMatrix = mBitmapDisplayed.getRotateMatrix();
            // mSuppMatrix.setRotate(degree, getWidth()/2, getHeight()/2);
            mSuppMatrix.postRotate(degree, getWidth() / 2, getHeight() / 2);
            setImageMatrix(getImageViewMatrix());
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom)
    {
        super.onLayout(changed, left, top, right, bottom);
        mWidth = right - left;
        mHeight = bottom - top;
        Runnable r = mOnLayoutRunnable;
        if (r != null)
        {
            mOnLayoutRunnable = null;
            r.run();
        }
        if (mBitmapDisplayed.getBitmap() != null)
        {
            getProperBaseMatrix(mBitmapDisplayed, mBaseMatrix);
            setImageMatrix(getImageViewMatrix());
        }
    }

    float ratio = 1;

    /**
     * 设置裁剪比例 ratio=宽/高, 不能小于1
     * 
     * @param ratio
     */
    public void setRatio(float ratio)
    {
        if (ratio < 1)
            ratio = 1;
        this.ratio = ratio;
    }

    private void setImageBitmap(Bitmap bitmap, int rotation)
    {
        super.setImageBitmap(bitmap);
        Drawable d = getDrawable();
        if (d != null)
        {
            d.setDither(true);
        }

        Bitmap old = mBitmapDisplayed.getBitmap();
        mBitmapDisplayed.setBitmap(bitmap);
        mBitmapDisplayed.setRotation(rotation);

        if (old != null && old != bitmap)
        {
            // if (!PhoneUtil.hasHoneycombMR1())
            old.recycle();
            old = null;
        }
    }

    public void clear()
    {
        setImageBitmapResetBase(null, true);
    }

    public void setImageBitmapResetBase(final Bitmap bitmap, final boolean resetSupp)
    {
        setImageRotateBitmapResetBase(new RotateBitmap(bitmap), resetSupp);
    }

    public void setImageRotateBitmapResetBase(final RotateBitmap bitmap, final boolean resetSupp)
    {
        final int viewWidth = getWidth();
        if (viewWidth <= 0)
        {
            mOnLayoutRunnable = new Runnable()
            {
                public void run()
                {
                    setImageRotateBitmapResetBase(bitmap, resetSupp);
                }
            };
            return;
        }

        if (bitmap.getBitmap() != null)
        {
            getProperBaseMatrix(bitmap, mBaseMatrix);
            setImageBitmap(bitmap.getBitmap(), bitmap.getRotation());
        } else
        {
            mBaseMatrix.reset();
            setImageBitmap(null);
        }

        if (resetSupp)
        {
            mSuppMatrix.reset();
        }
        setImageMatrix(getImageViewMatrix());
        mMaxZoom = maxZoom();
    }

    private void getProperBaseMatrix(RotateBitmap bitmap, Matrix matrix)
    {
        float viewWidth = getWidth();
        float viewHeight = getHeight();

        float w = bitmap.getWidth();
        float h = bitmap.getHeight();
        matrix.reset();

        float widthScale = 1f;
        float heightScale = 1f;
        if (viewWidth > w)
        {
            widthScale = Math.max(viewWidth * 0.8f / w, 1.25f);
        } else
        {
            widthScale = viewWidth / w;
        }

        if (viewHeight > h)
        {
            heightScale = Math.max(viewHeight * 0.8f / h, 1.25f);
        } else
        {
            heightScale = viewHeight / h;
        }

        startScale = Math.min(widthScale, heightScale);

        matrix.postConcat(bitmap.getRotateMatrix());
        matrix.postScale(startScale, startScale);

        startX = ((viewWidth - w * startScale) / 2f);
        startY = ((viewHeight - h * startScale) / 2f);
        imageWidth = (int) (w * startScale);
        imageHeight = (int) (h * startScale);
        matrix.postTranslate(startX, startY);

        float[] f = new float[9];
        mSuppMatrix.getValues(f);

        imagePointX = f[2];
        imagePointY = f[5];
    }

    private int imageWidth;
    private int imageHeight;
    private float startScale;
    private float startX;
    private float startY;

    public RotateBitmap getBitmapDisplay()
    {
        return mBitmapDisplayed;
    }

    protected void zoomIn(float rate)
    {
        if (getScale() >= mMaxZoom)
        {
            return;
        }
        if (mBitmapDisplayed.getBitmap() == null)
        {
            return;
        }

        int w = imageWidth;
        int h = imageHeight;
        imageWidth *= rate;
        imageHeight *= rate;

        float x = imagePointX;
        float y = imagePointY;

        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        float[] f0 = new float[9];
        float[] f = new float[9];

        // LOG.d("crop", "1>>>" + imagePointX + ">>" + imagePointY + ">>" + getWidth() + ">>" );
        mSuppMatrix.getValues(f0);
        mSuppMatrix.postScale(rate, rate, cx, cy);
        mSuppMatrix.getValues(f);
        reX = getWidth() * rate;
        reY = getHeight() * rate;
        imagePointX = f[2];
        imagePointY = f[5];
        // imagePointX -= f0[2] - f[2];// (rate - 1) * w * 1f / 2;
        // imagePointY -= f0[5] - f[5];// (rate - 1) * h * 1f / 2;
        // LOG.d("crop", "2>>>" + imagePointX + ">>" + imagePointY);

        setImageMatrix(getImageViewMatrix());
        invalidate();
    }

    protected void zoomOut(float rate)
    {
        if (mBitmapDisplayed.getBitmap() == null)
        {
            return;
        }

        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;

        Matrix tmp = new Matrix(mSuppMatrix);
        tmp.postScale(1f / rate, 1f / rate, cx, cy);
        // imageWidth *= 1f / rate;
        // imageHeight *= 1f / rate;
        int w = imageWidth;
        int h = imageHeight;
        float[] f0 = new float[9];
        float[] ft = new float[9];
        float[] f = new float[9];
        mSuppMatrix.getValues(f0);
        tmp.getValues(ft);
        // int td = 0;
        // float tx = imagePointX - (f0[2] - ft[2]);
        // tx += td + startX * getScale();
        // float ty = imagePointY - (f0[5] - ft[5]);
        // ty += td + startY * getScale();

        // if (tx > mLeft)
        // {
        // cx = mLeft;
        // }
        // if (ty > mTop)
        // {
        // cy = mTop;
        // }
        //
        // if (tx + imageWidth < mRight)
        // {
        // cx = mRight;
        // }
        // if (ty + imageHeight < mBottom)
        // {
        // cy = mBottom;
        // }
        if (getScale(tmp) < 0.6f)
        {
            return;
            // mSuppMatrix.postScale(0.8f, 0.8f, cx, cy);
            // imageWidth *= 0.8f;
            // imageHeight *= 0.8f;

        } else
        {
            mSuppMatrix.postScale(1f / rate, 1f / rate, cx, cy);
            imageWidth *= 1f / rate;
            imageHeight *= 1f / rate;
        }
        setImageMatrix(getImageViewMatrix());
        mSuppMatrix.getValues(f);
        imagePointX = f[2];
        imagePointY = f[5];
        // imagePointX -= f0[2] - f[2];// (rate - 1) * w * 1f / 2;
        // imagePointY -= f0[5] - f[5];// (rate - 1) * h * 1f / 2;m

        // mSuppMatrix.setTranslate(cx - imagePointX, cy - imagePointY);
        // setImageMatrix(getImageViewMatrix());
        // float[] ff = new float[9];
        // mSuppMatrix.getValues(ff);

        // imagePointX = ff[2];
        // imagePointY = ff[5];

        invalidate();
    }

    protected void postTranslate(float dx, float dy)
    {
        mSuppMatrix.preTranslate(0, 0);
        mSuppMatrix.postTranslate(dx, dy);
        float[] f = new float[9];
        mSuppMatrix.getValues(f);

        imagePointX = f[2];
        imagePointY = f[5];

        invalidate();

    }

    public void zoomIn()
    {
        zoomIn(SCALE_RATE);
    }

    public void zoomOut()
    {
        zoomOut(SCALE_RATE);
    }

    float reX;
    float reY;

    public void moveBy(float dx, float dy)
    {
        // imagePointX += dx + startX * getScale();
        // imagePointY += dy + startY * getScale();
        // LOG.d("crop", ">>" + imagePointX + "," + imagePointY);
        // if (imagePointX > mLeft || imagePointX + imageWidth < mRight)
        // {
        // // imagePointX -= dx;
        // dx = 0;
        // }
        // if (imagePointY > mTop || imagePointY + imageHeight < mBottom)
        // {
        // // imagePointY -= dy;
        // dy = 0;
        // }
        postTranslate(dx, dy);
        setImageMatrix(getImageViewMatrix());

    }

    float mLastX;
    float mLastY;

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        final int action = event.getAction();
        switch (action & MotionEvent.ACTION_MASK)
        {
        case MotionEvent.ACTION_DOWN :
            mode = DRAG;
            break;
        case MotionEvent.ACTION_POINTER_DOWN :
            if (event.getPointerCount() == 2)
            {
                mode = ZOOM;
                lastDistance = getMoveDistance(event);
            }
            break;
        case MotionEvent.ACTION_MOVE :
            if (mode == DRAG)
            {
                moveBy(event.getX() - mLastX, event.getY() - mLastY);
            } else if (mode == ZOOM)
            {
                float distance = getMoveDistance(event);
                float rate = distance / lastDistance;
                if (rate > 1f)
                {
                    zoomIn(rate);
                } else
                {
                    zoomOut(1f / (rate));
                }
                lastDistance = distance;
            }
            break;
        default:
            mode = NONE;
            break;
        }
        mLastX = event.getX();
        mLastY = event.getY();
        return true;
    }

    int mode;
    float lastDistance;

    final static int NONE = 0;
    final static int DRAG = 1;
    final static int ZOOM = 2;

    float getMoveDistance(MotionEvent event)
    {

        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);

        return (float) Math.sqrt(x * x + y * y);
    }

    int highlightSize = 0;
    int highlightXOffset = 0;
    int highlightYOffset = 0;

    void calcHightlight()
    {
        highlightSize = (int) (getWidth() * 1.0f);// 基础宽度
        int highlightSize_h = (int) (highlightSize / ratio);// 基础高度 根据比例计算
        // (int) Math
        // .min((Math.min(mBitmapDisplayed.getHeight(), mBitmapDisplayed.getWidth()) * startScale),
        // (getWidth() * 0.8f));
        // if (startScale > 1f)
        // {
        //
        // highlightSize = (int) (Math.min(mBitmapDisplayed.getHeight(), mBitmapDisplayed.getWidth()) * startScale);
        // }

        highlightXOffset = (int) ((getWidth() - highlightSize) / 2);
        highlightYOffset = (int) ((getHeight() - highlightSize_h) / 2);

        mLeft = (int) (highlightXOffset);
        mTop = (int) (highlightYOffset);
        mRight = mLeft + highlightSize;
        mBottom = mTop + highlightSize_h;

        testPaint.setColor(Color.YELLOW);
        testPaint.setStrokeWidth(20);
    }

    int mLeft;
    int mRight;
    int mTop;
    int mBottom;

    float imagePointX;
    float imagePointY;
    Paint testPaint = new Paint();

    @Override
    protected void onDraw(Canvas canvas)
    {
        canvas.save();
        super.onDraw(canvas);
        calcHightlight();

        // canvas.clipRect(0, 0, mWidth, mHeight);
        // canvas.clipRect(startX + highlightOffset, startY + highlightOffset, highlightSize, highlightSize,
        // Region.Op.DIFFERENCE);

        canvas.clipRect(mLeft, mTop, mRight, mBottom, Region.Op.DIFFERENCE);
        canvas.clipRect(0, 0, mWidth, mHeight);
        canvas.drawColor(0xcf000000);

        // canvas.drawPoint(imagePointX + startX * getScale(), imagePointY + startY * getScale(), testPaint);
        canvas.restore();
        if (needDash)
        {
            float d = 0.54f;

            canvas.drawLine(0, (int) (mTop * 1.2), getMeasuredWidth(), (int) (mTop * 1.2), dashPaint);

            canvas.drawLine(0, (int) (mTop * 1.2) + 200, getMeasuredWidth(), (int) (mTop * 1.2) + 200, dashPaint);
        }
        canvas.drawLine(mLeft - lineWidth, mTop - lineWidth, mLeft - lineWidth, mBottom + 0, linePaint);
        canvas.drawLine(mLeft - lineWidth, mTop - lineWidth, mRight + 0, mTop - lineWidth, linePaint);
        canvas.drawLine(mRight + 0, mTop - lineWidth, mRight + 0, mBottom + 0, linePaint);
        canvas.drawLine(mRight + 0, mBottom + 0, mLeft - lineWidth, mBottom + 0, linePaint);
    }

    private Paint dashPaint;
    private Paint linePaint;

    private boolean needDash = true;

    public void setNeedDash(boolean needDash)
    {
        this.needDash = needDash;
        invalidate();
    }

    public int getCropLeft()
    {
        return mLeft;
    }

    public int getCropRight()
    {
        return mRight;
    }

    public int getCropTop()
    {
        return mTop;
    }

    public int getCropBottom()
    {
        return mBottom;
    }

    int lineWidth = 1;

    public Bitmap getCropBitmap()
    {
        setNeedDash(false);
        setDrawingCacheEnabled(false);
        setDrawingCacheEnabled(true);
        // Bitmap b = Bitmap.createBitmap(getDrawingCache(), mLeft - lineWidth, mTop - lineWidth, mLeft + mRight
        // - lineWidth, mTop + mBottom - lineWidth);
        //
        // return b;

        Bitmap b = getDrawingCache();
        setNeedDash(true);
        return b;

        // float scale = startScale * getScale();
        //
        // //DLOG.e("crop", "" + scale + ">>>>>" + startScale + ">>>" + mBitmapDisplayed.getBitmap().getWidth());
        //
        // if (scale <= 1)
        // {
        // // scale = 1f / scale;
        // }
        //
        // int x = (int) ((mLeft - (imagePointX + startX * getScale())) / scale);
        // x = Math.max(x, 0);
        // int y = (int) ((mTop - (imagePointY + startY * getScale())) / scale);
        // y = Math.max(y, 0);
        // int width = (int) (highlightSize * 1f / scale);
        // int height = (int) (highlightSize * 1f / scale);
        // //DLOG.e("crop", "crop image >>" + width + ">>" + x + ">>" + height + ">>" + y);
        // return Bitmap.createBitmap(mBitmapDisplayed.getBitmap(), x, y, width, height);
    }

    public float getStartScale()
    {
        return startScale;
    }

    public void logPoint()
    {
        // DLOG.d("crop", "start >" + startX + "," + startY);
        // DLOG.d("crop", "" + imagePointX + "," + imagePointY);
    }

}
