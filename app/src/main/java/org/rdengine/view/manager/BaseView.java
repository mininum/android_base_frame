package org.rdengine.view.manager;

import android.app.Activity;
import android.content.Context;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.frame.R;

import org.rdengine.log.DLOG;
import org.rdengine.util.StringUtil;
import org.rdengine.util.UiUtil;

/**
 * 页面基类
 * 
 * @author CCCMAX
 */
public abstract class BaseView extends FrameLayout implements ContentStateLayout.ContentStateClickListener
{

    protected ViewParam mViewParam;

    private ViewController controller;

    private boolean refreshed = false;

    private BaseView(Context context)
    {
        super(context);
        if (!(context instanceof ViewController))
        {
            throw new Error("context is not a Activity implements ViewController");
        }
        controller = (ViewController) context;
        setClickable(true);
        refreshed = false;
    }

    public BaseView(Context context, ViewParam param)
    {
        this(context);
        mViewParam = param;
    }

    /**
     * 初始化 可以findviewbyid
     */
    public abstract void init();

    protected View btn_back;

    protected TextView tv_title;

    /** 系统状态栏高度，大于0时，说明应用全屏显示，有在状态栏的位置显示内容 */
    protected int statusBarHeight = 0;

    /**
     * 修改titlebar的高度，沉浸模式
     */
    protected void modifyTitleBarHeight()
    {
        if (UiUtil.isOpenTransparentStatusbar())
        {
            statusBarHeight = UiUtil.getStatusBarHeight();

            // View titleBar = findViewById(R.id.titlebar);
            // if (titleBar != null && titleBar.getVisibility() == View.VISIBLE)
            // {
            // ViewGroup.LayoutParams params = titleBar.getLayoutParams();
            // params.height = (int) (getResources().getDimension(R.dimen.titlebar_height) + statusBarHeight);
            // titleBar.setPadding(0, statusBarHeight, 0, 0);
            // titleBar.setLayoutParams(params);
            // }

            View statemask = findViewById(R.id.statemask);
            if (statemask != null)
            {
                statemask.setVisibility(View.INVISIBLE);
                ViewGroup.LayoutParams params = statemask.getLayoutParams();
                params.height = (int) (statusBarHeight);
                statemask.setLayoutParams(params);
            }
        }
    }

    /**
     * 设置titlebar信息
     */
    protected void setTitleBar()
    {
        btn_back = findViewById(R.id.btn_back);
        if (btn_back != null)
        {
            btn_back.setOnClickListener(new OnClickListener()
            {

                @Override
                public void onClick(View v)
                {
                    if (getController().getViewSize() <= 1)
                    {
                        ((Activity) getController()).finish();
                    } else
                    {
                        dismissCurrentView();
                    }
                }
            });
        }

        tv_title = (TextView) findViewById(R.id.titlebar_title_tv);
        if (tv_title != null)
        {
            if (mViewParam != null && !StringUtil.isEmpty(mViewParam.title))
            {
                tv_title.setText(mViewParam.title);
            }
        }
    }

    /** 最后一次refresh的时间 */
    public long lastRefreshTime = 0;

    /** 最后一次hide的时间 这个值的含义是用户观看此页面的最后时间 */
    public long lastHideTime = 0;

    /** 自动刷新页面的时间间隔 30分钟 1800000=30*60*1000 */
    public long autoRefreshTimeSpace = 1800000;

    /** 按时间自动刷新的开关 */
    public boolean autoRefreshOnTime = false;

    /**
     * 刷新 在页面第一次显示的时候会背动调用 也可以在之后手动调用
     */
    public void refresh()
    {
        refreshed = true;
        lastRefreshTime = System.currentTimeMillis();
    }

    public boolean hasRefresh()
    {
        return refreshed;
    }

    /**
     * 自动刷新页面的实际操作方法，需要的话可以重写方法<br>
     * 在autoRefreshOnTime为true时，并且页面时隔N再次显示时触发
     */
    public void autoRefreshOnShowAgain()
    {
        DLOG.i("BaseView", "autoRefreshOnShowAgain:" + this.getClass().getName());
    }

    public void setViewParam(ViewParam param)
    {
        mViewParam = param;
    }

    protected LayoutInflater mInflater;

    protected void setContentView(int layoutId)
    {
        mInflater = LayoutInflater.from(getContext());
        mInflater.inflate(layoutId, this, true);

        modifyTitleBarHeight();

        setTitleBar();
        setContentStateView();
    }

    /**
     * 获取当前View的Controller,用于控制View流程
     * 
     * @return
     */
    public ViewController getController()
    {
        return controller;
    }

    /**
     * baseview自身关闭当前界面
     */
    protected void dismissCurrentView()
    {
        if (controller.getTopView() != this)
            return;
        // if (!(this instanceof SwipeBackView))
        closeInputMethod();
        controller.backView();

        // controller.getActivityContainer().delViewInMask(this.getClass());
    }

    private Object parent;

    public Object getParentObj()
    {
        return parent;
    }

    public void setParentObj(Object parent)
    {
        this.parent = parent;
    }

    public void hideChildrenView()
    {

    }

    @Override
    protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();
    }

    /**
     * article中接收到back按键事件，询问当前baseview是否可以back, <br>
     * dismissCurrentView方法也会触发onBack()
     * 
     * @return true:默认执行Back,false:拦截Back事件
     */
    public boolean onBack()
    {
        return true;
    }

    private boolean isShown = false;

    public boolean getShown()
    {
        return isShown;
    }

    protected boolean isFirstShow = true;

    /**
     * 每次显示时调用 和refresh不同
     */
    public void onShow()
    {
        if (!isShown)
        {
            isShown = true;
            DLOG.i("BaseView", "onShow:" + this.getClass().getName());
            DLOG.eventPageStart(this.getTag());

            // 判断自动刷新
            if (autoRefreshOnTime && lastHideTime != 0
                    && System.currentTimeMillis() - lastHideTime >= autoRefreshTimeSpace)
            {
                // 显示此页面时间与上次隐藏此页面的时间间隔 超过预设时常，就触发自动刷新
                autoRefreshOnShowAgain();
            }
        }

        isFirstShow = false;
    }

    /**
     * 每次隐藏时调用 或在其他baseview覆盖其上时调用
     */
    public void onHide()
    {
        if (isShown)
        {
            isShown = false;
            DLOG.i("BaseView", "onHide:" + this.getClass().getName());
            DLOG.eventPageEnd(getTag());
            hideChildrenView();

            lastHideTime = System.currentTimeMillis();
        }
    }

    /**
     * 加载资源
     */
    public void onLoadResource()
    {
        // DLOG.i("BaseView", "onLoadResource:" + this.getClass().getName());
        // attachChild(this);
    }

    /**
     * 释放资源
     */
    public void onReleaseResource()
    {
        // DLOG.i("BaseView", "onReleaseResource:" + this.getClass().getName());
        // detachChild(this);
    }

    private void attachChild(ViewGroup group)
    {
        try
        {
            int count = group.getChildCount();
            if (count > 0)
            {
                for (int i = 0; i < count; i++)
                {
                    View v = group.getChildAt(i);
                    if (v instanceof ViewGroup)
                    {
                        attachChild((ViewGroup) v);
                    }
                }
            }
        } catch (Throwable e)
        {
        }
    }

    private void detachChild(ViewGroup group)
    {
        try
        {
            int count = group.getChildCount();
            if (count > 0)
            {
                for (int i = 0; i < count; i++)
                {
                    View v = group.getChildAt(i);
                    if (v instanceof ViewGroup)
                    {
                        detachChild((ViewGroup) v);
                    }
                }
            }
        } catch (Throwable e)
        {
        }
    }

    public abstract String getTag();

    /**
     * 强制关闭输入法
     */
    public void closeInputMethod(View... v)
    {
        try
        {
            View view = null;
            if (v != null && v.length > 0)
                view = v[0];
            if (getContext() instanceof BaseActivity)
            {
                BaseActivity activity = (BaseActivity) getContext();
                InputMethodManager inputMethodManager = (InputMethodManager) activity
                        .getSystemService(Activity.INPUT_METHOD_SERVICE);
                IBinder ibinder = activity.getCurrentFocus().getWindowToken();
                if (ibinder == null && view != null)
                {
                    ibinder = view.getWindowToken();
                    view.clearFocus();
                }
                inputMethodManager.hideSoftInputFromWindow(ibinder, InputMethodManager.HIDE_NOT_ALWAYS);
            }
        } catch (Exception e)
        {
        }
    }

    public void openInputMethod(View v)
    {
        try
        {
            v.requestFocus();
            InputMethodManager inputMethodManager = (InputMethodManager) getContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.showSoftInput(v, 0);
        } catch (Exception e)
        {
        }
    }

    public static final int INPUT_TYPE_TOUCH_KEY = 1;
    public static final int INPUT_TYPE_TOUCH_NOKEY = 2;
    public static final int INPUT_TYPE_NOTOUCH_KEY = 3;
    public static final int INPUT_TYPE_NOTOUCH_NOKEY = 4;
    /** touch事件可穿透给下层view */
    public static final int INPUT_TYPE_TOUCH_KEY_THROUGH = 5;
    public static final int INPUT_TYPE_TOUCH_NOKEY_THROUGH = 6;

    ViewGroup layoutEmpty;
    protected ContentStateLayout mContentStateLayout;

    protected void setContentStateView()
    {
        try
        {
            layoutEmpty = (ViewGroup) findViewById(R.id.empty_view);
            if (layoutEmpty != null)
            {
                mContentStateLayout = new ContentStateLayout(getContext());
                layoutEmpty.addView(mContentStateLayout, -1, -1);
                mContentStateLayout.hide();
                layoutEmpty.setBackgroundResource(R.color.full_transparent);
                layoutEmpty.setVisibility(View.VISIBLE);
                mContentStateLayout.setContentStateClickListener(this);
            }
        } catch (Exception e)
        {
        }
    }

    /**
     * 显示内容页特殊状态遮罩
     * 
     * @param type
     *            状态
     * @param imgID
     *            图片ID 0使用默认图
     * @param tip
     *            提示语 null使用默认提示语
     */
    protected void showContentState(ContentStateLayout.ContentStateType type, int imgID, String tip)
    {
        if (mContentStateLayout != null)
        {
            DLOG.e("cccmax", "showContentState type=" + type);
            mContentStateLayout.showState(type, imgID, tip);
        }
    }

    /**
     * 隐藏内容页特殊状态遮罩
     */
    protected void hideContentState()
    {
        if (mContentStateLayout != null)
        {
            DLOG.e("cccmax", "hideContentState");
            mContentStateLayout.hideAnimator();
        }
    }

    protected boolean isContentStateShowing()
    {
        return mContentStateLayout.getVisibility() == View.VISIBLE;
    }

    /*
     * 特殊装特遮罩 被点击
     */
    @Override
    public void onStateLayoutClick(ContentStateLayout.ContentStateType type, View v)
    {

    }

    /** 显示此页面时 系统状态栏是否需要设置成黑色文字 */
    public boolean statusBarNeedDark = true;

    /** 当设置系统状态栏颜色样式失败时的替补颜色 */
    public int statusBarSafeColor = 0xff000000;

    /** 设置此页面需要的系统状态栏文字颜色模式 */
    public boolean setStatusBarDarkTheme()
    {
        try
        {
            boolean ret = UiUtil.setStatusBarDarkTheme((Activity) getContext(), statusBarNeedDark);
            if (!ret && statusBarNeedDark)
            {
                // 设置黑色样式失败
                setStatemaskColorOnSysDarkErr();
            }
            return ret;
        } catch (Exception ex)
        {
            return false;
        }
    }

    /** 当设置系统状态栏颜色样式失败时 设置一个替补颜色做背景 */
    public void setStatemaskColorOnSysDarkErr()
    {
        try
        {
            View statemask = findViewById(R.id.statemask);
            if (statemask != null)
            {
                statemask.setBackgroundColor(statusBarSafeColor);
            }
        } catch (Exception ex)
        {
        }
    }

}
