package org.rdengine.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;

public abstract class RDBaseAdapter<T> extends BaseAdapter
{
    public Context mContext;
    public LayoutInflater mLayoutInflater;
    public ViewGroup parentview;

    public ArrayList<T> data;

    public RDBaseAdapter()
    {
        data = new ArrayList<T>();
    }

    public RDBaseAdapter(Context context)
    {
        data = new ArrayList<T>();
        mContext = context;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    public void addData(T obj)
    {
        if (data == null)
            data = new ArrayList<T>();
        data.add(obj);
    }

    public void addAll(ArrayList<T> dlist)
    {
        if (this.data == null)
            this.data = new ArrayList<T>();
        if (dlist != null)
            this.data.addAll(dlist);
    }

    public boolean removeItem(T item)
    {
        if (this.data != null)
        {
            return this.data.remove(item);
        }
        return false;
    }

    public void removeItem(int index)
    {
        if (this.data != null)
        {
            try
            {
                this.data.remove(index);
            } catch (Exception e)
            {
            }
        }
    }

    public void clearData()
    {
        if (this.data != null)
            this.data.clear();
    }

    public ArrayList<T> getData()
    {
        return data;
    }

    public int getCount()
    {
        if (data == null)
            return 0;
        return data.size();
    }

    public T getItem(int position)
    {
        if (data == null || position >= data.size() || position < 0)
            return null;
        return data.get(position);
    }

    public long getItemId(int position)
    {
        return 0;
    }

    public static Object getAdapterNextItemData(BaseAdapter adapter, int p)
    {
        try
        {
            if (adapter != null && p + 1 < adapter.getCount() && p >= 0)
            {
                return adapter.getItem(p + 1);
            }
        } catch (Exception ex)
        {
        }
        return null;
    }
}
