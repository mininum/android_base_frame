package org.rdengine.widget.tabhost.main;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.frame.R;

import org.rdengine.util.StringUtil;

public class MainTabButton extends RelativeLayout
{

    private ImageView image;
    private TextView tab_button;
    private View tab_tip;
    private TextView unread_count;
    private boolean isChecked = false;

    public MainTabButton(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        initLayout(context, attrs);
    }

    public MainTabButton(Context context)
    {
        super(context);
    }

    public MainTabButton(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        initLayout(context, attrs);
    }

    private void initLayout(Context context, AttributeSet attrs)
    {
        View contentView = LayoutInflater.from(context).inflate(R.layout.tabhost_main_tab_btn_item, this);
        image = (ImageView) contentView.findViewById(R.id.iv_tab_item_icon);
        tab_button = (TextView) contentView.findViewById(R.id.tv_tab_item_icon);
        tab_tip = contentView.findViewById(R.id.tab_tip);
        unread_count = (TextView) contentView.findViewById(R.id.unread_count);

        TypedArray a = getResources().obtainAttributes(attrs, R.styleable.tab_button);
        Drawable d = a.getDrawable(R.styleable.tab_button_drawableTop);
        String text = a.getString(R.styleable.tab_button_tabtext);
        int drawable_w = a.getDimensionPixelSize(R.styleable.tab_button_imgWidth, -2);
        int drawable_h = a.getDimensionPixelSize(R.styleable.tab_button_imgHeight, -2);

        a.recycle();
        if (text == null || text.length() == 0)
        {
            tab_button.setVisibility(View.GONE);
        } else
        {
            tab_button.setText(text);
            tab_button.setVisibility(View.VISIBLE);
        }

        image.setImageDrawable(d);
        ViewGroup.LayoutParams image_lp = image.getLayoutParams();
        image_lp.width = drawable_w;
        image_lp.height = drawable_h;
        image.setLayoutParams(image_lp);

    }

    public void setHasNew(boolean hasNew)
    {
        if (tab_tip != null)
        {
            tab_tip.setVisibility(hasNew ? View.VISIBLE : View.GONE);
        }
    }

    public void setUnreadCount(long count)
    {
        // unread_count.setText(String.valueOf(mtn));
        unread_count.setText(StringUtil.formatUnreadNumber(count));
        unread_count.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setSelected(boolean selected)
    {
        super.setSelected(selected);
    }
}
