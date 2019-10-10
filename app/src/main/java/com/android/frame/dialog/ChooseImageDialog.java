package com.android.frame.dialog;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;


import com.android.frame.view.ViewGT;

import org.rdengine.log.DLOG;
import org.rdengine.runtime.RT;
import org.rdengine.runtime.event.EventListener;
import org.rdengine.runtime.event.EventManager;
import org.rdengine.runtime.event.EventTag;
import org.rdengine.view.manager.BaseActivity;
import org.rdengine.view.manager.BaseActivity.PermissionRequestObj;
import org.rdengine.widget.cobe.ChooseImageFeature.ChooseImageCallback;
import org.rdengine.widget.cropimage.CropView.CropImageCallBack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ChooseImageDialog extends ActionSheet
{
    ArrayList<String> pathlist;
    int maxCount;
    int index;
    boolean needcrop;
    float ratio = 1;
    ChooseImageCallback callback;

    private String camera_photo_path = "";

    static List<Object[]> aa = new ArrayList<Object[]>();
    static
    {
        aa.add(new Object[]
        { "拍照", 0 });
        aa.add(new Object[]
        { "相册", 1 });
    }

    /**
     * @param context
     * @param pathlist
     *            原始已选图片列表
     * @param maxCount
     *            最多可以选几个图
     * @param index
     * @param needcrop
     *            是否需要裁剪
     * @param ratio
     *            裁剪图片比例 宽/高
     * @param callback
     *            回调
     */
    public ChooseImageDialog(Context context, ArrayList<String> pathlist, int maxCount, int index, boolean needcrop,
            float ratio, ChooseImageCallback callback)
    {
        super(context);
        this.titles = aa;
        this.mOnActionSheetListener = sheetSelectedlistener;

        this.pathlist = pathlist;
        this.maxCount = maxCount;
        this.index = index;
        this.needcrop = needcrop;
        this.ratio = ratio;
        this.callback = callback;
    }

    BaseDialog.OnActionSheetSelected sheetSelectedlistener = new BaseDialog.OnActionSheetSelected()
    {
        public void onClick(int whichButton)
        {
            switch (whichButton)
            {
            case 0 :
            {
                // 拍照
                takePhoto();
            }
                break;

            case 1 :
            {
                // 相册
                pickPhoto();
            }
                break;
            }
        }
    };

    private void pickPhoto()
    {
        // 相册选图
        ViewGT.showChooseLocalImageView(getContext(), null, maxCount, 0, new ChooseImageCallback()
        {
            public void onFinsh(List<String> pathlist)
            {
                if (needcrop && pathlist.size() == 1)
                {
                    // 裁剪
                    toCropPhoto(pathlist.get(0));
                } else
                {
                    if (callback != null)
                    {
                        callback.onFinsh(pathlist);
                    }
                }
            }
        });
    }

    private void takePhoto()
    {
        // 拍照
        Object obj = getOwnerActivity();
        if (getOwnerActivity() instanceof BaseActivity)
        {
            final BaseActivity activity = (BaseActivity) getOwnerActivity();
            ArrayList<String> permissions = new ArrayList<String>();
            permissions.add(Manifest.permission.CAMERA);//

            activity.doRequestPermissions(new PermissionRequestObj(permissions)
            {

                public void callback(boolean allGranted, List<String> permissionsList_denied, PermissionRequestObj pro)
                {
                    if (allGranted)
                    {
                        // 照片路径
                        try
                        {
                            camera_photo_path = RT.tempImage + String.valueOf(System.currentTimeMillis()) + ".jpg";
                            DLOG.e("cccmax", "camera_photo_path=" + camera_photo_path);
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            Uri uri = Uri.parse("file://" + camera_photo_path);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                            activity.startActivityForResult(intent, 999);
                            EventManager.ins().reomoveWhat(EventTag.APP_ON_ACTIVITY_RESULT);// 注销之前同类型监听
                            EventManager.ins().registListener(EventTag.APP_ON_ACTIVITY_RESULT, mEventListener);// 添加新的监听
                        } catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    } else
                    {
                        pro.showManualSetupDialog(activity, "相机权限");
                    }
                }
            });
        }
    }

    EventListener mEventListener = new EventListener()
    {
        public void handleMessage(int what, int requestCode, int resultCode, Object data)
        {
            if (requestCode != 999)
            {
                return;
            }
            EventManager.ins().removeListener(EventTag.APP_ON_ACTIVITY_RESULT, this);// 得到监听结果后注销监听

            if (resultCode == Activity.RESULT_OK)
            {
                if (!TextUtils.isEmpty(camera_photo_path))
                {
                    File file = new File(camera_photo_path);
                    if (file.exists())
                    {
                        if (needcrop)
                        {
                            // 需要裁切图片
                            toCropPhoto(camera_photo_path);
                        } else
                        {
                            if (callback != null)
                            {
                                ArrayList<String> ret = new ArrayList<String>();
                                ret.add(camera_photo_path);
                                callback.onFinsh(ret);
                            }
                        }
                    }
                }
            }
        }
    };

    /**
     * 裁剪图片
     * 
     * @param path
     */
    private void toCropPhoto(String path)
    {
        ViewGT.showImageCropView(getOwnerActivity(), path, ratio, new CropImageCallBack()
        {
            public void onFinsh(String imagePath)
            {
                if (callback != null)
                {
                    ArrayList<String> pathlist = new ArrayList<String>();
                    pathlist.add(imagePath);
                    callback.onFinsh(pathlist);
                }
            }
        });
    }

}
