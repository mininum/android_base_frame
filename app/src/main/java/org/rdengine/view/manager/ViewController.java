package org.rdengine.view.manager;

import android.widget.FrameLayout;

import java.util.List;

/**
 * View控制器接口
 * 
 * @author yangyu
 */
public interface ViewController
{

    /**
     * 显示新界面
     * 
     * @param clazz
     * @param param
     */
    public void showView(Class<? extends BaseView> clazz, ViewParam param);

    /**
     * 返回上一页
     * 
     * @return true:已到底了,没有View能back了,false:还没到底
     */
    public boolean backView();

    /**
     * 干掉上一页
     */
    public void killViewAt(int index);

    /**
     * 获取View数量
     * 
     * @return
     */
    public int getViewSize();

    /** 获取页面所在位置 */
    int getViewIndex(BaseView baseview);

    /**
     * 获取指定位置View
     * 
     * @param index
     * @return
     */
    public BaseView getViewAt(int index);

    /**
     * 获取最上层View
     * 
     * @return
     */
    public BaseView getTopView();

    /**
     * 干掉之前所有的页
     */
    public void killAllHistoryView();

    /**
     * 干掉所有相同Class的View
     * 
     * @param view
     */
    public void killAllSameView(BaseView view);

    /**
     * 干掉某个相同的class的view，但是保留最近的几个
     * 
     * @param view
     * @param keepCount
     *            保留最近的几个相同view
     */
    public void killSameViewAndKeepSome(BaseView view, int keepCount);

    /**
     * 查找view栈中 某个baseview子类的对象
     * 
     * @param clazz
     * @return
     */
    public List<BaseView> findClazzViews(Class<? extends BaseView> clazz);

    /**
     * 获最window显示状态,如果View是在Activity里, 这玩意返回true
     * 
     * @return
     */
    public boolean getWindowVisibility();

    /**
     * 设置Window的显示状态,如果View是在Activity里,这个方法无效
     * 
     * @param visibility
     */
    public void setWindowVisibility(boolean visibility);

    /**
     * 设置回默认的WindowLayoutParams,如果你的View不知道应该用哪个的话,就用这个,知道的View自己会修改
     */
    public void setDefaultLayoutParams();

    /**
     * 设置Window的LayoutParams,改变浮窗的显示状态,如果你不是浮窗专用的,就别用这个东西,如果View在Activity里这个方法无效
     * 
     * @param x
     *            相对手机屏幕的X坐标
     * @param y
     *            相对手机屏幕的Y坐标
     * @param w
     *            浮窗宽
     * @param h
     *            浮窗高
     * @param inputType
     *            支持的用户输入方式,TOUCH,KEY
     * @param topOnInput
     *            是否放在最高层显示浮窗,会盖在输入法上面,慎用!
     */
    public void updateWindowLayoutParams(int x, int y, int w, int h, int inputType, boolean topOnInput);

    // public void swipeviewOnDismiss(SwipeBackView sbv);

    public void moveToTop(BaseView view);

    public void moveToBottom(BaseView view);

    /**
     * 切换横竖屏
     * 
     * @param isPortrait
     *            true 竖屏 ； false 横屏
     */
    public void changeScreenOrientation(boolean isPortrait);

    /** 获取baseview容器 */
    ActivityContainer getActivityContainer();

    /** 获取悬浮遮罩层 */
    FrameLayout getMaskContainer();
}
