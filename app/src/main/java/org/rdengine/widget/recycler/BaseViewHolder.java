package org.rdengine.widget.recycler;

import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;

import org.rdengine.adapter.ListCell;

public class BaseViewHolder extends ViewHolder
{

    public BaseViewHolder(View itemView)
    {
        super(itemView);
    }

    public void setData(Object data, BaseRecyclerAdapter adapter)
    {
        if (itemView instanceof ListCell)
        {
            ((ListCell) itemView).setData(data, this.getPosition(), null);
        }
    }
}
