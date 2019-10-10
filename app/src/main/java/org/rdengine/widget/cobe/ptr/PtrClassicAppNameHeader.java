package org.rdengine.widget.cobe.ptr;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.android.frame.R;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.fresco.FrescoImageView;
import com.facebook.imagepipeline.animated.base.AnimatedDrawable;

import org.rdengine.log.DLOG;
import org.rdengine.widget.cobe.ptr.indicator.PtrIndicator;

import java.text.SimpleDateFormat;

/**
 * 下拉刷新 应用名header
 */
public class PtrClassicAppNameHeader extends FrameLayout implements PtrUIHandler
{

    private final static String KEY_SharedPreferences = "cube_ptr_classic_last_update";
    private static SimpleDateFormat sDataFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private String mLastUpdateTimeKey;

    // ----------------R.layout.cube_ptr_classic_appname_header-------------Start
    private FrescoImageView ptr_classic_header_rotate_appname;

    /** auto load R.layout.cube_ptr_classic_appname_header */
    private void autoLoad_cube_ptr_classic_appname_header()
    {
        ptr_classic_header_rotate_appname = (FrescoImageView) findViewById(R.id.ptr_classic_header_rotate_appname);
    }
    // ----------------R.layout.cube_ptr_classic_appname_header-------------End

    public PtrClassicAppNameHeader(Context context)
    {
        super(context);
        initViews(null);
    }

    public PtrClassicAppNameHeader(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        initViews(attrs);
    }

    public PtrClassicAppNameHeader(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        initViews(attrs);
    }

    protected void initViews(AttributeSet attrs)
    {
        View header = LayoutInflater.from(getContext()).inflate(R.layout.cube_ptr_classic_appname_header, this);

        autoLoad_cube_ptr_classic_appname_header();

        reLoadGif();
    }

    @Override
    protected void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();
    }

    /**
     * Specify the last update time by this key string
     * 
     * @param key
     */
    public void setLastUpdateTimeKey(String key)
    {
        if (TextUtils.isEmpty(key))
        {
            return;
        }
        mLastUpdateTimeKey = key;
    }

    /**
     * Using an object to specify the last update time.
     * 
     * @param object
     */
    public void setLastUpdateTimeRelateObject(Object object)
    {
        setLastUpdateTimeKey(object.getClass().getName());
    }

    private void stopAnimatable()
    {
        try
        {
            DraweeController controller = ptr_classic_header_rotate_appname.getController();
            Animatable anim = controller.getAnimatable();
            AnimatedDrawable ad = (AnimatedDrawable) anim;
            ad.stop();
        } catch (Exception ex)
        {
            // ex.printStackTrace();
        }
    }

    private void startAnimatable()
    {
        try
        {
            DraweeController controller = ptr_classic_header_rotate_appname.getController();
            Animatable anim = controller.getAnimatable();
            anim.start();
        } catch (Exception ex)
        {
            // ex.printStackTrace();
        }
    }

    private void reLoadGif()
    {
        // 此处加载一个gif文件资源
        // FrescoParam fp = new FrescoParam("res:///" + R.raw.refresh_header_appname);
        // fp.setAutoPlayAnimations(false);
        // FrescoImageHelper.getImage(fp, ptr_classic_header_rotate_appname,
        // new FrescoConfigConstants.FrescoPreHandleListener(ptr_classic_header_rotate_appname)
        // {
        // @Override
        // public void onFinalImageSet(String id, Object imageInfo, Animatable animatable)
        // {
        // if (animatable != null)
        // animatable.stop();
        // }
        //
        // @Override
        // public void handle(ImageInfo ii, boolean isgif, int w, int h, float _ratio)
        // {}
        // });
    }

    @Override
    public void onUIReset(PtrFrameLayout frame)
    {
        // 当位置回到初始位置。
        DLOG.e("PtrClassicAppNameHeader", "onUIReset");
        reLoadGif();
    }

    @Override
    public void onUIRefreshPrepare(PtrFrameLayout frame)
    {
        DLOG.e("PtrClassicAppNameHeader", "onUIRefreshPrepare");
        // 当Header离开初始位置
        stopAnimatable();

        if (frame.isPullToRefresh())
        {
        } else
        {
        }
    }

    @Override
    public void onUIRefreshBegin(PtrFrameLayout frame)
    {
        DLOG.e("PtrClassicAppNameHeader", "onUIRefreshBegin");
        // Header开始刷新动画
        startAnimatable();
    }

    @Override
    public void onUIRefreshComplete(PtrFrameLayout frame)
    {
        DLOG.e("PtrClassicAppNameHeader", "onUIRefreshComplete");
        // Header刷新动画完成。Header刷新完成之后，开始回归初始位置。
        stopAnimatable();
    }

    @Override
    public void onUIPositionChange(PtrFrameLayout frame, boolean isUnderTouch, byte status, PtrIndicator ptrIndicator)
    {
        // DLOG.e("PtrClassicAppNameHeader", "onUIPositionChange");
        // Header位置发生变化时此方法通知UI更新

        final int mOffsetToRefresh = frame.getOffsetToRefresh();
        final int currentPos = ptrIndicator.getCurrentPosY();
        final int lastPos = ptrIndicator.getLastPosY();

        if (currentPos < mOffsetToRefresh && lastPos >= mOffsetToRefresh)
        {
            if (isUnderTouch && status == PtrFrameLayout.PTR_STATUS_PREPARE)
            {
                crossRotateLineFromBottomUnderTouch(frame);
            }
        } else if (currentPos > mOffsetToRefresh && lastPos <= mOffsetToRefresh)
        {
            if (isUnderTouch && status == PtrFrameLayout.PTR_STATUS_PREPARE)
            {
                crossRotateLineFromTopUnderTouch(frame);
            }
        }
    }

    private void crossRotateLineFromTopUnderTouch(PtrFrameLayout frame)
    {
        if (!frame.isPullToRefresh())
        {
            // 释放刷新
            startAnimatable();
        }
    }

    private void crossRotateLineFromBottomUnderTouch(PtrFrameLayout frame)
    {
        // 往下拉
    }

}
