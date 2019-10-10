package org.rdengine.widget.cropimage;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;

import com.android.frame.R;

import org.rdengine.runtime.RT;
import org.rdengine.util.DMImageTool;
import org.rdengine.view.manager.BaseView;
import org.rdengine.view.manager.ViewParam;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class CropView extends BaseView implements OnClickListener
{
    private static final String TAG = "CropImageView";
    private View titlebar;
    private CropImageView image;
    private View zoomin;
    private View zoomout;
    private View save;
    private View left;
    private View right;
    private Drawable back;

    public CropView(Context context, ViewParam vp)
    {
        super(context, vp);
    }

    String imagesource_path;
    float ratio = 1;
    CropImageCallBack mCropImageCallBack;

    @Override
    public void init()
    {
        setContentView(R.layout.crop_view);
        titlebar = findViewById(R.id.titlebar);
        image = (CropImageView) findViewById(R.id.image);
        zoomin = findViewById(R.id.zoomin);
        zoomout = (View) findViewById(R.id.zoomout);
        save = (View) findViewById(R.id.save);
        left = (View) findViewById(R.id.left);
        right = (View) findViewById(R.id.right);
        zoomin.setOnClickListener(this);
        zoomout.setOnClickListener(this);
        save.setOnClickListener(this);
        left.setOnClickListener(this);
        right.setOnClickListener(this);

        // vp.data = path;
        // vp.data1 = callback;
        // vp.type = String.valueOf(ratio);
        imagesource_path = (String) mViewParam.data;
        ratio = Float.parseFloat(mViewParam.type);
        mCropImageCallBack = (CropImageCallBack) mViewParam.data1;
        if (ratio < 0)
            ratio = 1;

    }

    @Override
    public String getTag()
    {
        return TAG;
    }

    @Override
    public void refresh()
    {
        super.refresh();
        image.setNeedDash(false);
        image.setRatio(ratio);

        try
        {
            Bitmap bitmap = null;
            int w = getResources().getDisplayMetrics().widthPixels;
            int h = getResources().getDisplayMetrics().heightPixels;
            if (!TextUtils.isEmpty(imagesource_path))
            {
                Uri uri = Uri.fromFile(new File(imagesource_path));
                bitmap = safeDecodeStream(uri, w, h);
                if (bitmap == null)
                {
                    back();
                    return;
                }

                bitmapWidth = bitmap.getWidth();
                bitmapHeight = bitmap.getHeight();
                image.setImageBitmapResetBase(bitmap, true);
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private int bitmapWidth;
    private int bitmapHeight;

    public void onSave()
    {
        // 回调
        if (mCropImageCallBack != null)
        {
            mCropImageCallBack.onFinsh(savePath);
        }
    }

    protected Bitmap safeDecodeStream(Uri uri, int width, int height) throws FileNotFoundException
    {

        try
        {
            BitmapFactory.Options options = new BitmapFactory.Options();
            android.content.ContentResolver resolver = this.getContext().getContentResolver();

            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new BufferedInputStream(resolver.openInputStream(uri), 16 * 1024), null,
                    options);

            options.inJustDecodeBounds = false;

            options.inSampleSize = DMImageTool.calculateInSampleSize(options, width, height);
            Bitmap b = BitmapFactory.decodeStream(new BufferedInputStream(resolver.openInputStream(uri), 16 * 1024),
                    null, options);

            return b;// Bitmap.createScaledBitmap(b, width, height, false);
        } catch (Exception e)
        {
            // System.gc();

        } catch (Error e1)
        {
            // System.gc();
        }
        return null;
    }

    String savePath;

    int degree;

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
        case R.id.left :
            // degree -= 90;
            image.setRoate(-90);

            image.postInvalidate();
            break;
        case R.id.right :
            // degree += 90;
            image.setRoate(90);
            image.postInvalidate();
            break;
        case R.id.zoomin :
            image.zoomIn();
            break;
        case R.id.zoomout :
            image.zoomOut();
            break;
        case R.id.save :
            save();
            break;
        default:
            break;
        }
    }

    void testSave()
    {
        image.logPoint();
    }

    void save()
    {
        Bitmap b = null;
        try
        {
            setDrawingCacheEnabled(false);
            setDrawingCacheEnabled(true);

            int left = image.getCropLeft();
            int right = image.getCropRight();
            int top = image.getCropTop();
            int bottom = image.getCropBottom();
            top += titlebar.getMeasuredHeight();
            bottom += titlebar.getMeasuredHeight();

            b = Bitmap.createBitmap(getDrawingCache());
            b = Bitmap.createBitmap(b, left, top, right - left, bottom - top);
        } catch (Exception e1)
        {
        } catch (Error e)
        {
        }
        if (b == null)
        {
            return;
        }

        boolean sdCard = RT.isMount();

        String url = String.valueOf(System.currentTimeMillis()) + ".jpg";
        File f = new File(RT.tempImage, url);
        savePath = f.getPath();
        if (f.exists())
        {
            f.delete();
        }
        try
        {
            OutputStream stream = new FileOutputStream(f);
            b.compress(CompressFormat.JPEG, 100, stream);
            stream.flush();
            stream.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        onSave();
        back();
    }

    @Override
    public boolean onBack()
    {
        return false;
    }

    private void back()
    {
        if (getController().getViewSize() == 1)
        {
            ((Activity) getContext()).finish();
        } else
        {
            dismissCurrentView();
        }
    }

    public static interface CropImageCallBack
    {
        public void onFinsh(String imagePath);
    }
}