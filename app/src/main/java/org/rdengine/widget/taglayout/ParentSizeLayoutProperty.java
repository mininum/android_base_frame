package org.rdengine.widget.taglayout;

import android.graphics.Point;
import android.view.View;
import android.view.View.MeasureSpec;

import org.rdengine.widget.TagRelativeLayout.TagLayoutParams;

import java.util.ArrayList;
import java.util.List;

public class ParentSizeLayoutProperty extends AbstractTagLayoutProperty implements ITagLayoutProperty
{

    private ITagLayoutProperty width;

    private ITagLayoutProperty height;

    public ParentSizeLayoutProperty(View owner, ITagLayoutProperty width, ITagLayoutProperty height)
    {
        super(owner);
        this.width = width;
        this.height = height;
    }

    @Override
    protected int calcuateInternal(TagLayoutContext context)
    {
        width.calculate(context);
        height.calculate(context);
        int wsp = context.getWidthSpec();
        int hsp = context.getHeightSpec();
        int resultWidth = 0;
        int resultHeight = 0;
        Point size = null;
        // Log.d("tag_layout", " calculate parent start");
        switch (MeasureSpec.getMode(wsp))
        {
        case MeasureSpec.EXACTLY :
            resultWidth = MeasureSpec.getSize(wsp);
            // Log.d("tag_layout", "get exactly width: " + resultWidth);
            break;
        case MeasureSpec.AT_MOST :
            size = getSizeFromContent(context);
            resultWidth = Math.min(size.x, MeasureSpec.getSize(wsp));
            // Log.d("tag_layout", "get width from content: " + resultWidth);
            break;
        case MeasureSpec.UNSPECIFIED :
            size = getSizeFromContent(context);
            resultWidth = size.x;
            // Log.d("tag_layout", "get width from content: " + resultWidth);
            break;
        }
        switch (MeasureSpec.getMode(hsp))
        {
        case MeasureSpec.EXACTLY :
            resultHeight = MeasureSpec.getSize(hsp);
            break;
        case MeasureSpec.AT_MOST :
            if (size == null)
            {
                size = getSizeFromContent(context);
            }
            resultHeight = Math.min(size.y, MeasureSpec.getSize(hsp));
            break;
        case MeasureSpec.UNSPECIFIED :
            if (size == null)
            {
                size = getSizeFromContent(context);
            }
            resultHeight = size.y;
            break;
        }
        ((AbstractTagLayoutProperty) width).setValue(resultWidth);
        ((AbstractTagLayoutProperty) height).setValue(resultHeight);
        return 0;
    }

    private Point getSizeFromContent(TagLayoutContext context)
    {
        List<ITagLayoutProperty> list = context.getLinkList();
        List<View> children = new ArrayList<View>();
        if (list == null)
        {
            return new Point(0, 0);
        }
        for (ITagLayoutProperty prop : list)
        {
            View child = prop.getOwner();
            if (!children.contains(child))
            {
                children.add(child);
            }
        }
        int x = 0;
        int y = 0;
        for (View child : children)
        {
            TagLayoutParams params = context.getTagLayoutParams(child);
            if (params.getWidth().isValueReady())
            {
                int l = params.getLeft().isValueReady() ? params.getLeft().getValue() : 0;
                x = Math.max(l + params.getWidth().getValue(), x);
            }
            if (params.getHeight().isValueReady())
            {
                int t = params.getTop().isValueReady() ? params.getTop().getValue() : 0;
                y = Math.max(t + params.getHeight().getValue(), y);
            }
        }
        return new Point(x, y);
    }

}
