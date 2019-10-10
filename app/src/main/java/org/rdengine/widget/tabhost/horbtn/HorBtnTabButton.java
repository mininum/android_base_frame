package org.rdengine.widget.tabhost.horbtn;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.frame.R;

import org.rdengine.util.PhoneUtil;
import org.rdengine.util.StringUtil;

public class HorBtnTabButton extends RelativeLayout
{

    private TextView tab_button;
    private TextView unread_count;
    private View bottom_line;
    private boolean isChecked = false;

    public HorBtnTabButton(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        initLayout(context, attrs);
    }

    public HorBtnTabButton(Context context)
    {
        super(context);
    }

    public HorBtnTabButton(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        initLayout(context, attrs);
    }

    MarginLayoutParams mlp;
    int padding_n, padding_s;

    private void initLayout(Context context, AttributeSet attrs)
    {
        View contentView = LayoutInflater.from(context).inflate(R.layout.horbtn_tab_btn_item, this);
        tab_button = (TextView) contentView.findViewById(R.id.title);
        unread_count = (TextView) contentView.findViewById(R.id.unread_count);
        bottom_line = contentView.findViewById(R.id.bottom_line);

        TypedArray a = getResources().obtainAttributes(attrs, R.styleable.tab_button);
        String text = a.getString(R.styleable.tab_button_tabtext);

        a.recycle();
        if (text == null || text.length() == 0)
        {
            tab_button.setVisibility(View.GONE);
        } else
        {
            tab_button.setText(text);
            tab_button.setVisibility(View.VISIBLE);
        }

        mlp = (MarginLayoutParams) bottom_line.getLayoutParams();

        padding_n = PhoneUtil.dipToPixel(4, getContext());
        padding_s = (int) (padding_n * 0.75F);
    }

    public void setUnreadCount(long count)
    {
        // unread_count.setText(String.valueOf(mtn));
        // unread_count.setText("(" + StringUtil.formatUnreadNumber(count) + ")");
        unread_count.setText("(" + StringUtil.formatBigNumber(count, 0) + ")");
        unread_count.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setSelected(boolean selected)
    {
        super.setSelected(selected);
        if (selected)
        {
            // 选中
            // tab_button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
            // unread_count.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
            bottom_line.setVisibility(View.VISIBLE);
            mlp.setMargins(0, padding_n, 0, 0);
        } else
        {
            // 释放
            // tab_button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
            // unread_count.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
            bottom_line.setVisibility(View.INVISIBLE);
            mlp.setMargins(0, padding_s, 0, 0);
        }
    }
}
