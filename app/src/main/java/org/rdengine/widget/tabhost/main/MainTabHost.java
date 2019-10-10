package org.rdengine.widget.tabhost.main;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

public class MainTabHost extends LinearLayout implements OnClickListener
{

    public MainTabHost(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate()
    {
        super.onFinishInflate();
        int count = getChildCount();
        for (int i = 0; i < count; i++)
        {
            View view = getChildAt(i);
            if (view instanceof MainTabButton)
            {
                view.setOnClickListener(this);
            }
        }
    }

    public void setChecked(int position)
    {
        setChecked(position, false);
    }

    private void setChecked(int position, boolean byUser)
    {
        int count = getChildCount();

        if (mListener != null)
        {
            int last_id = -1;
            int new_id = -1;

            for (int i = 0; i < count; i++)
            {
                View view = getChildAt(i);
                if (view instanceof MainTabButton)
                {
                    boolean last = view.isSelected();
                    if (last)
                        last_id = i;

                    boolean nnew = position == i;
                    if (nnew)
                        new_id = i;
                }
            }

            if (mListener.onCheckedChange(position, byUser))
            {
                // 有效选中
                getChildAt(new_id).setSelected(true);
                if (last_id >= 0 && new_id != last_id)
                    getChildAt(last_id).setSelected(false);
            } else
            {
                // 无效选中
                getChildAt(new_id).setSelected(false);
                if (last_id >= 0)
                    getChildAt(last_id).setSelected(true);
            }
        } else
        {
            for (int i = 0; i < count; i++)
            {
                View view = getChildAt(i);
                if (view instanceof MainTabButton)
                {
                    view.setSelected(position == i);
                }
            }
        }
    }

    /**
     * 设置是否有更新
     * 
     * @param position
     * @param hasNew
     */
    public void setHasNew(int position, boolean hasNew)
    {
        MainTabButton button = (MainTabButton) getChildAt(position);
        if (button != null)
        {
            button.setHasNew(hasNew);
        }
    }

    /**
     * 设置未读数
     * 
     * @param position
     * @param count
     */
    public void setUnreadCount(int position, long count)
    {
        MainTabButton button = (MainTabButton) getChildAt(position);
        if (button != null)
        {
            button.setUnreadCount(count);
        }
    }

    @Override
    public void onClick(View v)
    {
        int count = getChildCount();
        for (int i = 0; i < count; i++)
        {
            View view = getChildAt(i);
            if (v == view)
            {
                setChecked(i, true);
                break;
            }
        }
    }

    private OnCheckedChangeListener mListener;

    public void setOnCheckedChangeListener(OnCheckedChangeListener mListener)
    {
        this.mListener = mListener;
    }

    public interface OnCheckedChangeListener
    {

        boolean onCheckedChange(int checkedPosition, boolean byUser);
    }
}
