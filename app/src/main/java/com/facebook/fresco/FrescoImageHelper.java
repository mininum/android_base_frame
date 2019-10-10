package com.facebook.fresco;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.BaseDataSubscriber;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.drawable.ScalingUtils.ScaleType;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.fresco.FrescoConfigConstants.ActualRatioControllerListener;
import com.facebook.fresco.FrescoConfigConstants.FrescoPreHandleListener;
import com.facebook.fresco.FrescoParam.QiniuParam;
import com.facebook.imagepipeline.animated.base.AnimatedImage;
import com.facebook.imagepipeline.animated.base.AnimatedImageFrame;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.image.CloseableAnimatedImage;
import com.facebook.imagepipeline.image.CloseableBitmap;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.android.frame.R;

import org.rdengine.log.DLOG;
import org.rdengine.runtime.RT;
import org.rdengine.util.FileUtils;
import org.rdengine.util.StringUtil;

import java.io.File;
import java.io.FileInputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FrescoImageHelper
{

    public static boolean log()
    {
        return RT.IMAGELOG;
    }

    /**
     * 通过图片URL获取本地缓存文件
     * 
     * @param image_net_url
     * @return
     */
    public static File getImageDiskCacheFile(String image_net_url)
    {
        return FrescoConfigConstants.getImageDiskCacheFile(image_net_url);
    }

    /**
     * 把一个本地图片添加成指定网络URL的图片缓存
     * 
     * @param localImgPath
     *            本地图片路径
     * @param netImgUrl
     *            本地图片对应的网络地址
     * @return 是否成功
     */
    public static boolean addDiskCacheFromLocalImg(String localImgPath, String netImgUrl)
    {
        boolean ret = false;
        String cachepath = null;
        try
        {
            if (!StringUtil.isEmpty(localImgPath) && !StringUtil.isEmpty(netImgUrl))
            {
                File localFile = new File(localImgPath);
                if (localFile != null && localFile.exists())
                {
                    cachepath = FrescoConfigConstants.getImageDiskCachePath(netImgUrl);
                    File cacheFile = new File(cachepath);
                    if (cacheFile != null)
                    {
                        if (cacheFile.exists())
                        {
                            ret = true; // netImgUrl对应的本地缓存文件已经存在
                        } else
                        {
                            FileUtils.fileChannelCopy(localFile, cacheFile);// 文件通道对拷
                            ret = cacheFile.exists();// copy是否成功
                        }
                    }
                }
            }
        } catch (Exception e)
        {
        }
        // Log.e("cccmax", "addDiskCacheFromLocalImg ret=" + ret + "\n localImgPath=" + localImgPath + "\n netImgUrl="
        // + netImgUrl + "\ncachepath=" + cachepath);
        return ret;
    }

    /**
     * 创建SimpleDraweeView
     * 
     * @param context
     * @return
     */
    public static SimpleDraweeView createView(Context context)
    {
        SimpleDraweeView view = new SimpleDraweeView(context, FrescoConfigConstants.getGenericDraweeHierarchy(context));
        return view;
    }

    /**
     * 创建SimpleDraweeView
     * 
     * @param context
     * @param hierarchy
     * @return
     */
    public static SimpleDraweeView createView(Context context, GenericDraweeHierarchy hierarchy)
    {
        SimpleDraweeView view = new SimpleDraweeView(context, hierarchy);
        return view;
    }

    // /**
    // * 简单的获取图片
    // *
    // * @param uri
    // * 图片地址
    // * @param view
    // */
    // public static void getImage(String uri, SimpleDraweeView view)
    // {
    // ImageRequest imageRequest = FrescoConfigConstants.getImageRequest(view,
    // uri);
    // DraweeController draweeController =
    // FrescoConfigConstants.getDraweeController(imageRequest, view);
    // view.setController(draweeController);
    // }
    //
    // /**
    // * 简单的获取图片 自定义控制器
    // *
    // * @param uri
    // * 图片地址
    // * @param view
    // * @param controllerlistener
    // * 控制器回调
    // */
    // public static void getImage(String uri, SimpleDraweeView view,
    // BaseControllerListener controllerlistener)
    // {
    // ImageRequest imageRequest = FrescoConfigConstants.getImageRequest(view,
    // uri);
    // DraweeController draweeController =
    // FrescoConfigConstants.getDraweeController(imageRequest, view,
    // controllerlistener);
    // view.setController(draweeController);
    // }

    /**
     * 获取图片 图片高度按真实比例设置(获取到图片后自动计算的)
     * 
     * @param param
     * @param view
     * @param ratio_max
     */
    public static void getImage_ChangeRatio(FrescoParam param, SimpleDraweeView view, float... ratio_max)
    {
        try
        {
            ActualRatioControllerListener l = new ActualRatioControllerListener(view);
            if (ratio_max != null && ratio_max.length > 0)
                l.ratio_max = ratio_max[0];
            getImage(param, view, l);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * 获取图片 加载到SimpleDraweeView中，支持Gif图片，自动释放内存
     * 
     * @param param
     * @param view
     *            需要用SimpleDraweeView来替代ImageView
     * @param controllerlistener
     *            加载图片的特殊操作可以在这里加，例如ActualRatioControllerListener
     */
    public static void getImage(FrescoParam param, SimpleDraweeView view, FrescoPreHandleListener controllerlistener)
    {

        if (param == null)
            return;

        try
        {
            // 设置圆角、圆形 ，gif无效
            RoundingParams rp = view.getHierarchy().getRoundingParams();
            if (!param.isNoRoundingParams())
            {
                if (rp == null)
                    rp = new RoundingParams();
                rp.setRoundAsCircle(param.getRoundAsCircle());
                if (!param.getRoundAsCircle())
                {
                    rp.setCornersRadii(param.getRadius_TL(), param.getRadius_TR(), param.getRadius_BR(),
                            param.getRadius_BL());
                }
                view.getHierarchy().setRoundingParams(rp);
            }
            // 设置描边颜色、宽度
            if (param.getBordeWidth() >= 0 && rp != null)
            {
                rp.setBorder(param.getBordeColor(), param.getBordeWidth());
                view.getHierarchy().setRoundingParams(rp);
            }

            // 默认图片（占位图）
            if (param.DefaultImageID > 0)
            {
                // view.getHierarchy().setPlaceholderImage(param.DefaultImageID);
                view.getHierarchy().setPlaceholderImage(
                        view.getContext().getResources().getDrawable(param.DefaultImageID), param.scaletype_def);
            }

            // 失败图
            if (param.FailureImageID > 0)
            {
                view.getHierarchy().setFailureImage(view.getContext().getResources().getDrawable(param.FailureImageID),
                        param.scaletype_def);
            }

            // view缩放模式
            view.getHierarchy().setActualImageScaleType(param.scaletype);

            // scaletype模式的焦点
            if (param.scaleFocusPoint != null)
                view.getHierarchy().setActualImageFocusPoint(param.scaleFocusPoint);

            // 请求
            ImageRequest imageRequest = FrescoConfigConstants.getImageRequest(view, param.getURI(),
                    param.getExpectantImgSize());

            // 图片请求log 包括 分辨率、比例、uri
            // if (controllerlistener == null) {
            // final String uri = param.getURI();
            // controllerlistener = new BaseControllerListener<Object>() {
            //
            // public void onFinalImageSet(String id, Object imageInfo, Animatable animatable) {
            // if (imageInfo != null && imageInfo instanceof ImageInfo) {
            // boolean isGif = CloseableAnimatedImage.class.isInstance(imageInfo);
            // ImageInfo ii = (ImageInfo) imageInfo;
            // int width = ii.getWidth();
            // int height = ii.getHeight();
            // float ratio = width * 1.0F / (height == 0 ? width : height);
            // DLOG.i("fresco", "BaseControllerListener onFinalImageSet w=" + width + " h=" + height
            // + " isGif=" + isGif + " ratio=" + ratio + " -----URI=" + uri);
            // }
            // };
            // };
            // }

            DraweeController draweeController = null;
            if (controllerlistener == null)
            {
                // draweeController = FrescoConfigConstants.getDraweeController(imageRequest,
                // param.getClickToRetryEnabled(), view);

                // 加一个默认处理 其中有必要的Resize处理
                controllerlistener = new FrescoPreHandleListener(view)
                {

                    public void handle(ImageInfo ii, boolean isgif, int w, int h, float _ratio)
                    {}
                };
                draweeController = FrescoConfigConstants.getDraweeController(imageRequest,
                        param.getClickToRetryEnabled(), view, param.getAutoPlayAnimations(), controllerlistener);
            } else
            {
                draweeController = FrescoConfigConstants.getDraweeController(imageRequest,
                        param.getClickToRetryEnabled(), view, param.getAutoPlayAnimations(), controllerlistener);
            }
            view.setController(draweeController);

        } catch (Exception e)
        {
            DLOG.e("fresco", "getImage exception", e);
        }
    }

    /**
     * 单纯的请求图片和view无关，加回调后可以做自定义操作
     * 
     * @param param
     * @param callback
     *            请求图片回调
     * @param onUIcallback
     *            是否在UI线程执行callback
     */
    public static void getImage(FrescoParam param, CloseableImageCallback callback, boolean onUIcallback)
    {

        try
        {
            ImageRequest imageRequest = FrescoConfigConstants.getImageRequest(null, param.getURI(),
                    param.getExpectantImgSize());
            ImagePipeline imagePipeline = Fresco.getImagePipeline();
            DataSource<CloseableReference<CloseableImage>> dataSource = imagePipeline.fetchDecodedImage(imageRequest,
                    null);
            FrescoBaseDataSubscriber dataSubscriber = new FrescoBaseDataSubscriber(callback, onUIcallback);
            dataSource.subscribe(dataSubscriber, getExecutor());
        } catch (Exception e)
        {
            DLOG.e("fresco", "getImage CloseableImageCallback exception", e);
        }
    }

    /**
     * 从bitmap内存缓存中获取
     * 
     * @param param
     * @return
     */
    public static Bitmap getMemoryCachedImage(FrescoParam param)
    {
        ImageRequest imageRequest = FrescoConfigConstants.getImageRequest(null, param.getURI(),
                param.getExpectantImgSize());
        DataSource<CloseableReference<CloseableImage>> dataSource = Fresco.getImagePipeline()
                .fetchImageFromBitmapCache(imageRequest, null);
        CloseableReference<CloseableImage> imageReference = null;
        try
        {
            imageReference = dataSource.getResult();
            if (imageReference != null)
            {
                CloseableImage image = imageReference.get();
                // do something with the image
                if (image instanceof CloseableBitmap)
                {
                    return ((CloseableBitmap) image).getUnderlyingBitmap();
                }
            }
        } finally
        {
            dataSource.close();
            CloseableReference.closeSafely(imageReference);
        }
        return null;
    }

    /**
     * 从disk缓存中获取图片
     * 
     * @param param
     * @return
     */
    public static Bitmap getDiskCachedImage(FrescoParam param)
    {
        try
        {
            File file = getImageDiskCacheFile(param.getURI());
            if (file != null && file.exists())
            {

                BitmapFactory.Options newOpts = new BitmapFactory.Options();
                newOpts.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(file.getAbsolutePath(), newOpts);
                int w = newOpts.outWidth;
                int h = newOpts.outHeight;
                float hh = 1024;
                float ww = 1024;
                int SCALE = 1;
                if (w > h && w > ww)
                {// 如果宽度大的话根据宽度固定大小缩放
                    SCALE = (int) (newOpts.outWidth / ww);
                } else if (w < h && h > hh)
                {// 如果高度高的话根据宽度固定大小缩放
                    SCALE = (int) (newOpts.outHeight / hh);
                }
                if (SCALE <= 1)
                    SCALE = 1;
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = false;
                options.inSampleSize = SCALE;
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
                return bitmap;
            }
        } catch (Exception e)
        {
        }
        return null;
    }

    /**
     * 获取已经缓存的图片的尺寸 （宽高）
     * 
     * @param param
     * @return
     */
    public static Point getCachedImageSize(FrescoParam param)
    {
        Point ret = null;

        try
        {
            ImageRequest imageRequest = FrescoConfigConstants.getImageRequest(null, param.getURI(),
                    param.getExpectantImgSize());
            DataSource<CloseableReference<CloseableImage>> dataSource = Fresco.getImagePipeline()
                    .fetchImageFromBitmapCache(imageRequest, null);
            CloseableReference<CloseableImage> imageReference = null;
            try
            {
                imageReference = dataSource.getResult();
                if (imageReference != null)
                {
                    CloseableImage image = imageReference.get();
                    if (image != null)
                    {
                        ret = new Point(image.getWidth(), image.getHeight());
                    }
                }
            } finally
            {
                dataSource.close();
                CloseableReference.closeSafely(imageReference);
            }
        } catch (Exception e)
        {
        }

        if (ret == null)
        {
            try
            {
                File file = getImageDiskCacheFile(param.getURI());
                if (file != null && file.exists())
                {
                    BitmapFactory.Options newOpts = new BitmapFactory.Options();
                    newOpts.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(file.getAbsolutePath(), newOpts);
                    int w = newOpts.outWidth;
                    int h = newOpts.outHeight;
                    ret = new Point(w, h);
                }
            } catch (Exception e)
            {
            }
        }
        if (ret != null && (ret.x <= 0 || ret.y <= 0))
            ret = null;

        if (log() && ret != null)
        {
            DLOG.d("fresco", "getCachedImageSize " + ret.toString());
        }

        return ret;
    }

    private static ExecutorService executor = null;

    private static ExecutorService getExecutor()
    {
        if (executor == null)
            executor = Executors.newFixedThreadPool(3);
        return executor;
    }

    public static interface CloseableImageCallback
    {

        public void callback(CloseableImage image, Bitmap bitmap);
    }

    /** fresco数据源请求监听 */
    private static class FrescoBaseDataSubscriber extends BaseDataSubscriber<CloseableReference<CloseableImage>>
    {

        boolean callbackOnUI = true;
        CloseableImageCallback callback;

        public FrescoBaseDataSubscriber(CloseableImageCallback cb, boolean callbackOnUI)
        {
            callback = cb;
            this.callbackOnUI = callbackOnUI;
        }

        protected void onNewResultImpl(DataSource<CloseableReference<CloseableImage>> dataSource)
        {

            // if (!dataSource.isFinished()) {
            // Log.e("fresco",
            // "Not yet finished - this is just another progressive scan.");
            // }

            CloseableReference<CloseableImage> imageReference = dataSource.getResult();
            if (imageReference != null)
            {
                try
                {
                    final CloseableImage image = imageReference.get();
                    if (image != null)
                    {

                        if (log())
                            DLOG.i("fresco", "onNewResultImpl " + " w=" + image.getWidth() + " h=" + image.getHeight()
                                    + " image=" + image);

                        Bitmap bitmap = null;

                        if (image instanceof CloseableBitmap)
                        {
                            // jpg png
                            bitmap = ((CloseableBitmap) image).getUnderlyingBitmap();
                        } else if (image instanceof CloseableAnimatedImage)
                        {
                            // GIF WEBP
                            try
                            {
                                CloseableAnimatedImage cai = (CloseableAnimatedImage) image;
                                if (cai.getImageResult().getPreviewBitmap() != null)
                                    bitmap = cai.getImageResult().getPreviewBitmap().get();
                                if (bitmap == null)
                                {
                                    AnimatedImage ai = cai.getImage();
                                    if (ai != null && ai.getFrameCount() > 0)
                                    {
                                        AnimatedImageFrame aif = ai.getFrame(0);
                                        if (aif != null)
                                        {
                                            bitmap = Bitmap.createBitmap(aif.getWidth(), aif.getHeight(),
                                                    Config.ARGB_8888);
                                            aif.renderFrame(aif.getWidth(), aif.getHeight(), bitmap);
                                        }
                                    }
                                }
                                if (bitmap != null)
                                {
                                    Bitmap tmp = bitmap.copy(Config.RGB_565, true);
                                    bitmap.recycle();
                                    bitmap = null;
                                    bitmap = tmp;
                                }
                            } catch (Exception e)
                            {
                            }
                        }

                        if (callbackOnUI)
                        {
                            final Bitmap tmpbitmap = bitmap;
                            Runnable runnable = new Runnable()
                            {

                                public void run()
                                {
                                    callback.callback(image, tmpbitmap);
                                }
                            };
                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.post(runnable);
                        } else
                        {
                            callback.callback(image, bitmap);
                        }
                    }

                } finally
                {
                    imageReference.close();
                }
            }
        }

        protected void onFailureImpl(DataSource dataSource)
        {
            try
            {
                Throwable throwable = dataSource.getFailureCause();
                Log.e("fresco", "onFailureImpl Throwable＝" + throwable.getMessage());

                // handle failure
            } catch (Exception e)
            {
            }
        }
    }

    /**
     * 清除某一个图片的缓存
     * 
     * @param param
     */
    public static void evictFromCache(FrescoParam param)
    {
        try
        {
            Fresco.getImagePipeline().evictFromCache(Uri.parse(param.getURI()));
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    // --------------------------fresco框架加载图片设置给ImageView

    private static HashMap<CloseableImageCallback, WeakReference<View>> mViews = new HashMap<CloseableImageCallback, WeakReference<View>>();

    /**
     * 获取图片 加载到ImageView中，不支持Gif图片
     * 
     * @param param
     * @param view
     *            ImageView
     */
    public static void getImage(FrescoParam param, ImageView view)
    {
        if (param == null || view == null)
            return;

        if (view instanceof SimpleDraweeView)
        {
            getImage(param, (SimpleDraweeView) view, null);
            return;
        }

        // final int key = param.getURI().hashCode();
        if (param.DefaultImageID != 0 && view.getDrawable() == null)
        {
            // 如果有Imageview已经设置了图片 drawable不是null，就不替换默认图了
            view.setImageResource(param.DefaultImageID);
        }
        CloseableImageCallback cicb = new CloseableImageCallback()
        {

            public void callback(CloseableImage image, Bitmap bitmap)
            {
                if (log())
                    DLOG.d("fresco", "getImageToImageView bitmap=" + bitmap);
                try
                {
                    if (bitmap != null && !bitmap.isRecycled())
                    {
                        FrescoConfigConstants.autoResizeBitmap(bitmap, null);
                        WeakReference<View> br = mViews.get(this);
                        View v = null;
                        if (br != null)
                        {
                            v = br.get();
                        }
                        if (v != null && (v instanceof ImageView))
                        {
                            ((ImageView) v).setImageBitmap(bitmap);
                        }
                    }
                } catch (Exception e)
                {
                    DLOG.e("frescoToImageview", "CloseableImageCallback exception", e);
                }
                mViews.remove(this);
            }
        };
        mViews.put(cicb, new WeakReference<View>(view));
        getImage(param, cicb, true);
    }

    /**
     * 给Fresco图片控件设置图片
     * 
     * @param uri
     *            网络地址、磁盘文件、asset、res
     * @param view
     *            SimpleDraweeView、FrescoImageView
     */
    public static void getImage(String uri, QiniuParam qp, SimpleDraweeView view)
    {
        FrescoParam fp = new FrescoParam(uri, qp);
        int defimageID = R.drawable.def_image;
        int failimageID = R.drawable.def_image_failure;
        if (qp != null && (qp == QiniuParam.C_L || qp == QiniuParam.Z_MAX_L || qp == QiniuParam.Z_MIN_L))
        {
            defimageID = R.drawable.def_image_big;
            failimageID = R.drawable.def_image_big_failure;
        }
        fp.setScaleTypeDef(ScaleType.FIT_XY);
        fp.setDefaultImage(defimageID);// 默认图
        fp.setFailureImage(failimageID);// 失败图
        getImage(fp, view, null);
    }

    /**
     * 给普通的ImageView设置图片
     * 
     * @param uri
     *            网络地址、磁盘文件、asset、res
     * @param qp
     * @param view
     */
    public static void getImage(String uri, QiniuParam qp, FrescoImageView view)
    {
        if (view instanceof SimpleDraweeView)
        {
            getImage(uri, qp, (SimpleDraweeView) view);
            return;
        }
        FrescoParam fp = new FrescoParam(uri, qp);
        int defimageID = R.drawable.def_image;
        int failimageID = R.drawable.def_image_failure;
        if (qp != null && (qp == QiniuParam.C_L || qp == QiniuParam.Z_MAX_L || qp == QiniuParam.Z_MIN_L))
        {
            defimageID = R.drawable.def_image_big;
            failimageID = R.drawable.def_image_big_failure;
        }
        fp.setScaleTypeDef(ScaleType.FIT_XY);
        fp.setDefaultImage(defimageID);// 默认图
        fp.setFailureImage(failimageID);// 失败图

        getImage(fp, view);
    }

    // -----------------------------------------------获取用户头像 start

    /**
     * 获取用户头像
     * 
     * @param uri
     *            网络地址、磁盘文件、asset、res
     * @param view
     *            SimpleDraweeView
     */
    private static void getAvatar(String uri, QiniuParam qp, SimpleDraweeView view)
    {
        FrescoParam fp = new FrescoParam(uri, qp);
        fp.setDefaultImage(R.drawable.icon_mine_user);// 默认图
        // 强制给头像默认图设置center_crop
        view.getHierarchy().setPlaceholderImage(view.getContext().getResources().getDrawable(fp.DefaultImageID),
                ScaleType.CENTER_CROP);
        getImage(fp, view, null);
    }

    /**
     * 获取用户头像 小
     * 
     * @param uri
     *            网络地址、磁盘文件、asset、res
     * @param view
     *            SimpleDraweeView
     */
    public static void getAvatar_S(String uri, SimpleDraweeView view)
    {
        getAvatar(uri, QiniuParam.C_S, view);
    }

    /**
     * 获取用户头像 中
     * 
     * @param uri
     *            网络地址、磁盘文件、asset、res
     * @param view
     *            SimpleDraweeView
     */
    public static void getAvatar_M(String uri, SimpleDraweeView view)
    {
        getAvatar(uri, QiniuParam.C_M, view);
    }

    /**
     * 获取用户头像 大
     * 
     * @param uri
     *            网络地址、磁盘文件、asset、res
     * @param view
     *            SimpleDraweeView
     */
    public static void getAvatar_L(String uri, SimpleDraweeView view)
    {
        getAvatar(uri, QiniuParam.C_L, view);
    }

    // -----------------------------------------------获取用户头像 end

    public static ArrayList<String> getRandomImageUrlList(int size)
    {
        if (size > urls_test.length)
            size = urls_test.length;
        ArrayList<String> ret = new ArrayList<String>();
        for (int i = 0; i < size; i++)
        {
            try
            {
                String path = "";
                do
                {
                    path = getRandomImageUrl();
                } while (ret.contains(path));
                ret.add(path);
            } catch (Exception e)
            {
            }
        }
        return ret;
    }

    public static String getRandomImageUrl()
    {

        int number = new Random().nextInt(urls_test.length - 1);
        return urls_test[number];
    }

    private static String[] urls_test = new String[]
    { "http://i-2.yxdown.com/2014/8/9/4ab8f770-dc64-4caf-8dcb-530812dd135f.gif",
            "http://fdfs.xmcdn.com/group5/M01/2E/8D/wKgDtVONtOaCCgiQAAHhAAnF2BY165_web_large.jpg",
            "http://ww1.sinaimg.cn/thumb180/63fb4ad5gw6denp4x9fduj.jpg",
            "http://www.sc115.com/wenku/uploads/allimg/121214/23440332q-0.jpg",
            "http://b.hiphotos.baidu.com/image/h%3D360/sign=d45d77b8b0b7d0a264c9029bfbee760d/b2de9c82d158ccbf15e8ae301bd8bc3eb1354167.jpg",
            "http://dimg04.c-ctrip.com/images/hhtravel/713/698/484/9159cabcda424168948cd82fb47f5f8d_C_250_140_Q80.jpg",
            "http://p1.pstatp.com/large/a78000825b7fd4bc35f",
            //
            // "http://img0.imgtn.bdimg.com/it/u=4238221358,1269566828&fm=21&gp=0.jpg",
            // "http://img2.imgtn.bdimg.com/it/u=3554169248,909688534&fm=11&gp=0.jpg",
            // "http://img5.imgtn.bdimg.com/it/u=1640990049,1096835342&fm=21&gp=0.jpg",
            // "http://img5.imgtn.bdimg.com/it/u=1917782863,3035268524&fm=21&gp=0.jpg",
            // "http://img2.imgtn.bdimg.com/it/u=1571982504,4022887356&fm=21&gp=0.jpg",
            // "http://img1.imgtn.bdimg.com/it/u=1275529500,3502365509&fm=21&gp=0.jpg",
            // "http://img4.imgtn.bdimg.com/it/u=3059802086,414491492&fm=21&gp=0.jpg",
            // "http://img3.imgtn.bdimg.com/it/u=3460548809,1953321296&fm=21&gp=0.jpg",
            // "http://img3.imgtn.bdimg.com/it/u=4038225167,4007235020&fm=21&gp=0.jpg",
            // "http://img0.imgtn.bdimg.com/it/u=1196515935,3444491293&fm=21&gp=0.jpg",
            // "http://img1.imgtn.bdimg.com/it/u=3760869447,3890165516&fm=21&gp=0.jpg",
            //
            "http://hbimg.b0.upaiyun.com/b135f883a738803d9e3518a2437a478528e27cc940766-z49S8t_/fw/480",
            "http://hbimg.b0.upaiyun.com/234657c13fd7195e2bbb5b0e7044ab55d0d2206979f3e-MkFBcM_/fw/480",
            "http://hbimg.b0.upaiyun.com/3f55596ac610e7b7a15aed6906ba8de510e809cb24f7a-vhRhIZ_/fw/480",
            "http://hbimg.b0.upaiyun.com/ee26878226bc005e56838be891c86ef4c01268cb39585-1c1JNJ_/fw/480",
            "http://hbimg.b0.upaiyun.com/07e9ff7aeb8836158a2957362d7ee1a5fc08610c88e53-JF9pUJ_/fw/480",
            "http://hbimg.b0.upaiyun.com/4db81d108247a601a9bb5ffc31fc19ceaba228a6331a6-90pmR7_fw658",
            "http://hbimg.b0.upaiyun.com/fd12bbc45925bd9a2fabf2e5436546b4ad4597ca4c8e0-ZR2NIx_fw658",
            "http://hbimg.b0.upaiyun.com/e795d344670481dfbf0dccfc7afa075e6fda8a0313de3-EegRYS_fw658",
            "http://hbimg.b0.upaiyun.com/f326f085258efae31d23c00e9ff21ecea1a63d7eee626-lxVUPb_fw658",
            "http://hbimg.b0.upaiyun.com/b09d7031ddd81d268ae74a883fa14b55191dde089b67-JZlsgE_fw658",
            "http://hbimg.b0.upaiyun.com/dc6b8d4f39382c714b8d6b80d4e4978447285ed81859b-pSrMkk_fw658",
            "http://hbimg.b0.upaiyun.com/cac72e2ae03464c29c9e3afc4c46a88c1c171db4a360-52lsQB_fw658",
            "http://hbimg.b0.upaiyun.com/0408ff1045b0c497101d0bfe4be4110c867eb912103c3-wkR2Fu_fw658",

    };

    /**
     * 判断一个本地图片是不是GIF
     * 
     * @param localImagePath
     * @return
     */
    public static boolean isGifWithLocalImage(String localImagePath)
    {
        FileInputStream imgFile = null;
        byte[] b = new byte[10];
        int l = -1;
        String s = "";
        try
        {
            imgFile = new FileInputStream(localImagePath);
            l = imgFile.read(b);
            imgFile.close();
            s = new String(b);
        } catch (Exception e)
        {
            return false;
        }
        if (l == 10)
        {
            byte b0 = b[0];
            byte b1 = b[1];
            byte b2 = b[2];
            byte b3 = b[3];
            byte b6 = b[6];
            byte b7 = b[7];
            byte b8 = b[8];
            byte b9 = b[9];
            if (b0 == (byte) 'G' && b1 == (byte) 'I' && b2 == (byte) 'F')
            {
                return true;
            }
        }
        return false;
    }
}
