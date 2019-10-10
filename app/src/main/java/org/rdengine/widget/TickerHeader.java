package org.rdengine.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.LinearLayout;


import com.android.frame.R;

import org.rdengine.adapter.RDBaseAdapter;
import org.rdengine.util.PhoneUtil;
import org.rdengine.widget.viewpager.RDViewPager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 通用轮播
 * 
 * @author CCCMAX
 */
public class TickerHeader extends LinearLayout implements OnItemClickListener, OnItemSelectedListener
{
    /** 容器 */
    View layout_container;

    private SlidingIndicator indicator;

    private CustomGallery gallery;
    private TickerAdapter mAdapter;

    private RDViewPager viewpager;
    private TickerPagerViewAdapter mPagerAdapter;

    public void autoLoad_layout_def_ticker()
    {
        gallery = (CustomGallery) findViewById(R.id.gallery);
        viewpager = (RDViewPager) findViewById(R.id.viewpager);
        indicator = (org.rdengine.widget.SlidingIndicator) findViewById(R.id.circle_pageindicator);
    }

    public TickerHeader(Context context)
    {
        this(context, null);
    }

    public TickerHeader(Context context, int layoutID)
    {
        super(context);
        init(layoutID);
    }

    public TickerHeader(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        int layoutID = R.layout.layout_def_ticker;
        if (attrs != null)
        {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.tiker);
            layoutID = a.getResourceId(R.styleable.tiker_tiker_layout_id, layoutID);
        }
        init(layoutID);
    }

    private void init(int layoutID)
    {
        layout_container = LayoutInflater.from(getContext()).inflate(layoutID, null);
        addView(layout_container, -1, -2);
        autoLoad_layout_def_ticker();

        if (gallery != null)
        {
            this.gallery.setOnItemSelectedListener(this);
            this.gallery.setOnItemClickListener(this);
        } else
        {
            viewpager.addOnPageChangeListener(onPageChangeListener);
        }

        layout_container.setVisibility(View.GONE);
    }

    private float ratio;
    private int height;

    /**
     * 设置画面显示比例 ratio = w / h
     * 
     * @param ratio
     */
    public void setTickerRatio(float ratio)
    {
        this.ratio = ratio;
        int w = PhoneUtil.getScreenWidth(getContext());
        height = (int) (w / ratio);
        if (gallery != null)
        {
            ViewGroup.LayoutParams lp = gallery.getLayoutParams();
            lp.height = height;
            gallery.setLayoutParams(lp);
        }
        if (viewpager != null)
        {
            ViewGroup.LayoutParams lp = viewpager.getLayoutParams();
            lp.height = height;
            viewpager.setLayoutParams(lp);
        }
    }

    public float getTickerRatio()
    {
        return ratio;
    }

    public int getTickerHeight()
    {
        return height;
    }

    TickerheaderHandle handle = null;

    public void setTickerheaderHandle(TickerheaderHandle h)
    {
        this.handle = h;
    }

    private List<Object> banners;

    /**
     * 没有数据的话 会隐藏其中的view
     * 
     * @param banners
     */
    public void setData(ArrayList banners)
    {
        this.banners = banners;

        if (this.banners == null || this.banners.size() <= 0)
        {
            stopLoop();
            layout_container.setVisibility(View.GONE);
            return;
        }

        if (gallery != null)
        {
            if (mAdapter == null)
            {
                mAdapter = new TickerAdapter();
                mAdapter.addAll(banners);
            } else
            {
                mAdapter.clearData();
                mAdapter.addAll(banners);
            }

            indicator.setCount(banners.size());
            indicator.onItemSelect(0);

            gallery.setAdapter(mAdapter);
            // if (gallery.getAdapter() == null)
            // {
            // gallery.setSelection(0);
            // mAdapter.notifyDataSetInvalidated();
            // } else
            // {
            // mAdapter.notifyDataSetInvalidated();
            // }

            int pos = 0;
            if (banners.size() > 1)
            {

                pos = Integer.MAX_VALUE / 2;
                while (pos % banners.size() != 0)
                {
                    pos++;
                }
                this.indicator.setVisibility(View.VISIBLE);
            } else
            {
                this.indicator.setVisibility(View.INVISIBLE);
            }
            gallery.setSelection(pos);
            if (banners.size() != 0)
                this.indicator.onItemSelect(pos % banners.size());
        } else
        {
            // viewpager
            mPagerAdapter = new TickerPagerViewAdapter(banners);
            viewpager.setAdapter(mPagerAdapter);

            indicator.setCount(banners.size());
            indicator.onItemSelect(0);
        }

        layout_container.setVisibility(View.VISIBLE);
        startLoop();
    }

    @Override
    public boolean hasFocusable()
    {
        return false;
    }

    public void startLoop()
    {
        if (layout_container.getVisibility() != View.VISIBLE || banners == null || banners.size() == 0)
            return;

        if (gallery != null)
        {
            gallery.startLoop();
        } else
        {
            // TODO viewpager
        }
    }

    public void stopLoop()
    {
        if (gallery != null)
        {
            gallery.removeLoop();
        } else
        {
            // TODO viewpager
        }
    }

    public class TickerAdapter extends RDBaseAdapter<Object>
    {

        @Override
        public int getCount()
        {
            if (getData() == null || getData().size() == 0)
            {
                return 0;
            }
            if (this.getData().size() == 1)
            {
                return 1;
            }
            return Integer.MAX_VALUE;
        }

        @Override
        public Object getItem(int position)
        {
            return getData().get(position % getData().size());
        }

        @Override
        public long getItemId(int position)
        {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            if (handle != null)
            {
                return handle.getView(this, position, convertView, parent);
            }
            return convertView;
        }

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        try
        {
            if (banners != null && banners.size() > 0)
            {
                if (position == 0 && banners.size() > 1)
                {
                    int pos = Integer.MAX_VALUE / 2;
                    while (pos % banners.size() != 0)
                    {
                        pos++;
                    }
                    gallery.setSelection(pos);
                }

                int p = position % banners.size();
                this.indicator.onItemSelect(p);
            }
        } catch (Exception e)
        {
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        Object banner = mAdapter.getItem(position);
        if (handle != null)
        {
            handle.onItemClick(banner, view);
        }
    }

    public static interface TickerheaderHandle
    {
        // 创建view
        public View getView(RDBaseAdapter adapter, int position, View convertView, ViewGroup parent);

        public View getView(TickerPagerViewAdapter adapter, int position, View convertView, ViewGroup parent);

        // item点击
        public void onItemClick(Object dataobj, View view);
    }

    public class TickerPagerViewAdapter extends PagerAdapter
    {
        protected List<Object> params;
        protected HashMap<Integer, View> views = new HashMap<Integer, View>();

        public TickerPagerViewAdapter(List<Object> p)
        {
            params = p;
        }

        public int getCount()
        {
            if (params == null)
            {
                return 0;
            }
            return this.params.size();
        }

        public Object getItem(int position)
        {
            try
            {
                return params.get(position);
            } catch (Exception e)
            {
            }
            return null;
        }

        public boolean isViewFromObject(View arg0, Object arg1)
        {
            return arg0 == arg1;
        }

        public Object instantiateItem(ViewGroup container, int position)
        {
            Object pp = params.get(position);
            View view = null;
            if (views.containsKey(position))
            {
                view = views.get(position);
            } else
            {
                if (handle != null)
                {
                    view = handle.getView(this, position, null, viewpager);
                }

                views.put(position, view);
            }

            if (container.indexOfChild(view) == -1)
            {
                container.addView(view);
            }
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object)
        {
            // super.destroyItem(container, position, object);
        }
    }

    OnPageChangeListener onPageChangeListener = new OnPageChangeListener()
    {

        public void onPageSelected(int position)
        {
            // if (position == 0 && banners.size() > 1)
            // {
            // int pos = Integer.MAX_VALUE / 2;
            // while (pos % banners.size() != 0)
            // {
            // pos++;
            // }
            // gallery.setSelection(pos);
            // }
            //
            // int p = position % banners.size();
            // indicator.onItemSelect(p);
            indicator.onItemSelect(position);
        }

        public void onPageScrolled(int paramInt1, float paramFloat, int paramInt2)
        {

        }

        public void onPageScrollStateChanged(int paramInt)
        {

        }
    };
}
