package com.android.frame.logic;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.android.frame.R;
import com.facebook.fresco.FrescoImageHelper;
import com.facebook.fresco.FrescoParam;
import com.facebook.fresco.FrescoParam.QiniuParam;

import org.rdengine.runtime.RT;
import org.rdengine.util.DMImageTool;
import org.rdengine.util.StringUtil;
import org.rdengine.widget.ToastHelper;

import java.io.File;
import java.util.HashMap;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.tencent.qzone.QZone;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;

/**
 * http://wiki.mob.com/不同平台分享内容的详细说明
 * 
 * @author CCCMAX
 */
public class ShareUtil
{

    /**
     * 分享到QQ
     * 
     * @param title
     *            标题
     * @param des
     *            描述
     * @param image
     *            图片 网络地址或本地路径
     * @param url
     *            链接
     */
    public static void shareQQ(String title, String des, String image, String url, PlatformActionListener callback)
    {
        ShareSDK.initSDK(RT.application);
        Platform pf = ShareSDK.getPlatform(QQ.NAME);
        pf.setPlatformActionListener(new PlatformActionListenerProxy(callback));
        QQ.ShareParams sp = new QQ.ShareParams();
        sp.setTitle(title);
        sp.setText(des);
        if (!StringUtil.isEmpty(image) && image.toLowerCase().startsWith("http"))
        {
            sp.setImageUrl(image);
        } else
        {
            sp.setImagePath(image);
        }
        sp.setTitleUrl(url);
        sp.setSiteUrl(url);
        pf.share(sp);
    }

    /**
     * 分享到QQ
     *
     * @param image
     *            图片 网络地址或本地路径
     */
    public static void shareQQImage(String image, PlatformActionListener callback)
    {
        ShareSDK.initSDK(RT.application);
        Platform pf = ShareSDK.getPlatform(QQ.NAME);
        pf.setPlatformActionListener(new PlatformActionListenerProxy(callback));
        QQ.ShareParams sp = new QQ.ShareParams();
        if (!StringUtil.isEmpty(image) && image.toLowerCase().startsWith("http"))
        {
            sp.setImageUrl(image);
        } else
        {
            sp.setImagePath(image);
        }
        pf.share(sp);
    }

    /**
     * 分享到Qzone
     * 
     * @param title
     *            标题
     * @param des
     *            描述
     * @param image
     *            图片 网络地址或本地路径
     * @param url
     *            链接
     */
    public static void shareQZone(String title, String des, String image, String url, PlatformActionListener callback)
    {
        ShareSDK.initSDK(RT.application);
        Platform pf = ShareSDK.getPlatform(QZone.NAME);
        pf.setPlatformActionListener(new PlatformActionListenerProxy(callback));
        QZone.ShareParams sp = new QZone.ShareParams();
        sp.setTitle(title);
        sp.setTitleUrl(url);
        sp.setText(des);
        sp.setTitleUrl(url);
        if (!StringUtil.isEmpty(image) && image.toLowerCase().startsWith("http"))
        {
            sp.setImageUrl(image);
        } else
        {
            sp.setImagePath(image);
        }
        sp.setSite(RT.getString(R.string.app_name));
        sp.setSiteUrl("https://wwww.evatarbj.com/");
        pf.share(sp);
    }

    /**
     * 分享到微信
     * 
     * @param title
     * @param des
     * @param image
     *            优先使用本地图片
     * @param url
     */
    public static void shareWX(String title, String des, String image, String url, PlatformActionListener callback)
    {
        ShareSDK.initSDK(RT.application);
        Platform pf = ShareSDK.getPlatform(Wechat.NAME);
        pf.setPlatformActionListener(new PlatformActionListenerProxy(callback));
        Wechat.ShareParams sp = new Wechat.ShareParams();
        sp.setShareType(Platform.SHARE_WEBPAGE);
        sp.setTitle(title);
        sp.setText(des);
        sp.setUrl(url);
        // sp.setImageData(imageData);
        Bitmap bitmap = getLocalBitmap(image);
        if (bitmap == null)
        {
            bitmap = BitmapFactory.decodeResource(RT.application.getResources(), R.mipmap.ic_launcher);
        }
        sp.setImageData(bitmap);
        pf.share(sp);
    }

    /**
     * 分享到微信————图片
     * 
     * @param image
     *            优先使用本地图片
     */
    public static void shareWXImg(String image, PlatformActionListener callback)
    {
        ShareSDK.initSDK(RT.application);
        Platform pf = ShareSDK.getPlatform(Wechat.NAME);
        pf.setPlatformActionListener(new PlatformActionListenerProxy(callback));
        Wechat.ShareParams sp = new Wechat.ShareParams();
        sp.setShareType(Platform.SHARE_IMAGE);
        // Bitmap bitmap = getLocalBitmap(image);
        // if (bitmap == null)
        // {
        // bitmap = BitmapFactory.decodeResource(RT.application.getResources(), R.drawable.ic_launcher);
        // }
        // sp.setImageData(bitmap);
        sp.setImagePath(getLocalPath(image));
        pf.share(sp);
    }

    /**
     * 分享到朋友圈
     * 
     * @param title
     * @param des
     * @param image
     *            优先使用本地图片
     * @param url
     */
    public static void shareWXMomentsImg(String title, String des, String image, String url,
            PlatformActionListener callback)
    {
        ShareSDK.initSDK(RT.application);
        Platform pf = ShareSDK.getPlatform(WechatMoments.NAME);
        pf.setPlatformActionListener(new PlatformActionListenerProxy(callback));
        WechatMoments.ShareParams sp = new WechatMoments.ShareParams();
        sp.setShareType(Platform.SHARE_WEBPAGE);
        sp.setTitle(title);
        sp.setText(des);
        sp.setUrl(url);
        // sp.setImageData(imageData);
        Bitmap bitmap = getLocalBitmap(image);
        if (bitmap == null)
        {
            bitmap = BitmapFactory.decodeResource(RT.application.getResources(), R.mipmap.ic_launcher);
        }
        sp.setImageData(bitmap);
        pf.share(sp);
    }

    /**
     * 分享到朋友圈——图片
     * 
     * @param image
     *            优先使用本地图片
     */
    public static void shareWXMoments(String image, PlatformActionListener callback)
    {
        ShareSDK.initSDK(RT.application);
        Platform pf = ShareSDK.getPlatform(WechatMoments.NAME);
        pf.setPlatformActionListener(new PlatformActionListenerProxy(callback));
        WechatMoments.ShareParams sp = new WechatMoments.ShareParams();
        sp.setShareType(Platform.SHARE_IMAGE);
        // Bitmap bitmap = getLocalBitmap(image);
        // if (bitmap == null)
        // {
        // bitmap = BitmapFactory.decodeResource(RT.application.getResources(), R.drawable.ic_launcher);
        // }
        // sp.setImageData(bitmap);
        sp.setImagePath(getLocalPath(image));
        pf.share(sp);
    }

    /**
     * 分享到新浪微博
     * <ul>
     * 文本内容:小于2000字<br>
     * 图片:支持imagePath(本地)、imageUrl(网络)，图片小于5M
     * </ul>
     * 
     * @param title
     * @param des
     * @param image
     * @param url
     */
    public static void shareSina(String title, String des, String image, String url, PlatformActionListener callback)
    {
        ShareSDK.initSDK(RT.application);
        Platform pf = ShareSDK.getPlatform(SinaWeibo.NAME);
        pf.setPlatformActionListener(new PlatformActionListenerProxy(callback));
        SinaWeibo.ShareParams sp = new SinaWeibo.ShareParams();
        sp.setTitle(title);
        sp.setText(des);
        sp.setUrl(url);
        sp.setTitleUrl(url);
        sp.setImagePath(getLocalPath(image));
        pf.share(sp);
    }

    public static Bitmap getLocalBitmap(String img)
    {
        if (img == null)
        {
            return null;
        }
        try
        {
            File imgfile = null;
            if (img.trim().startsWith("http"))
            {
                // 网络图片
                if (FrescoParam.isGbImage(img))
                {
                    // 关八七牛图片
                    String url_m = new FrescoParam(img, QiniuParam.C_M).getURI();
                    File file = FrescoImageHelper.getImageDiskCacheFile(url_m);
                    if (file != null && file.exists())
                    {
                        imgfile = file;
                    } else
                    {
                        url_m = new FrescoParam(img, QiniuParam.Z_MAX_M).getURI();
                        file = FrescoImageHelper.getImageDiskCacheFile(url_m);
                        if (file != null && file.exists())
                        {
                            imgfile = file;
                        } else
                        {
                            url_m = new FrescoParam(img, QiniuParam.Z_MAX_L).getURI();
                            file = FrescoImageHelper.getImageDiskCacheFile(url_m);
                            if (file != null && file.exists())
                            {
                                imgfile = file;
                            }
                        }
                    }

                } else
                {
                    File file = FrescoImageHelper.getImageDiskCacheFile(img);
                    if (file != null && file.exists())
                    {
                        imgfile = file;
                    }
                }
            } else
            {
                File file = new File(img);
                if (file != null && file.exists())
                {
                    imgfile = file;
                }
            }
            if (imgfile != null)
            {
                try
                {
                    Bitmap bitmap = DMImageTool.getBitmap(imgfile.getAbsolutePath(), true);
                    return bitmap;
                } catch (Exception e)
                {
                }
            }
        } catch (Exception ex)
        {
        }
        return null;
    }

    public static String getLocalPath(String img)
    {
        if (img == null)
        {
            return "";
        }
        if (img.trim().startsWith("http"))
        {
            File file = FrescoImageHelper.getImageDiskCacheFile(img);
            if (file != null && file.exists())
            {
                return file.getAbsolutePath();
            }
        }
        return img;
    }

    static class PlatformActionListenerProxy implements PlatformActionListener
    {
        PlatformActionListener callback;

        public PlatformActionListenerProxy(PlatformActionListener callback)
        {
            this.callback = callback;
        }

        public void onError(Platform paramPlatform, int paramInt, Throwable paramThrowable)
        {
            // 失败

            String expName = paramThrowable.getClass().getSimpleName();
            if ("WechatClientNotExistException".equals(expName) || "WechatTimelineNotSupportedException".equals(expName)
                    || "WechatFavoriteNotSupportedException".equals(expName))
            {
                ToastHelper.showToast("没有安装微信");
            } else if ("QQClientNotExistException".equals(expName))
            {
                ToastHelper.showToast("没有安装QQ");
            } else
            {
                ToastHelper.showToast(RT.application.getString(R.string.share_fail));
            }
            if (callback != null)
                callback.onError(paramPlatform, paramInt, paramThrowable);
        }

        @Override
        public void onComplete(Platform paramPlatform, int paramInt, HashMap<String, Object> paramHashMap)
        {
            // 成功
            ToastHelper.showToast(RT.application.getString(R.string.share_success));
            if (callback != null)
                callback.onComplete(paramPlatform, paramInt, paramHashMap);
        }

        @Override
        public void onCancel(Platform paramPlatform, int paramInt)
        {
            // 取消
            if (callback != null)
                callback.onCancel(paramPlatform, paramInt);
        }
    };
}
