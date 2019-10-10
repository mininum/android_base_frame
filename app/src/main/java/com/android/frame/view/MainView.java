package com.android.frame.view;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.android.frame.R;
import com.facebook.drawee.backends.pipeline.Fresco;


import org.rdengine.log.UMLogSender;
import org.rdengine.runtime.event.EventListener;
import org.rdengine.runtime.event.EventManager;
import org.rdengine.runtime.event.EventTag;
import org.rdengine.view.manager.BaseView;
import org.rdengine.view.manager.ViewParam;
import org.rdengine.widget.cobe.PtrListLayout;
import org.rdengine.widget.tabhost.main.MainTabButton;
import org.rdengine.widget.tabhost.main.MainTabHost;
import org.rdengine.widget.tabhost.main.MainTabHost.OnCheckedChangeListener;

public class MainView extends BaseView implements OnCheckedChangeListener
{
    public static final String TAG = "MainView";
    // ----------------R.layout.main_view-------------Start
    private FrameLayout container;
    private LinearLayout main_botton_bar_shadow;
    private MainTabButton HT_HOME;
    private MainTabButton HT_GAME;
    private MainTabButton HT_WELFARE;
    private MainTabButton HT_MINE;
    public MainTabHost tab_host;
    private ImageView btn_main_publish;
    private RelativeLayout main_botton_bar;

    /** auto load R.layout.main_view */
    private void autoLoad_main_view()
    {
        container = (FrameLayout) findViewById(R.id.container);
        main_botton_bar_shadow = (LinearLayout) findViewById(R.id.main_botton_bar_shadow);
        HT_HOME = (MainTabButton) findViewById(R.id.HT_HOME);
        HT_GAME = (MainTabButton) findViewById(R.id.HT_GAME);
        HT_WELFARE = (MainTabButton) findViewById(R.id.HT_WELFARE);
        HT_MINE = (MainTabButton) findViewById(R.id.HT_MINE);
        tab_host = (MainTabHost) findViewById(R.id.tab_host);
        main_botton_bar = (RelativeLayout) findViewById(R.id.main_botton_bar);
    }

    // ----------------R.layout.main_view-------------End

    public enum HostTitle
    {
        HT_HOME, // 首页
        HT_GAME, // 游戏
        HT_WELFARE, // 福利
        HT_MINE, // 我的
    }

    public MainView(Context context, ViewParam param)
    {
        super(context, param);
    }

    public String getTag()
    {
        return TAG;
    }

    public HomeView mHomeView;
    public GameView mGameView;
    public WelfareView mWelfareView;
    public MineView mMineView;

    public int checkedPosition = HostTitle.HT_HOME.ordinal();

    public void init()
    {
        setContentView(R.layout.main_view);
        autoLoad_main_view();

        tab_host.setOnCheckedChangeListener(this);
        tab_host.setChecked(checkedPosition);

        EventManager.ins().registListener(EventTag.ACCOUNT_LOGIN, mEventListener);
        EventManager.ins().registListener(EventTag.ACCOUNT_LOGOUT, mEventListener);

        onCheckedChange(HostTitle.HT_HOME.ordinal(), false);
    }

    EventListener mEventListener = new EventListener()
    {
        public void handleMessage(int what, int arg1, int arg2, Object dataobj)
        {
            switch (what)
            {
            case EventTag.ACCOUNT_LOGIN :
            {
            }
                break;
            case EventTag.ACCOUNT_LOGOUT :
            {
                // 退出登录
            }
                break;
            }
        }
    };

    protected void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();
        EventManager.ins().removeListener(EventTag.ACCOUNT_LOGIN, mEventListener);
        EventManager.ins().removeListener(EventTag.ACCOUNT_LOGOUT, mEventListener);
    }

    public void refresh()
    {
        if (!hasRefresh())
            preRequest();

        super.refresh();
        // tab_host.setUnreadCount(HostTitle.HT_MINE.ordinal(), MsgMgr.ins().getUnreadCount());

    }

    @Override
    public void onShow()
    {
        super.onShow();
        int count = container.getChildCount();
        for (int i = 0; i < count; i++)
        {
            BaseView view = (BaseView) container.getChildAt(i);
            if (view != null && getCheckedView() == view)
            {
                view.onShow();
                break;
            }
        }
        // ((Activity) getContext()).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

    }

    @Override
    public void onHide()
    {
        super.onHide();
        int count = container.getChildCount();
        for (int i = 0; i < count; i++)
        {
            BaseView view = (BaseView) container.getChildAt(i);
            view.onHide();
        }
        // ((Activity) getContext()).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    private BaseView getCheckedView()
    {
        if (checkedPosition == HostTitle.HT_HOME.ordinal())
        {
            return mHomeView;
        } else if (checkedPosition == HostTitle.HT_GAME.ordinal())
        {
            return mGameView;
        } else if (checkedPosition == HostTitle.HT_WELFARE.ordinal())
        {
            return mWelfareView;
        } else if (checkedPosition == HostTitle.HT_MINE.ordinal())
        {
            return mMineView;
        }
        return null;
    }

    @Override
    public boolean onCheckedChange(int checkedPosition, boolean byUser)
    {
        if (this.checkedPosition == checkedPosition && container.getChildCount() > 0)
        {
            // 点击当前页面
            try
            {
                PtrListLayout ptrListLayout = null;
                ListView listview = null;
                if (checkedPosition == HostTitle.HT_HOME.ordinal())
                {
                    // listview = mHomeView.listview;
                    // ptrListLayout = mHomeView.ptrlistlayout;
                    mHomeView.onMainTabClick();
                } else if (checkedPosition == HostTitle.HT_GAME.ordinal())
                {
                    // listview = mFollowView.listview;
                    // ptrListLayout = mFollowView.ptrlistlayout;
                    mGameView.onMainTabClick();
                } else if (checkedPosition == HostTitle.HT_WELFARE.ordinal())
                {
                    // listview = mFindView.listview;
                    // ptrListLayout = mFindView.ptrlistlayout;
                    mWelfareView.onMainTabClick();
                }
                // else if (checkedPosition == HostTitle.HT_MTN.ordinal())
                // {
                // // listview = mFindView.listview;
                // // ptrListLayout = mFindView.ptrlistlayout;
                // mMtnView.onMainTabClick();
                // }
                else if (checkedPosition == HostTitle.HT_MINE.ordinal())
                {
                    mMineView.onMainTabClick();
                }

                // if (listview != null && ptrListLayout != null)
                // {
                // int p = listview.getFirstVisiblePosition();
                // if (p == 0)
                // {
                // ptrListLayout.autoRefresh();
                // } else if (p < 20)
                // {
                // listview.smoothScrollToPositionFromTop(0, 0);
                // } else
                // {
                // listview.setSelection(0);
                // }
                // }
            } catch (Exception e)
            {
                e.printStackTrace();
            }
            return true;
        }

        Fresco.getImagePipeline().clearMemoryCaches();

        if (checkedPosition == HostTitle.HT_HOME.ordinal())
        {
            if (mHomeView == null)
            {
                mHomeView = new HomeView(getContext(), null);
                mHomeView.init();
            }
            mHomeView.setVisibility(View.VISIBLE);
            if (container.indexOfChild(mHomeView) == -1)
            {
                container.addView(mHomeView);
                mHomeView.refresh();
            }
            mHomeView.bringToFront();
            // hideOtherViews();
            mHomeView.onShow();

            main_botton_bar_shadow.setVisibility(View.VISIBLE);

            // DLOG.event(UMConstant.MainTabClick, "home");
        } else if (checkedPosition == HostTitle.HT_GAME.ordinal())
        {
            if (mGameView == null)
            {
                mGameView = new GameView(getContext(), null);
                mGameView.init();
            }
            mGameView.setVisibility(View.VISIBLE);
            if (container.indexOfChild(mGameView) == -1)
            {
                container.addView(mGameView);
                mGameView.refresh();
            }
            mGameView.bringToFront();
            // hideOtherViews();
            mGameView.onShow();

            main_botton_bar_shadow.setVisibility(View.VISIBLE);

            // DLOG.event(UMConstant.MainTabClick, "game");
        } else if (checkedPosition == HostTitle.HT_WELFARE.ordinal())
        {
            if (mWelfareView == null)
            {
                mWelfareView = new WelfareView(getContext(), null);
                mWelfareView.init();
            }
            mWelfareView.setVisibility(View.VISIBLE);
            if (container.indexOfChild(mWelfareView) == -1)
            {
                container.addView(mWelfareView);
                mWelfareView.refresh();
            }
            mWelfareView.bringToFront();
            // hideOtherViews();
            mWelfareView.onShow();

            main_botton_bar_shadow.setVisibility(View.VISIBLE);

            // DLOG.event(UMConstant.MainTabClick, "count");
        } else if (checkedPosition == HostTitle.HT_MINE.ordinal())
        {
            // TODO 判断登录才能显示
            // if (!UserMgr.checkLoginedAndTipGotoLoginView(getContext(), new UserMgr.LoginListener()
            // {
            // @Override
            // public void onLogined()
            // {
            // // onCheckedChange(HostTitle.HT_MINE.ordinal(), false);
            // tab_host.setChecked(HostTitle.HT_MINE.ordinal());
            // }
            //
            // public void onError()
            // {}
            //
            // public void onCanceled()
            // {}
            // }))
            // {
            // // 未登录
            // return false;
            // }

            if (mMineView == null)
            {
                mMineView = new MineView(getContext(), null);
                mMineView.init();
            }
            mMineView.setVisibility(View.VISIBLE);
            if (container.indexOfChild(mMineView) == -1)
            {
                container.addView(mMineView);
                mMineView.refresh();
            }
            mMineView.bringToFront();
            // hideOtherViews();
            mMineView.onShow();

            main_botton_bar_shadow.setVisibility(View.VISIBLE);

            // DLOG.event(UMConstant.MainTabClick, "mine");
        }

        this.checkedPosition = checkedPosition;

        hideOtherViews();
        container.postInvalidate();

        UMLogSender.sendMainTabHostLog(this.checkedPosition);

        return true;
    }

    private void hideOtherViews()
    {
        BaseView showview = getCheckedView();
        if (showview != null)
        {
            int count = container.getChildCount();
            for (int i = 0; i < count; i++)
            {
                BaseView view = (BaseView) container.getChildAt(i);
                if (showview != view)
                {
                    view.setVisibility(View.GONE);
                    view.onHide();
                }
            }
        }
    }

    /** 个别数据预请求好缓存数据 */
    private void preRequest()
    {
        new Thread(new Runnable()
        {
            public void run()
            {
                try
                {
                    // TODO
                } catch (Exception e)
                {

                }
            }
        }).start();
    }

}
