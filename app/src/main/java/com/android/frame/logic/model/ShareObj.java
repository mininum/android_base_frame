package com.android.frame.logic.model;

import org.rdengine.util.DMImageTool;

/**
 * Created by CCCMAX on 17/8/1.
 */

public class ShareObj
{
    public static final int TYPE_APP = 0;
    public static final int TYPE_FEED = 1;
    public static final int TYPE_IMAGE = 2;
    public static final int TYPE_TOPIC = 3;
    public static final int TYPE_TOPIC_TIMELINE = 4;
    public static final int TYPE_USER = 5;
    public static final int TYPE_WEB = 6;
    public static final int TYPE_COMMENT = 7;

    public int type = TYPE_APP;
    public Object data;
    public String imageurl;
    public String title, des, weburl;

    public ShareObj(int type, Object data, String imageurl)
    {
        this.type = type;
        this.data = data;
        this.imageurl = imageurl;
    }

    public ShareObj(int type, String title, String des, String weburl, String imageurl)
    {
        this.type = type;
        this.imageurl = imageurl;
        this.title = title;
        this.des = des;
        this.weburl = weburl;
    }

    public ShareObj(int type, String title, String des, String weburl, int resImgId)
    {
        this.type = type;
        this.title = title;
        this.weburl = weburl;
        this.des = des;
        this.imageurl = DMImageTool.resImgToDiskImgTemp(resImgId);
    }
}
