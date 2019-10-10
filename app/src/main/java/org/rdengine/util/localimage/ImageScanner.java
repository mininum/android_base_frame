package org.rdengine.util.localimage;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;

/**
 * 手机图片扫描
 * 
 * @author CCCMAX
 */
public class ImageScanner
{
    private Context mContext;

    public ImageScanner(Context context)
    {
        this.mContext = context;
    }

    public void scanImages(final ScanCompleteCallBack callback)
    {
        final Handler mHandler = new Handler()
        {

            @Override
            public void handleMessage(Message msg)
            {
                super.handleMessage(msg);
                callback.scanComplete((Cursor) msg.obj);
            }
        };

        new Thread(new Runnable()
        {

            @Override
            public void run()
            {
                Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                ContentResolver mContentResolver = mContext.getContentResolver();

                Cursor mCursor = mContentResolver.query(mImageUri, null,
                        MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=?",
                        new String[]
                { "image/jpeg", "image/png" },
                        // new String[]{"image/jpeg"},
                        MediaStore.Images.Media.DATE_MODIFIED + " DESC");

                // Cursor mCursor = mContentResolver.query(mImageUri, null, null, null, null);

                Message msg = mHandler.obtainMessage();
                msg.obj = mCursor;
                mHandler.sendMessage(msg);
            }
        }).start();

    }

    public static interface ScanCompleteCallBack
    {
        public void scanComplete(Cursor cursor);
    }

}
