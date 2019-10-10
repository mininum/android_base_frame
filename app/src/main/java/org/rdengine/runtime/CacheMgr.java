package org.rdengine.runtime;

import org.rdengine.util.FileUtils;
import org.rdengine.util.StringUtil;

import java.io.File;
import java.util.ArrayList;

/**
 * 缓存管理
 * 
 * @author CCCMAX
 */
public class CacheMgr
{
    private static String TAG = "CacheMgr";

    private static CacheMgr mMgr;

    private long cache_size = 0;

    private CacheMgr()
    {
        // 文本缓存 网络请求等
        // RT.defaultCache

        // 音频文件缓存
        // RT.defaultVoice

        // 图片类缓存
        // RT.defaultScreenshot
        // RT.defaultImage
        // RT.tempImage

        // 日志和异常 可以不清理
        // RT.defaultError
        // RT.defaultLog
    }

    public static CacheMgr getInstance()
    {
        if (mMgr == null)
        {
            mMgr = new CacheMgr();
        }
        return mMgr;
    }

    public String getCacheSize()
    {
        return StringUtil.getUnitBySize(cache_size);
    }

    /**
     * 扫描缓存大小
     */
    public void scanAllCacheSize(final Runnable callback)
    {
        new Thread(new Runnable()
        {
            public void run()
            {
                ArrayList<String> paths = new ArrayList<String>();
                paths.add(RT.defaultCache);// 文本缓存
                paths.add(RT.defaultScreenshot);// 截屏
                paths.add(RT.defaultImage);// 图片缓存 包含tempImage
                paths.add(RT.defaultVoice);// 语音缓存

                long size = 0;
                for (String path : paths)
                {
                    File file = new File(path);
                    long s = FileUtils.getDirSize(file);
                    size += s;
                }
                cache_size = size;

                if (callback != null)
                {
                    RT.getMainHandler().post(callback);
                }
            }
        }).start();
    }

    public void clearAllCache(final Runnable callback)
    {
        new Thread(new Runnable()
        {
            public void run()
            {
                try
                {
                    ArrayList<String> paths = new ArrayList<String>();
                    paths.add(RT.defaultCache);// 文本缓存
                    paths.add(RT.defaultScreenshot);// 截屏
                    paths.add(RT.defaultImage);// 图片缓存 包含tempImage
                    paths.add(RT.defaultVoice);// 语音缓存
                    paths.add(RT.defaultVersion);// 新版本安装包
                    for (String path : paths)
                    {
                        File file = new File(path);
                        FileUtils.deleteAllFilesInDir(file);
                    }
                    cache_size = 0;
                } catch (Exception e)
                {
                }

                clearWebviewCache();

                if (callback != null)
                {
                    RT.getMainHandler().post(callback);
                }
            }
        }).start();
    }

    public void clearTempImage(final Runnable callback)
    {
        new Thread(new Runnable()
        {
            public void run()
            {
                try
                {
                    File file = new File(RT.tempImage);
                    FileUtils.deleteAllFilesInDir(file);
                } catch (Exception e)
                {
                }

                if (callback != null)
                {
                    RT.getMainHandler().post(callback);
                }
            }
        }).start();
    }

    public void clearWebviewCache()
    {
        try
        {
            // webview缓存
            RT.application.deleteDatabase("webview.db");
            RT.application.deleteDatabase("webview.db-shm");
            RT.application.deleteDatabase("webview.db-wal");
            RT.application.deleteDatabase("webviewCache.db");
            RT.application.deleteDatabase("webviewCache.db-shm");
            RT.application.deleteDatabase("webviewCache.db-wal");
        } catch (Exception e)
        {
        }
    }
}
