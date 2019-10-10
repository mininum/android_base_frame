package org.rdengine.widget.tabhost.scrollbottomline;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.android.frame.R;

import org.rdengine.util.PhoneUtil;
import org.rdengine.util.StringUtil;

/**
 * tabhost 有底线的 可以跟随滑动
 * 
 * @author CCCMAX
 */
public class BottomLineTabHost extends RelativeLayout implements OnClickListener
{

    int viewWidth = 0;

    private Context mContext;
    String[] titles;

    LinearLayout lineLayout;
    LinearLayout layout;

    String[] titlesback;

    int color_line = Color.parseColor("#ffffff");
    int item_layout_id = R.layout.tabhost_scrollbottomline_tab_btn_item;

    public BottomLineTabHost(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        this.mContext = context;
        // viewWidth = getResources().getDisplayMetrics().widthPixels;

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.bottomlinetabhost);
        initParams(a);
    }

    public void initParams(TypedArray ta)
    {
        try
        {
            color_line = ta.getColor(R.styleable.bottomlinetabhost_bottomlinetabhost_line_color, color_line);
            item_layout_id = ta.getResourceId(R.styleable.bottomlinetabhost_bottomlinetabhost_item_layout_id,
                    item_layout_id);
        } catch (Exception e)
        {
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {
        super.onLayout(changed, l, t, r, b);
        if (viewWidth == 0 || changed)
        {
            viewWidth = r - l;
            if (titlesback != null)
            {
                postDelayed(new Runnable()
                {

                    public void run()
                    {
                        setTitles(titlesback);
                    }
                }, 10);
            }
        }
    }

    float bottomLine_ratio = 0.4F;

    /** 设置底线宽度比例 0~1， 1是完整宽度 */
    public void setBottomLineRatio(float ratio)
    {
        if (ratio < 0)
            ratio = 0;
        if (ratio > 1)
            ratio = 1;
        this.bottomLine_ratio = ratio;
    }

    public void setTitles(String[] titles)
    {
        if (viewWidth == 0)
        {
            titlesback = titles;
            return;
        }
        titlesback = null;
        removeAllViews();
        this.titles = titles;
        if (this.titles != null && this.titles.length > 0)
        {
            layout = new LinearLayout(getContext());
            for (int i = 0; i < this.titles.length; i++)
            {
                layout.setOrientation(LinearLayout.HORIZONTAL);

                RelativeLayout relative = (RelativeLayout) inflate(getContext(), item_layout_id, null);

                TextView text = (TextView) relative.findViewById(R.id.title);
                text.setText(this.titles[i]);
                relative.setClickable(true);
                relative.setId(i);
                relative.setOnClickListener(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -1, 1);
                layout.addView(relative, params);

                TextView tv_count = (TextView) relative.findViewById(R.id.count);
                View tv_tip = relative.findViewById(R.id.tip);
                if (!isInEditMode())
                {
                    tv_count.setVisibility(View.GONE);
                    tv_tip.setVisibility(View.GONE);
                }
            }
            addView(layout, -1, -1);

            LayoutParams bottomLine = new LayoutParams(-1, 1);
            bottomLine.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);

            lineLayout = new LinearLayout(getContext());
            lineLayout.setOrientation(LinearLayout.HORIZONTAL);

            View redLine = new View(getContext());
            redLine.setBackgroundColor(color_line);

            int lineWidth = (viewWidth / titles.length);
            lineWidth = (int) (lineWidth * bottomLine_ratio);
            LinearLayout.LayoutParams lineParams = new LinearLayout.LayoutParams(lineWidth,
                    PhoneUtil.dipToPixel(3f, mContext));
            lineParams.leftMargin = (viewWidth / titles.length - lineWidth) / 2;
            lineLayout.addView(redLine, lineParams);

            // RelativeLayout.LayoutParams lineLayoutParams = new RelativeLayout.LayoutParams(lineParams.width
            // * (titles.length + 1), -2);
            LayoutParams lineLayoutParams = new LayoutParams(-1, -2);
            lineLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);

            addView(lineLayout, lineLayoutParams);
        }
    }

    /**
     * 设置某一项选中
     * 
     * @param position
     */
    public void setCheckedItem(int position)
    {
        if (layout != null)
        {
            int count = layout.getChildCount();
            for (int i = 0; i < count; i++)
            {
                View view = layout.getChildAt(i);
                if (i == position)
                {
                    view.setSelected(true);
                } else
                {
                    view.setSelected(false);
                }
            }
        }
    }

    public int getPosition()
    {
        int ret = 0;
        if (layout != null)
        {
            int count = layout.getChildCount();
            for (int i = 0; i < count; i++)
            {
                View view = layout.getChildAt(i);
                if (view.isSelected())
                {
                    ret = i;
                    break;
                }
            }
        }
        return ret;
    }

    /**
     * 设置某一项的标题
     * 
     * @param title
     * @param position
     */
    public void setTitle(String title, int position)
    {
        View view = layout.getChildAt(position);
        if (view instanceof RelativeLayout)
        {
            TextView text = (TextView) view.findViewById(R.id.title);
            text.setText(title);
        }
    }

    /**
     * 设置数量
     * 
     * @param count
     * @param position
     */
    public void setCount(long count, int position)
    {
        View view = layout.getChildAt(position);
        if (view instanceof RelativeLayout)
        {
            TextView text = (TextView) view.findViewById(R.id.count);
            // text.setText(String.valueOf(mtn));
            text.setText(StringUtil.formatUnreadNumber(count));
            text.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
        }
    }

    public void setHasNew(boolean hasNew, int position)
    {
        if (layout != null)
        {
            View view = layout.getChildAt(position);
            if (view != null && view instanceof RelativeLayout)
            {
                View tip = view.findViewById(R.id.tip);
                tip.setVisibility(hasNew ? View.VISIBLE : View.GONE);
                return;
            }
        }

    }

    public void scrollTo(int position, float x)
    {
        if (titles == null)
            return;
        int _x = -(int) (viewWidth / titles.length * (position + x));
        scrollTo(_x, 0);
    }

    @Override
    public void scrollTo(int x, int y)
    {
        if (lineLayout != null)
        {
            lineLayout.scrollTo(x, y);
        } else
        {
            super.scrollTo(x, y);
        }
    }

    @Override
    public void scrollBy(int x, int y)
    {
        if (lineLayout != null)
        {
            lineLayout.scrollBy(x, y);
        } else
        {
            super.scrollBy(x, y);
        }
    }

    @Override
    public void onClick(View v)
    {
        if (listener != null)
        {
            listener.onTabCheckedChange(v.getId());
        }
    }

    private OnTabCheckedChangeListener listener;

    public void setOnTabCheckedChangeListener(OnTabCheckedChangeListener listener)
    {
        this.listener = listener;
    }

    public interface OnTabCheckedChangeListener
    {

        public void onTabCheckedChange(int position);
    }

}
