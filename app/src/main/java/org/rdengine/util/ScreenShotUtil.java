package org.rdengine.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.util.Log;

import org.rdengine.log.DLOG;
import org.rdengine.runtime.PreferenceHelper;
import org.rdengine.runtime.RT;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 截图工具
 * 
 * @author CCCMAX
 */
public class ScreenShotUtil
{
    public static final String SCREENSHOT_NEW_PATH = "screenshot_new_path";

    /**
     * 判断当前手机是否有ROOT权限
     * 
     * @return
     */
    public static boolean isRoot()
    {
        boolean bool = false;
        try
        {
            if ((!new File("/system/bin/su").exists()) && (!new File("/system/sbin/su").exists())
                    && (!new File("/system/xbin/su").exists()))
            {
                bool = false;
            } else
            {
                bool = true;
            }
        } catch (Exception e)
        {
        }
        return bool;
    }

    /**
     * 判断当前应用是否有root权限
     * 
     * @return
     */
    public static boolean checkAppRoot()
    {
        boolean rootOk = false;
        DataOutputStream os = null;
        InputStream stdin = null;
        try
        {
            Process process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("pwd \n");
            os.flush();
            os.close();
            stdin = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(stdin);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            line = br.readLine();
            rootOk = (line != null);
            stdin.close();
        } catch (Exception ex)
        {
            ex.printStackTrace();
            try
            {
                if (os != null)
                    os.close();
                if (stdin != null)
                    stdin.close();
            } catch (IOException e)
            {
            }
        }
        return rootOk;
    }

    // public static boolean isHasRoot()
    // {
    // String apkRoot = "chmod 777 " + RT.application.getPackageCodePath();
    // Process process = null;
    // DataOutputStream os = null;
    // try
    // {
    // process = Runtime.getRuntime().exec("su");
    // os = new DataOutputStream(process.getOutputStream());
    // os.writeBytes(apkRoot + "\n");
    // os.writeBytes("exit\n");
    // os.flush();
    // process.waitFor();
    // } catch (Exception e)
    // {
    // return false;
    //
    // } finally
    // {
    // try
    // {
    // if (os != null)
    // {
    // os.close();
    // }
    // process.destroy();
    // } catch (Exception e)
    // {
    // }
    // }
    // return true;
    // }

    /**
     * 截图保存图片
     * 
     * @param path
     *            截图保存的路径
     * @return 0 成功 -1路径错误 -2截图命令异常(权限) -3找不到图片文件
     */
    public static int screenshot(String path)
    {
        long lasttime = System.currentTimeMillis();

        // path = "/sdcard/icon_mtn_a_14.png";//图片路径

        if (path == null || path.length() == 0)
            return -1;// 路径错误

        int err = 0;

        File imgfile = new File(path);// 已存在 则删除旧文件
        if (imgfile.exists())
            imgfile.delete();

        DataOutputStream os = null;
        try
        {
            Process process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("screencap -p " + path + "\n exit\n");
            os.flush();
            err = process.waitFor();// 0 成功 1 失败
        } catch (Throwable e)
        {
            Log.e("cccmax", "截图异常");
            e.printStackTrace();
            err = -2;// 截图命令异常
        } finally
        {
            try
            {
                os.close();
            } catch (Throwable e)
            {
                e.printStackTrace();
            }
        }
        if (err == 0)
        {
            if (!new File(path).exists())// 判断文件是否存在
            {
                err = -3;// 返回0成功 但是找不到图片
            }
        }
        DLOG.d("over", (System.currentTimeMillis() - lasttime) + "");
        DLOG.d("over", "screenshot resultCode: " + err);
        return err;
    }

    private static Context mContext;

    public static int getDrawableId(String name)
    {
        if (mContext == null || name == null)
        {
            return 0;
        }

        return mContext.getResources().getIdentifier(name, "drawable", mContext.getPackageName());

    }

    public static int getLayoutId(String name)
    {
        if (mContext == null || name == null)
        {
            return 0;
        }

        return mContext.getResources().getIdentifier(name, "drawable", mContext.getPackageName());
    }

    public static int getStringId(String name)
    {
        if (mContext == null || name == null)
        {
            return 0;
        }

        return mContext.getResources().getIdentifier(name, "string", mContext.getPackageName());
    }

    /**
     * 读取图片属性：旋转的角度
     * 
     * @param path
     *            图片绝对路径
     * @return degree旋转的角度
     */
    public static int readPictureDegree(String path)
    {
        int degree = 0;
        try
        {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation)
            {
            case ExifInterface.ORIENTATION_ROTATE_90 :
                degree = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180 :
                degree = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270 :
                degree = 270;
                break;
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return degree;
    }

    /**
     * 旋转图片，使图片保持正确的方向。
     * 
     * @param bitmap
     *            原始图片
     * @param degrees
     *            原始图片的角度
     * @return Bitmap 旋转后的图片
     */
    public static Bitmap rotateBitmap(Bitmap bitmap, int degrees)
    {
        if (degrees == 0 || null == bitmap)
        {
            return bitmap;
        }
        Matrix matrix = new Matrix();
        matrix.setRotate(degrees, bitmap.getWidth() / 2, bitmap.getHeight() / 2);
        Bitmap bmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        if (null != bitmap)
        {
            bitmap.recycle();
        }
        return bmp;
    }

    public static void saveBitmap(String path, Bitmap mBitmap)
    {
        FileUtils.deleteFile(path);
        File file = new File(path);
        try
        {
            file.createNewFile();
        } catch (Exception e)
        {
        }
        FileOutputStream fOut = null;
        try
        {
            fOut = new FileOutputStream(file);
        } catch (Throwable e)
        {
            e.printStackTrace();
        }
        if (fOut != null)
        {
            try
            {
                mBitmap.compress(Bitmap.CompressFormat.PNG, 50, fOut);
                fOut.flush();
            } catch (Throwable e)
            {
                e.printStackTrace();
            }
            try
            {
                fOut.close();
            } catch (Throwable e)
            {
                e.printStackTrace();
            }
        }
        try
        {
            MediaScannerConnection.scanFile(RT.application, new String[]
            { path }, null, null);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        PreferenceHelper.ins().storeShareData(SCREENSHOT_NEW_PATH, path.getBytes(), false);
    }

    @SuppressLint("SimpleDateFormat")
    public static String getScreenShotName()
    {
        String filename = "screenshot_";
        try
        {
            long time = System.currentTimeMillis();
            Date date = new Date(time);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            filename = filename.concat(sdf.format(date));
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return filename;
    }

    public static String makeScreenShotPath()
    {

        return RT.defaultScreenshot + getScreenShotName() + ".png";
    }
}
