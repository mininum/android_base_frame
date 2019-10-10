package com.android.frame.view;

import android.content.Context;

import android.support.v7.widget.RecyclerView;

import com.android.frame.R;

import org.rdengine.view.manager.BaseView;
import org.rdengine.view.manager.ViewParam;


import java.util.ArrayList;

public class GameView extends BaseView
{
    private final static String TAG = "GameView";
    // ----------------R.layout.layout_game_view-------------Start
    private RecyclerView recycle_game; // ----------------R.layout.layout_game_view-------------End

    @Override
    public String getTag()
    {
        return TAG;
    }

    public GameView(Context context, ViewParam param)
    {
        super(context, param);
    }



    @Override
    public void init()
    {
        setContentView(R.layout.layout_game_view);


    }

    //设置banner数据
//    private void setBannerData(ArrayList<String> list){
//
//        tickerHeader.setData(list);
//
//    }
//
//    /**
//     * 焦点banner 轮播控制器 生成view 以及 点击
//     */
//
//    TickerHeader.TickerheaderHandle tickerheaderHandle = new TickerHeader.TickerheaderHandle() {
//
//        int padding = PhoneUtil.dipToPixel(15, getContext());
//        int radius = PhoneUtil.dipToPixel(8, getContext());
//        @Override
//        public View getView(RDBaseAdapter adapter, int position, View convertView, ViewGroup parent) {
//
//
//            if (convertView == null)
//            {
//                RelativeLayout relativeLayout = new RelativeLayout(getContext());
//                relativeLayout.setBackgroundResource(R.color.view_background);
//                Gallery.LayoutParams lp = new Gallery.LayoutParams(-1, tickerHeader.getTickerHeight());
//                relativeLayout.setLayoutParams(lp);
//
//                SimpleDraweeView fiv = FrescoImageHelper.createView(getContext());
//                relativeLayout.setPadding(padding, 0, padding, 0);
//                relativeLayout.addView(fiv, -1, -1);
//
//                convertView = relativeLayout;
//            }
//
//            RelativeLayout relativeLayout = (RelativeLayout) convertView;
//            SimpleDraweeView fiv = (SimpleDraweeView) relativeLayout.getChildAt(0);
//            String url = (String) adapter.getItem(position);
//
//            FrescoParam fp = new FrescoParam(url, FrescoParam.QiniuParam.Z_MAX_L);
//            fp.setRoundedCornerRadius(radius);
//            fp.DefaultImageID = R.drawable.def_image_big;
//            FrescoImageHelper.getImage(fp, fiv, null);
//
//            return convertView;
//        }
//
//        @Override
//        public View getView(TickerHeader.TickerPagerViewAdapter adapter, int position, View convertView, ViewGroup parent) {
//            return convertView;
//        }
//
//        @Override
//        public void onItemClick(Object dataobj, View view) {
//            //todo banner点击事件
//        }
//    };

    public void onMainTabClick()
    {
        try
        {
        } catch (Exception ex)
        {
        }
    }

}
