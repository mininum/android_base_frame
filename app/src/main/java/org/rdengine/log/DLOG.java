package org.rdengine.log;

import com.umeng.analytics.MobclickAgent;

import org.rdengine.net.Network;
import org.rdengine.net.Network.NetworkMode;
import org.rdengine.runtime.RT;
import org.rdengine.util.StringUtil;

import java.util.Arrays;
import java.util.Map;
import java.util.Vector;

import okhttp3.Call;
import okhttp3.Response;

/**
 * 日志服务 <br>
 * <br>
 * 功能模块日志:需要输出带模块tag的log日志,记录功能实现状态与结果,如注册登录,<br>
 * 请求列表,榜单,刷新专辑图等, LOG中需要标识结果,并定义关键字<br>
 * <br>
 * UI控制日志:需要记录操作动作,激活事件,定义关键字.
 * 
 * @author yangyu
 */

/**
 * @author CCCMAX
 */
public class DLOG
{

    private final static boolean forTest = false;

    /**
     * Instantiates a new log.
     */
    public DLOG()
    {
        makeLogHead();
    }

    /** The dmlog. */
    static DLOG dmlog = null;

    private static Object lock = new Object();
    private static StringBuilder strBuffer;

    /**
     * Instance.
     * 
     * @return the log
     */
    public static DLOG instance()
    {
        if (dmlog == null)
        {
            dmlog = new DLOG();
        }

        return dmlog;
    }

    /**
     * Debug TAG log
     * 
     * @param tag
     *            the symbol
     * @param log
     *            the log
     */
    public static void d(String tag, String log)
    {
        if (RT.DEBUG || forTest)
            log(LE.d, tag, log, null);
    }

    /**
     * Error
     * 
     * @param tag
     *            the symbol
     * @param log
     *            the log
     */
    public static void e(String tag, String log)
    {
        if (RT.DEBUG || forTest)
            log(LE.e, tag, log, null);
    }

    /**
     * Info
     * 
     * @param tag
     *            the symbol
     * @param log
     *            the log
     */
    public static void i(String tag, String log)
    {
        if (RT.DEBUG || forTest)
            log(LE.i, tag, log, null);
    }

    /**
     * 带错误堆栈.
     * 
     * @param tag
     *            the symbol
     * @param log
     *            the log
     * @param e
     *            the e
     */
    public static void log(String tag, String log, Throwable e)
    {
        if (RT.DEBUG || forTest)
            synchronized (lock)
            {
                if (strBuffer == null)
                    strBuffer = new StringBuilder();
                else strBuffer.delete(0, strBuffer.length());
                if (dmlog == null)
                    dmlog = instance();
                if (e != null)
                    dmlog.log(LE.d, tag, strBuffer.append(log).append(":")
                            .append(android.util.Log.getStackTraceString(e)).toString());
                else dmlog.log(LE.d, tag, strBuffer.append(log).toString());
            }
    }

    /**
     * Log.
     * 
     * @param le
     *            the le
     * @param tag
     *            the symbol
     * @param log
     *            the log
     * @param e
     *            the e
     */
    private static void log(LE le, String tag, String log, Throwable e)
    {
        if (RT.DEBUG || forTest)
        {
            synchronized (lock)
            {
                if (strBuffer == null)
                    strBuffer = new StringBuilder();
                else strBuffer.delete(0, strBuffer.length());
                if (dmlog == null)
                    dmlog = instance();
                if (e != null)
                    dmlog.log(le, tag, strBuffer.append(log).append(":").append(android.util.Log.getStackTraceString(e))
                            .toString());
                else dmlog.log(le, tag, strBuffer.append(log).toString());
            }
        }

    }

    /**
     * The Enum LE.
     */
    public enum LE
    {

        /** The d. */
        d,

        /** The e. */
        e,
        /** The i. */
        i;
    }

    /**
     * Log.
     * 
     * @param tag
     *            the symbol
     * @param log
     *            the log
     */
    public void log(String tag, String log)
    {
        if (RT.DEBUG || forTest)
            log(LE.d, tag, log);
    }

    /**
     * Log.
     * 
     * @param le
     *            the le
     * @param tag
     *            the symbol
     * @param log
     *            the log
     */
    public void log(LE le, String tag, String log)
    {
        try
        {
            // filter(symbol, log);
        } catch (Exception ex)
        {

        }
        if (RT.WriteLog)
        {
            try
            {
                writeLog(tag, log);
            } catch (Exception ex)
            {

            }
        }

        if (RT.DEBUG || forTest)
        {
            if (log == null)
            {
                toandroidlog(le, tag, "");
                return;
            }

            boolean isstr = false;// log是否是有效的可见字符串
            int size = 0;// log的大小
            try
            {
                byte[] logbytes = log.getBytes();
                size = logbytes.length;
                if (log.length() > 30)
                {
                    isstr = !hasMessyCode(log.substring(20, 30));
                } else
                {
                    isstr = !hasMessyCode(log);
                }
            } catch (Exception e1)
            {
                e1.printStackTrace();
            }

            if (isstr)
            {
                // 可读的文字log输出 针对logcat折行
                try
                {
                    int start = 0;
                    while (start < log.length())
                    {
                        int end = start + 3000;
                        if (end >= log.length())
                            end = log.length();
                        toandroidlog(le, tag, log.substring(start, end));
                        start = end;
                    }
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            } else
            {
                toandroidlog(le, tag, "THE LOG NOT STRING ！！ SIZE=" + size);
            }
        }
    }

    private boolean hasMessyCode(String str)
    {
        boolean ret = false;
        if (str == null)
            return ret;
        if (str.indexOf("�") > 0)
            ret = true;
        return ret;
    }

    private void toandroidlog(LE le, String tag, String log)
    {
        switch (le)
        {
        case d :
            android.util.Log.d(tag, log);
            break;
        case e :
            android.util.Log.e(tag, log);
            break;
        case i :
            android.util.Log.i(tag, log);
            break;
        }
    }

    /** The loghead. */
    static String LOGHEAD = null;

    /**
     * Make log head.
     */
    void makeLogHead()
    {
        LOGHEAD = "|UA:TEST|";
    }

    /**
     * Write log.
     * 
     * @param tag
     *            the symbol
     * @param log
     *            the log
     */
    public void writeLog(String tag, String log)
    {

    }

    /**
     * Filter.
     * 
     * @param tag
     *            the symbol
     * @param log
     *            the log
     */
    public void filter(String tag, String log)
    {
        Vector<DMLogFilter> filters = DMLogFilterManager.instance().Filters;
        if (filters == null)
        {
            return;
        }

        for (int i = 0; i < filters.size(); i++)
        {
            filter(filters.elementAt(i), tag, log);
        }

    }

    /**
     * Filter.
     * 
     * @param filter
     *            the filter
     * @param tag
     *            the symbol
     * @param log
     *            the log
     */
    public void filter(DMLogFilter filter, String tag, String log)
    {
        if (filter != null && tag != null)
        {
            if (filter.KEYTag != null && filter.KEYTag.trim().length() > 0
                    && !filter.KEYTag.trim().toLowerCase().equals(tag.trim().toLowerCase()))
            {
                return;// 过滤器KEYTag不是null,不是"",同时日志Tag与KEYTag不同,直接返回
            }

            if (filter.KEYs != null && filter.KEYs.length > 0)
            {
                if (log == null || log.trim().length() == 0)
                {
                    return;// Log是null,或"",过滤器KEYs不是空,不满足条件,直接返回
                }
                for (int i = 0; i < filter.KEYs.length; i++)
                {
                    if (log.toLowerCase().indexOf(filter.KEYs[i].trim().toLowerCase()) < 0)
                    {
                        return;// 不满足关键字filter.KEYs[i],返回
                    }
                }

                // 过滤器包含KEYs,满足全部条件
                filter.callback(tag, log);
            } else
            {
                // 无KEYs,同时满足Tag
                filter.callback(tag, log);
            }

        }
    }

    /**
     * V.
     * 
     * @param logTag
     *            the log symbol
     * @param string
     *            the string
     */
    public static void v(String logTag, String string)
    {
        if (RT.DEBUG || forTest)
            d(logTag, string);

    }

    /**
     * E.
     * 
     * @param tag
     *            the symbol
     * @param string
     *            the string
     * @param e
     *            the e
     */
    public static void e(String tag, String string, Throwable e)
    {
        if (RT.DEBUG || forTest)
            log(tag, string, e);

    }

    /**
     * W.
     * 
     * @param logTag
     *            the log symbol
     * @param e
     *            the e
     */
    public static void w(String logTag, String log, Throwable e)
    {
        // d(logTag, android.util.Log.getStackTraceString(e));
        if (RT.DEBUG || forTest)
            log(LE.i, logTag, log, e);
    }

    public static void w(String logTag, String log)
    {
        // d(logTag, android.util.Log.getStackTraceString(e));
        if (RT.DEBUG || forTest)
            log(LE.i, logTag, log, null);
    }

    public static void w(String logTag, Throwable e)
    {
        // d(logTag, android.util.Log.getStackTraceString(e));
        if (RT.DEBUG || forTest)
            w(logTag, "", e);
    }

    /**
     * 统计埋点
     *
     * @param event
     */
    public static void event(UMConstant event)
    {
        DLOG.d("event", event.name());
        MobclickAgent.onEvent(RT.application, event.name());
    }

    public static void event(UMConstant event, String tag)
    {
        DLOG.d("event", event.name() + ":" + tag);
        MobclickAgent.onEvent(RT.application, event.name(), tag);
    }

    /**
     * 统计埋点
     * 
     * @param event
     * @param params
     */
    public static void event(UMConstant event, Map<String, String> params)
    {
        try
        {
            DLOG.d("event", event.name() + "  map=" + Arrays.toString(params.entrySet().toArray()));
        } catch (Exception ex)
        {
        }
        MobclickAgent.onEvent(RT.application, event.name(), params);
    }

    public static void eventPageStart(String viewtag)
    {
        if (UMPageFilter.allow(viewtag))
        {
            DLOG.d("eventPageStart", viewtag);
            MobclickAgent.onPageStart(viewtag);
        }
    }

    public static void eventPageEnd(String viewtag)
    {
        if (UMPageFilter.allow(viewtag))
        {
            DLOG.d("eventPageEnd", viewtag);
            MobclickAgent.onPageEnd(viewtag);
        }
    }

    /**
     * 上报自定义错误
     * 
     * @param error
     */
    public static void reportUmengError(String error)
    {
        MobclickAgent.reportError(RT.application, error);
    }

    /**
     * 上报自定义错误
     * 
     * @param e
     */
    public static void reportUmengError(Throwable e)
    {
        MobclickAgent.reportError(RT.application, e);
    }

    /**
     * 上报HTTP请求错误
     * 
     * @param call
     * @param e
     */
    public static void reportHttpError(Call call, Response response, Throwable e)
    {
        try
        {
            // 有网时请求失败 上报
            if (Network.getNetworkState() != NetworkMode.NET_WORK_OK)
            {
                return;
            }

            StringBuilder sb = new StringBuilder();
            sb.append("http request fail");
            if (e != null)
            {
                // 异常类型
                sb.append(" : " + e.getClass().getSimpleName());
            }

            sb.append("\n");
            if (call != null && call.request() != null)
            {
                // 请求内容
                sb.append("Request=").append(call.request().toString()).append("\n");
            }
            if (response != null)
            {
                // http响应码
                sb.append("Code=").append(response.code());
            }
            if (e != null)
            {
                // 异常信息
                sb.append(StringUtil.exceptionToString(e));
            }
            String content = sb.toString();

            reportUmengError(content);

            e("http", content);
        } catch (Exception e2)
        {
        }
    }
}
