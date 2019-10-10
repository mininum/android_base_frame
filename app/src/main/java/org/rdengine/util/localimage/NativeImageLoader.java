package org.rdengine.util.localimage;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.view.View;

import org.rdengine.log.DLOG;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 取本地图
 * 
 * @author chengbo
 */
public class NativeImageLoader
{
    private static final String TAG = NativeImageLoader.class.getSimpleName();
    private static NativeImageLoader mInstance = new NativeImageLoader();
    private static LruCache<String, Bitmap> mMemoryCache;
    private static HashMap<View, Future<?>> runnables = new HashMap<View, Future<?>>();
    private ExecutorService mImageThreadPool = Executors.newFixedThreadPool(1);

    private NativeImageLoader()
    {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory());

        final int cacheSize = maxMemory / 4;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize)
        {

            @Override
            protected int sizeOf(String key, Bitmap bitmap)
            {
                return bitmap.getRowBytes() * bitmap.getHeight();
            }
        };
    }

    /**
     * @return
     */
    public static NativeImageLoader getInstance()
    {
        return mInstance;
    }

    /**
     * @param path
     * @param mCallBack
     * @return
     */
    public void loadNativeImage(View view, final String path, final NativeImageCallBack mCallBack)
    {
        this.loadNativeImage(view, path, 0, 0, mCallBack);
    }

    /**
     * @param path
     * @param mPoint
     * @param mCallBack
     * @return
     */
    public void loadNativeImage(final View view, final String path, final int width, final int height,
            final NativeImageCallBack mCallBack)
    {

        final Bitmap bitmap = getBitmapFromMemCache(path);

        final Handler mHander = new Handler()
        {

            @Override
            public void handleMessage(Message msg)
            {
                super.handleMessage(msg);
                if (msg.obj == null)
                {
                    mCallBack.onImageLoader(null, path);
                } else
                {
                    mCallBack.onImageLoader((Bitmap) msg.obj, path);
                }
            }

        };

        if (bitmap == null)
        {
            Message msg = mHander.obtainMessage();
            msg.obj = null;
            mHander.sendMessage(msg);

            if (runnables.get(view) != null)
            {
                if (runnables.get(view).isCancelled() || runnables.get(view).isDone())
                {

                } else
                {
                    runnables.get(view).cancel(true);
                }
            }

            Runnable runnable = new Runnable()
            {

                @Override
                public void run()
                {
                    Bitmap mBitmap = decodeThumbBitmapForFile(path, width, height);
                    addBitmapToMemoryCache(path, mBitmap);
                    Message msg = mHander.obtainMessage();
                    msg.obj = mBitmap;
                    if (runnables.containsKey(view))
                        mHander.sendMessage(msg);
                }
            };
            Future<?> mFuture = mImageThreadPool.submit(runnable);
            runnables.put(view, mFuture);
        } else
        {
            Message msg = mHander.obtainMessage();
            msg.obj = bitmap;
            mHander.sendMessage(msg);
        }
        // return bitmap;

    }

    public void pause()
    {

    }

    public void resume()
    {

    }

    /**
     * @param key
     * @param bitmap
     */
    private void addBitmapToMemoryCache(String key, Bitmap bitmap)
    {
        if (getBitmapFromMemCache(key) == null && bitmap != null)
        {
            mMemoryCache.put(key, bitmap);
        }
    }

    /**
     * @param key
     * @return
     */
    private Bitmap getBitmapFromMemCache(String key)
    {

        Bitmap bitmap = mMemoryCache.get(key);

        if (bitmap != null)
        {
            // DLOG.i(TAG, "get image for MemCache , path = " + key);
        }

        return bitmap;
    }

    /**
     * @param path
     * @param viewWidth
     * @param viewHeight
     * @return
     */
    private Bitmap decodeThumbBitmapForFile(String path, int viewWidth, int viewHeight)
    {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        options.inSampleSize = computeScale(options, viewWidth, viewHeight);

        options.inJustDecodeBounds = false;

        DLOG.e(TAG, "get Iamge form file,  path = " + path);
        return BitmapFactory.decodeFile(path, options);
    }

    /**
     * @param options
     * @param width
     * @param height
     */
    private int computeScale(BitmapFactory.Options options, int viewWidth, int viewHeight)
    {
        int inSampleSize = 1;
        if (viewWidth == 0 || viewWidth == 0)
        {
            return inSampleSize;
        }
        int bitmapWidth = options.outWidth;
        int bitmapHeight = options.outHeight;

        // ����Bitmap�Ŀ�Ȼ�߶ȴ��������趨ͼƬ��View�Ŀ�ߣ��������ű���
        if (bitmapWidth > viewWidth || bitmapHeight > viewWidth)
        {
            int widthScale = Math.round((float) bitmapWidth / (float) viewWidth);
            int heightScale = Math.round((float) bitmapHeight / (float) viewWidth);

            // Ϊ�˱�֤ͼƬ����ű��Σ�����ȡ��߱�����С���Ǹ�
            inSampleSize = widthScale < heightScale ? widthScale : heightScale;
        }
        return inSampleSize;
    }

    public void release()
    {
        if (mMemoryCache != null)
        {
            mMemoryCache.evictAll();
        }

        runnables.clear();
    }

    /**
     * ���ر���ͼƬ�Ļص�ӿ�
     * 
     * @author xiaanming
     */
    public interface NativeImageCallBack
    {
        /**
         * �����̼߳������˱��ص�ͼƬ����Bitmap��ͼƬ·���ص��ڴ˷�����
         * 
         * @param bitmap
         * @param path
         */
        public void onImageLoader(Bitmap bitmap, String path);
    }
}
