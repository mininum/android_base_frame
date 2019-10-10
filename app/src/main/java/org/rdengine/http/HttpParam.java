package org.rdengine.http;

import org.json.JSONException;
import org.json.JSONObject;
import org.rdengine.util.MD5Util;
import org.rdengine.util.StringUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import okhttp3.Headers;

public class HttpParam
{

    private Map<String, String> headerMap;

    private JSONObject json;

    public HttpParam()
    {
        json = new JSONObject();
    }

    public HttpParam(JSONObject json)
    {
        this.json = json;
        if (this.json == null)
        {
            this.json = new JSONObject();
        }
    }

    public HttpParam(String str)
    {
        try
        {
            json = new JSONObject(str);
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
        if (json == null)
        {
            json = new JSONObject();
        }
    }

    public HttpParam(Map copyMap)
    {
        try
        {
            json = new JSONObject(copyMap);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        if (json == null)
        {
            json = new JSONObject();
        }
    }

    public HttpParam put(String name, boolean value)
    {
        try
        {
            json.put(name, value);
        } catch (Exception e)
        {
        }
        return this;
    }

    public HttpParam put(String name, int value)
    {
        try
        {
            json.put(name, value);
        } catch (Exception e)
        {
        }
        return this;
    }

    public HttpParam put(String name, double value)
    {
        try
        {
            json.put(name, value);
        } catch (Exception e)
        {
        }
        return this;
    }

    public HttpParam put(String name, long value)
    {
        try
        {
            json.put(name, value);
        } catch (Exception e)
        {
        }
        return this;
    }

    public HttpParam put(String name, Object value)
    {
        try
        {
            json.put(name, value);
        } catch (Exception e)
        {
        }
        return this;
    }

    public HttpParam putOpt(String name, Object value)
    {
        try
        {
            json.putOpt(name, value);
        } catch (Exception e)
        {
        }
        return this;
    }

    public JSONObject getJson()
    {
        return json;
    }

    public String toPostString()
    {
        return this.json.toString();
    }

    public String toGetString()
    {
        StringBuilder sb = new StringBuilder();
        Iterator iterator = this.json.keys();
        while (iterator.hasNext())
        {
            String key = (String) iterator.next();
            String value = this.json.optString(key);
            if (!StringUtil.isEmpty(value))
            {
                try
                {
                    value = URLEncoder.encode(value, "UTF-8");
                } catch (UnsupportedEncodingException e)
                {
                    e.printStackTrace();
                }
                sb = sb.append(key + "=" + value);
                if (iterator.hasNext())
                {
                    sb = sb.append("&");
                }
            }
        }
        return sb.toString();
    }

    /** 向header中添加一条数据 */
    public void addHeaderItem(String key, String value)
    {
        if (StringUtil.isEmpty(key) || StringUtil.isEmpty(value))
            return;
        if (headerMap == null)
        {
            headerMap = new HashMap<String, String>();
        }
        try
        {
            headerMap.put(key, value);
        } catch (Exception ex)
        {
        }
    }

    public Headers getOKhttpHeader()
    {
        if (headerMap != null && headerMap.size() > 0)
        {
            Headers.Builder builder = new Headers.Builder();
            Iterator iter = headerMap.entrySet().iterator();
            while (iter.hasNext())
            {
                try
                {
                    Map.Entry entry = (Map.Entry) iter.next();
                    String key = (String) entry.getKey();
                    String val = (String) entry.getValue();
                    builder.add(key, val);
                } catch (Exception ex)
                {
                }
            }
            Headers headers = builder.build();
            return headers;
        }
        return null;
    }

    public String getHeaderString()
    {
        StringBuilder sb = new StringBuilder();
        if (headerMap != null && headerMap.size() > 0)
        {
            Iterator iter = headerMap.entrySet().iterator();
            while (iter.hasNext())
            {
                try
                {
                    Map.Entry entry = (Map.Entry) iter.next();
                    String key = (String) entry.getKey();
                    String val = (String) entry.getValue();
                    sb.append(key).append("=").append(val).append(";");
                } catch (Exception ex)
                {
                }
            }
        }
        return sb.toString();
    }

    /**
     * 生成唯一key
     * 
     * @param url
     * @return
     */
    public String createMD5Key(String url)
    {
        String key = "";
        String param = json != null ? json.toString() : "";
        String header = getHeaderString();
        key = MD5Util.getMd5((url + param + header).getBytes());
        return key;
    }

}
