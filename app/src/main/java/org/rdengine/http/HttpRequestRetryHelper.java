package org.rdengine.http;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONObject;

/**
 * Created by CCCMAX on 2019/6/13.
 */

public abstract class HttpRequestRetryHelper implements JSONResponse
{
    int retry;
    long space;

    Handler mHandler;

    public HttpRequestRetryHelper(int retryCount, long space)
    {
        retry = retryCount;
        this.space = space;
        mHandler = new Handler(Looper.getMainLooper());
    }

    public void onJsonResponse(JSONObject json, int errCode, String msg, boolean cached)
    {
        if (errCode < ErrorCode.ERROR_OK && retryRequest())
        {
            // 网络错误,并且在重试次数内
        } else
        {
            try
            {
                parseData(json, errCode, msg, cached);
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
    }

    public boolean retryRequest()
    {
        // 重试检测 服务器检测超时、网络问题、请求失败（非服务器返回失败）
        if (retry > 0)
        {
            retry--;
            mHandler.postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    request(HttpRequestRetryHelper.this);
                }
            }, space);

            return true;
        } else
        {
            // 多次重试查询后没有结果
            return false;
        }
    }

    public void start()
    {
        request(this);
    }

    /** 请求方法写在这里 callback用给定的 */
    public abstract void request(JSONResponse callback);

    /** 处理返回结果，网络错误的重试 之前都处理好了 */
    public abstract boolean parseData(JSONObject json, int errCode, String msg, boolean cached);
}
