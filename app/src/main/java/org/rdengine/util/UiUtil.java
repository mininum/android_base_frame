package org.rdengine.util;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLES10;
import android.opengl.GLES20;
import android.os.Build;
import android.support.annotation.IntDef;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;

import org.rdengine.log.DLOG;
import org.rdengine.runtime.RT;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.co.chrisjenx.calligraphy.TypefaceUtils;

public class UiUtil
{
    // 小米
    public final static int TYPE_MIUI = 0;
    // 魅族
    public final static int TYPE_FLYME = 1;
    // android 6.0以上
    public final static int TYPE_M = 3;

    public static void setStatusBarDarkTheme(Context context) {
    }

    @IntDef(
    { TYPE_MIUI, TYPE_FLYME, TYPE_M })
    @Retention(RetentionPolicy.SOURCE)
    @interface ViewType
    {
    }

    /**
     * 透明系统状态栏开关 会改变所有页面titlebar的高度
     */
    public static final boolean TransparentStatusbar = true;

    /** 判断设备是否开启系统状态栏 */
    public static boolean isOpenTransparentStatusbar()
    {
        return TransparentStatusbar && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    private static int statusBarHeight = 0;

    public static int getStatusBarHeight()
    {
        if (statusBarHeight <= 0)
        {
            statusBarHeight = PhoneUtil.getStatusBarHeight(RT.application);
        }
        return statusBarHeight;

    }

    public static void waitViewRealLayout(View v, final Runnable callback)
    {
        waitViewRealLayout(v, callback, 0);
    }

    /**
     * 等待View真实构造在window上，可以用来获取真实尺寸，或者在真实构造之后再做某些事
     * 
     * @param v
     */
    public static void waitViewRealLayout(View v, final Runnable callback, final long delayMillis)
    {
        if (v == null)
            return;

        final WeakReference<View> wv = new WeakReference<View>(v);

        v.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener()
        {

            @SuppressLint("NewApi")
            public void onGlobalLayout()
            {
                if (wv != null && wv.get() != null)
                {
                    try
                    {
                        if (Build.VERSION.SDK_INT < 16)
                        {
                            ViewTreeObserver vto = wv.get().getViewTreeObserver();
                            vto.removeGlobalOnLayoutListener(this);
                        } else
                        {
                            ViewTreeObserver vto = wv.get().getViewTreeObserver();
                            vto.removeOnGlobalLayoutListener(this);
                        }
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                    if (delayMillis <= 0)
                    {
                        // wv.get().post(callback);
                        RT.getMainHandler().post(callback);
                    } else
                    {
                        // wv.get().postDelayed(callback, delayMillis);
                        RT.getMainHandler().postDelayed(callback, delayMillis);
                    }
                }
            }
        });
    }

    /**
     * 点击时间延迟代理，5.0Ripple波纹效果 要有点击延迟等动画结束再操作
     * 
     * @author CCCMAX
     */
    public static class OnItemClickProxy implements AdapterView.OnItemClickListener
    {
        AdapterView.OnItemClickListener mListener;

        public OnItemClickProxy(AdapterView.OnItemClickListener listener)
        {
            mListener = listener;
        }

        public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id)
        {
            if (ClickUtil.isFastDoubleClick())
                return;
            if (mListener == null || mListener instanceof OnItemClickProxy)
                return;
            try
            {
                parent.postDelayed(new Runnable()
                {
                    public void run()
                    {
                        try
                        {
                            mListener.onItemClick(parent, view, position, id);
                        } catch (Throwable ex)
                        {
                            ex.printStackTrace();
                        }
                    }
                }, 150);
            } catch (Exception e)
            {
            }
        }

    }

    /**
     * 点击时间延迟代理，5.0Ripple波纹效果 要有点击延迟等动画结束再操作
     * 
     * @author CCCMAX
     */
    public static class OnClickProxy implements View.OnClickListener
    {
        View.OnClickListener mListener;
        boolean checkFastClick = true;

        public OnClickProxy(View.OnClickListener listener)
        {
            mListener = listener;
        }

        public OnClickProxy(View.OnClickListener listener, boolean checkFastClick)
        {
            mListener = listener;
            this.checkFastClick = checkFastClick;
        }

        @Override
        public void onClick(final View v)
        {
            if (checkFastClick && ClickUtil.isFastDoubleClick())
                return;
            if (mListener == null || mListener instanceof OnClickProxy)
                return;
            try
            {
                v.postDelayed(new Runnable()
                {
                    public void run()
                    {
                        mListener.onClick(v);
                    }
                }, 150);
            } catch (Exception e)
            {
            }
        }

    }

    /**
     * 获取View的位置 相对位置
     * 
     * @param v1
     * @param v2
     * @return
     */
    public static Point getViewRelativeSize(View v1, View v2)
    {
        Point ret = null;
        try
        {
            int[] xy_a = new int[2];
            v1.getLocationOnScreen(xy_a);
            int[] xy_b = new int[2];
            v2.getLocationOnScreen(xy_b);
            ret = new Point();
            ret.x = (xy_b[0] - xy_a[0]);
            ret.y = (xy_b[1] - xy_a[1]);
        } catch (Exception e)
        {
        }
        return ret;
    }

    public static Point measureViewSize(View v)
    {
        return measureViewSize(v, PhoneUtil.getScreenWidth(RT.application));
    }

    /**
     * 测量View 的尺寸 不论是否隐藏 是否绘制了
     * 
     * @param v
     * @return
     */
    public static Point measureViewSize(View v, int parentwidth)
    {
        Point ret = null;
        try
        {
            int width = View.MeasureSpec.makeMeasureSpec(parentwidth, View.MeasureSpec.UNSPECIFIED);
            int height = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            v.measure(width, height);
            int h = v.getMeasuredHeight();
            int w = v.getMeasuredWidth();
            ret = new Point(w, h);
            DLOG.e("UiUtil", "measureViewSize w=" + w + " h=" + h);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * 控制View的硬件加速 ，需要api11以上，api11以下不操作
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static void manageLayer(View v, boolean enableHardware)
    {
        if (Build.VERSION.SDK_INT < 11)
            return;
        int layerType = enableHardware ? View.LAYER_TYPE_NONE : View.LAYER_TYPE_SOFTWARE;
        if (layerType != v.getLayerType())
            v.setLayerType(layerType, null);
    }

    /**
     * Drawable 保留图形和透明度，替换颜色
     * 
     * @param drawable
     * @param desColor
     * @return
     */
    public static Drawable changeDrawableColor(Drawable drawable, int desColor)
    {
        // desColor会全尺寸画到drawable上，src_in是两层图形相交区域覆盖上层
        drawable.setColorFilter(desColor, PorterDuff.Mode.SRC_IN);
        return drawable;
    }

    /**
     * 更改bitmap的颜色，保留原有图形, 性能有消耗，在7.0系统略微卡顿
     * 
     * @param bitmap
     * @param desColor
     * @return
     */
    public static Bitmap changeBitmapColor(Bitmap bitmap, int desColor)
    {
        try
        {
            int w = bitmap.getWidth();
            int h = bitmap.getHeight();
            int r = Color.red(desColor);
            int g = Color.green(desColor);
            int b = Color.blue(desColor);

            int array[] = new int[w * h];
            int n = 0;
            for (int i = 0; i < h; i++)
            {
                for (int j = 0; j < w; j++)
                { // 从上往下扫描
                    int color = bitmap.getPixel(j, i);
                    int A_a = Color.alpha(color);
                    if (A_a > 0)
                    {
                        color = Color.argb(A_a, r, g, b);
                    }
                    array[n] = color;
                    n++;
                }
            }

            bitmap = Bitmap.createBitmap(array, w, h, Bitmap.Config.ARGB_8888);

            return bitmap;

        } catch (Exception e)
        {
            return bitmap;
        }
    }

    public static Bitmap changeBitmapColorMatrix(Bitmap bp, int desColor)
    {
        try
        {
            int r = Color.red(desColor);
            int g = Color.green(desColor);
            int b = Color.blue(desColor);
            float[] color_hsv = new float[3];
            Color.RGBToHSV(r, g, b, color_hsv);
            DLOG.i("cccmax",
                    "RGB=" + r + "," + g + "," + b + " HSV=" + color_hsv[0] + "," + color_hsv[1] + "," + color_hsv[2]);
            return changeBitmapColorMatrix(bp, color_hsv[0], color_hsv[1], color_hsv[2]);
        } catch (Exception ex)
        {
            return bp;
        }
    }

    /**
     * 调整bitmap,HSV色彩空间
     * 
     * @param bp
     *            图片
     * @param hue
     *            色调
     * @param saturation
     *            饱和度
     * @param lum
     *            亮度
     * @return
     */
    public static Bitmap changeBitmapColorMatrix(Bitmap bp, float hue, float saturation, float lum)
    {
        try
        {
            Bitmap bitmap = Bitmap.createBitmap(bp.getWidth(), bp.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

            // 色调
            ColorMatrix hueMatrix = new ColorMatrix();
            hueMatrix.setRotate(0, hue);// R
            hueMatrix.setRotate(1, hue);// G
            hueMatrix.setRotate(2, hue);// B

            // 饱和度
            ColorMatrix saturationMatrix = new ColorMatrix();
            saturationMatrix.setSaturation(saturation);

            // 亮度
            ColorMatrix lumMatrix = new ColorMatrix();
            lumMatrix.setScale(lum, lum, lum, 1);

            ColorMatrix imageMatrix = new ColorMatrix();
            imageMatrix.postConcat(hueMatrix);
            imageMatrix.postConcat(saturationMatrix);
            imageMatrix.postConcat(lumMatrix);

            paint.setColorFilter(new ColorMatrixColorFilter(imageMatrix));
            canvas.drawBitmap(bp, 0, 0, paint);// 此处如果换成bitmap就会仅仅调用一次，图像将不能被编辑
            return bitmap;
        } catch (Exception ex)
        {
            ex.printStackTrace();
            return bp;
        }
    }

    public static Bitmap convertViewToBitmap(View view)
    {
        // view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
        // MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        // view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.setDrawingCacheEnabled(false);
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();
        return bitmap;
    }

    /**
     * 获取一个view的模糊截图，会缩放尺寸 得到的bitmap不大
     * 
     * @param view
     * @return
     */
    public static Bitmap getBaseviewBlur(View view)
    {
        try
        {
            Bitmap bitmap_view = convertViewToBitmap(view);
            // DMImageTool.savePhotoToSDCard(bitmap_view, RT.tempImage + "test_a.jpg");
            Bitmap bitmap_zoom = DMImageTool.zoomBitmap(bitmap_view, bitmap_view.getWidth() / 10,
                    bitmap_view.getHeight() / 10);
            // DMImageTool.savePhotoToSDCard(bitmap_zoom, RT.tempImage + "test_b.jpg");
            bitmap_zoom = FastBlur.doBlur(bitmap_zoom, Math.max(bitmap_zoom.getWidth() / 15, 10), true);
            // DMImageTool.savePhotoToSDCard(bitmap_zoom, RT.tempImage + "test_c.jpg");
            return bitmap_zoom;
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 对一个bitmap进行模糊处理，会缩放尺寸 得到的bitmap不大
     *
     * @param bitmap
     * @return
     */
    public static Bitmap getBitmapBlur(Bitmap bitmap)
    {
        try
        {
            Bitmap bitmap_zoom = DMImageTool.zoomBitmap(bitmap, bitmap.getWidth() / 3, bitmap.getHeight() / 3);
            bitmap_zoom = FastBlur.doBlur(bitmap_zoom, Math.max(bitmap_zoom.getWidth() / 15, 10), true);
            return bitmap_zoom;
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /** GPU支持的bitmap最大分辨率 */
    public static int maxTextureSize = -1;
    /** 5.0一下手机默认的尺寸 */
    public static final int DEFAULT_MAX_BITMAP_DIMENSION = 4096;

    /** 获取GPU支持的bitmap最大分辨率（单边） */
    @SuppressLint("NewApi")
    public static int getMaxTextureSize(Context context)
    {
        if (maxTextureSize != -1)
        {
            return maxTextureSize;
        }
        maxTextureSize = 0;
        int[] maxSize = new int[1];
        try
        {
            ConfigurationInfo configurationInfo = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE))
                    .getDeviceConfigurationInfo();
            int glesVersion = configurationInfo.reqGlEsVersion;
            if (Build.VERSION.SDK_INT >= 21)
            {
                // configureEGLContext
                EGLDisplay mEGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
                if (mEGLDisplay == EGL14.EGL_NO_DISPLAY)
                {
                    throw new IllegalStateException("No EGL14 display");
                }
                int[] version = new int[2];
                if (!EGL14.eglInitialize(mEGLDisplay, version, /* offset */0, version, /* offset */1))
                {
                    throw new IllegalStateException("Cannot initialize EGL14");
                }
                int[] attribList =
                { EGL14.EGL_RED_SIZE, 8, EGL14.EGL_GREEN_SIZE, 8, EGL14.EGL_BLUE_SIZE, 8, EGL14.EGL_RENDERABLE_TYPE,
                        EGL14.EGL_OPENGL_ES2_BIT,
                        // EGL_RECORDABLE_ANDROID, 1,
                        EGL14.EGL_SURFACE_TYPE, EGL14.EGL_PBUFFER_BIT | EGL14.EGL_WINDOW_BIT, EGL14.EGL_NONE };
                EGLConfig[] configs = new EGLConfig[1];
                int[] numConfigs = new int[1];
                EGL14.eglChooseConfig(mEGLDisplay, attribList, /* offset */0, configs, /* offset */0, configs.length,
                        numConfigs, /* offset */0);
                if (EGL14.eglGetError() != EGL14.EGL_SUCCESS)
                {
                    throw new IllegalStateException("eglCreateContext RGB888+recordable ES2" + ": EGL error: 0x"
                            + Integer.toHexString(EGL14.eglGetError()));
                }
                int[] attrib_list =
                { EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE };
                android.opengl.EGLContext mEGLContext = EGL14.eglCreateContext(mEGLDisplay, configs[0],
                        EGL14.EGL_NO_CONTEXT, attrib_list, /* offset */0);
                if (EGL14.eglGetError() != EGL14.EGL_SUCCESS)
                {
                    throw new IllegalStateException(
                            "eglCreateContext" + ": EGL error: 0x" + Integer.toHexString(EGL14.eglGetError()));
                }
                if (mEGLContext == EGL14.EGL_NO_CONTEXT)
                {
                    throw new IllegalStateException("No EGLContext could be made");
                }
                int[] surfaceAttribs =
                { EGL14.EGL_WIDTH, 64, EGL14.EGL_HEIGHT, 64, EGL14.EGL_NONE };
                EGLSurface surface = EGL14.eglCreatePbufferSurface(mEGLDisplay, configs[0], surfaceAttribs, 0);
                EGL14.eglMakeCurrent(mEGLDisplay, surface, surface, mEGLContext);
                // getMaxTextureSize
                if (glesVersion >= 0x20000)
                {
                    GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, maxSize, 0);
                } else if (glesVersion >= 0x10000)
                {
                    GLES10.glGetIntegerv(GLES10.GL_MAX_TEXTURE_SIZE, maxSize, 0);
                }
                // releaseEGLContext
                EGL14.eglMakeCurrent(mEGLDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
                EGL14.eglDestroyContext(mEGLDisplay, mEGLContext);
                EGL14.eglReleaseThread();
                EGL14.eglTerminate(mEGLDisplay);
            } else
            {
                if (glesVersion >= 0x20000)
                {
                    GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, maxSize, 0);
                } else if (glesVersion >= 0x10000)
                {
                    GLES10.glGetIntegerv(GLES10.GL_MAX_TEXTURE_SIZE, maxSize, 0);
                }
            }
        } catch (IllegalStateException e)
        {
            e.printStackTrace();
        }
        maxTextureSize = maxSize[0] > 0 ? maxSize[0] : DEFAULT_MAX_BITMAP_DIMENSION;
        return maxTextureSize;
    }

    /**
     * 判断图片是否超出硬件加速支持的范围（gpu处理图片的像素尺寸）
     * 
     * @param w
     * @param h
     * @return
     */
    public boolean isNeedCloseHardwareAcceleration(int w, int h)
    {
        int max = getMaxTextureSize(RT.application);
        if (max < h || max < w)
        {
            return true;
        }
        return false;
    }

    /**
     * 4.4以上支持透明状态栏 修改状态栏颜色
     */
    public static void setTransparentStatus(Activity activity)
    {
        setTransparentStatus(activity, android.R.color.transparent);
    }

    /**
     * 4.4以上支持透明状态栏 修改状态栏颜色
     */
    public static void setTransparentStatus(Activity activity, int colorID)
    {
        if (!TransparentStatusbar)
            return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            try
            {
                // 防止布局顶到状态栏要配合CustomInsetFrameLayout使用
                View rootView = ((ViewGroup) (activity.getWindow().getDecorView().findViewById(android.R.id.content)))
                        .getChildAt(0);
                if (rootView != null)
                    rootView.setFitsSystemWindows(true);
                // 第三方适配库
                SystemBarTintManager tintManager = new SystemBarTintManager(activity);
                tintManager.setStatusBarTintEnabled(true);
                // tintManager.setNavigationBarTintEnabled(true);
                tintManager.setTintResource(colorID);
            } catch (Throwable e)
            {
            }
        }

        if (RomUtil.isEmui())
        {

        }
    }

    /** 设置虚拟按键颜色 */
    public static void setNavigationBarColor(Activity activity, int colorResId)
    {
        // 这句可以占用navigation的空间
        // activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        try
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            {
                Window window = activity.getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                // window.setStatusBarColor(activity.getResources().getColor(colorResId));
                // 底部导航栏
                window.setNavigationBarColor(activity.getResources().getColor(colorResId));
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * 判断有没有虚拟按键
     * 
     * @param context
     */
    public static boolean HasNavigationBar(Context context)
    {
        boolean isHave = false;
        try
        {
            Resources rs = context.getResources();
            int id = rs.getIdentifier("config_showNavigationBar", "bool", "android");
            if (id > 0)
            {
                isHave = rs.getBoolean(id);
            }
            try
            {
                Class systemPropertiesClass = Class.forName("android.os.SystemProperties");
                Method m = systemPropertiesClass.getMethod("get", String.class);
                String navBarOverride = (String) m.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
                if ("1".equals(navBarOverride))
                {
                    isHave = false;
                } else if ("0".equals(navBarOverride))
                {
                    isHave = true;
                }
            } catch (Exception e)
            {
                Log.w("TAG", e);
            }

        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return isHave;
    }

    /**
     * 修改状态栏颜色，支持4.4以上版本
     *
     * @param colorId
     *            颜色
     */
    public static void setStatusBarColor(Activity activity, int colorId)
    {

        if (!isOpenTransparentStatusbar())
            return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)// android 5.0以上
        {
            setTranslucentStatus(activity);
            Window window = activity.getWindow();
            window.setStatusBarColor(colorId);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)// android 4.4以上
        {
            // 使用SystemBarTintManager,需要先将状态栏设置为透明
            setTranslucentStatus(activity);
            SystemBarTintManager systemBarTintManager = new SystemBarTintManager(activity);
            systemBarTintManager.setStatusBarTintEnabled(true);// 显示状态栏
            systemBarTintManager.setStatusBarTintColor(colorId);// 设置状态栏颜色
        }
    }

    /**
     * 设置状态栏透明
     */
    @TargetApi(19)
    public static void setTranslucentStatus(Activity activity)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            // 5.x开始需要把颜色设置透明，否则导航栏会呈现系统默认的浅灰色
            Window window = activity.getWindow();
            View decorView = window.getDecorView();
            // 两个 flag 要结合使用，表示让应用的主体内容占用系统状态栏的空间
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            // 导航栏颜色也可以正常设置
            // window.setNavigationBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        {
            Window window = activity.getWindow();
            WindowManager.LayoutParams attributes = window.getAttributes();
            int flagTranslucentStatus = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
            attributes.flags |= flagTranslucentStatus;
            // int flagTranslucentNavigation = WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION; //attributes.flags |= flagTranslucentNavigation;
            window.setAttributes(attributes);
        }
    }

    /**
     * 代码实现android:fitsSystemWindows<br>
     * 当FitsSystemWindows设置 true 时，会在屏幕最上方预留出状态栏高度的 padding<br>
     * 不加下面这句 在沉浸模式下 会影响屏幕resize
     *
     * @param activity
     */
    public static void setRootViewFitsSystemWindows(Activity activity, boolean fitSystemWindows)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        {
            ViewGroup winContent = (ViewGroup) activity.findViewById(android.R.id.content);
            if (winContent.getChildCount() > 0)
            {
                ViewGroup rootView = (ViewGroup) winContent.getChildAt(0);
                if (rootView != null)
                {
                    rootView.setFitsSystemWindows(fitSystemWindows);
                }
            }
        }
    }

    /**
     * 设置状态栏深色浅色切换
     */
    public static boolean setStatusBarDarkTheme(Activity activity, boolean dark)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                setStatusBarFontIconDark(activity, TYPE_M, dark);
            } else if (RomUtil.isMiui())
            {
                setStatusBarFontIconDark(activity, TYPE_MIUI, dark);
            } else if (RomUtil.isFlyme())
            {
                setStatusBarFontIconDark(activity, TYPE_FLYME, dark);
            } else
            {// 其他情况
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * 设置 状态栏深色浅色切换
     */
    private static boolean setStatusBarFontIconDark(Activity activity, @ViewType int type, boolean dark)
    {
        switch (type)
        {
        case TYPE_MIUI :
            return setMiuiUI(activity, dark);
        case TYPE_FLYME :
            return setFlymeUI(activity, dark);
        case TYPE_M :
        default:
            return setCommonUI(activity, dark);
        }
    }

    // 设置6.0 状态栏深色浅色切换
    private static boolean setCommonUI(Activity activity, boolean dark)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            View decorView = activity.getWindow().getDecorView();
            if (decorView != null)
            {
                int vis = decorView.getSystemUiVisibility();
                if (dark)
                {
                    vis |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                } else
                {
                    vis &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                }
                if (decorView.getSystemUiVisibility() != vis)
                {
                    decorView.setSystemUiVisibility(vis);
                }
                return true;
            }
        }
        return false;
    }

    // 设置Flyme 状态栏深色浅色切换
    private static boolean setFlymeUI(Activity activity, boolean dark)
    {
        try
        {
            Window window = activity.getWindow();
            WindowManager.LayoutParams lp = window.getAttributes();
            Field darkFlag = WindowManager.LayoutParams.class.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
            Field meizuFlags = WindowManager.LayoutParams.class.getDeclaredField("meizuFlags");
            darkFlag.setAccessible(true);
            meizuFlags.setAccessible(true);
            int bit = darkFlag.getInt(null);
            int value = meizuFlags.getInt(lp);
            if (dark)
            {
                value |= bit;
            } else
            {
                value &= ~bit;
            }
            meizuFlags.setInt(lp, value);
            window.setAttributes(lp);
            return true;
        } catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    // 设置MIUI 状态栏深色浅色切换
    private static boolean setMiuiUI(Activity activity, boolean dark)
    {
        try
        {
            Window window = activity.getWindow();
            Class<?> clazz = activity.getWindow().getClass();
            @SuppressLint("PrivateApi")
            Class<?> layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
            Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
            int darkModeFlag = field.getInt(layoutParams);
            Method extraFlagField = clazz.getDeclaredMethod("setExtraFlags", int.class, int.class);
            extraFlagField.setAccessible(true);
            if (dark)
            { // 状态栏亮色且黑色字体.
                extraFlagField.invoke(window, darkModeFlag, darkModeFlag);
            } else
            {
                extraFlagField.invoke(window, 0, darkModeFlag);
            }
            return true;
        } catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    static HashMap<String, Integer> imgSpanMap = new HashMap<>();
    static
    {
        // imgSpanMap.put("[g_m]", R.drawable.icon_game_mobile);
        // imgSpanMap.put("[g_p]", R.drawable.icon_game_pc);
        // imgSpanMap.put("[cmc]", R.drawable.icon_comic);
    }

    /**
     * 文字tag替换成图片 <br/>
     * [g_m]手游,[g_p]PC游戏，[cmc]二次元漫画
     *
     * @return SpannableString
     */
    public static SpannableString replaceStrTagToImgSpan(String str, boolean onlyFirst)
    {
        if (StringUtil.isEmpty(str))
            return null;

        SpannableString ret = new SpannableString(str);
        try
        {
            Iterator iter = imgSpanMap.entrySet().iterator();

            while (iter.hasNext())
            {
                Map.Entry entry = (Map.Entry) iter.next();

                String key = (String) entry.getKey();
                int imgID = (int) entry.getValue();

                String _key = key.replace("[", "\\[").replace("]", "\\]");
                Pattern pattern = Pattern.compile(_key);
                Matcher matcher = pattern.matcher(str);
                while (matcher.find())
                {
                    int start = matcher.start();
                    int end = start + key.length();
                    ImageSpan span = new ImageSpan(RT.application, imgID, ImageSpan.ALIGN_BOTTOM);
                    ret.setSpan(span, start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                    if (onlyFirst)
                    {
                        break;
                    }
                }
            }
            return ret;
        } catch (Exception ex)
        {
        }
        return ret;
    }

    public enum TypeFaceEnum
    {
        // 数字英文
        Helvetica,
        // 数字英文 加粗
        Helvetica_Bold,
        // Din加粗
        DIN_Condensed_Bold,
    }

    /** 设置字体 */
    public static void setTypeface(TextView tv, TypeFaceEnum tfe)
    {
        try
        {
            String path = "fonts/" + tfe.name() + ".ttf";
            Typeface typeface = TypefaceUtils.load(tv.getContext().getAssets(), path);
            tv.setPaintFlags(tv.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
            tv.setTypeface(typeface);
        } catch (Exception ex)
        {
            // ex.printStackTrace();
        }
    }

    public static void printFontMetrics(TextView textView)
    {
        Paint.FontMetrics fm = textView.getPaint().getFontMetrics();
        DLOG.e("cccmax", fm.toString());
        float ascent = fm.ascent;
        float descent = fm.descent;
        float top = fm.top;
        float bottom = fm.bottom;
        float leading = fm.leading;
        DLOG.e("cccmax", "size:" + textView.getTextSize() + "  ascent:" + ascent + "  descent:" + descent + "  top:"
                + top + "  bottom:" + bottom + "  leading:" + leading);
    }

    /** edittext的光标 移动到内容最后 */
    public static void setTextCursorToEnd(EditText et)
    {
        try
        {
            String c = et.getText().toString();
            if (!StringUtil.isEmpty(c))
            {
                int l = c.length();
                et.setSelection(l);
            }
        } catch (Throwable e)
        {
        }
    }
}
