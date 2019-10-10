package org.rdengine.widget.taglayout;

import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import org.rdengine.widget.TagRelativeLayout.TagLayoutParams;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TagLayoutContext
{

    public static final int PROPERTY_LEFT = 0;

    public static final int PROPERTY_TOP = 1;

    public static final int PROPERTY_WIDTH = 2;

    public static final int PROPERTY_HEIGHT = 3;

    /**
     * 没有计算能力
     */
    public static final int RESULT_NO_CALCULATE = -1;

    public static final int RESULT_NEED_ANDROID_LAYOUT_HEIGHT = -1;

    private int widthSpec;

    private int heightSpec;

    private ViewGroup container;

    private List<ITagLayoutProperty> linkList = new ArrayList<ITagLayoutProperty>();

    private List<ITagLayoutProperty> dependParentList = new ArrayList<ITagLayoutProperty>();

    private SparseArray<TagLayoutParams> cachedTagLayoutParams = new SparseArray<TagLayoutParams>();

    private SparseArray<ITagLayoutProperty> cachedComposites = new SparseArray<ITagLayoutProperty>();

    public TagLayoutContext(ViewGroup container)
    {
        this.container = container;
    }

    public void prepareMeasure(int widthSpec, int heightSpec)
    {
        this.widthSpec = widthSpec;
        this.heightSpec = heightSpec;
    }

    public List<ITagLayoutProperty> getLinkList()
    {
        return linkList;
    }

    /**
     * 把property node连接起来,建立关联网.因为android里的 width和height需要同时设置,所以width和height被包装载一个组合节点里.
     */
    public void unionLink(View child, ITagLayoutProperty width, ITagLayoutProperty height)
    {
        if (cachedComposites.get(child.hashCode()) != null)
        {
            return;
        }
        ITagLayoutProperty composite = null;
        if (child == container)
        {
            composite = new ParentSizeLayoutProperty(child, width, height);
        } else
        {
            composite = new SizeCompositeLayoutProperty(child, width, height);
        }
        cachedComposites.put(child.hashCode(), composite);
        String expression = width.getExpression();
        link(expression, composite);
        expression = height.getExpression();
        link(expression, composite);
    }

    /**
     * 把property node连接起来,建立关联网
     */
    public void link(ITagLayoutProperty property)
    {
        link(property.getExpression(), property);
    }

    private void link(String expression, ITagLayoutProperty sub)
    {
        // 加入link 列表
        if (!linkList.contains(sub))
        {
            linkList.add(sub);
        } else
        {
            // return;
        }
        // 得到依赖的view及其 属性
        if (expression == null)
        {
            return;
        }
        Pattern p = Pattern.compile("(\\d+(\\.\\d*)?([whlt]%\\w*)+)");
        Matcher m = p.matcher(expression);
        while (m.find())
        {
            String group = m.group();
            int index = group.indexOf("%");
            String idString = group.substring(index + 1);
            String propertyString = group.substring(index - 1, index);
            View child = null;
            if (idString == null || idString.length() <= 0)
            {
                child = sub.getOwner();
            } else if (idString.equals("p"))
            {
                child = this.container;
            } else
            {
                child = container.findViewById(getId(idString));
            }
            AbstractTagLayoutProperty parent = (AbstractTagLayoutProperty) getPropertyUsingLink(child, propertyString);
            parent.addSub(sub);
            ((AbstractTagLayoutProperty) sub).addParent((ITagLayoutProperty) parent);
        }
    }

    public ITagLayoutProperty getPropertyUsingLink(View child, String property)
    {
        TagLayoutParams tagLayoutParams = getTagLayoutParams(child);
        if (property.equals("w") || property.equals("h"))
        {
            return cachedComposites.get(child.hashCode());
        } else if (property.equals("l"))
        {
            return tagLayoutParams.getLeft();

        } else if (property.equals("t"))
        {
            return tagLayoutParams.getTop();
        }
        return null;
    }

    public TagLayoutParams getTagLayoutParams(View child)
    {
        TagLayoutParams tagLayoutParams = cachedTagLayoutParams.get(child.hashCode());
        if (tagLayoutParams == null)
        {
            tagLayoutParams = new TagLayoutParams(this, child, child == container ? null : child.getTag());
            cachedTagLayoutParams.put(child.hashCode(), tagLayoutParams);
            tagLayoutParams.createProperties();
        }
        return tagLayoutParams;
    }

    public void invalidate()
    {
        if (linkList != null)
        {
            for (ITagLayoutProperty prop : linkList)
            {
                prop.invalidte();
            }
        }
        if (dependParentList != null)
        {
            for (ITagLayoutProperty prop : dependParentList)
            {
                prop.invalidte();
            }
        }
    }

    /**
     * 分为两部分,一部分依赖parent,一部分不依赖
     */
    public void linkSplit()
    {
        ITagLayoutProperty widthAndHeight = cachedComposites.get(this.container.hashCode());
        List<ITagLayoutProperty> descends = null;
        if (widthAndHeight != null)
        {
            descends = getDecends(widthAndHeight);
            descends.add(widthAndHeight);
        }
        dependParentList = descends;
        if (linkList != null && descends != null)
        {
            linkList.removeAll(descends);
        }

    }

    public ITagLayoutProperty getParentLayoutProperty()
    {
        return cachedComposites.get(this.container.hashCode());
    }

    public List<ITagLayoutProperty> getDependParentList()
    {
        return dependParentList;
    }

    private List<ITagLayoutProperty> getDecends(ITagLayoutProperty property)
    {
        List<ITagLayoutProperty> descends = new ArrayList<ITagLayoutProperty>();
        List<ITagLayoutProperty> subChildren = property.getChildren();
        if (subChildren != null)
        {
            descends.addAll(subChildren);
            for (ITagLayoutProperty sub : subChildren)
            {
                descends.addAll(getDecends(sub));
            }
        }
        return descends;
    }

    public int getWidthSpec()
    {
        return widthSpec;
    }

    public void setWidthSpec(int widthSpec)
    {
        this.widthSpec = widthSpec;
    }

    public int getHeightSpec()
    {
        return heightSpec;
    }

    public void setHeightSpec(int heightSpec)
    {
        this.heightSpec = heightSpec;
    }

    public ViewGroup getContaienr()
    {
        return container;
    }

    public void setContainer(ViewGroup parent)
    {
        this.container = parent;
    }

    public View getViewById(int id)
    {
        if (id == 0)
        {
            return container;
        }
        return container.findViewById(id);
    }

    public int getId(String idString)
    {
        String name = container.getContext().getApplicationContext().getPackageName();
        int id = -1;
        try
        {
            id = container.getContext().getResources().getIdentifier(idString, "id", name);

        } catch (Exception e)
        {
            Log.d("tag_layout", e.getMessage());
        }
        if (id <= 0)
        {
            try
            {
                Class clazz = container.getContext().getApplicationContext().getClassLoader().loadClass(name + ".R$id");
                Field field = clazz.getDeclaredField(idString);
                return field.getInt(null);
            } catch (Exception e)
            {
                Log.d("tag_layout", e.getMessage());
            }
        }
        return id;
    }
}