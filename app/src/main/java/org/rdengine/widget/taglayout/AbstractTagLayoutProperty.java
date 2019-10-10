package org.rdengine.widget.taglayout;

import android.view.View;

import org.rdengine.widget.TagRelativeLayout.TagLayoutParams;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractTagLayoutProperty
{

    protected List<ITagLayoutProperty> parents;

    /**
     * @see TagLayoutParams PROPERTY_LEFT
     */
    protected int property;

    protected View nodeView;

    protected String expression;

    protected List<ITagLayoutProperty> subs;

    private int value;

    private boolean valudeGenerated;

    private View owner;

    public AbstractTagLayoutProperty(View owner)
    {
        this.owner = owner;
    }

    public AbstractTagLayoutProperty(View owner, String expression)
    {
        this.owner = owner;
        this.expression = expression;
    }

    public String getExpression()
    {
        return this.expression;
    }

    public void addParent(ITagLayoutProperty property)
    {
        if (parents == null)
        {
            parents = new ArrayList<ITagLayoutProperty>();
        }
        if (parents.contains(property))
        {
            return;
        }
        parents.add(property);
    }

    public void addSub(ITagLayoutProperty property)
    {
        if (subs == null)
        {
            subs = new ArrayList<ITagLayoutProperty>();
        }
        if (subs.contains(property))
        {
            return;
        }
        subs.add(property);
    }

    public List<ITagLayoutProperty> getParents()
    {
        return parents;
    }

    public boolean canCalcuate()
    {
        if (parents == null)
        {
            return true;
        }
        for (ITagLayoutProperty prop : parents)
        {
            if (!prop.isValueReady())
            {
                return false;
            }
        }
        return true;
    }

    public void invalidte()
    {
        valudeGenerated = false;
    }

    public boolean isValueReady()
    {
        return valudeGenerated;
    }

    public void calculate(TagLayoutContext context)
    {
        value = calcuateInternal(context);
        valudeGenerated = true;
    }

    protected abstract int calcuateInternal(TagLayoutContext context);

    public int getValue()
    {
        return value;
    }

    protected void setValue(int value)
    {
        this.value = value;
    }

    public List<ITagLayoutProperty> getChildren()
    {
        return subs;
    }

    public View getOwner()
    {
        return owner;
    }

}
