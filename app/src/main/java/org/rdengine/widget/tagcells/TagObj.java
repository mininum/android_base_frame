package org.rdengine.widget.tagcells;

import android.view.View;

import java.lang.ref.WeakReference;

/**
 * 标签类型
 * 
 * @author CCCMAX
 */
public class TagObj
{

    public int type;// 类型
    public String text;// 文字
    public boolean select = false;// 是否选中状态
    public Object data;// 数据扩展
    public WeakReference<View> view;

    public TagObj(TagObj to)
    {
        this.type = to.type;
        this.text = to.text;
        this.select = to.select;
        this.data = to.data;
    }

    public TagObj(int type, String text, boolean select)
    {
        super();
        this.type = type;
        this.text = text;
        this.select = select;
    }

    public TagObj(int type, String text, boolean select, Object data)
    {
        this(type, text, select);
        this.data = data;
    }

    public void setView(View v)
    {
        if (v != null)
        {
            view = new WeakReference<View>(v);
        } else
        {
            view = null;
        }
    }

    public View getView()
    {
        if (view != null)
        {
            return view.get();
        }
        return null;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == null)
        {
            return false;
        }
        if (o == this)
        {
            return true;
        }
        if (o instanceof TagObj)
        {
            return ((TagObj) o).text.equals(text);
        }
        return super.equals(o);
    }
}