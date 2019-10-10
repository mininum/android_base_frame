package org.rdengine.view.manager;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Activity里的View容器
 * 
 * @author yangyu
 */
public class ActivityContainer extends BaseView
{

    public FrameLayout viewStackContainer;
    public FrameLayout maskContainer;

    public ActivityContainer(Context context, ViewParam param)
    {
        super(context, param);
        // view栈容器
        viewStackContainer = new FrameLayout(getContext());
        addView(viewStackContainer, -1, -1);
        // 顶层遮罩容器
        maskContainer = new FrameLayout(getContext());
        maskContainer.setVisibility(View.GONE);
        addView(maskContainer, -1, -1);
    }

    @Override
    public String getTag()
    {
        return null;
    }

    @Override
    public void init()
    {

    }

    public void hideMask()
    {
        maskContainer.setVisibility(View.GONE);
    }

    public void showMask()
    {
        if (maskContainer.getChildCount() > 0)
            maskContainer.setVisibility(View.VISIBLE);
    }

    public View findViewInMask(Class clazz)
    {
        if (maskContainer != null && maskContainer.getChildCount() > 0)
        {
            for (int i = 0; i < maskContainer.getChildCount(); i++)
            {
                View v = maskContainer.getChildAt(i);

                if (clazz.getName().equals(v.getClass().getName()))
                {
                    return v;
                }
            }
        }
        return null;
    }

    public void delViewInMask(Class clazz)
    {
        if (maskContainer != null && maskContainer.getChildCount() > 0)
        {
            for (int i = 0; i < maskContainer.getChildCount(); i++)
            {
                View v = maskContainer.getChildAt(i);

                if (clazz.getName().equals(v.getClass().getName()))
                {
                    try
                    {
                        maskContainer.removeView(v);
                        if (v instanceof BaseView)
                        {
                            ((BaseView) v).onHide();
                            ((BaseView) v).onReleaseResource();
                        }
                    } catch (Exception e)
                    {
                    }
                    i--;
                }
            }
        }

        if (maskContainer != null && maskContainer.getChildCount() == 0)
            maskContainer.setVisibility(View.GONE);
    }

    public BaseView addBaseViewToMask(Class<? extends BaseView> clazz, ViewParam vp)
    {
        try
        {
            BaseView view = ViewManager.createView(clazz, vp, getContext());
            maskContainer.addView(view);
            view.setVisibility(View.VISIBLE);
            view.onLoadResource();
            view.onShow();
            view.refresh();
            maskContainer.setVisibility(View.VISIBLE);
            return view;
        } catch (Exception ex)
        {
        }
        return null;
    }

    /**
     * @return true，mask层不处理back事件
     */
    public boolean backInMask()
    {
        if (maskContainer.getVisibility() == View.VISIBLE && maskContainer.getChildCount() > 0)
        {
            BaseView last = null;
            for (int i = maskContainer.getChildCount() - 1; i >= 0; i--)
            {
                View v = maskContainer.getChildAt(i);
                if (v instanceof BaseView && v.getVisibility() == View.VISIBLE)
                {
                    last = (BaseView) v;
                    break;
                }
            }
            if (last != null)
            {
                delViewInMask(last.getClass());
                return false;
            }
        }
        return true;
    }

}
