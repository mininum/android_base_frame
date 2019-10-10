package org.rdengine.localimage;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.fresco.FrescoImageHelper;
import com.facebook.fresco.FrescoImageView;
import com.facebook.fresco.FrescoParam;
import com.android.frame.R;

import org.rdengine.adapter.ListCell;
import org.rdengine.adapter.ListStateItem;
import org.rdengine.adapter.RDBaseAdapter;
import org.rdengine.util.PhoneUtil;
import org.rdengine.view.manager.BaseView;
import org.rdengine.view.manager.ViewParam;
import org.rdengine.widget.ToastHelper;
import org.rdengine.widget.cobe.ChooseImageFeature;
import org.rdengine.widget.cobe.CodePullHandler;
import org.rdengine.widget.cobe.GridViewWithHeaderAndFooter;
import org.rdengine.widget.cobe.PtrGridLayout;
import org.rdengine.widget.cobe.loadmore.LoadMoreContainer;
import org.rdengine.widget.cobe.ptr.PtrFrameLayout;

import java.util.ArrayList;

public class LocalImageGridView extends BaseView implements CodePullHandler, OnClickListener
{

    public static final String TAG = "LocalImageGridView";
    // ----------------R.layout.local_image_grid_view-------------Start
    private View statemask; // include R.layout.titlebar
    private ImageView btn_back; // include R.layout.titlebar
    private TextView titlebar_title_tv; // include R.layout.titlebar
    private TextView titlebar_right_tv; // include R.layout.titlebar
    private ImageView titlebar_right_iv; // include R.layout.titlebar
    private LinearLayout titlebar; // include R.layout.titlebar
    private GridViewWithHeaderAndFooter gridview;
    private PtrGridLayout ptrlistlayout;
    private RelativeLayout empty_view;

    /** auto load R.layout.local_image_grid_view */
    private void autoLoad_local_image_grid_view()
    {
        statemask = (View) findViewById(R.id.statemask); // include R.layout.titlebar
        btn_back = (ImageView) findViewById(R.id.btn_back); // include R.layout.titlebar
        titlebar_title_tv = (TextView) findViewById(R.id.titlebar_title_tv); // include R.layout.titlebar
        titlebar_right_tv = (TextView) findViewById(R.id.titlebar_right_tv); // include R.layout.titlebar
        titlebar_right_iv = (ImageView) findViewById(R.id.titlebar_right_iv); // include R.layout.titlebar
        titlebar = (LinearLayout) findViewById(R.id.titlebar); // include R.layout.titlebar
        gridview = (GridViewWithHeaderAndFooter) findViewById(R.id.gridview);
        ptrlistlayout = (PtrGridLayout) findViewById(R.id.ptrlistlayout);
        empty_view = (RelativeLayout) findViewById(R.id.empty_view);
    }

    // ----------------R.layout.local_image_grid_view-------------End

    public String getTag()
    {
        return TAG;
    }

    public LocalImageGridView(Context context, ViewParam param)
    {
        super(context, param);
    }

    ChooseImageFeature cif;
    LocalImageGroup localimagegroup;

    ImageSelectAdapter mAdapter = new ImageSelectAdapter(getContext());

    public void init()
    {
        setContentView(R.layout.local_image_grid_view);
        autoLoad_local_image_grid_view();
        ptrlistlayout.setCodePullHandler(this);

        titlebar_right_tv.setText("确定");
        titlebar_right_tv.setTextColor(Color.parseColor("#333333"));
        titlebar_right_tv.setVisibility(View.VISIBLE);
        titlebar_right_tv.setOnClickListener(this);

        cif = (ChooseImageFeature) mViewParam.data;
        localimagegroup = (LocalImageGroup) mViewParam.data1;

        gridview.setAdapter(mAdapter);

    }

    public void refresh()
    {
        super.refresh();

        try
        {
            ArrayList<ListStateItem<String>> data = new ArrayList<ListStateItem<String>>();
            for (String path : localimagegroup.getImages())
            {
                ListStateItem<String> lsi = new ListStateItem<String>(path);
                lsi.flag = cif.choosePathList.contains(path);
                data.add(lsi);
            }
            mAdapter.addAll(data);
            mAdapter.notifyDataSetInvalidated();
        } catch (Exception e)
        {
            dismissCurrentView();
        }
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
        case R.id.titlebar_right_tv :
        {
            if (cif.choosePathList.size() == 0)
            {
                ToastHelper.showToast("还木有选好图片～");
                return;
            }
            // 选好图片了 结束选图
            {
                getController().killAllHistoryView();
                ((Activity) getController()).finish();
                cif.mChooseImageCallback.onFinsh(cif.choosePathList);
            }
        }
            break;

        }
    }

    public void updateOKbtn()
    {
        String str = "确定  (" + cif.choosePathList.size() + ")";
        titlebar_right_tv.setText(str);
    }

    @Override
    public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header)
    {
        // return PtrDefaultHandler.checkContentCanBePulledDown(frame, listview, header_pad);
        return false;
    }

    @Override
    public void onRefreshBegin(PtrFrameLayout frame)
    {}

    @Override
    public void onLoadMore(LoadMoreContainer loadMoreContainer)
    {}

    /**
     * 显示空View
     */
    protected void showEmptyView()
    {
        empty_view.setVisibility(View.VISIBLE);
        gridview.setVisibility(View.GONE);
    }

    /**
     * 隐藏空View
     */
    protected void dismissEmptyView()
    {
        empty_view.setVisibility(View.GONE);
        gridview.setVisibility(View.VISIBLE);
    }

    public class ImageSelectAdapter extends RDBaseAdapter<ListStateItem<String>>
    {
        Context mContext;
        LayoutInflater layoutinflater;
        int columnnum = 3;
        int spacing = 0;
        public int item_w = 0;

        public ImageSelectAdapter(Context context)
        {
            mContext = context;
            layoutinflater = LayoutInflater.from(context);
            spacing = PhoneUtil.dipToPixel(3, context);
            item_w = (PhoneUtil.getScreenWidth(context) - spacing * (columnnum - 1)) / columnnum;
        }

        public View getView(int position, View convertView, ViewGroup parent)
        {
            if (convertView == null)
            {
                convertView = layoutinflater.inflate(R.layout.cell_choose_localimage, null);
                new ViewHolder(convertView);
            }
            if (convertView.getTag() instanceof ListCell)
            {
                ((ListCell) convertView.getTag()).setData(getItem(position), position, this);
            }
            return convertView;
        }

    }

    public class ViewHolder implements ListCell
    {

        private View view;
        // ----------------R.layout.cell_choose_localimage-------------Start
        private FrescoImageView fiv_img;
        private FrameLayout btn_select;
        private TextView tv_selected;

        public void autoLoad_cell_choose_localimage(View view)
        {
            fiv_img = (FrescoImageView) view.findViewById(R.id.fiv_img);
            btn_select = (FrameLayout) view.findViewById(R.id.btn_select);
            tv_selected = (TextView) view.findViewById(R.id.tv_selected);
        }

        // ----------------R.layout.cell_choose_localimage-------------End

        public ViewHolder(View view)
        {
            autoLoad_cell_choose_localimage(view);
            view.setTag(this);
            this.view = view;

        }

        ListStateItem<String> itemdata;

        public void setBtnSelect()
        {
            int index = cif.choosePathList.indexOf(itemdata.data);

            if (itemdata.flag && index > -1)
            {
                tv_selected.setText("" + (index + 1));
                tv_selected.setVisibility(View.VISIBLE);
            } else
            {
                tv_selected.setVisibility(View.GONE);
            }
        }

        public void setData(Object data, int position, BaseAdapter mBaseAdapter)
        {
            itemdata = (ListStateItem<String>) data;

            FrescoParam fp = new FrescoParam(itemdata.data);
            fp.setDefaultImage(R.drawable.def_image);
            fp.setFailureImage(R.drawable.def_image_failure);
            // FrescoImageHelper.getImage(fp, fiv_img,
            // new FrescoPreHandleListener(fiv_img, mAdapter.item_w, mAdapter.item_w)
            // {
            // public void handle(ImageInfo ii, boolean isgif, int w, int h, float _ratio)
            // {}
            // });
            fp.setExpectantImgSize(240, 240);
            FrescoImageHelper.getImage(fp, fiv_img);

            setBtnSelect();

            btn_select.setTag(ViewHolder.this);
            btn_select.setOnClickListener(selectImgListener);
            view.setOnClickListener(proxListener);
        }

    }

    OnClickListener proxListener = new OnClickListener()
    {
        public void onClick(View v)
        {
            selectImgListener.onClick(v);
        }
    };

    OnClickListener selectImgListener = new OnClickListener()
    {
        public void onClick(View v)
        {
            ViewHolder vh = (ViewHolder) v.getTag();
            if (vh != null)
            {
                if (!vh.itemdata.flag && cif.choosePathList.size() == cif.max_count)
                {
                    ToastHelper.showToast("最多只能选择" + cif.max_count + "个图片");
                    return;
                }

                vh.itemdata.flag = !vh.itemdata.flag;

                if (vh.itemdata.flag && !cif.choosePathList.contains(vh.itemdata.data))
                {
                    cif.choosePathList.add(vh.itemdata.data);
                } else
                {
                    cif.choosePathList.remove(vh.itemdata.data);
                }

                vh.setBtnSelect();

                updateOKbtn();

                try
                {
                    for (int i = 0; i < gridview.getChildCount(); i++)
                    {
                        View child = gridview.getChildAt(i);
                        if (child != null && child.getTag() != null && child.getTag() instanceof ViewHolder)
                        {
                            ViewHolder childvh = (ViewHolder) child.getTag();
                            if (childvh.itemdata.flag)
                                childvh.setBtnSelect();
                        }
                    }
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    };

}
