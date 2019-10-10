package org.rdengine.widget.viewpager;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import org.rdengine.log.DLOG;
import org.rdengine.view.manager.BaseView;
import org.rdengine.view.manager.ViewManager;
import org.rdengine.view.manager.ViewParam;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RDPagerAdapter extends PagerAdapter
{
    protected List<? extends PagerParam> params;
    protected HashMap<Integer, BaseView> views = new HashMap<Integer, BaseView>();

    boolean preRefreshNext;
    protected int defaultIndex = 0;

    protected int lastindex = -1;
    protected List<String> titles;

    public RDPagerAdapter(List<PagerParam> params)
    {
        this.params = params;
    }

    public RDPagerAdapter(List<PagerParam> params, boolean preRefreshNext)
    {
        this.params = params;
        this.preRefreshNext = preRefreshNext;
    }

    public RDPagerAdapter(List<? extends PagerParam> params, List<String> titles, boolean preRefreshNext)
    {
        this.params = params;
        this.titles = titles;
        this.preRefreshNext = preRefreshNext;
    }

    public int getDefaultIndex()
    {
        return defaultIndex;
    }

    public CharSequence getPageTitle(int position)
    {
        if (titles != null && titles.size() > 0)
        {

            return titles.get(position);
        }
        return "";
    }

    public void setDefaultIndex(int defaultIndex)
    {
        this.defaultIndex = defaultIndex;
    }

    @Override
    public int getCount()
    {
        if (params == null)
        {
            return 0;
        }
        return this.params.size();
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1)
    {
        return arg0 == arg1;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position)
    {
        PagerParam pp = params.get(position);
        BaseView view = null;
        if (views.containsKey(position))
        {
            view = views.get(position);
        } else
        {
            view = ViewManager.createView(pp.clazz, pp.param, container.getContext());

            if (position == defaultIndex)
            {
                view.refresh();
            } else if (preRefreshNext)
            {
                view.refresh();
            }
            views.put(position, view);
        }

        if (container.indexOfChild(view) == -1)
        {
            container.addView(view);
        }
        // if (container instanceof JazzyViewPager)// 页面滑动动画 必须要做的步骤
        // ((JazzyViewPager) container).setObjectForPosition(view, position);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object)
    {
        // super.destroyItem(container, position, object);
    }

    public BaseView getItem(int position)
    {
        if (views != null)
        {
            return views.get(position);
        }
        return null;
    }

    public void onPageSelected(int position)
    {
        DLOG.d("RDPagerAdapter", "onPageSelected = " + position);
        lastindex = position;
        final BaseView currentView = getItem(position);
        BaseView view;
        for (int i = 0; i < getCount(); i++)
        {
            view = getItem(i);
            if (view != null && view != currentView)
            {
                view.onHide();
                view.onReleaseResource();
            }
        }
        if (currentView != null)
        {
            currentView.onShow(); // MFViewPager 在滑动的时候 已经调用过onshow
            if (!currentView.hasRefresh())
            {
                currentView.postDelayed(new Runnable()
                {
                    public void run()
                    {
                        currentView.refresh();
                    }
                }, 250);
            }
        }
    }

    public void onPause()
    {
        if (views != null && views.size() > 0)
        {
            Set<Map.Entry<Integer, BaseView>> set = views.entrySet();
            for (Iterator<Map.Entry<Integer, BaseView>> it = set.iterator(); it.hasNext();)
            {
                Map.Entry<Integer, BaseView> entry = (Map.Entry<Integer, BaseView>) it.next();
                BaseView view = entry.getValue();
                if (view != null)
                {
                    view.onHide();
                    view.onReleaseResource();
                }
            }
        }
    }

    public void onResume()
    {
        if (lastindex < 0)
            lastindex = defaultIndex;

        if (views != null && views.size() > 0)
        {
            Set<Map.Entry<Integer, BaseView>> set = views.entrySet();
            for (Iterator<Map.Entry<Integer, BaseView>> it = set.iterator(); it.hasNext();)
            {
                Map.Entry<Integer, BaseView> entry = (Map.Entry<Integer, BaseView>) it.next();
                if (entry.getKey() == lastindex)
                {
                    BaseView view = entry.getValue();
                    if (view != null)
                    {
                        view.onLoadResource();
                        view.onShow();
                    }
                    break;
                }
            }
        }
    }

    public static class PagerParam
    {
        public Class<? extends BaseView> clazz;
        public ViewParam param;

        public PagerParam(Class<? extends BaseView> clazz)
        {
            this.clazz = clazz;
        }

        public PagerParam(Class<? extends BaseView> clazz, ViewParam param)
        {
            this.clazz = clazz;
            this.param = param;
        }

    }

    private BaseView mParentView;

    public BaseView getParentView()
    {
        return mParentView;
    }

    public void setParentView(BaseView parentView)
    {
        this.mParentView = parentView;
    }

}