package org.rdengine.log;

import com.android.frame.view.MainView;

/**
 * 友盟埋点 Created by CCCMAX on 2019/4/8.
 */

public class UMLogSender
{

    /** 主tab点击埋点 */
    public static void sendMainTabHostLog(int position)
    {
        try
        {
            String pagename = "unknow";
            MainView.HostTitle ht = MainView.HostTitle.values()[position];
            switch (ht)
            {
            case HT_HOME :
                pagename = "home";
                break;
            case HT_GAME :
                pagename = "game";
                break;
            case HT_WELFARE :
                pagename = "welfare";
                break;
            case HT_MINE :
                pagename = "mine";
                break;
            }
            DLOG.event(UMConstant.motion_app_main_tab_click, pagename);
        } catch (Exception ex)
        {
        }
    }
}
