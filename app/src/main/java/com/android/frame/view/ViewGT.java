package com.android.frame.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;


import com.android.frame.R;

import org.rdengine.localimage.LocalImageGroupListView;
import org.rdengine.runtime.RT;
import org.rdengine.view.manager.ActivityRecord;
import org.rdengine.view.manager.BaseActivity;
import org.rdengine.view.manager.GenericActivity;
import org.rdengine.view.manager.IntentParams;
import org.rdengine.view.manager.ViewController;
import org.rdengine.view.manager.ViewParam;
import org.rdengine.widget.cobe.ChooseImageFeature;
import org.rdengine.widget.cobe.ChooseImageFeature.ChooseImageCallback;
import org.rdengine.widget.cropimage.CropView;
import org.rdengine.widget.cropimage.CropView.CropImageCallBack;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class ViewGT
{
    public static final int[] ANIM_START_PUSHUP = new int[]
    { R.anim.push_up_in, R.anim.anim_null };

    public static final int[] ANIM_FINISH_PUSHOUT = new int[]
    { R.anim.anim_null, R.anim.push_up_out };

    public static final int[] ANIM_START_ALPHA = new int[]
    { R.anim.alpha_enter, R.anim.anim_null };

    public static final int[] ANIM_FINISH_ALPHA = new int[]
    { R.anim.anim_null, R.anim.alpha_exit };

    public static final int[] ANIM_NULL = new int[]
    { R.anim.anim_null, R.anim.anim_null };

    private static long lastClickLoginTime;

    public static void startActivity(Context context, Intent intent)
    {
        if (!(context instanceof Activity))
        {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    /**
     * 在通用activity中启动一个页面
     *
     * @param context
     * @param clazz
     *            继承BaseView的页面类
     * @param vp
     *            页面参数
     */
    public static void showSomeviewInGenericActivity(Context context, Class clazz, ViewParam vp, int[] anim_start,
                                                     int[] anim_finish)
    {
        Intent intent = new Intent(context, GenericActivity.class);
        intent.putExtra(GenericActivity.BASEVIEW_CLASS_NAME, clazz.getName());
        String key = IntentParams.getInstance().createKey(context.getClass(), clazz);
        IntentParams.getInstance().put(key, vp);
        intent.putExtra(IntentParams.INTENT_PARAMS_KEY, key);
        if (anim_finish != null)
            intent.putExtra(GenericActivity.ANIM_FINISH, anim_finish);
        startActivity(context, intent);
        if (anim_start != null && context instanceof Activity)
        {
            try
            {
                Activity activity = (Activity) context;
                activity.overridePendingTransition(anim_start[0], anim_start[1]);
            } catch (Exception e)
            {
            }
        }
    }

    /**
     * 选择本地图片
     *
     * @param context
     * @param pathlist
     *            已经选择的图片路径列表
     * @param maxCount
     *            最多选多少张图
     * @param index
     *            当先选中的pathlist中的第几张
     */
    public static void showChooseLocalImageView(Context context, ArrayList<String> pathlist, int maxCount, int index,
            ChooseImageCallback callback)
    {
        ChooseImageFeature cif = new ChooseImageFeature(pathlist, index, maxCount, callback);
        ViewParam vp = new ViewParam();
        vp.data = cif;
        showSomeviewInGenericActivity(context, LocalImageGroupListView.class, vp, null, null);
    }

    /**
     * 裁剪图片
     *
     * @param context
     * @param path
     *            原图路径
     * @param ratio
     *            宽高比例 宽/高
     * @param callback
     *            回调
     */
    public static void showImageCropView(Context context, String path, float ratio, CropImageCallBack callback)
    {
        ViewParam vp = new ViewParam();
        vp.data = path;
        vp.data1 = callback;
        vp.type = String.valueOf(ratio);
        showSomeviewInGenericActivity(context, CropView.class, vp, null, null);
    }

    /**
     * 图片浏览 支持多图
     *
     * @param vc
     * @param photoarray
     * @param index
     */
    public static void showBrowsePicturesView(ViewController vc, List<String> photoarray, int index)
    {
        // ViewParam vp = new ViewParam();
        // vp.data = photoarray;
        // vp.index = index;
        // vc.showView(BrowsePicturesView.class, vp);
    }

    // todo
    // /**
    // * 图片浏览 支持多图 在Dialog中显示
    // *
    // * @param activity
    // * @param photoarray
    // * @param index
    // * @param menuParams
    // * 查看图片时支持的菜单
    // * @param obj
    // * @return
    // */
    // public static FullScreenDialog showBrowsePicturesViewInDialog(Activity activity, List<String> photoarray, int index,
    // int menuParams, Object obj)
    // {
    // if (photoarray == null || photoarray.size() == 0)
    // return null;
    // FullScreenDialog fsd = new FullScreenDialog(activity, R.style.dialog_fullscreen_zoom);
    // ViewParam vp = new ViewParam();
    // vp.data = photoarray;
    // vp.index = index;
    // vp.objectType = menuParams;
    // vp.data1 = obj;
    // fsd.setBaseView(BrowsePicturesView.class, vp);
    // fsd.show();
    // return fsd;
    // }

    // /**
    // * 图片浏览 支持多图 带文字描述 在Dialog中显示
    // *
    // * @param activity
    // * @param photoarray
    // * @param index
    // * @param menuParams
    // * 查看图片时支持的菜单
    // * @param obj
    // * @return
    // */
    // public static FullScreenDialog showBrowseDescPicturesViewInDialog(Activity activity, List<GbPictureBean> photoarray,
    // int index, int menuParams, Object obj)
    // {
    // if (photoarray == null || photoarray.size() == 0)
    // return null;
    // FullScreenDialog fsd = new FullScreenDialog(activity, R.style.dialog_fullscreen_zoom);
    // ViewParam vp = new ViewParam();
    // vp.data = photoarray;
    // vp.index = index;
    // vp.objectType = menuParams;
    // vp.data1 = obj;
    // fsd.setBaseView(BrowsePicturesView.class, vp);
    // fsd.show();
    // return fsd;
    // }

    /**
     * 启动一个网页
     *
     * @param controller
     * @param title
     * @param url
     */
    public static void gotoWebView(ViewController controller, String title, String url)
    {
        // TODO
        // if (url != null)
        // url = url.trim();
        // ViewParam vp = new ViewParam(title);
        // vp.data = url;
        // controller.showView(GeneralWebView.class, vp);
    }

    /**
     * 打开外部浏览器
     * 
     * @param context
     * @param url
     */
    public static void openWebViewOutside(Context context, String url)
    {
        try
        {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            Uri content_url = Uri.parse(url);
            intent.setData(content_url);
            context.startActivity(intent);
        } catch (Exception ex)
        {
        }
    }


}
