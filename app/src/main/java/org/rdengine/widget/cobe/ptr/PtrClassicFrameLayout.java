package org.rdengine.widget.cobe.ptr;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public class PtrClassicFrameLayout extends PtrFrameLayout
{

    private PtrClassicDefaultHeader mPtrClassicHeader;
    private PtrClassicAppNameHeader mPtrClassicHeader_appname;
    private PtrClassicRotateMoveHeader mPtrClassicHeader_rotatemove;
    private PtrUIHandler ptruihandler;

    public PtrClassicFrameLayout(Context context)
    {
        super(context);
        initViews();
    }

    public PtrClassicFrameLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        initViews();
    }

    public PtrClassicFrameLayout(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        initViews();
    }

    private void initViews()
    {
        // mPtrClassicHeader = new PtrClassicDefaultHeader(getContext());
        // mPtrClassicHeader_appname = new PtrClassicAppNameHeader(getContext());
        mPtrClassicHeader_rotatemove = new PtrClassicRotateMoveHeader(getContext());
        setHeaderView(mPtrClassicHeader_rotatemove);
        addPtrUIHandler(mPtrClassicHeader_rotatemove);
    }

    public void changeHeaderDefault()
    {
        mPtrClassicHeader = new PtrClassicDefaultHeader(getContext());
        setHeaderView(mPtrClassicHeader);
        addPtrUIHandler(mPtrClassicHeader);
        if (mPtrClassicHeader_appname != null)
        {
            removePtrUIHandler(mPtrClassicHeader_appname);
            mPtrClassicHeader_appname = null;
        }
        if (mPtrClassicHeader_rotatemove != null)
        {
            removePtrUIHandler(mPtrClassicHeader_rotatemove);
            mPtrClassicHeader_rotatemove = null;
        }
    }

    public View getHeader()
    {
        if (mPtrClassicHeader_rotatemove != null)
            return mPtrClassicHeader_rotatemove;
        if (mPtrClassicHeader_appname != null)
            return mPtrClassicHeader_appname;
        if (mPtrClassicHeader != null)
            return mPtrClassicHeader;
        return (View) ptruihandler;
    }

    /**
     * Specify the last update time by this key string
     * 
     * @param key
     */
    public void setLastUpdateTimeKey(String key)
    {
        if (mPtrClassicHeader_rotatemove != null)
        {
            // 无此功能
        }
        if (mPtrClassicHeader_appname != null)
        {
            mPtrClassicHeader_appname.setLastUpdateTimeKey(key);
        }
        if (mPtrClassicHeader != null)
        {
            mPtrClassicHeader.setLastUpdateTimeKey(key);
        }
    }

    /**
     * Using an object to specify the last update time.
     * 
     * @param object
     */
    public void setLastUpdateTimeRelateObject(Object object)
    {
        if (mPtrClassicHeader_rotatemove != null)
        {
            // 无此功能
        }
        if (mPtrClassicHeader_appname != null)
        {
            mPtrClassicHeader_appname.setLastUpdateTimeRelateObject(object);
        }
        if (mPtrClassicHeader != null)
        {
            mPtrClassicHeader.setLastUpdateTimeRelateObject(object);
        }
    }
}
