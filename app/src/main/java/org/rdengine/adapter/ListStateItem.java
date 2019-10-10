package org.rdengine.adapter;

public class ListStateItem<T>
{
    public T data;
    public int state = 0;
    public boolean flag = false;
    public String tag = null;
    public int viewtype = 0;
    public Object obj;

    public ListStateItem(T data)
    {
        this.data = data;
    }

    public ListStateItem(T data, int viewType)
    {
        this.data = data;
        this.viewtype = viewType;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == null || getClass() != o.getClass())
            return false;
        ListStateItem<?> that = (ListStateItem<?>) o;
        if (data != null ? !data.equals(that.data) : that.data != null)
            return false;
        return true;
    }

}
