package org.rdengine.widget.taglayout;

import android.view.View;

public class NoCalculateLayoutProperty extends AbstractTagLayoutProperty implements ITagLayoutProperty
{

    public NoCalculateLayoutProperty(View owner)
    {
        super(owner);
    }

    private int defaultValue = 0;

    public void setDefault(int defaultValue)
    {
        this.defaultValue = defaultValue;
    }

    @Override
    protected int calcuateInternal(TagLayoutContext context)
    {
        return defaultValue;
    }
}
