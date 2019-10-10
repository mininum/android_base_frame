package org.rdengine.http;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;

import com.android.frame.R;
import com.android.frame.logic.UserMgr;

import org.json.JSONException;
import org.json.JSONObject;
import org.rdengine.log.DLOG;
import org.rdengine.log.UMConstant;
import org.rdengine.net.Network;
import org.rdengine.net.Network.NetworkMode;
import org.rdengine.runtime.RT;
import org.rdengine.util.FileUtils;
import org.rdengine.util.MD5Util;
import org.rdengine.util.StringUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpUtil
{

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private static OkHttpClient client = new OkHttpClient();

    /** okhttp用的hearder */
    private static Headers headers_okhttp_def;
    static
    {

        // 重设超时时间 默认都是10秒
        client = new OkHttpClient.Builder()
                // .connectTimeout(60 * 1000, TimeUnit.MILLISECONDS)
                .readTimeout(20 * 1000, TimeUnit.MILLISECONDS)
                // .writeTimeout(10 * 1000, TimeUnit.MILLISECONDS)
                .build();

        Headers.Builder builder = new Headers.Builder();
        headers_okhttp_def = builder.build();
    }

    static Handler mainHandler;

    private static Handler getMainHandler()
    {
        if (mainHandler == null)
        {
            mainHandler = new Handler(RT.application.getMainLooper());
        }
        return mainHandler;
    }

    public static OkHttpClient getClient()
    {
        return client;
    }

    /**
     * 异步的post请求，默认不读缓存
     * 
     * @param url
     * @param param
     *            组织成json放到body里
     * @param response
     */
    public static void postAsync(String url, HttpParam param, final JSONResponse response)
    {
        postAsync(url, param, response, false, false);
    }

    /**
     * 异步的post请求，回调在UI线程
     * 
     * @param url
     * @param param
     *            组织成json放到body里
     * @param response
     */
    public static void postAsync(final String url, HttpParam param, final JSONResponse response,
            final boolean readCache, final boolean saveCache)
    {
        String content = "";
        if (param != null)
        {
            content = param.toPostString();
        }

        RequestBody body = RequestBody.create(JSON, content);
        String tag = MD5Util.getMd5(url + content);
        // try
        // {
        // client.cancel(symbol);
        // } catch (Exception e2)
        // {
        //
        // }
        final String key = param.createMD5Key(url);
        if (readCache)
        {
            String result = readHttpCache(RT.application, key);
            if (result != null)
            {
                try
                {
                    JSONObject jResult = new JSONObject(result);
                    int errCode = ErrorCode.ERROR_FAIL;
                    String msg = "";
                    try
                    {

                        JSONObject j_code = jResult.has("code") ? jResult.optJSONObject("code") : jResult;
                        if (j_code != null)
                        {
                            errCode = j_code.optInt("code", ErrorCode.ERROR_FAIL);
                            msg = j_code.optString("message");
                        }

                        if (errCode == ErrorCode.ERROR_OK)
                        {
                            if (response != null)
                            {
                                response.onJsonResponse(jResult, errCode, msg, true);
                            }
                        }

                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                } catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        }

        Headers headers = param.getOKhttpHeader();
        if (headers == null)
            headers = headers_okhttp_def;
        Request request = new Request.Builder().url(url).headers(headers).post(body).tag(tag).build();
        Call call = client.newCall(request);

        if (RT.DEBUG)
            DLOG.d("http", "post request:" + url + "\n--body:" + content + "\n--header_pad:" + param.getHeaderString());

        call.enqueue(new Callback()
        {

            @Override
            public void onResponse(Call paramCall, final Response res) throws IOException
            {
                if (res.isSuccessful() && res.body() != null)
                {
                    final String result = res.body().string();
                    // UIUtils.showToast(result);
                    DLOG.d("http", "post result:" + "localPath=" + url + "   " + result);
                    JSONObject json = null;
                    int errCode = ErrorCode.ERROR_FAIL;
                    String msg = "";
                    try
                    {
                        json = new JSONObject(result);
                        if (json != null)
                        {
                            try
                            {
                                JSONObject j_code = json.has("code") ? json.optJSONObject("code") : json;
                                if (j_code != null)
                                {
                                    errCode = j_code.optInt("code", ErrorCode.ERROR_FAIL);
                                    msg = j_code.optString("message");
                                }
                            } catch (Exception ex)
                            {
                            }

                            // header数据转到json中
                            JSONObject jheader = parseHeader(res);
                            if (jheader != null)
                                json.put("header", jheader);
                        }
                    } catch (JSONException e)
                    {
                        e.printStackTrace();
                    }

                    if (saveCache && json != null && errCode == ErrorCode.ERROR_OK)
                    {
                        writeHttpCache(key, json.toString());
                    } else if (errCode == ErrorCode.SERVICE_USER_UNLOGIN)
                    {
                        // 未登录、登录过期
                        getMainHandler().post(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                UserMgr.getInstance().unlogin();
                                // UserMgr.checkLoginedAndTipGotoLoginView(MainActivity.getSelf(), null);
                            }
                        });
                        try
                        {
                            Map<String, String> map = new HashMap<>();
                            map.put("url", paramCall.request().url().toString());
                            map.put("method", paramCall.request().method());
                            map.put("response", result);
                            DLOG.event(UMConstant.AppTokenInvalid, map);
                        } catch (Exception ex)
                        {
                            DLOG.event(UMConstant.AppTokenInvalid);
                        }
                        // EventManager.ins().sendEvent(EventTag.ACCOUNT_LOGOUT, 0, 0, null);
                    }

                    final JSONObject jResult = json;
                    final int code = errCode;
                    final String message = (code != 0 && TextUtils.isEmpty(msg))
                            ? RT.application.getString(R.string.error_do_anything)
                            : msg;

                    getMainHandler().post(new Runnable()
                    {

                        @Override
                        public void run()
                        {
                            if (response != null)
                            {
                                // API_Serviceinfo.netProbe(code);
                                response.onJsonResponse(jResult, code, message, false);
                            }
                        }
                    });

                } else
                {
                    DLOG.reportHttpError(paramCall, res, null);
                    getMainHandler().post(new Runnable()
                    {

                        @Override
                        public void run()
                        {
                            if (response != null)
                            {
                                int errcode = Network.getNetworkState() == NetworkMode.NET_WORK_OK
                                        ? ErrorCode.ERROR_FAIL
                                        : ErrorCode.ERROR_NO_NET;
                                String msg = Network.getNetworkState() == NetworkMode.NET_WORK_OK
                                        ? RT.getString(R.string.error_http_request)
                                        : RT.getString(R.string.error_network);

                                // API_Serviceinfo.netProbe(errcode);
                                response.onJsonResponse(null, errcode, msg, false);
                            }

                            DLOG.d("http", "post Fail " + res.code());
                        }
                    });

                }
            }

            @Override
            public void onFailure(Call paramCall, final IOException paramIOException)
            {
                DLOG.reportHttpError(paramCall, null, paramIOException);

                getMainHandler().post(new Runnable()
                {

                    @Override
                    public void run()
                    {
                        if (response != null)
                        {
                            int errcode = Network.getNetworkState() == NetworkMode.NET_WORK_OK ? ErrorCode.ERROR_FAIL
                                    : ErrorCode.ERROR_NO_NET;
                            String msg = Network.getNetworkState() == NetworkMode.NET_WORK_OK ? "失败"
                                    : RT.getString(R.string.error_network);

                            // API_Serviceinfo.netProbe(errcode, paramIOException);
                            response.onJsonResponse(null, errcode, msg, false);
                        }
                    }
                });
            }
        });
    }

    /**
     * 异步的get请求，回调在UI线程
     *
     * @param url
     * @param param
     *            拼成get参数加到url尾部
     * @param response
     */
    public static void getAsync(String url, HttpParam param, JSONResponse response)
    {
        getAsync(url, param, response, false, false);
    }

    /**
     * 异步的get请求，回调在UI线程
     * 
     * @param url
     * @param param
     *            拼成get参数加到url尾部
     * @param response
     */
    public static void getAsync(String url, HttpParam param, final JSONResponse response, final boolean readCache,
            final boolean saveCache)
    {
        String content = "";
        if (param != null)
        {
            content = param.toGetString();
        }

        // if (!localPath.endsWith("?"))
        // {
        // localPath = localPath + "?";
        // }
        // localPath = localPath + content;

        url = url.trim();
        if (!StringUtil.isEmpty(content))
        {
            if (url.equals("?"))
            {
                url = url + content;
            } else if (url.contains("?"))
            {
                url = url + "&" + content;
            } else
            {
                url = url + "?" + content;
            }
        }

        String tag = MD5Util.getMd5(url);
        // try
        // {
        // client.cancel(symbol);
        // } catch (Exception e2)
        // {
        //
        // }

        final String key = param != null ? param.createMD5Key(url) : MD5Util.getMd5(url.getBytes());
        if (readCache)
        {
            String result = readHttpCache(RT.application, key);
            if (result != null)
            {
                try
                {
                    JSONObject jResult = new JSONObject(result);
                    int errCode = ErrorCode.ERROR_FAIL;
                    String msg = "";
                    try
                    {

                        JSONObject j_code = jResult.has("code") ? jResult.optJSONObject("code") : jResult;
                        if (j_code != null)
                        {
                            errCode = j_code.optInt("code", ErrorCode.ERROR_FAIL);
                            msg = j_code.optString("message");
                        }

                        if (errCode == ErrorCode.ERROR_OK)
                        {
                            if (response != null)
                            {
                                response.onJsonResponse(jResult, errCode, msg, true);
                            }
                        }

                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                } catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        }

        Headers headers = param.getOKhttpHeader();
        if (headers == null)
            headers = headers_okhttp_def;
        Request request = new Request.Builder().url(url).headers(headers).get().tag(tag).build();
        Call call = client.newCall(request);

        if (RT.DEBUG)
            DLOG.d("http", "get request:" + url + "\n--header_pad:" + param.getHeaderString());

        call.enqueue(new Callback()
        {

            @Override
            public void onResponse(Call paramCall, final Response res) throws IOException
            {
                // TODO Auto-generated method stub
                if (res.isSuccessful() && res.body() != null)
                {
                    final String result = res.body().string();

                    if (RT.DEBUG)
                    {
                        DLOG.d("http", "get result:" + "   " + result + "\nfrom:" + paramCall.request().url());
                    }

                    JSONObject json = null;
                    int errCode = ErrorCode.ERROR_FAIL;
                    String msg = "";
                    try
                    {
                        json = new JSONObject(result);
                        if (json != null)
                        {
                            try
                            {
                                JSONObject j_code = json.has("code") ? json.optJSONObject("code") : json;
                                if (j_code != null)
                                {
                                    errCode = j_code.optInt("code", ErrorCode.ERROR_FAIL);
                                    msg = j_code.optString("message");
                                }
                            } catch (Exception ex)
                            {
                            }

                            // header数据转到json中
                            JSONObject jheader = parseHeader(res);
                            if (jheader != null)
                                json.put("header", jheader);
                        }
                    } catch (JSONException e)
                    {
                        e.printStackTrace();
                    }
                    if (saveCache && json != null && errCode == ErrorCode.ERROR_OK)
                    {
                        writeHttpCache(key, json.toString());
                    } else if (errCode == ErrorCode.SERVICE_USER_UNLOGIN)
                    {
                        // 未登录、登录过期
                        getMainHandler().post(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                UserMgr.getInstance().unlogin();
                                // UserMgr.checkLoginedAndTipGotoLoginView(MainActivity.getSelf(), null);
                            }
                        });

                        try
                        {
                            Map<String, String> map = new HashMap<>();
                            map.put("url", paramCall.request().url().toString());
                            map.put("method", paramCall.request().method());
                            map.put("response", result);
                            DLOG.event(UMConstant.AppTokenInvalid, map);
                        } catch (Exception ex)
                        {
                            DLOG.event(UMConstant.AppTokenInvalid);
                        }

                        // EventManager.ins().sendEvent(EventTag.ACCOUNT_LOGOUT, 0, 0, null);

                    }

                    final JSONObject r_json = json;
                    final int r_code = errCode;
                    final String r_msg = (r_code != 0 && TextUtils.isEmpty(msg))
                            ? RT.application.getString(R.string.error_do_anything)
                            : msg;

                    getMainHandler().post(new Runnable()
                    {
                        public void run()
                        {
                            if (response != null)
                            {
                                // API_Serviceinfo.netProbe(r_code);
                                response.onJsonResponse(r_json, r_code, r_msg, false);
                            }
                        }
                    });

                } else
                {
                    DLOG.reportHttpError(paramCall, res, null);
                    getMainHandler().post(new Runnable()
                    {
                        public void run()
                        {
                            if (response != null)
                            {
                                int errcode = Network.getNetworkState() == NetworkMode.NET_WORK_OK
                                        ? ErrorCode.ERROR_FAIL
                                        : ErrorCode.ERROR_NO_NET;
                                String msg = Network.getNetworkState() == NetworkMode.NET_WORK_OK
                                        ? RT.getString(R.string.error_http_request)
                                        : RT.getString(R.string.error_network);

                                // API_Serviceinfo.netProbe(errcode);
                                response.onJsonResponse(null, errcode, msg, false);
                            }
                            DLOG.d("http", "get Fail " + res.code());
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call paramCall, final IOException paramIOException)
            {
                DLOG.reportHttpError(paramCall, null, paramIOException);
                getMainHandler().post(new Runnable()
                {

                    @Override
                    public void run()
                    {
                        if (response != null)
                        {
                            int errcode = Network.getNetworkState() == NetworkMode.NET_WORK_OK ? ErrorCode.ERROR_FAIL
                                    : ErrorCode.ERROR_NO_NET;
                            String msg = Network.getNetworkState() == NetworkMode.NET_WORK_OK ? "失败"
                                    : RT.getString(R.string.error_network);

                            // API_Serviceinfo.netProbe(errcode, paramIOException);
                            response.onJsonResponse(null, errcode, msg, false);
                        }
                    }
                });
            }
        });
    }

    /**
     * 同步的post请求
     * 
     * @param url
     * @param param
     * @return
     */
    public static JSONObject postSync(String url, HttpParam param)
    {
        JSONObject json = null;

        String content = "";
        if (param != null)
        {
            content = param.toPostString();
        }

        RequestBody body = RequestBody.create(JSON, content);
        Request request = new Request.Builder().url(url).post(body).build();
        Response response = null;
        try
        {
            response = client.newCall(request).execute();
            if (response.isSuccessful())
            {
                String result = response.body().string();
                json = new JSONObject(result);
                return json;
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 同步的get请求
     * 
     * @param url
     * @param param
     * @return
     */
    public static JSONObject getSync(String url, HttpParam param)
    {
        JSONObject json = null;

        String content = "";
        if (param != null)
        {
            content = param.toGetString();
        }

        url = url.trim();
        if (!StringUtil.isEmpty(content))
        {
            if (url.equals("?"))
            {
                url = url + content;
            } else if (url.contains("?"))
            {
                url = url + "&" + content;
            } else
            {
                url = url + "?" + content;
            }
        }

        Request request = new Request.Builder().url(url).get().build();
        Response response = null;
        try
        {
            response = client.newCall(request).execute();
            if (response.isSuccessful())
            {
                String result = response.body().string();
                json = new JSONObject(result);
                return json;
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 表单提交
     * 
     * @param url
     * @param map
     * @param file
     * @param response
     */
    public static void formUpload(String url, Map<String, String> map, File file, final JSONResponse response)
    {

        MultipartBody.Builder builder = new MultipartBody.Builder();
        if (file != null && file.exists())
        {
            RequestBody fileBody = RequestBody.create(MediaType.parse("application/octet-stream"), file);
            builder.addFormDataPart("file", file.getName(), fileBody);
        }
        for (Map.Entry<String, String> entry : map.entrySet())
        {
            String key = entry.getKey();
            String value = entry.getValue();
            builder.addFormDataPart(key, value);
        }

        RequestBody body = builder.build();
        Request request = new Request.Builder().url(url).post(body).build();
        Call call = client.newCall(request);

        call.enqueue(new Callback()
        {

            @Override
            public void onResponse(Call paramCall, final Response res) throws IOException
            {
                // TODO Auto-generated method stub
                if (res.isSuccessful() && res.body() != null)
                {
                    final String result = res.body().string();
                    getMainHandler().post(new Runnable()
                    {

                        @Override
                        public void run()
                        {
                            if (response != null)
                            {
                                JSONObject json = null;
                                int errCode = ErrorCode.ERROR_OK;
                                String msg = "";
                                try
                                {
                                    json = new JSONObject(result);
                                    if (json.optJSONObject("meta") != null)
                                    {
                                        errCode = json.optInt("code", ErrorCode.ERROR_FAIL);
                                        msg = json.optString("message");
                                        // if (json.has("code"))
                                        // {
                                        // JSONObject j_code = json.optJSONObject("code");
                                        // if (j_code != null)
                                        // {
                                        // errCode = j_code.optInt("code", ErrorCode.ERROR_FAIL);
                                        // msg = j_code.optString("message");
                                        // }
                                        // }
                                    }
                                } catch (JSONException e)
                                {
                                    e.printStackTrace();
                                }
                                response.onJsonResponse(json, errCode, msg, false);
                            }
                        }
                    });

                } else
                {
                    getMainHandler().post(new Runnable()
                    {
                        public void run()
                        {
                            if (response != null)
                            {
                                int errcode = Network.getNetworkState() == NetworkMode.NET_WORK_OK
                                        ? ErrorCode.ERROR_FAIL
                                        : ErrorCode.ERROR_NO_NET;
                                response.onJsonResponse(null, errcode, "失败", false);
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call paramCall, IOException paramIOException)
            {
                getMainHandler().post(new Runnable()
                {

                    @Override
                    public void run()
                    {
                        if (response != null)
                        {
                            int errcode = Network.getNetworkState() == NetworkMode.NET_WORK_OK ? ErrorCode.ERROR_FAIL
                                    : ErrorCode.ERROR_NO_NET;
                            response.onJsonResponse(null, errcode, "失败", false);
                        }
                    }
                });
            }
        });
    }

    public static void downloadFile(final String url, final File dstFile, final DownloadListener mListener)
    {
        String tag = MD5Util.getMd5(url);
        // try
        // {
        // client.cancel(symbol);
        // } catch (Exception e2)
        // {
        //
        // }
        Request request = new Request.Builder().url(url).headers(headers_okhttp_def).tag(tag).build();
        Call call = client.newCall(request);
        call.enqueue(new Callback()
        {

            @Override
            public void onResponse(Call paramCall, Response response) throws IOException
            {
                if (response.isSuccessful())
                {
                    InputStream is = null;
                    byte[] buf = new byte[2018];
                    int len = 0;
                    FileOutputStream fos = null;
                    try
                    {
                        is = response.body().byteStream();
                        final long length = response.body().contentLength();
                        long sum = 0;
                        fos = new FileOutputStream(dstFile);
                        while ((len = is.read(buf)) != -1)
                        {
                            sum += len;
                            fos.write(buf, 0, len);
                        }
                    } catch (Exception e)
                    {

                        if (dstFile != null)
                        {
                            dstFile.deleteOnExit();
                        }

                        if (mListener != null)
                        {
                            mListener.onFailed();
                        }

                    } finally
                    {
                        if (is != null)
                        {
                            is.close();
                        }

                        if (fos != null)
                        {
                            fos.flush();
                            fos.close();
                        }

                        if (mListener != null)
                        {
                            mListener.onSuccess(url);
                        }
                    }

                } else
                {
                    if (mListener != null)
                    {
                        mListener.onFailed();
                    }
                }
            }

            @Override
            public void onFailure(Call paramCall, IOException paramIOException)
            {
                if (mListener != null)
                {
                    mListener.onFailed();
                }
            }
        });
    }

    /**
     * 读缓存
     * 
     * @param context
     * @param key
     * @return
     */
    public static String readHttpCache(Context context, String key)
    {
        try
        {
            String path = RT.defaultCache + key;
            StringBuilder sb = FileUtils.readFile(path, "UTF-8");
            return sb != null ? sb.toString() : null;
        } catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 写缓存
     * 
     * @param key
     * @param content
     */
    public static void writeHttpCache(String key, String content)
    {
        try
        {
            String path = RT.defaultCache + key;
            FileUtils.writeFile(path, content, false);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public interface DownloadListener
    {

        public void onSuccess(String url);

        public void onFailed();
    }

    private static JSONObject parseHeader(Response res)
    {

        if (res != null)
        {
            try
            {
                String[] keys = new String[]
                { "token" };

                JSONObject json = new JSONObject();

                for (String key : keys)
                {
                    String v = res.header(key);
                    if (!TextUtils.isEmpty(v))
                        json.put(key, v);
                }

                if (json.length() > 0)
                    return json;
            } catch (Exception ex)
            {
            }
        }
        return null;
    }

}
