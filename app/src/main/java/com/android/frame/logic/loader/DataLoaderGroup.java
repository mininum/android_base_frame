package com.android.frame.logic.loader;

import org.json.JSONObject;
import org.rdengine.http.JSONResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 数据加载器的封装组 切换不同type 来切换DataLoader
 * 
 * @param <Tp>
 *            type
 * @param <Dt>
 *            数据加载器返回的类型
 */
public class DataLoaderGroup<Tp, Dt> extends DataLoader<Dt>
{

    private HashMap<Tp, DataLoader<Dt>> dlmap = new HashMap<>();
    // HashMap<Tp, DlProxyCallback<Tp>> cbmap = new HashMap<>();

    private Tp current_type = null;// 默认
    private DataLoader<Dt> current_dl;

    public DataLoaderGroup(HashMap<Tp, DataLoader<Dt>> map, Tp defType)
    {
        dlmap = map;
        Iterator iter = dlmap.entrySet().iterator();
        while (iter.hasNext())
        {
            Map.Entry entry = (Map.Entry) iter.next();
            Tp type = (Tp) entry.getKey();
            DataLoader<Dt> dl = (DataLoader<Dt>) entry.getValue();
            DlProxyCallback mDlProxyCallback = new DlProxyCallback(type);
            dl.addCallback(mDlProxyCallback);
        }

        setDlType(defType);
    }

    /** 切换数据源 */
    public void setDlType(Tp type)
    {
        current_type = type;
        current_dl = dlmap.get(current_type);
    }

    public Tp getDlType()
    {
        return current_type;
    }

    @Override
    protected void requestData(boolean readCache, JSONResponse callback)
    {
        current_dl.requestData(readCache, callback);// callback 是group的, 然后调用group的parseData
    }

    @Override
    protected void parseData(JSONObject json, int errCode, String msg, boolean cached)
    {
        current_dl.parseData(json, errCode, msg, cached); // 解析内容交给具体的dl中，通过代理的DlProxyCallback 调用requestComplete
    }

    @Override
    protected void requestComplete(int errCode, String msg, int page, int pageSize, ArrayList<Dt> pagedata)
    {
        super.requestComplete(errCode, msg, page, pageSize, pagedata);
    }

    @Override
    public boolean isRequesting()
    {
        return current_dl.isRequesting();
    }

    @Override
    public void setRequesting(boolean requesting)
    {
        current_dl.setRequesting(requesting);
    }

    @Override
    public void refreshData(boolean readCache)
    {
        current_dl.refreshData(readCache);
    }

    @Override
    public void loadMoreData()
    {
        current_dl.loadMoreData();
    }

    @Override
    public ArrayList<Dt> getAllData()
    {
        return current_dl.getAllData();
    }

    @Override
    public void addCallback(DlCallback<Dt> callback)
    {
        super.addCallback(callback);
    }

    @Override
    public void removeCallback(DlCallback<Dt> callback)
    {
        super.removeCallback(callback);
    }

    @Override
    public void destroy()
    {
        try
        {
            Iterator iter = dlmap.entrySet().iterator();
            while (iter.hasNext())
            {
                Map.Entry entry = (Map.Entry) iter.next();
                // Tp type = (Tp) entry.getKey();
                DataLoader dl = (DataLoader) entry.getValue();
                dl.destroy();
            }
            dlmap.clear();
            // cbmap.clear();
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    @Override
    public int getPage()
    {
        return current_dl.getPage();
    }

    @Override
    public int getPagesize()
    {
        return current_dl.getPagesize();
    }

    @Override
    public int getTotal()
    {
        return current_dl.getTotal();
    }

    @Override
    public boolean hasMore()
    {
        return current_dl.hasMore();
    }

    @Override
    public long getLastRefreshTime()
    {
        return current_dl.getLastRefreshTime();
    }

    private class DlProxyCallback implements DlCallback<Dt>
    {
        public Tp type;

        public DlProxyCallback(Tp type)
        {
            this.type = type;
        }

        @Override
        public void onRequestComplete(DataLoader<Dt> dataloader, int errCode, String msg, int page, int pageSize,
                ArrayList<Dt> pagedata)
        {
            if (type == current_type)
                requestComplete(errCode, msg, page, pagesize, pagedata);
        }
    }
}
