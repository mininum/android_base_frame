package org.rdengine.localimage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.fresco.FrescoImageHelper;
import com.facebook.fresco.FrescoParam.QiniuParam;
import com.android.frame.R;
import com.android.frame.dialog.LoadingDialog;

import org.rdengine.adapter.RDBaseAdapter;
import org.rdengine.localimage.LocalImageScanTask.LocalImageScanListener;
import org.rdengine.log.DLOG;
import org.rdengine.util.UiUtil.OnItemClickProxy;
import org.rdengine.view.manager.BaseListView;
import org.rdengine.view.manager.ViewParam;
import org.rdengine.widget.cobe.ChooseImageFeature;
import org.rdengine.widget.cobe.loadmore.LoadMoreContainer;
import org.rdengine.widget.cobe.ptr.PtrFrameLayout;

import java.util.ArrayList;

;

/**
 * 本地图片分组列表
 * 
 * @author CCCMAX
 */
public class LocalImageGroupListView extends BaseListView implements OnItemClickListener
{
    public static final String TAG = "LocalImageGroupView";

    public String getTag()
    {
        return TAG;
    }

    public LocalImageGroupListView(Context context, ViewParam param)
    {
        super(context, param);
    }

    ChooseImageFeature cif;

    ImageGroupAdapter mAdapter = new ImageGroupAdapter();
    LoadingDialog ld = new LoadingDialog(getContext(), "正在加载...");

    boolean hasjump = false;

    public void init()
    {
        super.init();
        ptrlistlayout.setFooterPadViewVisibility(false);
        listview.setAdapter(mAdapter);
        listview.setBackgroundColor(0XFFFFFFFF);

        cif = (ChooseImageFeature) mViewParam.data;
        if (cif.choosePathList == null)
            cif.choosePathList = new ArrayList<String>();

        ld.show();
        new LocalImageScanTask(getContext(), mLocalImageScanListener).execute();

        listview.setOnItemClickListener(new OnItemClickProxy(this));

    }

    public void refresh()
    {
        super.refresh();
    }

    @Override
    protected void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();
        Fresco.getImagePipeline().clearMemoryCaches();
    }

    LocalImageScanListener mLocalImageScanListener = new LocalImageScanListener()
    {
        public void onNotify(int action_type, ArrayList<LocalImageGroup> data)
        {
            switch (action_type)
            {
            case LocalImageScanTask.TYPE_LAST100_FINSH :
            {
                try
                {
                    if (ld != null)
                        ld.dismiss();
                    ViewParam vp = new ViewParam();
                    vp.data = cif;
                    vp.data1 = data.get(0);
                    getController().showView(LocalImageGridView.class, vp);
                    hasjump = true;
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
                break;
            case LocalImageScanTask.TYPE_NEW_GROUP :
            case LocalImageScanTask.TYPE_IMAGE_COUNT_UPDATE :
            {
                if (getShown() && hasjump)
                {
                    mAdapter.clearData();
                    mAdapter.addAll(data);
                    mAdapter.notifyDataSetChanged();
                }
            }
                break;
            case LocalImageScanTask.TYPE_FINSH :
            {
                mAdapter.clearData();
                mAdapter.addAll(data);
                mAdapter.notifyDataSetChanged();
                DLOG.e("cccmax", "TYPE_FINSH");
            }
                break;

            }
        }
    };

    // ---------------------------------------------------------

    @Override
    public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header)
    {
        // return PtrDefaultHandler.checkContentCanBePulledDown(frame, listview, header_pad);
        return false;
    }

    @Override
    public void onRefreshBegin(PtrFrameLayout frame)
    {

    }

    @Override
    public void onLoadMore(LoadMoreContainer loadMoreContainer)
    {

    }

    public class ImageGroupAdapter extends RDBaseAdapter<LocalImageGroup>
    {
        public View getView(int position, View convertView, ViewGroup parent)
        {
            if (convertView == null)
            {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.cell_local_imagegroup, null);
                convertView.setTag(new Viewholder(convertView));
            }
            Viewholder vh = (Viewholder) convertView.getTag();
            vh.setData(getItem(position));
            vh.line.setVisibility(position >= getCount() ? View.GONE : View.VISIBLE);
            return convertView;
        }

    }

    public class Viewholder
    {

        // ----------------R.layout.cell_local_imagegroup-------------Start
        private com.facebook.fresco.FrescoImageView fiv_image;
        private TextView tv_title;
        private TextView tv_num;
        private View line;

        public void autoLoad_cell_local_imagegroup(View view)
        {
            fiv_image = (com.facebook.fresco.FrescoImageView) view.findViewById(R.id.fiv_image);
            tv_title = (TextView) view.findViewById(R.id.tv_title);
            tv_num = (TextView) view.findViewById(R.id.tv_num);
            line = view.findViewById(R.id.line);
        }

        // ----------------R.layout.cell_local_imagegroup-------------End

        public Viewholder(View view)
        {
            autoLoad_cell_local_imagegroup(view);
        }

        public void setData(LocalImageGroup data)
        {
            if (data != null)
            {
                FrescoImageHelper.getImage(data.getFirstImgPath(), QiniuParam.C_M, fiv_image);
                tv_title.setText(data.getDirName());
                tv_num.setText("" + data.getImageCount());
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        ViewParam vp = new ViewParam();
        vp.data = cif;
        vp.data1 = parent.getAdapter().getItem(position);
        getController().showView(LocalImageGridView.class, vp);
    }

}
