package org.rdengine.widget;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.RelativeLayout;

import com.android.frame.R;


/**
 * 绘制的转圈圈loading 先从小变大 然后开始转圈
 * 
 * @author CCCMAX
 */
public class ProgressBarCircular extends RelativeLayout
{

    final static String ANDROIDXML = "http://schemas.android.com/apk/res/android";

    int backgroundColor = Color.parseColor("#1E88E5");

    public ProgressBarCircular(Context context)
    {
        super(context);
        setBackgroundColor(backgroundColor);
    }

    public ProgressBarCircular(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        setAttributes(attrs);

    }

    boolean noZoom = false;

    public void setNoZoom(boolean noZoom)
    {
        this.noZoom = noZoom;
    }

    public int dpToPx(float dp, Resources resources)
    {
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
        return (int) px;
    }

    // Set atributtes of XML to View
    protected void setAttributes(AttributeSet attrs)
    {

        setMinimumHeight(dpToPx(32, getResources()));
        setMinimumWidth(dpToPx(32, getResources()));

        // Set background Color
        // Color by resource
        int bacgroundColor = attrs.getAttributeResourceValue(ANDROIDXML, "background", -1);
        if (bacgroundColor != -1)
        {
            setBackgroundColor(getResources().getColor(bacgroundColor));
        } else
        {
            // Color by hexadecimal
            // String background = attrs.getAttributeValue(ANDROIDXML, "background");
            // if (background != null)
            // setBackgroundColor(Color.parseColor(background));
            // else
            // setBackgroundColor(Color.parseColor("#1E88E5"));
            String background = attrs.getAttributeValue(ANDROIDXML, "background");
            if (background != null)
            {
                setBackgroundColor(Color.parseColor(background));
            } else
            {
                // TypedValue outValue = new TypedValue();
                // getContext().getTheme().resolveAttribute(R.attr.theme_desc_text_color, outValue, true);
                // setBackgroundColor(getResources().getColor(outValue.resourceId));
                setBackgroundColor(getResources().getColor(R.color.progressbar_color));
            }
        }

        setMinimumHeight(dpToPx(3, getResources()));

    }

    /**
     * Make a dark color to ripple effect
     * 
     * @return
     */
    protected int makePressColor()
    {
        int r = (this.backgroundColor >> 16) & 0xFF;
        int g = (this.backgroundColor >> 8) & 0xFF;
        int b = (this.backgroundColor >> 0) & 0xFF;
        // r = (r+90 > 245) ? 245 : r+90;
        // g = (g+90 > 245) ? 245 : g+90;
        // b = (b+90 > 245) ? 245 : b+90;
        return Color.argb(128, r, g, b);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        try
        {
            if (noZoom)
            {
                drawSecondAnimation(canvas);
            } else
            {
                if (firstAnimationOver == false)
                    drawFirstAnimation(canvas);
                if (cont > 0)
                    drawSecondAnimation(canvas);
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        invalidate();

    }

    float radius1 = 0;
    float radius2 = 0;
    int cont = 0;
    boolean firstAnimationOver = false;

    /**
     * Draw first animation of view
     * 
     * @param canvas
     */
    private void drawFirstAnimation(Canvas canvas)
    {
        float stepping = getWidth() / 2.0F / 20.0F;// 步进 20帧 放大动画一共20帧
        stepping = stepping < 1 ? 1 : stepping;

        if (radius1 < getWidth() / 2)
        {
            Paint paint = new Paint();
            paint.setDither(true);
            paint.setAntiAlias(true);
            paint.setColor(makePressColor());
            radius1 = (radius1 >= getWidth() / 2) ? (float) getWidth() / 2 : radius1 + stepping;
            canvas.drawCircle(getWidth() / 2, getHeight() / 2, radius1, paint);
        } else
        {
            Bitmap bitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas temp = new Canvas(bitmap);
            Paint paint = new Paint();
            paint.setDither(true);
            paint.setAntiAlias(true);
            paint.setColor(makePressColor());
            temp.drawCircle(getWidth() / 2, getHeight() / 2, getHeight() / 2, paint);
            Paint transparentPaint = new Paint();
            transparentPaint.setDither(true);
            transparentPaint.setAntiAlias(true);
            transparentPaint.setColor(getResources().getColor(android.R.color.transparent));
            transparentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            if (cont >= 50)
            {
                radius2 = (radius2 >= getWidth() / 2) ? (float) getWidth() / 2 : radius2 + stepping;
            } else
            {
                radius2 = (radius2 >= getWidth() / 2 - dpToPx(4, getResources()))
                        ? (float) getWidth() / 2 - dpToPx(3, getResources()) : radius2 + stepping;
            }
            temp.drawCircle(getWidth() / 2, getHeight() / 2, radius2, transparentPaint);
            canvas.drawBitmap(bitmap, 0, 0, new Paint());
            if (radius2 >= getWidth() / 2 - dpToPx(4, getResources()))
                cont++;
            if (radius2 >= getWidth() / 2)
                firstAnimationOver = true;
            if (bitmap != null)
                bitmap.recycle();
        }
    }

    int arcD = 1;
    int arcO = 0;
    float rotateAngle = 0;
    int limite = 0;

    /**
     * Draw second animation of view
     * 
     * @param canvas
     */
    private void drawSecondAnimation(Canvas canvas)
    {
        if (arcO == limite)
            arcD += 6;
        if (arcD >= 290 || arcO > limite)
        {
            arcO += 6;
            arcD -= 6;
        }
        if (arcO > limite + 290)
        {
            limite = arcO;
            arcO = limite;
            arcD = 1;
        }
        rotateAngle += 4;
        canvas.rotate(rotateAngle, getWidth() / 2, getHeight() / 2);

        Bitmap bitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas temp = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setDither(true);
        paint.setAntiAlias(true);
        paint.setColor(backgroundColor);
        // temp.drawARGB(0, 0, 0, 255);
        temp.drawArc(new RectF(0, 0, getWidth(), getHeight()), arcO, arcD, true, paint);
        Paint transparentPaint = new Paint();
        transparentPaint.setDither(true);
        transparentPaint.setAntiAlias(true);
        transparentPaint.setColor(getResources().getColor(android.R.color.transparent));
        transparentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        temp.drawCircle(getWidth() / 2, getHeight() / 2, (getWidth() / 2) - dpToPx(3, getResources()),
                transparentPaint);

        canvas.drawBitmap(bitmap, 0, 0, new Paint());
        if (bitmap != null)
            bitmap.recycle();
    }

    // Set color of background
    public void setBackgroundColor(int color)
    {
        super.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        this.backgroundColor = color;
    }

    @Override
    public void setVisibility(int visibility)
    {
        if (visibility == View.VISIBLE && getVisibility() != View.VISIBLE)
        {
            radius1 = 0;
            radius2 = 0;
            cont = 0;
            firstAnimationOver = false;
        }

        super.setVisibility(visibility);
    }

}
