package org.rdengine.widget.taglayout;

import android.view.View;

import org.rdengine.widget.TagRelativeLayout.TagLayoutParams;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TagExpressionLayoutProperty extends AbstractTagLayoutProperty implements ITagLayoutProperty
{

    public TagExpressionLayoutProperty(View owner, String expression)
    {
        super(owner, expression);
    }

    @Override
    protected int calcuateInternal(TagLayoutContext context)
    {
        String expression = getExpression();
        Pattern p = Pattern.compile("(\\d+(\\.\\d*)?([whlt]%\\w*)*)");
        Matcher m = p.matcher(expression);
        float value = 0;
        int operatorIndex = 0;
        String operator;
        while (m.find())
        {
            String group = m.group();
            m.start();
            operator = expression.substring(operatorIndex, m.start()).trim();
            if (operator.equals("+"))
            {
                value += getValue(context, group);
            } else if (operator.equals("-"))
            {
                value -= getValue(context, group);
            } else
            {
                value = getValue(context, group);
            }
            operatorIndex = m.end();
        }
        return (int) value;
    }

    private float getValue(TagLayoutContext context, String group)
    {
        int index = group.indexOf("%");
        if (index < 0)
        {
            return Float.parseFloat(group)
                    * context.getContaienr().getContext().getResources().getDisplayMetrics().density;
        }
        String idString = group.substring(index + 1);
        String propertyString = group.substring(index - 1, index);
        float numer = Float.parseFloat(group.substring(0, index - 1));
        View target = null;
        float value = 0;
        if (idString == null || idString.length() <= 0)
        {
            target = getOwner();
        } else if (idString.equals("p"))
        {
            target = context.getContaienr();
        } else
        {
            target = context.getContaienr().findViewById((context.getId(idString)));
        }
        TagLayoutParams tagLayout = context.getTagLayoutParams(target);
        if (propertyString.equals("w"))
        {
            value = tagLayout.getWidth().getValue() * numer / 100;
        } else if (propertyString.equals("h"))
        {
            value = tagLayout.getHeight().getValue() * numer / 100;
        } else if (propertyString.equals("l"))
        {
            value = tagLayout.getLeft().getValue() * numer / 100;
        } else
        {
            value = tagLayout.getTop().getValue() * numer / 100;
        }
        return value;
    }

}
