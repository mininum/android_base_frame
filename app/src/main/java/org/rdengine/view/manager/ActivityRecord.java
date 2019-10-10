package org.rdengine.view.manager;

import android.app.Activity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * 记录除了MainActivity以外的其他Activity
 * 
 * @author CCCMAX
 */
public class ActivityRecord
{
    private Stack<WeakReference<BaseActivity>> mStack = new Stack<WeakReference<BaseActivity>>();

    private static ActivityRecord mSelf;

    public ActivityRecord()
    {
        mStack.clear();
    }

    public static ActivityRecord ins()
    {
        if (mSelf == null)
        {
            mSelf = new ActivityRecord();
        }
        return mSelf;
    }

    public void push(BaseActivity activity)
    {
        mStack.push(new WeakReference<BaseActivity>(activity));
    }

    public void remove(BaseActivity activity)
    {
        List<WeakReference<BaseActivity>> dels = new ArrayList<WeakReference<BaseActivity>>();
        for (WeakReference<BaseActivity> fra : mStack)
        {

            if (fra.get() == null || (fra.get() != null && fra.get() == activity))
            {
                dels.add(fra);
            }
        }
        mStack.removeAll(dels);
    }

    public int getCount()
    {
        return mStack.size();
    }

    public Stack<WeakReference<BaseActivity>> getStack()
    {
        return mStack;
    }

    /**
     * 结束所有Activity
     */
    public void finishAllActivitys()
    {
        try
        {
            for (WeakReference<BaseActivity> frag : mStack)
            {
                if (frag.get() != null)
                {
                    Activity activity = frag.get();
                    if (activity != null)
                    {
                        activity.finish();
                    }
                }
            }
            mStack.clear();
        } catch (Exception e)
        {
        }
    }
}
