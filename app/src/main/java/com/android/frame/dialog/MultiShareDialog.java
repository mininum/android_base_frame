package com.android.frame.dialog;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;


import com.android.frame.R;
import com.android.frame.logic.ShareUtil;
import com.android.frame.logic.api.API_Serviceinfo;
import com.android.frame.logic.model.ShareObj;

import org.rdengine.log.DLOG;
import org.rdengine.log.UMConstant;
import org.rdengine.runtime.RT;
import org.rdengine.util.ClickUtil;
import org.rdengine.util.PhoneUtil;
import org.rdengine.util.StringUtil;
import org.rdengine.widget.recycler.BaseRecyclerAdapter;
import org.rdengine.widget.recycler.BaseViewHolder;

import java.util.ArrayList;
import java.util.HashMap;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;

/**
 * 分享+菜单 综合dialog
 */
public class MultiShareDialog extends BaseDialog implements View.OnClickListener
{
    public static final int TYPE_WEIXIN = 0; // 微信-好友
    public static final int TYPE_WEIXIN_MOMENTS = 1; // 微信-朋友圈
    public static final int TYPE_QQ = 2; // QQ
    public static final int TYPE_QQ_ZONE = 3; // QQ空间
    public static final int TYPE_SINA = 4; // 新浪微博

    // ----------------R.layout.dialog_share_multi-------------Start
    private RecyclerView recyclerview_share;
    private RecyclerView recyclerview_menu;
    private TextView btn_cancle;

    /** auto load R.layout.dialog_share_multi */
    private void autoLoad_dialog_share_multi()
    {
        recyclerview_share = (RecyclerView) findViewById(R.id.recyclerview_share);
        recyclerview_menu = (RecyclerView) findViewById(R.id.recyclerview_menu);
        btn_cancle = (TextView) findViewById(R.id.btn_cancle);
    }
    // ----------------R.layout.dialog_share_multi-------------End

    public MultiShareDialog(Context context, ShareObj shareobj, ArrayList<ItemBean> menulist)
    {
        super(context, R.style.ActionSheet);

        Window w = getWindow();
        WindowManager.LayoutParams lp = w.getAttributes();
        w.getDecorView().setPadding(0, 0, 0, 0);
        lp.gravity = Gravity.BOTTOM;
        lp.width = context.getResources().getDisplayMetrics().widthPixels;
        w.setAttributes(lp);

        setContentView(R.layout.dialog_share_multi);
        autoLoad_dialog_share_multi();
        recyclerview_share
                .setLayoutManager(new LinearLayoutManager(getOwnerActivity(), OrientationHelper.HORIZONTAL, false));
        recyclerview_menu
                .setLayoutManager(new LinearLayoutManager(getOwnerActivity(), OrientationHelper.HORIZONTAL, false));

        btn_cancle.setOnClickListener(this);

        if (shareobj != null)
        {
            setData(shareobj);

            ArrayList<ItemBean> data_share = new ArrayList<ItemBean>();
            data_share.add(new ItemBean(TYPE_WEIXIN, "微信好友", R.drawable.icon_share_wechat));// 微信好友
            data_share.add(new ItemBean(TYPE_WEIXIN_MOMENTS, "朋友圈", R.drawable.icon_share_wechat_circle));// 朋友圈
            // data_share.add(new ItemBean(TYPE_SINA, "新浪微博", R.drawable.icon_share_sinaweibo));// 微博 todo 暂无
            data_share.add(new ItemBean(TYPE_QQ, "QQ好友", R.drawable.icon_share_qq));// QQ好友
            data_share.add(new ItemBean(TYPE_QQ_ZONE, "QQ空间", R.drawable.icon_share_qqzone));// QQ空间
            mAdapter_share = new MultiItemAdapter();
            mAdapter_share.addAll(data_share);
            mAdapter_share.setOnItemClickListener(shareOnItemClickListener);
            recyclerview_share.setAdapter(mAdapter_share);

        } else
        {
            recyclerview_share.setVisibility(View.GONE);
        }

        if (menulist != null)
        {
            mAdapter_menu = new MultiItemAdapter();
            mAdapter_menu.addAll(menulist);
            recyclerview_menu.setAdapter(mAdapter_menu);
        } else
        {
            recyclerview_menu.setVisibility(View.GONE);
        }

    }

    public void setMenuListener(BaseRecyclerAdapter.OnItemClickListener menuClicklistener)
    {
        mAdapter_menu.setOnItemClickListener(menuClicklistener);
    }

    MultiItemAdapter mAdapter_share, mAdapter_menu;

    String title;// = "关八Test";
    String des;// = "我们不生产八卦，我们只是娱乐圈的搬运工！";
    String image;// = FrescoImageHelper.getRandomImageUrl();
    String url;// = "http://www.guanba.com";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
        case R.id.btn_cancle :
        {
            dismiss();
        }
            break;
        }
    }

    /** 分享 选项 点击 */
    BaseRecyclerAdapter.OnItemClickListener shareOnItemClickListener = new BaseRecyclerAdapter.OnItemClickListener()
    {
        @Override
        public void onItemClickListener(View view, int position)
        {
            try
            {
                if (ClickUtil.isFastDoubleClick())
                    return;
                ItemBean item = mAdapter_share.getItem(position);
                if (item != null)
                {
                    if (item != null)
                    {
                        dismiss();
                        try
                        {
                            parseShareObj(shareObj, item.type);
                        } catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                        DLOG.e("cccmax", "share url=" + url);
                        switch (item.type)
                        {
                        case TYPE_QQ :
                        {
                            // QQ
                            if (shareObj.type == ShareObj.TYPE_IMAGE || shareObj.type == ShareObj.TYPE_COMMENT)
                            {
                                ShareUtil.shareQQImage(image, sharecallback);
                            } else
                            {
                                ShareUtil.shareQQ(title, des, image, url, sharecallback);
                            }
                            DLOG.event(UMConstant.ShareTo, "QQ");
                        }
                            break;
                        case TYPE_QQ_ZONE :
                        {
                            // QQ空间
                            ShareUtil.shareQZone(title, des, image, url, sharecallback);
                            DLOG.event(UMConstant.ShareTo, "QZone");
                        }
                            break;
                        case TYPE_SINA :
                        {
                            // 微博
                            ShareUtil.shareSina(title, des, image, url, sharecallback);
                            DLOG.event(UMConstant.ShareTo, "SinaWeibo");
                        }
                            break;
                        case TYPE_WEIXIN :
                        {
                            // 微信
                            if (shareObj.type == ShareObj.TYPE_IMAGE)
                            {
                                ShareUtil.shareWXImg(image, sharecallback);
                            } else
                            {
                                ShareUtil.shareWX(title, des, image, url, sharecallback);
                            }
                            DLOG.event(UMConstant.ShareTo, "Wechat");
                        }
                            break;
                        case TYPE_WEIXIN_MOMENTS :
                        {
                            // 朋友圈
                            if (shareObj.type == ShareObj.TYPE_IMAGE)
                            {
                                ShareUtil.shareWXMoments(image, sharecallback);
                            } else
                            {
                                ShareUtil.shareWXMomentsImg(title, des, image, url, sharecallback);
                            }
                            DLOG.event(UMConstant.ShareTo, "WechatMoments");
                        }
                            break;
                        }
                    }
                }
            } catch (Exception ex)
            {
            }
        }
    };

    private ShareObj shareObj;

    public void setData(ShareObj shareObj)
    {
        this.shareObj = shareObj;

        if (shareObj != null)
        {
            // 点击分享 article、channel、image、web、app
            String tag = "app";
            switch (shareObj.type)
            {
            case ShareObj.TYPE_FEED :
                tag = "feed";
                break;
            case ShareObj.TYPE_IMAGE :
                tag = "image";
                break;
            case ShareObj.TYPE_WEB :
                tag = "web";
                break;
            case ShareObj.TYPE_APP :
                tag = "app";
                break;
            case ShareObj.TYPE_TOPIC :
                tag = "topic";
                break;
            case ShareObj.TYPE_TOPIC_TIMELINE :
                tag = "topic_timeline";
                break;
            case ShareObj.TYPE_USER :
                tag = "user";
                break;
            }
            DLOG.event(UMConstant.ShareClass, tag);
        }
    }

    /**
     * 解析
     * 
     * @param obj
     */
    private void parseShareObj(ShareObj obj, int ptype)
    {
        switch (obj.type)
        {
        case ShareObj.TYPE_APP :
        {
            // 应用
        }
            break;
        case ShareObj.TYPE_USER :
        {
            // 用户个人页面
        }
            break;
        case ShareObj.TYPE_IMAGE :
        {
            // 图片
            this.image = obj.imageurl;
            // 新浪分享文章中的图片 需要带url、symbol
            if (ptype == TYPE_QQ)
            {
                this.url = API_Serviceinfo.getMotionWebHost();
            }
        }
            break;
        case ShareObj.TYPE_WEB :
        {
            // 其他网页
            this.image = obj.imageurl;
            this.title = obj.title;
            this.des = obj.des;
            this.url = obj.weburl;
            if (ptype == TYPE_SINA)
            {
                this.title = obj.title + obj.weburl;
                this.des = this.title;
            }
        }
            break;
        }
    }

    public static class ItemBean
    {
        public int type;
        public String title;
        public int icon_id;
        public boolean isSelected = false;

        public ItemBean(int type, String title, int icon_id)
        {
            this.type = type;
            this.title = title;
            this.icon_id = icon_id;
        }

        public ItemBean(int type, String title, int icon_id, boolean isSelected)
        {
            this.type = type;
            this.title = title;
            this.icon_id = icon_id;
            this.isSelected = isSelected;
        }
    }

    class MultiItemAdapter extends BaseRecyclerAdapter<ItemBean, ItemViewHolder>
    {
        public int padding_left = PhoneUtil.dipToPixel(30, RT.application);
        // item间隔19
        public int padding = PhoneUtil.dipToPixel(20, RT.application);
        // (屏幕宽- 左右边距25*2 - item间距19*4)/5
        public int item_w = (PhoneUtil.getScreenWidth(getContext()) - padding_left * 2 - padding * 4) / 5;

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int position)
        {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.cell_multi_share_dialog_item, parent, false);
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            lp.width = item_w;
            lp.height = -2;
            ItemViewHolder mHolder = new ItemViewHolder(view);
            mHolder.item_w = item_w;
            mHolder.padding_left = padding_left;
            mHolder.padding = padding;
            return mHolder;
        }
    }

    public static class ItemViewHolder extends BaseViewHolder
    {
        // ----------------R.layout.cell_multi_share_dialog_item-------------Start
        private ImageView iv_icon;
        private TextView tv_title;

        /** auto load R.layout.cell_multi_share_dialog_item */
        private void autoLoad_cell_multi_share_dialog_item(View v)
        {
            iv_icon = (ImageView) v.findViewById(R.id.iv_icon);
            tv_title = (TextView) v.findViewById(R.id.tv_title);
        }
        // ----------------R.layout.cell_multi_share_dialog_item-------------End

        public ItemViewHolder(View itemView)
        {
            super(itemView);
            autoLoad_cell_multi_share_dialog_item(itemView);
        }

        public int padding;
        public int padding_left;
        public int item_w;

        @Override
        public void setData(Object data, BaseRecyclerAdapter adapter)
        {
            if (adapter != null)
            {
                // MultiItemAdapter miadapter = (MultiItemAdapter) adapter;

                int count = adapter.getItemCount();
                int position = this.getPosition();

                // item 尺寸
                ViewGroup.MarginLayoutParams lp_item = (ViewGroup.MarginLayoutParams) itemView.getLayoutParams();
                lp_item.width = item_w;
                itemView.setLayoutParams(lp_item);

                // 图标尺寸
                ViewGroup.LayoutParams lp_icon = iv_icon.getLayoutParams();
                lp_icon.width = item_w;
                lp_icon.height = item_w;
                iv_icon.setLayoutParams(lp_icon);

                if (position == 0)
                {
                    // 第一个
                    lp_item.leftMargin = padding_left;
                    lp_item.rightMargin = 0;
                    itemView.setLayoutParams(lp_item);
                } else if (count > 1 && position == count - 1)
                {
                    // 最后一个
                    lp_item.leftMargin = padding;
                    lp_item.rightMargin = padding_left;
                    itemView.setLayoutParams(lp_item);
                } else
                {
                    // 不是最后一个
                    lp_item.leftMargin = padding;
                    lp_item.rightMargin = 0;
                    itemView.setLayoutParams(lp_item);
                }
            }

            ItemBean ib = (ItemBean) data;
            if (ib != null)
            {
                // ICON
                iv_icon.setImageResource(ib.icon_id);

                // 标题
                if (StringUtil.isEmpty(ib.title))
                {
                    tv_title.setText("");
                    tv_title.setVisibility(View.GONE);
                } else
                {
                    tv_title.setText(ib.title);
                    tv_title.setVisibility(View.VISIBLE);
                }

                itemView.setSelected(ib.isSelected);
            }
        }

    }

    PlatformActionListener sharecallback = new PlatformActionListener()
    {
        @Override
        public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap)
        {
            try
            {
                if (shareObj != null && shareObj.data != null)
                {

                }
            } catch (Exception ex)
            {
            }
        }

        @Override
        public void onError(Platform platform, int i, Throwable throwable)
        {}

        @Override
        public void onCancel(Platform platform, int i)
        {}
    };
}
