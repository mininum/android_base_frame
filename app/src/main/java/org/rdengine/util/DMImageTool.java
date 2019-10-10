package org.rdengine.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.provider.MediaStore.Images;

import org.rdengine.log.DLOG;
import org.rdengine.runtime.RT;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class DMImageTool
{
    private static Object lock = new Object();

    /**
     * 非安全，没有回收原图
     * 
     * @param hole
     *            the hole
     * @param src
     *            the src
     * @return the bitmap
     */
    private static Bitmap digImage(Bitmap hole, Bitmap src)
    {
        int[] pp = null;
        int[] ppp = null;
        // synchronized (lock)
        {
            // int[] dstarray = null;
            if (hole == null || src == null)
            {
                // DLOG.e("Image", "Error >> null");
                return null;
            }
            int bw = hole.getWidth();
            int bh = hole.getHeight();
            try
            {
                pp = new int[bw * bh];
                ppp = new int[bw * bh];
                // dstarray = new int[bw * bh];
            } catch (Throwable e)
            {
                // memory error
                return null;
            }
            Bitmap dst;
            int p, alpha = 0;
            int srcc = 0;
            src.getPixels(pp, 0, bw, 0, 0, bw, bh);
            hole.getPixels(ppp, 0, bw, 0, 0, bw, bh);
            for (int i = 0; i < pp.length; i++)
            {
                alpha = ppp[i];
                srcc = pp[i];
                if (alpha == 1)
                {
                    p = srcc;
                } else if (alpha == 0)
                {
                    // continue;
                    p = 0x00000000;
                } else
                {
                    p = ((alpha) | (srcc & 0xffffff));
                    // p = ((alpha) | (srcc & 0x000000));
                }
                // dstarray[i] = p;
                pp[i] = p;
            }
            // dst = Bitmap.createBitmap(dstarray, bw, bh, Config.ARGB_8888);
            ppp = null;
            dst = Bitmap.createBitmap(pp, bw, bh, Config.ARGB_8888);
            pp = null;
            return dst;
        }
    }

    // public static Drawable digHole(InputStream holeStream, BitmapDrawable srcDrawable)
    // {
    // Bitmap hole = null;
    // Bitmap src = null;
    // Bitmap dst = null;
    // synchronized (lock)
    // {
    // try
    // {
    // hole = BitmapFactory.decodeStream(holeStream);
    // src = srcDrawable.getBitmap();
    //
    // src = Bitmap.createScaledBitmap(src, hole.getWidth(), hole.getHeight(), true);
    // dst = digImage(hole, src);
    //
    // holeStream.close();
    // } catch (Exception e)
    // {
    // e.printStackTrace();
    // } catch (Error ex)
    // {
    // ex.printStackTrace();
    // // DLOG.e("Image", "Error");
    // } finally
    // {
    //
    // try
    // {
    // // if (!PhoneUtil.hasHoneycombMR1())
    // {
    // hole.recycle();
    // src.recycle();
    // }
    // } catch (Exception e)
    // {
    // e.printStackTrace();
    // }
    // hole = null;
    // src = null;
    // }
    // }
    // // System.gc();
    // return new BitmapDrawable(RT.application.getResources(), dst);
    // }

    public static Bitmap digHole(InputStream holeStream, Bitmap src)
    {
        // DLOG.i("Image", ">>打洞");
        Bitmap hole = null;
        Bitmap dst = null;
        Bitmap tmp = null;
        synchronized (lock)
        {

            try
            {
                hole = BitmapFactory.decodeStream(holeStream);
                tmp = Bitmap.createScaledBitmap(src, hole.getWidth(), hole.getHeight(), true);
                dst = digImage(hole, tmp);
            } catch (Throwable e)
            {
                e.printStackTrace();
            } finally
            {
                try
                {
                    holeStream.close();
                } catch (Exception e1)
                {
                    e1.printStackTrace();
                }
                try
                {
                    // if (!PhoneUtil.hasHoneycombMR1())
                    {
                        if (hole != null && dst != hole)
                            hole.recycle();
                        if (src != null && dst != src)
                            src.recycle();
                        if (tmp != null && dst != tmp)
                            tmp.recycle();
                    }
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
                hole = null;
                src = null;
                tmp = null;
                // System.gc();
            }
        }
        return dst;
    }

    public static Bitmap cutImage(Bitmap src)
    {

        int bit_x, bit_y, bit_width, bit_height;

        bit_y = 2;
        bit_height = src.getHeight() - 2 * bit_y;
        bit_width = bit_height * 10 / 16;
        bit_x = (src.getWidth() - bit_width) / 2 + 1;
        Bitmap ret = null;
        synchronized (lock)
        {

            try
            {
                ret = Bitmap.createBitmap(src, bit_x, bit_y, bit_width, bit_height);
                if (ret != src)
                {
                    if (src != null && !src.isRecycled())
                    {
                        src.recycle();
                        // DLOG.e("dmimage", "recyle old bitmap!!");
                    }
                }
            } catch (Throwable e)
            {
                e.printStackTrace();
            }
        }
        return ret;

    }

    public static int getRotationForImage(String path)
    {
        int rotation = 0;

        try
        {
            ExifInterface exif = new ExifInterface(path);
            rotation = (int) exifOrientationToDegrees(
                    exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL));
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        return rotation;
    }

    public static float exifOrientationToDegrees(int exifOrientation)
    {

        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90)
        {

            return 90;

        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180)
        {

            return 180;

        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270)
        {

            return 270;

        }

        return 0;

    }

    /**
     * 柔化效果(高斯模糊)(优化后比上面快三倍)
     * 
     * @param bmp
     * @return
     */
    public static Bitmap blurImageAmeliorate(Bitmap bmp)
    {
        long start = System.currentTimeMillis();
        // 高斯矩阵
        int[] gauss = new int[]
        { 1, 2, 1, 2, 4, 2, 1, 2, 1 };

        int width = bmp.getWidth();
        int height = bmp.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Config.RGB_565);

        int pixR = 0;
        int pixG = 0;
        int pixB = 0;

        int pixColor = 0;

        int newR = 0;
        int newG = 0;
        int newB = 0;

        int delta = 16; // 值越小图片会越亮，越大则越暗

        int idx = 0;
        int[] pixels = new int[width * height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 1, length = height - 1; i < length; i++)
        {
            for (int k = 1, len = width - 1; k < len; k++)
            {
                idx = 0;
                for (int m = -1; m <= 1; m++)
                {
                    for (int n = -1; n <= 1; n++)
                    {
                        pixColor = pixels[(i + m) * width + k + n];
                        pixR = Color.red(pixColor);
                        pixG = Color.green(pixColor);
                        pixB = Color.blue(pixColor);

                        newR = newR + (int) (pixR * gauss[idx]);
                        newG = newG + (int) (pixG * gauss[idx]);
                        newB = newB + (int) (pixB * gauss[idx]);
                        idx++;
                    }
                }

                newR /= delta;
                newG /= delta;
                newB /= delta;

                newR = Math.min(255, Math.max(0, newR));
                newG = Math.min(255, Math.max(0, newG));
                newB = Math.min(255, Math.max(0, newB));

                pixels[i * width + k] = Color.argb(255, newR, newG, newB);

                newR = 0;
                newG = 0;
                newB = 0;
            }
        }

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        long end = System.currentTimeMillis();
        return bitmap;
    }

    /** 水平方向模糊度 */
    private static float hRadius = 10;
    /** 竖直方向模糊度 */
    private static float vRadius = 10;
    /** 模糊迭代度 */
    private static int iterations = 7;

    /**
     * 高斯模糊
     */
    public static Bitmap BoxBlurFilter(Bitmap bmp)
    {
        long start = System.currentTimeMillis();
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        int[] inPixels = new int[width * height];
        int[] outPixels = new int[width * height];
        Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_4444);
        bmp.getPixels(inPixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < iterations; i++)
        {
            blur(inPixels, outPixels, width, height, hRadius);
            blur(outPixels, inPixels, height, width, vRadius);
        }
        blurFractional(inPixels, outPixels, width, height, hRadius);
        blurFractional(outPixels, inPixels, height, width, vRadius);
        bitmap.setPixels(inPixels, 0, width, 0, 0, width, height);

        long end = System.currentTimeMillis();
        // DLOG.d("blur", "cost:"+(end - start));

        return bitmap;
    }

    public static void blur(int[] in, int[] out, int width, int height, float radius)
    {
        int widthMinus1 = width - 1;
        int r = (int) radius;
        int tableSize = 2 * r + 1;
        int divide[] = new int[256 * tableSize];

        for (int i = 0; i < 256 * tableSize; i++)
            divide[i] = i / tableSize;

        int inIndex = 0;

        for (int y = 0; y < height; y++)
        {
            int outIndex = y;
            int ta = 0, tr = 0, tg = 0, tb = 0;

            for (int i = -r; i <= r; i++)
            {
                int rgb = in[inIndex + clamp(i, 0, width - 1)];
                ta += (rgb >> 24) & 0xff;
                tr += (rgb >> 16) & 0xff;
                tg += (rgb >> 8) & 0xff;
                tb += rgb & 0xff;
            }

            for (int x = 0; x < width; x++)
            {
                out[outIndex] = (divide[ta] << 24) | (divide[tr] << 16) | (divide[tg] << 8) | divide[tb];

                int i1 = x + r + 1;
                if (i1 > widthMinus1)
                    i1 = widthMinus1;
                int i2 = x - r;
                if (i2 < 0)
                    i2 = 0;
                int rgb1 = in[inIndex + i1];
                int rgb2 = in[inIndex + i2];

                ta += ((rgb1 >> 24) & 0xff) - ((rgb2 >> 24) & 0xff);
                tr += ((rgb1 & 0xff0000) - (rgb2 & 0xff0000)) >> 16;
                tg += ((rgb1 & 0xff00) - (rgb2 & 0xff00)) >> 8;
                tb += (rgb1 & 0xff) - (rgb2 & 0xff);
                outIndex += height;
            }
            inIndex += width;
        }
    }

    public static void blurFractional(int[] in, int[] out, int width, int height, float radius)
    {
        radius -= (int) radius;
        float f = 1.0f / (1 + 2 * radius);
        int inIndex = 0;

        for (int y = 0; y < height; y++)
        {
            int outIndex = y;

            out[outIndex] = in[0];
            outIndex += height;
            for (int x = 1; x < width - 1; x++)
            {
                int i = inIndex + x;
                int rgb1 = in[i - 1];
                int rgb2 = in[i];
                int rgb3 = in[i + 1];

                int a1 = (rgb1 >> 24) & 0xff;
                int r1 = (rgb1 >> 16) & 0xff;
                int g1 = (rgb1 >> 8) & 0xff;
                int b1 = rgb1 & 0xff;
                int a2 = (rgb2 >> 24) & 0xff;
                int r2 = (rgb2 >> 16) & 0xff;
                int g2 = (rgb2 >> 8) & 0xff;
                int b2 = rgb2 & 0xff;
                int a3 = (rgb3 >> 24) & 0xff;
                int r3 = (rgb3 >> 16) & 0xff;
                int g3 = (rgb3 >> 8) & 0xff;
                int b3 = rgb3 & 0xff;
                a1 = a2 + (int) ((a1 + a3) * radius);
                r1 = r2 + (int) ((r1 + r3) * radius);
                g1 = g2 + (int) ((g1 + g3) * radius);
                b1 = b2 + (int) ((b1 + b3) * radius);
                a1 *= f;
                r1 *= f;
                g1 *= f;
                b1 *= f;
                out[outIndex] = (a1 << 24) | (r1 << 16) | (g1 << 8) | b1;
                outIndex += height;
            }
            out[outIndex] = in[width - 1];
            inIndex += width;
        }
    }

    public static int clamp(int x, int a, int b)
    {
        return (x < a) ? a : (x > b) ? b : x;
    }

    public static Bitmap compressImage(Bitmap image)
    {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);// 质量压缩方法
        int options = 90;// 先压到90%
        while (baos.toByteArray().length / 1024 > 1024)
        { // 循环判断如果压缩后图片是否大于1024K,大于继续压缩
            baos.reset();// 重置baos即清空baos
            options -= 10;// 每次都减少10
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// 把ByteArrayInputStream数据生成图片
        return bitmap;
    }

    public static Bitmap getBitmap(String srcPath, boolean needCompress)
    {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        // 开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);// 此时返回bm为空

        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        // 现在主流手机比较多是1280*800分辨率，所以高和宽我们设置为
        float hh = 1280F;
        float ww = 1280F;
        // 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;// be=1表示不缩放
        if (w > h && w > ww)
        {// 如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh)
        {// 如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 1)
            be = 1;
        else be += 1;
        newOpts.inSampleSize = be;// 设置缩放比例
        // 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        if (needCompress)
        {
            return compressImage(bitmap);// 压缩好比例大小后再进行质量压缩
        } else
        {
            return bitmap;
        }
    }

    public static Point getBitmapPxSize(String srcPath)
    {
        try
        {
            BitmapFactory.Options newOpts = new BitmapFactory.Options();
            newOpts.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(srcPath, newOpts);// 此时返回bm为空
            int w = newOpts.outWidth;
            int h = newOpts.outHeight;
            return new Point(w, h);
        } catch (Exception ex)
        {
        }
        return null;
    }

    /**
     * 获取bitmap
     *
     * @return
     */
    public static Bitmap getBitmapFromFileByOptions(String pathName, int scale)
    {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inSampleSize = scale;
        Bitmap bitmap = BitmapFactory.decodeFile(pathName, options);

        return bitmap;
    }

    public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle)
    {

        int i;
        int j;
        // i = bmp.getWidth();
        // j = bmp.getHeight();
        if (bmp.getHeight() > bmp.getWidth())
        {
            i = bmp.getWidth();
            j = bmp.getWidth();
        } else
        {
            i = bmp.getHeight();
            j = bmp.getHeight();
        }

        Bitmap localBitmap = Bitmap.createBitmap(i, j, Config.RGB_565);
        Canvas localCanvas = new Canvas(localBitmap);

        while (true)
        {
            localCanvas.drawBitmap(bmp, new Rect(0, 0, i, j), new Rect(0, 0, i, j), null);
            if (needRecycle)
                bmp.recycle();
            ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
            localBitmap.compress(Bitmap.CompressFormat.JPEG, 100, localByteArrayOutputStream);
            localBitmap.recycle();
            byte[] arrayOfByte = localByteArrayOutputStream.toByteArray();
            try
            {
                localByteArrayOutputStream.close();
                return arrayOfByte;
            } catch (Exception e)
            {
                // F.out(e);
            }
            i = bmp.getHeight();
            j = bmp.getHeight();
        }
    }

    /**
     * 放大缩小图片
     *
     * @param bitmap
     * @param w
     * @param h
     * @return
     */
    public static Bitmap zoomBitmap(Bitmap bitmap, int w, int h)
    {
        Bitmap newbmp = null;
        if (bitmap != null)
        {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            Matrix matrix = new Matrix();
            float scaleWidht = ((float) w / width);
            float scaleHeight = ((float) h / height);
            matrix.postScale(scaleWidht, scaleHeight);
            newbmp = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        }
        return newbmp;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int maxWidth, int maxHeight)
    {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (width > maxWidth || height > maxHeight)
        {
            if (width > height)
            {
                inSampleSize = Math.round((float) height / (float) maxHeight);
            } else
            {
                inSampleSize = Math.round((float) width / (float) maxWidth);
            }

            final float totalPixels = width * height;

            final float maxTotalPixels = maxWidth * maxHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > maxTotalPixels)
            {
                inSampleSize++;
            }
        }
        return inSampleSize;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) // drawable 转换成bitmap
    {
        int width = drawable.getIntrinsicWidth();// 取drawable的长宽
        int height = drawable.getIntrinsicHeight();
        Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Config.ARGB_8888 : Config.RGB_565;// 取drawable的颜色格式
        Bitmap bitmap = Bitmap.createBitmap(width, height, config);// 建立对应bitmap
        Canvas canvas = new Canvas(bitmap);// 建立对应bitmap的画布
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);// 把drawable内容画到画布中
        return bitmap;
    }

    public static void savePhotoToSDCard(Bitmap photoBitmap, String imagePath)
    {

        File photoFile = new File(imagePath);
        if (photoFile.exists())
        {
            photoFile.delete();
        }
        if (!photoFile.getParentFile().exists())
        {
            photoFile.getParentFile().mkdirs();
        }
        FileOutputStream fileOutputStream = null;
        try
        {
            photoFile.createNewFile();
            fileOutputStream = new FileOutputStream(photoFile);
            if (photoBitmap != null)
            {
                if (photoBitmap.compress(Bitmap.CompressFormat.JPEG, 80, fileOutputStream))
                {
                    fileOutputStream.flush();
                    // fileOutputStream.close();
                }
            }
        } catch (FileNotFoundException e)
        {
            photoFile.delete();
            e.printStackTrace();
        } catch (IOException e)
        {
            photoFile.delete();
            e.printStackTrace();
        } finally
        {

            try
            {
                fileOutputStream.close();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * 保存一个bitmap到手机图片库
     *
     * @param bitmap
     * @param context
     * @return
     */
    public static boolean saveBitmapToPictures(Bitmap bitmap, Context context)
    {
        if (bitmap != null)
        {

            try
            {
                ContentResolver cr = context.getContentResolver();
                String uri = Images.Media.insertImage(cr, bitmap, "Motion", "Photo");
                String AbsoluteImagePath = getAbsoluteImagePath(context, Uri.parse(uri));
                // context.sendBroadcast(new
                // Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                // Uri.parse("file://"
                // + Environment.getExternalStorageState())));
                context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(uri)));
                MediaScannerConnection.scanFile(context, new String[]
                { uri, AbsoluteImagePath }, null, null);
                return true;
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 保存一个本地的GIF到手机图片库
     *
     * @param file
     * @param context
     * @return
     */
    public static boolean saveLocalGifToPictures(File file, Context context)
    {
        if (file != null && file.exists())
        {

            ContentValues values = new ContentValues();
            values.put(Images.Media.TITLE, "guanba");
            values.put(Images.Media.DESCRIPTION, "photo");
            values.put(Images.Media.MIME_TYPE, "image/gif");

            Uri url = null;
            String stringUrl = null; /* value to be returned */
            ContentResolver cr = context.getContentResolver();
            try
            {

                url = cr.insert(Images.Media.EXTERNAL_CONTENT_URI, values);
                String[] proj =
                { Images.Media.DATA };
                Cursor cursor = cr.query(url, proj, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow(Images.Media.DATA);
                cursor.moveToFirst();
                String path = cursor.getString(column_index);
                cursor.close();

                String newPath = path;
                if (path.endsWith(".jpg"))
                {
                    newPath = path.replace(".jpg", ".gif");
                } else if (path.endsWith(".png"))
                {
                    newPath = path.replace(".png", ".gif");
                }

                File dstFile = new File(newPath);
                FileUtils.fileChannelCopy(file, dstFile);

                ContentValues updateValues = new ContentValues();
                updateValues.put(Images.Media.DATA, newPath);
                String where = Images.Media.DATA + "=?";
                cr.update(Images.Media.EXTERNAL_CONTENT_URI, updateValues, where, new String[]
                { path });
            } catch (Exception e)
            {
                DLOG.e("ImageTool", "Failed to insert image", e);
                if (url != null)
                {
                    cr.delete(url, null, null);
                    url = null;
                }
                return false;
            }

            if (url != null)
            {
                stringUrl = url.toString();
                String AbsoluteImagePath = getAbsoluteImagePath(context, Uri.parse(stringUrl));
                context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(stringUrl)));
                MediaScannerConnection.scanFile(context, new String[]
                { stringUrl, AbsoluteImagePath }, null, null);
                return true;
            }

        }
        return false;
    }

    /**
     * 通过uri获取文件的绝对路径
     *
     * @param uri
     * @return
     */
    public static String getAbsoluteImagePath(Context context, Uri uri)
    {
        String imagePath = "";
        String[] proj =
        { Images.Media.DATA };
        System.out.println(uri + "  img path uri");
        Cursor cursor = context.getContentResolver().query(uri, proj, // Which columns to
                // return
                null, // WHERE clause; which rows to return (all rows)
                null, // WHERE clause selection arguments (none)
                null); // Order-by clause (ascending by name)

        if (cursor != null)
        {
            int column_index = cursor.getColumnIndexOrThrow(Images.Media.DATA);
            if (cursor.getCount() > 0 && cursor.moveToFirst())
            {
                imagePath = cursor.getString(column_index);
            }
        }
        return imagePath;
    }

    public static String resImgToDiskImgTemp(int resImgID)
    {
        try
        {
            Bitmap bitmap = BitmapFactory.decodeResource(RT.application.getResources(), resImgID);
            // 临时图片路径
            String path = RT.tempImage + "resimage" + resImgID + ".png";
            File file = new File(path);
            if (file.exists())
                return path;
            else return bitmapToDiskImgPng(bitmap, path);
        } catch (Exception e)
        {
            return null;
        }
    }

    public static String bitmapToDiskImgPng(Bitmap bitmap, String path)
    {
        try
        {

            File photoFile = new File(path);
            FileOutputStream fileOutputStream = null;
            try
            {
                fileOutputStream = new FileOutputStream(photoFile);
                if (bitmap != null)
                {
                    if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream))
                    {
                        fileOutputStream.flush();
                    }
                }
            } catch (FileNotFoundException e)
            {
                photoFile.delete();
                e.printStackTrace();
            } catch (IOException e)
            {
                photoFile.delete();
                e.printStackTrace();
            } finally
            {

                try
                {
                    fileOutputStream.close();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            return path;
        } catch (Exception e)
        {
            return null;
        }
    }

    public static Bitmap bitmapAddBackgroundColor(Bitmap source, int color)
    {
        try
        {
            Bitmap ret = Bitmap.createBitmap(source.getWidth(), source.getHeight(), source.getConfig());

            Canvas canvas = new Canvas(ret);
            canvas.drawColor(color);
            Paint p = new Paint();
            p.setAntiAlias(true);
            canvas.drawBitmap(source, 0, 0, p);
            return ret;
        } catch (Exception ex)
        {
            ex.printStackTrace();
            return source;
        }
    }
}
