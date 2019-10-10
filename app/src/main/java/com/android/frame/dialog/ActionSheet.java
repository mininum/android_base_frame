package com.android.frame.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;


import com.android.frame.R;

import java.util.List;

public class ActionSheet extends Dialog implements View.OnClickListener, OnItemClickListener
{

    ListView mList;
    TextView mCancel;

    protected BaseDialog.OnActionSheetSelected mOnActionSheetListener;
    protected List<Object[]> titles;

    public ActionSheet(Context context)
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

    public ActionSheet(Context context, List<Object[]> titles, BaseDialog.OnActionSheetSelected mOnActionSheetListener)
    {
        this(context);
        this.titles = titles;
        this.mOnActionSheetListener = mOnActionSheetListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actionsheet);
        mList = (ListView) findViewById(R.id.listview);
        mList.setOnItemClickListener(this);
        mCancel = (TextView) findViewById(R.id.cancel);
        mCancel.setOnClickListener(this);
        SheetAdapter mAdapter = new SheetAdapter();
        mList.setAdapter(mAdapter);

    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
        case R.id.cancel :
            break;
        }
        this.dismiss();
    }

    class SheetAdapter extends BaseAdapter
    {

        @Override
        public int getCount()
        {
            return titles.size();
        }

        @Override
        public Object[] getItem(int position)
        {
            return titles.get(position);
        }

        @Override
        public long getItemId(int position)
        {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            if (convertView == null)
            {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.cell_actionsheet, null);
            }
            TextView tv = (TextView) convertView.findViewById(R.id.tv1);
            tv.setText(getItem(position)[0].toString());
            if (getItem(position).length >= 3)
            {
                tv.setTextColor(getContext().getResources().getColor((Integer) getItem(position)[2]));
            } else
            {
                tv.setTextColor(getContext().getResources().getColor(R.color.black));
            }
            View lineView = convertView.findViewById(R.id.line);
            lineView.setVisibility(position == getCount() - 1 ? View.GONE : View.VISIBLE);
            return convertView;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        this.dismiss();
        Object[] title = (Object[]) parent.getAdapter().getItem(position);
        if (mOnActionSheetListener != null)
        {
            mOnActionSheetListener.onClick(Integer.parseInt(title[1].toString()));
        }

    }

}
