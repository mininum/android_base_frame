package org.rdengine.log;


import org.rdengine.view.manager.BaseListView;
import org.rdengine.view.manager.BaseRecyclerView;

public class UMPageFilter
{
    final static String[] pagearray = new String[]
    {
            // 基类
            "BaseView",
            // 通用listview
            BaseListView.TAG, BaseRecyclerView.TAG,


    };

    /**
     * @param pagename
     * @return true 有效页面埋点 false 无效页面
     */
    public static boolean allow(String pagename)
    {
        if (pagename == null || pagename.length() == 0)
            return false;
        for (String p : pagearray)
        {
            if (p.equals(pagename))
                return false;
        }
        return true;
    }
}
