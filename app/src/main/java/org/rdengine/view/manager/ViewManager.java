package org.rdengine.view.manager;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 * View栈管理
 * 
 * @author yangyu
 */
public class ViewManager
{
    private Context mContext;
    private ViewGroup mContainer;
    private Stack<BaseView> mViewStack;

    private ViewManager(Context context)
    {
        if (!(context instanceof ViewController))
        {
            throw new Error("context is not a Activity implements ViewController");
        }
        mContext = context;
        mViewStack = new Stack<BaseView>();
        mViewStack.clear();
    }

    public ViewManager(Context context, ViewGroup container)
    {
        this(context);
        mContainer = container;
    }

    /**
     * 插到栈顶
     * 
     * @param view
     */
    public void addViewToTop(BaseView view)
    {
        if (!mViewStack.empty())
        {
            BaseView currentView = mViewStack.peek();
            currentView.onHide();
            currentView.setVisibility(View.GONE);
        }
        mViewStack.push(view);
        mContainer.addView(view);
        view.setVisibility(View.VISIBLE);
        view.onLoadResource();
        view.onShow();
        view.setStatusBarDarkTheme();

        if (mViewStack.size() > 2)
        {
            mViewStack.get(mViewStack.size() - 2).onReleaseResource();
        }
    }

    /**
     * 显示新View
     * 
     * @param clazz
     * @param param
     */
    public void showView(Class<? extends BaseView> clazz, ViewParam param)
    {
        BaseView view = createView(clazz, param, mContext);
        addViewToTop(view);
        view.refresh();
    }

    /**
     * 自然返回
     * 
     * @return
     */
    public boolean backView()
    {
        // 第一个页面 需要接收返回事件, 可以在类名上加入@FirstBaseViewNeedBackEvent注解
        if (mViewStack.size() == 1
                && mViewStack.peek().getClass().isAnnotationPresent(FirstBaseViewNeedBackEvent.class))
        {
            return mViewStack.peek().onBack();

        } else if (mViewStack.size() > 1)
        {
            BaseView currentView = mViewStack.peek();
            if (currentView.onBack())
            {
                // if (currentView instanceof SwipeBackView)
                // {
                // ((SwipeBackView) currentView).dismiss();
                // } else
                {
                    currentView = mViewStack.pop();
                    mContainer.removeView(currentView);
                    currentView.onHide();
                    currentView.onReleaseResource();

                    BaseView showView = mViewStack.peek();
                    if (!showView.getShown())
                    {
                        showView.onLoadResource();
                        showView.onShow();
                    }
                    showView.setVisibility(View.VISIBLE);
                    showView.setStatusBarDarkTheme();
                }
            }

            return false;
        } else
        {
            return true;
        }
    }

    // /** 滑动返回的view 结束了滑动 开始销毁 */
    // public void swipeviewOnDismiss(SwipeBackView sbv)
    // {
    // if (sbv != null)
    // {
    // try
    // {
    // // mContainer.removeView(sbv);
    // // sbv.onHide();
    // // sbv.onReleaseResource();
    //
    // BaseView currentView = mViewStack.pop();
    // mContainer.removeView(currentView);
    // currentView.onHide();
    // currentView.onReleaseResource();
    // currentView.closeInputMethod();
    //
    // BaseView showView = mViewStack.peek();
    // if (!showView.getShown())
    // {
    // showView.onLoadResource();
    // showView.onShow();
    // }
    // showView.setVisibility(View.VISIBLE);
    // if (showView instanceof SwipeBackView && showView.getWidth() > 0)// 校正位置
    // {
    // ((SwipeBackView) showView).scrollTo(showView.getWidth(), 0);
    // }
    // } catch (Exception e)
    // {
    // e.printStackTrace();
    // }
    // }
    // }

    public void killViewAt(int index)
    {
        if (mViewStack != null && mViewStack.size() > 0 && index >= 0 && index < mViewStack.size())
        {
            BaseView view = mViewStack.get(index);
            mViewStack.remove(index);
            mContainer.removeView(view);
        }
    }

    public int getViewSize()
    {
        return mViewStack.size();
    }

    public int getViewIndex(BaseView baseview)
    {
        return mViewStack.indexOf(baseview);
    }

    public void killAllHistoryView()
    {
        if (mViewStack.size() <= 1)
            return;

        // 方法一
        // BaseView currentView = mViewStack.peek();
        // mViewStack.clear();
        // mContainer.removeAllViews();
        // mViewStack.push(currentView);
        // mContainer.addView(currentView);

        // 方法二
        BaseView currentView = mViewStack.peek();
        mViewStack.clear();
        mViewStack.push(currentView);

        ArrayList<BaseView> delviewlist = new ArrayList<>();
        for (int i = 0; i < mContainer.getChildCount(); i++)
        {
            BaseView bv = (BaseView) mContainer.getChildAt(i);
            if (bv != currentView)
                delviewlist.add(bv);
        }
        for (BaseView bv : delviewlist)
        {
            mContainer.removeView(bv);
        }
    }

    public BaseView getViewAt(int index)
    {
        BaseView view = mViewStack.get(index);
        return view;
    }

    public BaseView getTopView()
    {
        try
        {
            return mViewStack.peek();
        } catch (Exception e)
        {
            return null;
        }
    }

    public void killAllSameView(BaseView view)
    {
        BaseView v = null;
        for (int i = 0; i < mViewStack.size(); i++)
        {
            v = mViewStack.get(i);

            if (view.getClass().getName().equals(v.getClass().getName()))
            {
                if (v != view)
                {
                    try
                    {
                        mViewStack.remove(i);
                        mContainer.removeView(v);
                        v.onHide();
                        v.onReleaseResource();
                    } catch (Exception e)
                    {
                    }
                    i--;
                }
            }
        }
    }

    /**
     * 干掉某个相同的class的view，但是保留最近的几个
     *
     * @param view
     * @param keepCount
     *            保留最近的几个相同view
     */
    public void killSameViewAndKeepSome(BaseView view, int keepCount)
    {
        BaseView v = null;
        ArrayList<BaseView> needRemoveList = new ArrayList<BaseView>();

        // 倒序从最近的开始遍历
        for (int i = mViewStack.size() - 1; i >= 0; i--)
        {
            v = mViewStack.get(i);

            if (view.getClass().getName().equals(v.getClass().getName()))
            {
                // 同名类
                if (v != view)
                {
                    if (keepCount <= 0)
                    {
                        // 没有要保留的了 就正常清理
                        needRemoveList.add(v);
                        // try
                        // {
                        // mViewStack.remove(i);
                        // mContainer.removeView(v);
                        // v.onHide();
                        // v.onReleaseResource();
                        // } catch (Exception e)
                        // {
                        // }
                        // i++;
                    } else
                    {
                        // 保留一个
                        keepCount--;
                    }
                }
            }
        }

        if (needRemoveList != null && needRemoveList.size() > 0)
        {
            for (BaseView bv : needRemoveList)
            {
                try
                {
                    mViewStack.remove(bv);
                    mContainer.removeView(bv);
                    bv.onHide();
                    bv.onReleaseResource();
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    public List<BaseView> findClazzViews(Class<? extends BaseView> clazz)
    {
        LinkedList<BaseView> retlist = null;
        BaseView v = null;
        for (int i = 0; i < mViewStack.size(); i++)
        {
            v = mViewStack.get(i);
            if (clazz.getName().equals(v.getClass().getName()))
            {
                if (retlist == null)
                    retlist = new LinkedList<BaseView>();
                retlist.add(v);
            }
        }
        return retlist;
    }

    /**
     * 创建BaseView
     * 
     * @param clazz
     * @param param
     * @return
     */
    public static <T extends BaseView> T createView(Class<T> clazz, ViewParam param, Context context)
    {
        BaseView view = null;
        if (view == null)
        {
            Constructor constructor = null;
            if (param == null)
            {
                param = new ViewParam();
            }

            try
            {
                constructor = clazz.getDeclaredConstructor(Context.class, ViewParam.class);
                constructor.setAccessible(true);
                view = (BaseView) constructor.newInstance(context, param);
                view.mViewParam = param;
            } catch (Exception ex)
            {
                ex.printStackTrace();
                throw new Error(ex);
            }

            view.init();

        }
        return (T) view;
    }

    /**
     * 将一个view移动到最顶层
     * 
     * @param view
     */
    public void moveToTop(BaseView view)
    {
        if (mViewStack != null && mViewStack.size() > 1 && view != null)
        {
            mViewStack.remove(view);

            BaseView currentView = mViewStack.peek();
            currentView.onHide();
            currentView.setVisibility(View.GONE);

            mViewStack.push(view);
            boolean hasview = false;
            for (int i = 0; i < mContainer.getChildCount(); i++)
            {
                View child = mContainer.getChildAt(i);
                if (view.equals(child))
                {
                    hasview = true;
                    break;
                }
            }
            if (hasview)
            {
                view.bringToFront();
            } else
            {
                mContainer.addView(view);
            }
            view.setVisibility(View.VISIBLE);
            view.onLoadResource();
            view.onShow();
            view.setStatusBarDarkTheme();

            if (mViewStack.size() > 2)
            {
                mViewStack.get(mViewStack.size() - 2).onReleaseResource();
            }
        }
    }

    public void moveToBottom(BaseView view)
    {
        if (mViewStack != null && mViewStack.size() > 1 && view != null)
        {
            mViewStack.remove(view);
            mViewStack.insertElementAt(view, 0);
            view.onHide();
            view.setVisibility(View.GONE);

            BaseView currentView = mViewStack.peek();
            currentView.setVisibility(View.VISIBLE);
            currentView.onLoadResource();
            currentView.onShow();
            currentView.bringToFront();
            currentView.setStatusBarDarkTheme();

            if (mViewStack.size() > 2)
            {
                mViewStack.get(mViewStack.size() - 2).onReleaseResource();
            }
        }
    }

}
