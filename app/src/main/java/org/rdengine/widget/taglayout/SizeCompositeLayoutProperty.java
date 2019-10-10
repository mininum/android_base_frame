package org.rdengine.widget.taglayout;

import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

public class SizeCompositeLayoutProperty extends AbstractTagLayoutProperty implements ITagLayoutProperty
{

    private ITagLayoutProperty width;

    private ITagLayoutProperty height;

    private View child;

    public SizeCompositeLayoutProperty(View child, ITagLayoutProperty width, ITagLayoutProperty height)
    {
        super(child);
        this.child = child;
        this.width = width;
        this.height = height;
    }

    @Override
    protected int calcuateInternal(TagLayoutContext context)
    {

        width.calculate(context);
        height.calculate(context);
        // Log.d("tag_layout", "view size calculate start");
        int widthResult = width.getValue();
        int heightResult = height.getValue();
        ViewGroup container = context.getContaienr();
        LayoutParams lp = child.getLayoutParams();
        // need android layout
        if (widthResult == TagLayoutContext.RESULT_NO_CALCULATE)
        {
            widthResult = ViewGroup.getChildMeasureSpec(context.getWidthSpec(),
                    container.getPaddingLeft() + container.getPaddingTop(), lp.width);
        } else
        {
            widthResult = MeasureSpec.makeMeasureSpec(widthResult, MeasureSpec.EXACTLY);
        }

        if (heightResult == TagLayoutContext.RESULT_NO_CALCULATE)
        {
            heightResult = ViewGroup.getChildMeasureSpec(context.getHeightSpec(),
                    container.getPaddingTop() + container.getPaddingBottom(), lp.height);
        } else
        {
            heightResult = MeasureSpec.makeMeasureSpec(heightResult, MeasureSpec.EXACTLY);
        }
        /**
         * Log.d("tag_layout", "given width: " + MeasureSpec.getSize(widthResult) + " mode: " + MeasureSpec.getMode(widthResult)); Log.d("tag_layout", "given height: " + MeasureSpec.getSize(heightResult) + " mode: " + MeasureSpec.getMode(heightResult));
         */
        child.measure(widthResult, heightResult);

        ((AbstractTagLayoutProperty) width).setValue(child.getMeasuredWidth());
        ((AbstractTagLayoutProperty) height).setValue(child.getMeasuredHeight());
        /**
         * if(child.getMeasuredWidth() ==0 && child.getMeasuredHeight() ==0) { Log.d("tag_layout", "view: " + child.hashCode() + " w: " + child.getMeasuredWidth() + " h: " + child.getMeasuredHeight()); } Log.d("tag_layout", "view: " + child.hashCode() + " w: " + child.getMeasuredWidth() + " h: " + child.getMeasuredHeight());
         */
        return 0;
    }

}
