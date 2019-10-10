package com.android.frame.logic.loader;

import org.json.JSONObject;
import org.rdengine.http.JSONResponse;

import java.util.ArrayList;

/**
 * 数据加载器 Created by CCCMAX on 18/12/20.
 */

public abstract class DataLoader<Dt>
{

    ArrayList<DlCallback<Dt>> callbackList;

    ArrayList<Dt> data = new ArrayList<>();

    int page = 0;
    int pagesize = 20;
    int total = -1;
    boolean hasMore;

    Object lastId;
    boolean lastIdMode = false;

    boolean requesting = false;

    long lastRefreshTime = 0;

    public boolean isRequesting()
    {
        return requesting;
    }

    public void setRequesting(boolean requesting)
    {
        this.requesting = requesting;
    }

    /** 请求新数据 */
    public void refreshData(boolean readCache)
    {
        setRequesting(true);
        if (lastIdMode)
        {
            lastId = null;
            hasMore = false;
        } else
        {
            page = 0;
            total = -1;
            hasMore = false;
        }

        if (data == null)
            data = new ArrayList<>();
        else data.clear();
        mJResponse = new JResponse();
        requestData(readCache, mJResponse);
        lastRefreshTime = System.currentTimeMillis();
    }

    /** 请求更多数据 */
    public void loadMoreData()
    {
        if (hasMore())
            requestData(false, mJResponse);
    }

    public ArrayList<Dt> getAllData()
    {
        if (data == null)
            data = new ArrayList<>();
        return data;
    }

    JResponse mJResponse;

    private class JResponse implements JSONResponse
    {
        public void onJsonResponse(JSONObject json, int errCode, String msg, boolean cached)
        {
            if (JResponse.this == mJResponse)
            {
                setRequesting(false);

                try
                {
                    parseData(json, errCode, msg, cached);
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        }
    }

    /** 请求完成 分发结果 */
    protected void requestComplete(int errCode, String msg, int page, int pageSize, ArrayList<Dt> pagedata)
    {
        try
        {
            if (callbackList != null)
            {
                for (DlCallback callback : callbackList)
                {
                    try
                    {
                        callback.onRequestComplete(this, errCode, msg, page, pagesize, pagedata);
                    } catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                }
            }

        } catch (Exception ex)
        {
        }
    }

    /** 具体的请求数据方法 */
    protected abstract void requestData(boolean readCache, JSONResponse callback);

    /**
     * 具体解析数据的方法<br>
     * 解析数据后需要调用requestComplete()分发结果
     */
    protected abstract void parseData(JSONObject json, int errCode, String msg, boolean cached);

    public interface DlCallback<Dt>
    {
        void onRequestComplete(DataLoader<Dt> dataloader, int errCode, String msg, int page, int pageSize,
                               ArrayList<Dt> pagedata);
    }

    public void addCallback(DlCallback<Dt> callback)
    {
        if (callbackList == null)
            callbackList = new ArrayList<>();
        if (!callbackList.contains(callback))
            callbackList.add(callback);
    }

    public void removeCallback(DlCallback<Dt> callback)
    {
        try
        {
            if (callbackList != null)
                callbackList.remove(callback);
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public void destroy()
    {
        if (callbackList != null)
            callbackList.clear();
        callbackList = null;
    }

    public Object getLastId()
    {
        return lastId;
    }

    public int getPage()
    {
        return page;
    }

    public int getPagesize()
    {
        return pagesize;
    }

    public int getTotal()
    {
        return total;
    }

    public boolean hasMore()
    {
        return hasMore;
    }

    public long getLastRefreshTime()
    {
        return lastRefreshTime;
    }
}
