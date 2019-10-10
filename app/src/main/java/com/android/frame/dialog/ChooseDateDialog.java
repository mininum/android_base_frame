package com.android.frame.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;


import com.android.frame.R;

import org.rdengine.log.DLOG;
import org.rdengine.util.TimeUtil;
import org.rdengine.widget.wheel.OnWheelScrollListener;
import org.rdengine.widget.wheel.WheelView;
import org.rdengine.widget.wheel.adapter.AbstractWheelTextAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/** 选择年月日 滚轮 */
public class ChooseDateDialog extends Dialog implements View.OnClickListener
{

    // ----------------R.layout.dialog_choose_date-------------Start
    private TextView btn_cancel;
    private TextView btn_ok;
    private WheelView wv_year;
    private WheelView wv_month;
    private WheelView wv_day;

    /** auto load R.layout.dialog_choose_date */
    private void autoLoad_dialog_choose_date()
    {
        btn_cancel = (TextView) findViewById(R.id.btn_cancel);
        btn_ok = (TextView) findViewById(R.id.btn_ok);
        wv_year = (WheelView) findViewById(R.id.wv_year);
        wv_month = (WheelView) findViewById(R.id.wv_month);
        wv_day = (WheelView) findViewById(R.id.wv_day);
    }
    // ----------------R.layout.dialog_choose_date-------------End

    public ChooseDateDialog(Context context)
    {
        super(context, R.style.ActionSheet);
        setOwnerActivity((Activity) context);
        WindowManager mWm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        mWm.getDefaultDisplay().getMetrics(dm);
        Window w = getWindow();
        w.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams lp = w.getAttributes();
        lp.gravity = Gravity.BOTTOM;
        lp.width = dm.widthPixels;
        w.setAttributes(lp);
    }

    /** 当前年 之前多少年 */
    int YEAR_MAX_BEFORE = 100;
    /** 当前年 之后多少年 */
    int YEAR_MAX_AFTER = 0;

    /** 适配器 */
    ItemAdapter adapter_year, adapter_month, adapter_day;

    ArrayList<String> data_year = new ArrayList<String>();
    ArrayList<String> data_month = new ArrayList<String>();
    ArrayList<String> data_day = new ArrayList<String>();

    /** 选中的日历 */
    private Calendar selectCal = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_choose_date);
        autoLoad_dialog_choose_date();

        btn_cancel.setOnClickListener(this);
        btn_ok.setOnClickListener(this);

        adapter_year = new ItemAdapter(getContext());
        adapter_month = new ItemAdapter(getContext());
        adapter_day = new ItemAdapter(getContext());

        wv_year.setViewAdapter(adapter_year);
        wv_month.setViewAdapter(adapter_month);
        wv_day.setViewAdapter(adapter_day);

        ArrayList<WheelView> wvlist = new ArrayList<>();
        wvlist.add(wv_year);
        wvlist.add(wv_month);
        wvlist.add(wv_day);
        for (WheelView wv : wvlist)
        {
            wv.setVisibleItems(5);// 显示5个
            wv.setCurrentItem(0);
            wv.setItemAlphaMin(0.2F);// 最远位置透明度
            wv.setItemScaleMin(0.7F);// 最远位置的缩放
            wv.setItemIndentMax(0.3F); // 最远位置的缩进比例
            wv.setItemRotationXMax(45F);// 最远位置的透视倾斜角度 X轴
            wv.setWheelBackground(R.color.full_transparent);
            wv.setWheelForeground(R.color.full_transparent);
            wv.setShadowColor(0x00000000, 0x00000000, 0x00000000);
        }

        // 当前年 前后5年
        wv_year.addScrollingListener(new OnWheelScrollListener()
        {
            public void onScrollingStarted(WheelView wheel)
            {}

            public void onScrollingFinished(WheelView wheel)
            {
                int index = wv_year.getCurrentItem();
                selectCal.set(Calendar.YEAR, Integer.parseInt(data_year.get(index)));
                // makeYear();
                makeDay(selectCal.getTime());
                test();
            }
        });

        // 月 1-12
        wv_month.addScrollingListener(new OnWheelScrollListener()
        {
            public void onScrollingStarted(WheelView wheel)
            {}

            public void onScrollingFinished(WheelView wheel)
            {
                int index = wv_month.getCurrentItem();

                // 已选中的日期
                int select_day = selectCal.get(Calendar.DAY_OF_MONTH);

                selectCal.set(Calendar.DAY_OF_MONTH, 1);
                selectCal.set(Calendar.MONTH, index);
                makeDay(selectCal.getTime());

                int maxDay = data_day.size();// 选中月的最大日期
                if (select_day <= maxDay)
                {
                    selectCal.set(Calendar.DAY_OF_MONTH, select_day);
                    wv_day.setCurrentItem(select_day - 1);
                } else
                {
                    wv_day.setCurrentItem(0);
                }
                test();
            }
        });

        // 日
        wv_day.addScrollingListener(new OnWheelScrollListener()
        {
            public void onScrollingStarted(WheelView wheel)
            {}

            public void onScrollingFinished(WheelView wheel)
            {
                int index = wv_day.getCurrentItem();
                selectCal.set(Calendar.DAY_OF_MONTH, index + 1);// 1-31
                test();
            }
        });

        {
            // 月 英文字母
            // String[] months = DateFormatSymbols.getInstance(new Locale("en")).getShortMonths();
            // for (String m : months)
            // data_month.add(m);
            data_month.add("01");
            data_month.add("02");
            data_month.add("03");
            data_month.add("04");
            data_month.add("05");
            data_month.add("06");
            data_month.add("07");
            data_month.add("08");
            data_month.add("09");
            data_month.add("10");
            data_month.add("11");
            data_month.add("12");
        }

        init(selectCal.getTime());

    }

    public void makeYear()
    {
        // 年
        int currentYear = TimeUtil.getYearOfDate(new Date());
        data_year = new ArrayList<String>();

        int select_year = selectCal.get(Calendar.YEAR);// 已选时间的年

        // 年的可选范围
        int start = currentYear - YEAR_MAX_BEFORE;
        int end = currentYear + YEAR_MAX_AFTER;

        // 如果已选年不在范围内 则容错 增加范围到已选年
        if (select_year < start)
            start = select_year;
        if (end < select_year)
            end = select_year;

        for (int i = start; i <= end; i++)
        {
            data_year.add(String.valueOf(i));
        }

        adapter_year.updata(data_year);
    }

    public void makeDay(Date date)
    {
        // 日
        data_day = new ArrayList<String>();
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.setTime(date); // 默认时间
        int dayMax = cal.getActualMaximum(Calendar.DAY_OF_MONTH);// 指定年的某月的最多天数
        for (int i = 1; i <= dayMax; i++)
        {
            data_day.add(String.valueOf(i));
        }
        adapter_day.updata(data_day);
    }

    private boolean hasInit = false;

    private void init(Date date)
    {
        hasInit = true;

        // 关联数据对象
        adapter_month.updata(data_month);

        selectCal.setTime(date);

        makeYear();
        makeDay(date);

        wv_month.setCyclic(true);// 循环
        wv_day.setCyclic(true);// 循环

        int y = selectCal.get(Calendar.YEAR);
        int mo = selectCal.get(Calendar.MONTH);
        int d = selectCal.get(Calendar.DAY_OF_MONTH);
        int h = selectCal.get(Calendar.HOUR);
        int mi = selectCal.get(Calendar.MINUTE);
        int ampm = selectCal.get(Calendar.AM_PM);

        int y_index = data_year.indexOf(String.valueOf(y));
        wv_year.setCurrentItem(y_index);
        wv_month.setCurrentItem(mo);
        wv_day.setCurrentItem(d - 1);

    }

    /** 设置时间 毫秒 */
    public void setChoosedDate(long date)
    {
        try
        {
            if (hasInit)
                init(new Date(date));
            else selectCal.setTime(new Date(date));
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private void test()
    {
        DLOG.d("ChooseDateDialog", "date=" + TimeUtil.formatPublishTime(selectCal.getTimeInMillis()));
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
        case R.id.btn_cancel :
        {
            // 取消
            dismiss();
        }
            break;
        case R.id.btn_ok :
        {
            // 确定
            try
            {
                long time = selectCal.getTime().getTime();
                if (choseListener != null)
                    choseListener.onChose(time);
            } catch (Exception ex)
            {
                ex.printStackTrace();
            } finally
            {
                dismiss();
            }

        }
            break;
        }
    }

    private class ItemAdapter extends AbstractWheelTextAdapter
    {

        private List<String> list = new ArrayList<String>();

        public void updata(List<String> list)
        {
            this.list = list;
            notifyDataInvalidatedEvent();
            // notifyDataChangedEvent();
        }

        protected ItemAdapter(Context context)
        {
            super(context, R.layout.cell_choose_date, NO_RESOURCE);
        }

        @Override
        public View getItem(int index, View cachedView, ViewGroup parent)
        {
            View view = super.getItem(index, cachedView, parent);
            // TextView text = (TextView) view.findViewById(R.id.text_item_content);
            TextView text = (TextView) view;// R.id.tv_item
            text.setText(list.get(index));
            // UiUtil.setTypefaceNumber(text);// 换字体
            return view;
        }

        @Override
        public int getItemsCount()
        {
            return list.size();
        }

        @Override
        protected CharSequence getItemText(int index)
        {
            return list.get(index);
        }
    }

    public interface OnDateChoseListener
    {
        void onChose(long time);
    }

    OnDateChoseListener choseListener = null;

    public ChooseDateDialog setOnDateChoseListener(OnDateChoseListener choseListener)
    {
        this.choseListener = choseListener;
        return this;
    }
}
