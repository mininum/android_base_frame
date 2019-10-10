package org.rdengine.localimage;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore.Images.Media;

import java.io.File;
import java.util.ArrayList;

/**
 * 本地图片扫描任务
 * 
 * @author CCCMAX
 */
public class LocalImageScanTask extends AsyncTask<Void, Integer, Boolean>
{
    /* 已经扫描完第一组相册 */
    public final static int TYPE_LAST100_FINSH = 0x001;
    /* 扫描完新的分组 */
    public final static int TYPE_NEW_GROUP = 0x002;
    /* 图片数量更新 */
    public final static int TYPE_IMAGE_COUNT_UPDATE = 0x003;
    /* 结束 */
    public final static int TYPE_FINSH = 0x004;

    /**
     * 文件大小过滤 1K一下不要
     */
    private final long filesizecheck_min = 1024 * 1;
    /** 多少图片通知一次 */
    private final int count_notify_num = 500;

    Context mContext;
    LocalImageScanListener mListener;

    private ArrayList<LocalImageGroup> mGruopList = new ArrayList<LocalImageGroup>();

    public LocalImageScanTask(Context context, LocalImageScanListener listener)
    {
        mContext = context;
        mListener = listener;
    }

    /*
     * 开始前
     */
    protected void onPreExecute()
    {
        super.onPreExecute();
    }

    /** 任务执行中 */
    protected Boolean doInBackground(Void... params)
    {
        boolean isSentLast100 = false;

        Cursor mCursor = null;

        try
        {
            Uri mImageUri = Media.EXTERNAL_CONTENT_URI;
            ContentResolver mContentResolver = mContext.getContentResolver();
            StringBuilder selection = new StringBuilder();
            selection.append(Media.MIME_TYPE).append("=?");
            selection.append(" or ");
            selection.append(Media.MIME_TYPE).append("=?");
            selection.append(" or ");
            selection.append(Media.MIME_TYPE).append("=?");
            selection.append(" or ");
            selection.append(Media.MIME_TYPE).append("=?");

            mCursor = mContentResolver.query(mImageUri, null, selection.toString(), new String[]
            { "image/jpeg", "image/png", "image/gif",/* "image/webp" */ }, Media.DATE_MODIFIED + " DESC");

            LocalImageGroup item_last100 = new LocalImageGroup();
            item_last100.setDirName("最近100张");
            mGruopList.add(item_last100);

            long count = 0;

            // 遍历结果
            while (mCursor.moveToNext())
            {
                // 获取图片的路径
                String path = mCursor.getString(mCursor.getColumnIndex(Media.DATA));
                long size = mCursor.getLong(mCursor.getColumnIndex(Media.SIZE));

                // 获取该图片的所在文件夹的路径
                File file = new File(path);

                // 过滤
                if (!file.exists())
                    continue;
                // 文件大小过滤
                if (size < filesizecheck_min)
                    continue;

                String parentName = "";
                if (file.getParentFile() != null)
                {
                    parentName = file.getParentFile().getName();
                } else
                {
                    parentName = file.getName();
                }

                // 文件夹名称过滤
                if (parentName.startsWith("temp") || parentName.startsWith("cache"))
                    continue;

                count++;

                // 构建一个imageGroup对象
                LocalImageGroup item = new LocalImageGroup();
                // 设置imageGroup的文件夹名称
                item.setDirName(parentName);

                // 向最新100张照片组中添加
                if (item_last100.getImageCount() < 100)
                {
                    item_last100.addImage(path);
                    if (item_last100.getImageCount() == 100)
                    {
                        // 最近100张照片更新完毕
                        if (!isSentLast100)
                        {
                            publishProgress(TYPE_LAST100_FINSH);
                            isSentLast100 = true;
                        }
                    }
                }

                // 寻找该imageGroup是否是其所在的文件夹中的一个
                int searchIdx = mGruopList.indexOf(item);
                if (searchIdx >= 0)
                {
                    // 如果是，该组的图片数量+1
                    LocalImageGroup imageGroup = mGruopList.get(searchIdx);
                    imageGroup.addImage(path);
                    // Todo 通知更新组内图片
                } else
                {
                    item.addImage(path);
                    mGruopList.add(item);
                    // TODO 通知更新组
                    publishProgress(TYPE_NEW_GROUP);
                }

                if (count % count_notify_num == 0)
                {
                    publishProgress(TYPE_IMAGE_COUNT_UPDATE);
                }

                if (!isSentLast100)
                {
                    publishProgress(TYPE_LAST100_FINSH);
                    isSentLast100 = true;
                }

            }

        } catch (Exception e)
        {
            e.printStackTrace();
        } finally
        {
            // 关闭游标
            if (mCursor != null && !mCursor.isClosed())
            {
                mCursor.close();
            }
        }

        return true;
    }

    /**
     * 过程中更新
     */
    protected void onProgressUpdate(Integer... values)
    {
        super.onProgressUpdate(values);
        int type = values[0];
        if (mListener != null)
        {
            mListener.onNotify(type, mGruopList);
        }
    }

    /**
     * 扫描完成
     */
    protected void onPostExecute(Boolean result)
    {
        if (mListener != null)
        {
            mListener.onNotify(TYPE_FINSH, mGruopList);
        }
    }

    public static interface LocalImageScanListener
    {
        public void onNotify(int action_type, ArrayList<LocalImageGroup> data);
    }
}
