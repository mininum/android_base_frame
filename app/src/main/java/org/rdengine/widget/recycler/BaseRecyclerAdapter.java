package org.rdengine.widget.recycler;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseRecyclerAdapter<T, VH extends ViewHolder> extends RecyclerView.Adapter
{
    /**
     * 循环模式
     */
    boolean isLoop = false;

    /**
     * 数据
     */
    ArrayList<T> data;

    public BaseRecyclerAdapter()
    {
        data = new ArrayList<T>();
    }

    public void setLoopMode(boolean loop)
    {
        isLoop = loop;
    }

    public void addData(T obj)
    {
        if (data == null)
            data = new ArrayList<T>();
        data.add(obj);
    }

    public void addAll(List<T> dlist)
    {
        if (this.data == null)
            this.data = new ArrayList<T>();
        this.data.addAll(dlist);
    }

    public void removeItem(T item)
    {
        if (this.data != null)
        {
            this.data.remove(item);
        }
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

    public T getItem(int position)
    {
        if (data == null || data.size() == 0)
            return null;

        if (isLoop)
        {
            int index = position % data.size();
            return data.get(index);
        } else
        {
            if (data == null || position >= data.size() || position < 0)
                return null;
            return data.get(position);
        }
    }

    @Override
    public int getItemCount()
    {
        if (isLoop)
        {
            if (data == null || data.size() == 0)
                return 0;
            return Integer.MAX_VALUE;
        } else
        {
            if (data == null)
                return 0;
            return data.size();
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder vh, int position)
    {
        if (vh != null && vh instanceof BaseViewHolder)
        {
            ((BaseViewHolder) vh).setData(getItem(position), this);
        }
        if (listener != null)
        {
            addClickToItem(vh, position);
        }
    }

    public void addClickToItem(ViewHolder vh, int position)
    {
        if (listener != null)
        {
            vh.itemView.setTag(position);
            vh.itemView.setOnClickListener(new OnClickListener()
            {

                public void onClick(View v)
                {
                    listener.onItemClickListener(v, Integer.parseInt(v.getTag().toString()));
                }
            });
        }
    }

    @Override
    public abstract ViewHolder onCreateViewHolder(ViewGroup parent, int position);

    public interface OnItemClickListener
    {
        void onItemClickListener(View view, int position);
    }

    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener)
    {
        this.listener = listener;
    }

}
