package com.facebook.fresco;

import android.graphics.Point;
import android.graphics.PointF;
import android.text.TextUtils;

import com.facebook.drawee.drawable.ScalingUtils;
import com.android.frame.logic.QiniuMgr.Account_Motion;

/**
 * 远程图片 http://, https:// HttpURLConnection <br>
 * 本地文件 file:// FileInputStream<br>
 * Content provider content:// ContentResolver<br>
 * asset目录下的资源 asset:// AssetManager<br>
 * res目录下的资源 res:// ,"res://包名(实际可以是任何字符串甚至留空)/" + R.drawable.ic_launcher<br>
 * 
 * @author CCCMAX
 */
public class FrescoParam
{

    private String URI = "";
    public int DefaultImageID = 0;
    public int FailureImageID = 0;

    public ScalingUtils.ScaleType scaletype = ScalingUtils.ScaleType.CENTER_CROP;
    public ScalingUtils.ScaleType scaletype_def = ScalingUtils.ScaleType.FIT_XY;

    public PointF scaleFocusPoint = null;

    /** 圆形，如果设置圆，圆角半径失效 */
    private boolean isRound = false;
    /** 圆角半径 */
    private float Radius_topLeft, Radius_topRight, Radius_bottomRight, Radius_bottomLeft;
    private boolean noRoundingParams = true;

    /** 描边颜色 */
    private int Borde_color = 0xFFFFFFFF;
    /** 描边宽度 */
    private int Borde_width = -1;

    /** 图片加载失败时 是否可以点击重新加载 */
    private boolean ClickToRetryEnabled = false;

    /** 自动播放动画 */
    private boolean autoPlayAnimations = true;

    public FrescoParam()
    {}

    public FrescoParam(String uri)
    {
        setURI(uri);
    }

    public FrescoParam(String uri, QiniuParam qp)
    {
        setURI(uri);
        setQiniuParam(qp);
    }

    public FrescoParam setURI(String uri)
    {
        this.URI = uri;
        if (uri != null)
            if (uri.startsWith("/storage/") || uri.startsWith("/system") || uri.startsWith("/mnt"))
            {
                this.URI = "file://" + uri;
            }
        return FrescoParam.this;
    }

    public FrescoParam setDefaultImage(int resid)
    {
        this.DefaultImageID = resid;
        return FrescoParam.this;
    }

    public FrescoParam setFailureImage(int resid)
    {
        this.FailureImageID = resid;
        return FrescoParam.this;
    }

    /**
     * view缩放模式
     * 
     * @param scale_type
     */
    public FrescoParam setScaleType(ScalingUtils.ScaleType scale_type)
    {
        scaletype = scale_type;
        return FrescoParam.this;
    }

    /**
     * view缩放模式
     * 
     * @param scale_type
     */
    public FrescoParam setScaleTypeDef(ScalingUtils.ScaleType scale_type)
    {
        scaletype_def = scale_type;
        return FrescoParam.this;
    }

    /**
     * 设置是否自动播放动图
     * 
     * @param play
     * @return
     */
    public FrescoParam setAutoPlayAnimations(boolean play)
    {
        this.autoPlayAnimations = play;
        return FrescoParam.this;
    }

    /**
     * 是否自动播放动图
     */
    public boolean getAutoPlayAnimations()
    {
        return this.autoPlayAnimations;
    }

    /**
     * 设置图片缩放模式时的中心点
     * 
     * @param x
     *            范围0~1 0屏幕左边 1屏幕右边
     * @param y
     *            范围0~1 0屏幕上边 1屏幕下边
     * @return
     */
    public FrescoParam setScaleFocusPoint(float x, float y)
    {
        scaleFocusPoint = new PointF(x, y);
        return FrescoParam.this;
    }

    /**
     * 描边
     * 
     * @param color
     * @param width
     * @return
     */
    public FrescoParam setBorde(int color, int width)
    {
        this.Borde_color = color;
        this.Borde_width = width;
        return FrescoParam.this;
    }

    public int getBordeColor()
    {
        return Borde_color;
    }

    public int getBordeWidth()
    {
        return Borde_width;
    }

    /**
     * 设置圆形
     * 
     * @param isround
     */
    public FrescoParam setRoundAsCircle(boolean isround)
    {
        isRound = isround;
        noRoundingParams = false;
        return FrescoParam.this;
    }

    /**
     * 圆角半径
     * 
     * @param radius
     */
    public FrescoParam setRoundedCornerRadius(float radius)
    {
        setRoundedCornerRadius(radius, radius, radius, radius);
        noRoundingParams = false;
        return FrescoParam.this;
    }

    /**
     * 加载图片失败时 点击重新加载
     * 
     * @param retry
     * @return
     */
    public FrescoParam setClickToRetryEnabled(boolean retry)
    {
        this.ClickToRetryEnabled = retry;
        return FrescoParam.this;
    }

    public boolean isNoRoundingParams()
    {
        return noRoundingParams;
    }

    public boolean getRoundAsCircle()
    {
        return isRound;
    }

    public float getRadius_TL()
    {
        return Radius_topLeft;
    }

    public float getRadius_TR()
    {
        return Radius_topRight;
    }

    public float getRadius_BL()
    {
        return Radius_bottomLeft;
    }

    public float getRadius_BR()
    {
        return Radius_bottomRight;
    }

    public String getURI()
    {
        // return URI;
        return makeGbImageUrl(URI);
    }

    public boolean getClickToRetryEnabled()
    {
        return ClickToRetryEnabled;
    }

    /**
     * 圆角半径
     * 
     * @param topLeft
     *            左上
     * @param topRight
     *            右上
     * @param bottomLeft
     *            左下
     * @param bottomRight
     *            右下
     */
    public void setRoundedCornerRadius(float topLeft, float topRight, float bottomLeft, float bottomRight)
    {
        Radius_topLeft = topLeft;
        Radius_topRight = topRight;
        Radius_bottomRight = bottomRight;
        Radius_bottomLeft = bottomLeft;

    }

    private Point expectantImgSize;

    /** 手动设置需要的图片尺寸 否则会用七牛图片参数来安排一个适合的尺寸 */
    public void setExpectantImgSize(int x, int y)
    {
        if (x <= 0 || y <= 0)
        {
            expectantImgSize = null;
        } else
        {
            expectantImgSize = new Point(x, y);
        }
    }

    /**
     * 获取期待的图片尺寸 根据七牛尺寸参数
     *
     * @return
     */
    public Point getExpectantImgSize()
    {
        if (expectantImgSize != null)
            return expectantImgSize;

        if (noQiniuParam)
            return null;
        Point ret = new Point(-1, -1);
        QiniuParam qp = mQiniuParam;
        if (qp == null)
        {
            switch (qp)
            {
            case C_S :
            case B_C_S :
            case Z_MAX_S :
            case B_Z_MAX_S :
            {
                ret.x = 80;
                ret.y = 80;
            }
                break;
            case C_M :
            case B_C_M :
            case Z_MAX_M :
            case B_Z_MAX_M :
            {
                ret.x = 240;
                ret.y = 240;
            }
                break;
            case C_L :
            case B_C_L :
            case Z_MAX_L :
            case B_Z_MAX_L :
            {
                ret.x = 720;
                ret.y = 720;
            }
                break;
            }
        }
        return ret;
    }

    // --------------------------------------七牛 关八 图片参数

    /**
     * —————————————————————<br>
     * 限制最高分辨率，适合在同一页面显示多图，优先控制内存，<br>
     * 但是某些宽高比例很夸张的图就会看不清楚(如长文字图),<br>
     * 如某图1280*4000，设置Z.MAX.L 得到分辨率230*720<br>
     * <ul>
     * Z.MAX.S
     * <ul>
     * 最大边长不超过80，图片不超过80*80的范围 imageView2/0/w/80/q/75
     * </ul>
     * Z.MAX.M
     * <ul>
     * 最大边长不超过240，图片不超过240*240的范围 imageView2/0/w/240/q/75
     * </ul>
     * Z.MAX.L
     * <ul>
     * 最大边长不超过720，图片不超过720*720的范围 imageView2/0/w/720/q/75
     * </ul>
     * </ul>
     * —————————————————————<br>
     * 保证最低分辨率 ，让图片尽量不失真，适合查看大图用<br>
     * 如某图1280*4000，设置Z.MIN.L 得到分辨率720*2250<br>
     * <ul>
     * Z.MIN.S
     * <ul>
     * 最小边不小于80，图片最少保证80*80的范围 imageView2/4/w/80/q/75
     * </ul>
     * Z.MIN.M
     * <ul>
     * 最小边不小于240，图片最少保证240*240的范围 imageView2/4/w/240/q/75
     * </ul>
     * Z.MIN.L
     * <ul>
     * 最小边不小于720，图片最少保证720*720的范围 imageView2/4/w/720/q/75
     * </ul>
     * </ul>
     * ————————————————————— 裁切图片 正方形
     * <ul>
     * C.S
     * <ul>
     * 正方形 单边80 imageView2/1/w/80/h/80/q/75
     * </ul>
     * C.M
     * <ul>
     * 正方形 单边240 imageView2/1/w/240/h/240/q/75
     * </ul>
     * C.L
     * <ul>
     * 正方形 单边720 imageView2/1/w/720/h/720/q/75
     * </ul>
     * </ul>
     * —————————————————————<br>
     */
    public void setQiniuParam(QiniuParam qp)
    {
        mQiniuParam = qp;
    }

    private boolean noQiniuParam = false;

    public void setNoQiniuParam(boolean noQiniuParam)
    {
        this.noQiniuParam = noQiniuParam;
    }

    private QiniuParam mQiniuParam = QiniuParam.Z_MAX_L;

    /**
     * 判断是否是关八图片
     * 
     * @param url
     * @return
     */
    public static boolean isGbImage(String url)
    {
        if (!TextUtils.isEmpty(url))
        {
            for (String host : Account_Motion.allhost)
            {
                if (url.contains(host))
                {
                    return true;
                }
            }
        }
        return false;
    }

    public String makeGbImageUrl(String url)
    {
        if (noQiniuParam)
            return url;

        try
        {
            // 判断是否是关八七牛图片
            if (isGbImage(url))
            {
                String _url = url;

                // 判断图片url是否包含七牛分隔符"-"
                if (url.contains("-"))
                {
                    try
                    {
                        // 去除url可能残留的多余七牛参数
                        // String regular = "-([a-zA-Z0-9]{1,9}[\\._]+){1,4}([a-zA-Z0-9]{1,9})";

                        // 匹配-XXX.XXX.XXX.XXX样式，"."分隔,每段最多三个字母最多四段
                        String regular_new = "-([a-zA-Z]{1,3}[\\.]+){1,3}([a-zA-Z]{1,3})";
                        _url = _url.replaceAll(regular_new, "");

                        // 匹配-GB_xxx_xxx_xxx样式
                        if (_url.contains("-"))
                        {
                            String regular_old = "-GB(_[a-zA-Z0-9]{1,9}){2}";
                            _url = _url.replaceAll(regular_old, "");
                        }
                    } catch (Exception ex)
                    {
                        // 只取"-"前面的
                        String[] sss = url.split("-");
                        if (sss != null && sss.length > 0)
                        {
                            _url = sss[0];
                        }
                    }
                }

                // if (url.trim().endsWith("-GB_watermark_T"))
                // {
                // _url = url.substring(0, url.length() - "-GB_watermark_T".length());
                // } else if (url.trim().endsWith("-GB_watermark_A"))
                // {
                // _url = url.substring(0, url.length() - "-GB_watermark_A".length());
                // }

                QiniuParam qp = mQiniuParam;
                if (qp == null)
                    qp = QiniuParam.Z_MAX_L;
                String param = qp.name().replace('_', '.');
                _url = _url + "-" + param;
                return _url;
            }
        } catch (Exception e)
        {
        }
        return url;
    }

    public static enum QiniuParam
    {
        /** 正方形 单边80 imageView2/1/w/80/h/80/q/75 */
        C_S,
        /** 正方形 单边240 imageView2/1/w/240/h/240/q/75 */
        C_M,
        /** 正方形 单边720 imageView2/1/w/720/h/720/q/75 */
        C_L,

        // --------------------
        /** 最大边长不超过80，图片不超过80*80的范围 imageView2/0/w/80/q/75 */
        Z_MAX_S,
        /** 最大边长不超过240，图片不超过240*240的范围 imageView2/0/w/240/q/75 */
        Z_MAX_M,
        /** 最小边不小于720，图片最少保证720*720的范围 imageView2/4/w/720/q/75 */
        Z_MAX_L,

        // --------------------

        /** 最小边不小于80，图片最少保证80*80的范围 imageView2/4/w/80/q/75 */
        Z_MIN_S,
        /** 最小边不小于240，图片最少保证240*240的范围 imageView2/4/w/240/q/75 */
        Z_MIN_M,
        /** 最小边不小于720，图片最少保证720*720的范围 imageView2/4/w/720/q/75 */
        Z_MIN_L,

        // 下面是带高斯模糊的参数
        B_C_S, B_C_M, B_C_L, B_Z_MAX_S, B_Z_MAX_M, B_Z_MAX_L, B_Z_MIN_S, B_Z_MIN_M, B_Z_MIN_L,
    };

}
