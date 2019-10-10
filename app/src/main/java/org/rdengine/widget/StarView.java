package org.rdengine.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.android.frame.R;


public class StarView extends View {
    public StarView(Context context) {
        super(context);

        init();
    }
    public StarView(Context context,  AttributeSet attrs) {
        super(context, attrs);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.StarView);
        star_num = typedArray.getInteger(R.styleable.StarView_star_num, 5);
        select_num = typedArray.getInteger(R.styleable.StarView_select_num,0);
        init();
    }

    private Paint sPaint;

    private Bitmap selectStar;
    private Bitmap defaultStar;

    int starWidth;
    int starHeight;
    int star_num = 4;
    int h_pad = 5;
    float select_num ;
    private void init() {

        sPaint = new Paint();

        selectStar = BitmapFactory.decodeResource(getResources(), R.drawable.icon_star_select);
        defaultStar = BitmapFactory.decodeResource(getResources(),R.drawable.icon_star_default);

        starWidth = defaultStar.getWidth();
        starHeight = defaultStar.getHeight();

        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int width = (getPaddingLeft() + (starWidth + h_pad) * star_num + getPaddingRight());
        int height = (getPaddingTop() + starHeight + getPaddingBottom());
        setMeasuredDimension(widthMode == MeasureSpec.EXACTLY ? widthSize : width,
                heightMode == MeasureSpec.EXACTLY ? heightSize : height);
    }

    public void setSelect_num(int select_num){

        this.select_num = select_num;
        invalidate();

    }
    public void setStar_num(int star_num){
        this.star_num = star_num;
        invalidate();

    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float currentRating = select_num < 0 ? 0 : (select_num > star_num ? star_num : select_num);
        int mLeft = 0;
        int mTop = 0;
        int full = (int) currentRating;
        /**
         * 画选中的整颗星
         */
        for (int i = 0; i < full; i++) {
            canvas.drawBitmap(selectStar, mLeft, mTop, sPaint);
            mLeft = mLeft + starWidth + h_pad;
        }

        if (currentRating == star_num) {
            return;
        }
        /**
         * 画默认的整颗星
         */
        for (int i = full; i < star_num; i++) {
            canvas.drawBitmap(defaultStar, mLeft, mTop, sPaint);
            mLeft = mLeft + starWidth + h_pad;
        }



    }
}
