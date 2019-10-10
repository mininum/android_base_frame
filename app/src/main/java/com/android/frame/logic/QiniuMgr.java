package com.android.frame.logic;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.facebook.fresco.FrescoImageHelper;
import com.qiniu.android.common.Zone;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.Configuration;
import com.qiniu.android.storage.UpCancellationSignal;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UpProgressHandler;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.android.storage.UploadOptions;
import com.qiniu.android.utils.UrlSafeBase64;

import org.json.JSONException;
import org.json.JSONObject;
import org.rdengine.http.HttpUtil;
import org.rdengine.http.JSONResponse;
import org.rdengine.log.DLOG;
import org.rdengine.runtime.RT;
import org.rdengine.util.DMImageTool;
import org.rdengine.util.StringUtil;

import java.io.File;
import java.io.IOException;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class QiniuMgr
{
    public static final String TAG = "QiniuMgr";
    private static final String MAC_NAME = "HmacSHA1";
    private static final String ENCODING = "UTF-8";

    /** 视频转码预处理扩展名 transcoding ， 视频url以此结尾 就是做过预处理转码的文件 */
    public static final String VIDEO_TRANSCODING_EXT = "_tcmp4";

    /** 测试账号 */
    public interface Account_Test
    {
        public final String AccessKey = "-xZFbZhb9-TonhdDOx1N8728xURSDgxoR0erHvZO";
        public final String SecretKey = "-a2SWxxpE2IsRmXVztGv2t2jE4WAs17xA5TM8Wnj";
        public final Scope scope = new Scope("test", "http://ocnz3jaoe.bkt.clouddn.com/");
    }

    /** motion正式 */
    public interface Account_Motion
    {
        public final String AccessKey = "4OmubRiWEZhcGZv4vARSH8eZJZI7EfVEeXxYZVeg";
        public final String SecretKey = "A7ngzHrS0shlnV0xMuuCYDQPdpNWXisG5n1jWPZF";
        public final Scope scope_officalimg = new Scope("officalimg", "http://officalimg.cdn.motiong.com/");
        public final Scope scope_officalvideo = new Scope("officalvideo", "http://officalvideo.cdn.motiong.com/");
        public final Scope scope_ugcimg = new Scope("ugcimg", "http://ugcimg.cdn.motiong.com/");
        public final Scope scope_ugcvideo = new Scope("ugcvideo", "http://ugcvideo.cdn.motiong.com/");

        public final String[] allhost = new String[]
        {
                //
                "ugcimg.cdn.motiong.com/", // 用户图片
                "ugcvideo.cdn.motiong.com/", // 用户视频
                "officalimg.cdn.motiong.com/", // 官方图片
                // "officalvideo.cdn.motiong.com/", // 官方视频
        };
    }

    public static class Scope
    {
        public final String name;
        public final String host;

        public Scope(String name, String host)
        {
            this.name = name;
            this.host = host;
        }
    }

    private UploadManager mUploadManager;

    private volatile static QiniuMgr mQiniuMgr;

    private QiniuMgr()
    {
        getUploadManager();
    }

    public static QiniuMgr getInstance()
    {
        if (mQiniuMgr == null)
        {
            synchronized (QiniuMgr.class)
            {
                if (mQiniuMgr == null)
                    mQiniuMgr = new QiniuMgr();
            }
        }
        return mQiniuMgr;
    }

    public UploadManager getUploadManager()
    {
        if (mUploadManager == null)
        {
            Configuration config = new Configuration.Builder().chunkSize(256 * 1024) // 分片上传时，每片的大小。 默认256K
                    .putThreshhold(512 * 1024) // 启用分片上传阀值。默认512K
                    .connectTimeout(10) // 链接超时。默认10秒
                    .responseTimeout(60) // 服务器响应超时。默认60秒
                    // .recorder(recorder) // recorder分片上传时，已上传片记录器。默认null
                    // .recorder(recorder, keyGen) // keyGen 分片上传时，生成标识符，用于片记录器区分是那个文件的上传记录
                    .zone(Zone.zone0) // 设置区域，指定不同区域的上传域名、备用域名、备用IP。默认 Zone.zone0
                    .build();
            // 重用uploadManager。一般地，只需要创建一个uploadManager对象
            mUploadManager = new UploadManager(config);
        }
        return mUploadManager;
    }

    public static void uploadLocalImage(String path, Scope scope, final QiniuUploadListener mQiniuUploadListener)
    {
        try
        {
            // Scope scope = hasWaterMark ? Account_Motion.scope_ugcimg : Account_Motion.scope_officalimg;
            // Scope scope = Account_Motion.scope_ugcvideo; // TODO 使用饭拍存储空间

            int type = QiniuUploadListener.TYPE_IMAGE;
            if (mQiniuUploadListener != null)
                type = mQiniuUploadListener.filetype;

            // 先拿token 视频会单独设置文件夹
            // String uploadtoken = makeUploadToken(path, Account_Motion.AccessKey, Account_Motion.SecretKey, scope.name,
            // type == QiniuUploadListener.TYPE_VIDEO ? "video" : null);

            // 不设置文件夹
            String uploadtoken = makeUploadToken(path, Account_Motion.AccessKey, Account_Motion.SecretKey, scope.name,
                    null, type);

            DLOG.e(TAG, "uploadtoken = " + uploadtoken);

            UploadOptions mUploadOptions = null;
            if (mQiniuUploadListener != null)
            {
                // 设置有无水印 不同的scope来区分是否有水印
                mQiniuUploadListener.scope = scope;

                path = mQiniuUploadListener.pre_handle_path;// 如果有监听 上传图片可能会被压缩
                mUploadOptions = new UploadOptions(null, null, false, mQiniuUploadListener, mQiniuUploadListener);
            }
            UploadManager uploadManager = QiniuMgr.getInstance().getUploadManager();
            uploadManager.put(path, null, uploadtoken, mQiniuUploadListener, mUploadOptions);

        } catch (Exception e)
        {
            e.printStackTrace();
            if (mQiniuUploadListener != null)
                mQiniuUploadListener.onComplete(null, null);
        }

    }

    /**
     * 七牛图片上传监听
     * 
     * @author CCCMAX
     */
    public static abstract class QiniuUploadListener
            implements UpCompletionHandler, UpProgressHandler, UpCancellationSignal
    {
        public final static int TYPE_IMAGE = 1;
        public final static int TYPE_VIDEO = 2;

        public int filetype = TYPE_IMAGE;

        /** 添加的原始地址 */
        public String source_path = "";
        /** 图片预处理之后的地址（图片分辨率压缩、图片质量压缩） */
        public String pre_handle_path = "";

        /** 当前上传进度比例 0-1 */
        public double progress = 0;

        private boolean isCancelled = false;

        private Scope scope = Account_Motion.scope_ugcimg;

        /** 上传后的宽高 */
        public int upload_w, upload_h;

        public QiniuUploadListener(String source_path, boolean needCompress)
        {
            this(source_path, needCompress, TYPE_IMAGE);
        }

        public QiniuUploadListener(String source_path, boolean needCompress, int filetype)
        {
            this.source_path = source_path;
            this.pre_handle_path = source_path;

            this.filetype = filetype;

            // 压缩图片 && 图片不是gif
            if (needCompress && !FrescoImageHelper.isGifWithLocalImage(source_path) && filetype == TYPE_IMAGE)
            {
                preHandleImage();
            }

            DLOG.i(TAG, "QiniuUploadListener source_paht=" + source_path + "  pre_handle_path=" + pre_handle_path);
        }

        /** 预处理图片 */
        public void preHandleImage()
        {
            try
            {
                boolean sdCard = RT.isMount();
                if (!sdCard)
                {
                    return;
                }
                long length = new File(source_path).length();// 原始图片文件大小
                if (length < 100 * 1024)
                {
                    // 小于100K不压缩
                    return;
                }

                BitmapFactory.Options newOpts = new BitmapFactory.Options();
                newOpts.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(source_path, newOpts);// 此时返回bm为空 newOpts中有图片宽高
                // 原始图片宽高
                int w = newOpts.outWidth;
                int h = newOpts.outHeight;

                // 现在主流手机比较多是1280*800分辨率，所以高和宽我们设置为
                float hh = 1280F;
                float ww = 1280F;
                // 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
                int SCALE = 1;// be=1表示不缩放
                if (w > h && w > ww)
                {// 如果宽度大的话根据宽度固定大小缩放
                    SCALE = (int) (newOpts.outWidth / ww);
                } else if (w < h && h > hh)
                {// 如果高度高的话根据宽度固定大小缩放
                    SCALE = (int) (newOpts.outHeight / hh);
                }
                if (SCALE <= 1)
                    SCALE = 1;
                // else SCALE += 1;

                // 获取原始图片
                Bitmap bitmap = DMImageTool.getBitmapFromFileByOptions(source_path, SCALE);
                // 临时图片路径
                String path = RT.tempImage + String.valueOf(System.currentTimeMillis()) + "_" + bitmap.getWidth() + "x"
                        + bitmap.getHeight() + ".jpg";
                // 保存
                DMImageTool.savePhotoToSDCard(bitmap, path);
                pre_handle_path = path;

                upload_w = bitmap.getWidth();
                upload_h = bitmap.getHeight();
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        /**
         * 取消上传
         */
        public void cancel()
        {
            isCancelled = true;
        }

        public boolean isCancelled()
        {
            // 是否取消上传
            return isCancelled;
        }

        public void progress(String key, double percent)
        {
            // 进度 主线程
            try
            {
                DLOG.i(TAG, "progress = +" + percent);
                progress = percent;
                onProgressChange(progress);
            } catch (Exception e)
            {
                e.printStackTrace();
            }

        }

        public void complete(String key, ResponseInfo info, JSONObject response)
        {
            // 完成请求 主线程

            try
            {
                DLOG.i(TAG, "complete key=" + key + " -------------- info=" + info + " -------------- res=" + response);

                if (response != null && response.has("key"))
                {
                    String urlkey = response.optString("key");
                    String imageurl = scope.host + urlkey;
                    DLOG.i(TAG, "ImageUrl=" + imageurl);
                    onComplete(imageurl, info);
                } else
                {
                    onComplete(null, info);
                }
            } catch (Exception e)
            {
            }

        }

        /**
         * 上传进度 0～1 主线程
         * 
         * @param progress
         */
        public abstract void onProgressChange(double progress);

        /**
         * 图片上传完成
         * 
         * @param imageUrl
         */
        public abstract void onComplete(String imageUrl, ResponseInfo info);
    }

    // -----------------------------------------------------------------------

    /**
     * 生成上传token
     *
     * @param filepath
     *            待上传的文件
     * @param accesskey
     * @param secretkey
     * @param scope
     *            云存储空间
     * @param savedir
     *            云存储上的文件夹
     * @return
     */
    public static String makeUploadToken(String filepath, String accesskey, String secretkey, String scope,
            String savedir, int type)
    {
        String token = "";

        try
        {
            // 上传策略json
            JSONObject json = mekeUploadStrategy(scope, savedir);
            // 添加预处理 呃 不支持图片 目前只支持视频转码 20170512
            // https://developer.qiniu.com/kodo/manual/1206/put-policy#put-policy-persistent-ops-explanation
            if (type == QiniuUploadListener.TYPE_VIDEO)
            {
                String avthumb = "avthumb/mp4/ab/160k/ar/44100/acodec/libfaac/r/30/vb/2200k/vcodec/libx264/s/1280x1280/autoscale/1/stripmeta/0";
                // 视频上传 预处理
                String saveas = UrlSafeBase64
                        .encodeToString(scope + ":" + json.optString("saveKey") + VIDEO_TRANSCODING_EXT);
                json.put("persistentOps", avthumb + "|saveas/" + saveas);
                json.put("persistentPipeline", "Video_Transcoding");
            }
            String _encodedPutPolicy = UrlSafeBase64.encodeToString(json.toString().getBytes());
            byte[] _sign = HmacSHA1Encrypt(_encodedPutPolicy, secretkey);
            String _encodedSign = UrlSafeBase64.encodeToString(_sign);
            token = accesskey + ':' + _encodedSign + ':' + _encodedPutPolicy;
        } catch (Exception e)
        {
        }

        return token;
    }

    /** 构造上传策略 */
    public static JSONObject mekeUploadStrategy(String scope, String savedir)
    {
        // 有效时间为一个小时 单位秒
        long deadline = System.currentTimeMillis() / 1000 + 3600;

        // 定义上传后的文件名
        // 秒、分、时、日、月、年、文件大小、扩展名
        String saveKey = "$(sec)$(min)$(hour)$(day)$(mon)$(year)$(fsize)$(ext)";
        if (!StringUtil.isEmpty(savedir))
        {
            if (savedir.endsWith("/"))
                saveKey = savedir + saveKey;
            else saveKey = savedir + "/" + saveKey;
        }

        return mekeUploadStrategy(scope, deadline, saveKey);
    }

    /** 构造上传策略 */
    /**
     * @param scope
     *            空间名
     * @param deadline
     *            有效时间
     * @param saveKey
     *            存储文件名
     * @return
     */
    public static JSONObject mekeUploadStrategy(String scope, long deadline, String saveKey)
    {
        JSONObject _json = new JSONObject();
        try
        {
            // 必要
            _json.put("scope", scope);// 存储空间名称
            _json.put("deadline", deadline);// 有效时间 单位秒
            // 可选-------------------------
            _json.put("saveKey", saveKey);// 存储的文件名
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
        return _json;
    }

    /**
     * 这个签名方法 使用 HMAC-SHA1 签名方法对对encryptText进行签名
     * 
     * @param encryptText
     *            被签名的字符串
     * @param encryptKey
     *            密钥
     * @return
     * @throws Exception
     */
    public static byte[] HmacSHA1Encrypt(String encryptText, String encryptKey) throws Exception
    {
        byte[] data = encryptKey.getBytes(ENCODING);
        // 根据给定的字节数组构造一个密钥,第二参数指定一个密钥算法的名称
        SecretKey secretKey = new SecretKeySpec(data, MAC_NAME);
        // 生成一个指定 Mac 算法 的 Mac 对象
        Mac mac = Mac.getInstance(MAC_NAME);
        // 用给定密钥初始化 Mac 对象
        mac.init(secretKey);
        byte[] text = encryptText.getBytes(ENCODING);
        // 完成 Mac 操作
        return mac.doFinal(text);
    }

    // 管理---------------------

    /**
     * @param bucketName
     *            空间名
     * @param limit
     *            获取数量 1-10000
     * @param marker
     *            上一次请求结尾的标记
     */
    public static void getList(String bucketName, int limit, String marker, final JSONResponse callback)
    {
        String host = "http://rsf.qbox.me";

        // /move/bmV3ZG9jczpmaW5kX21hbi50eHQ=/bmV3ZG9jczpmaW5kLm1hbi50eHQ=\n

        StringBuffer sb = new StringBuffer();
        sb.append("/list?");
        sb.append("bucket=").append(bucketName);
        sb.append("&limit=").append(limit);
        sb.append("&marker=").append(marker == null ? "" : marker);
        sb.append("&prefix=");
        sb.append("&delimiter=");
        String path = sb.toString();

        String url = host + path;

        DLOG.d("cccmax", url);

        String accessToken = "getQiniuResList=" + url;
        try
        {
            byte[] _sign = HmacSHA1Encrypt(path + "\n", Account_Motion.SecretKey);
            String _encodedSign = UrlSafeBase64.encodeToString(_sign);
            accessToken = "QBox " + Account_Motion.AccessKey + ":" + _encodedSign;
        } catch (Exception e)
        {
        }

        // OkHttpClient client = new OkHttpClient();
        OkHttpClient client = HttpUtil.getClient();
        Headers.Builder builder = new Headers.Builder();
        builder.add("Content-Type", "application/x-www-form-urlencoded");
        builder.add("Authorization", accessToken);
        Headers headers_okhttp = builder.build();

        RequestBody body = RequestBody.create(HttpUtil.JSON, "");
        Request request = new Request.Builder().url(url).headers(headers_okhttp).post(body).build();

        Call call = client.newCall(request);
        call.enqueue(new Callback()
        {
            public void onResponse(Call paramCall, Response paramResponse) throws IOException
            {
                if (paramResponse.isSuccessful())
                {
                    String jsonstr = paramResponse.body().string();
                    DLOG.e("cccmax", "onResponse " + jsonstr);
                    try
                    {
                        final JSONObject json = new JSONObject(jsonstr);
                        if (callback != null)
                        {
                            RT.getMainHandler().post(new Runnable()
                            {
                                public void run()
                                {
                                    callback.onJsonResponse(json, 0, "", false);
                                }
                            });
                        }
                    } catch (Exception e)
                    {
                    }

                } else
                {
                    final int code = paramResponse.code();
                    DLOG.e("cccmax", "onResponse code=" + code);
                    if (callback != null)
                    {
                        RT.getMainHandler().post(new Runnable()
                        {
                            public void run()
                            {
                                callback.onJsonResponse(null, code, "", false);
                            }
                        });
                    }

                }
            }

            public void onFailure(Call paramCall, IOException paramIOException)
            {
                DLOG.e("cccmax", "onFailure ", paramIOException);
                RT.getMainHandler().post(new Runnable()
                {
                    public void run()
                    {
                        callback.onJsonResponse(null, -1, "", false);
                    }
                });

            }
        });
    }

}
