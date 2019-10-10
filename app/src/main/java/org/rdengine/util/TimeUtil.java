package org.rdengine.util;

/**  
 * 有关日期工具类(extends TimeUtil)  
 *   
 * TimeUtil主要功能有：  
 * 1.各种日期类型（字符，util.Date，sql.Date，Calendar等）转换  
 * 2.获取指定日期的年份，月份，日份，小时，分，秒，毫秒  
 * 3.获取当前/系统日期(指定日期格式)  
 * 4.获取字符日期一个月的天数  
 * 5.获取指定月份的第一天,最后一天  
 *   
 * DateUtil主要功能有：  
 * 1.日期比较  
 * 2.获取2个字符日期的天数差，周数差，月数差，年数差  
 * 3.日期添加  
 * 4.判断给定日期是不是润年  
 */

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public abstract class TimeUtil
{

    public static final SimpleDateFormat dateFormat_1 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    public static final SimpleDateFormat dateFormat_2 = new SimpleDateFormat("yy-M-d HH:mm");
    public static final SimpleDateFormat dateFormat_3 = new SimpleDateFormat("MM-dd HH:mm");
    public static final SimpleDateFormat dateFormat_4 = new SimpleDateFormat("yyyy.M.d  HH:mm");
    public static final SimpleDateFormat dateFormat_5 = new SimpleDateFormat("yyyy-MM-dd");
    public static final SimpleDateFormat dateFormat_6 = new SimpleDateFormat("HH:mm");
    public static final SimpleDateFormat dateFormat_7 = new SimpleDateFormat("HH:mm:ss");
    public static final SimpleDateFormat dateFormat_8 = new SimpleDateFormat("yyyy/MM/dd HH:mm");
    public static final SimpleDateFormat dateFormat_9 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    // ---当前日期的年，月，日，时，分，秒
    public static Calendar now = Calendar.getInstance();

    // int year = now.get(Calendar.YEAR);
    // int date = now.get(Calendar.DAY_OF_MONTH);
    // int month = now.get(Calendar.RANK_MONTH) + 1;
    // int hour = now.get(Calendar.HOUR);
    // int min = now.get(Calendar.MINUTE);
    // int sec = now.get(Calendar.SECOND);

    // -------------------------------日期类型转换---------------------------------------------------------------------------
    /**
     * 字符型日期转化util.Date型日期
     * 
     * @param p_strDate
     *            字符型日期
     * @param p_format
     *            格式:"yyyy-MM-dd" / "yyyy-MM-dd HH:mm:ss"
     * @Return java.util.Date util.Date型日期
     * @Throws: ParseException
     * @Date: 2006-10-31
     */
    public static Date toUtilDateFromStrDateByFormat(String p_strDate, String p_format) throws ParseException
    {
        Date l_date = null;
        java.text.DateFormat df = new SimpleDateFormat(p_format);
        if (p_strDate != null && p_strDate.length() > 0 && p_format != null && p_format.length() > 0)
        {
            l_date = df.parse(p_strDate);
        }
        return l_date;
    }

    /**
     * 字符型日期转化成sql.Date型日期
     *
     * @param p_strDate
     *            字符型日期
     * @return java.sql.Date sql.Date型日期
     * @throws ParseException
     * @Date: 2006-10-31
     */
    public static java.sql.Date toSqlDateFromStrDate(String p_strDate) throws ParseException
    {
        java.sql.Date returnDate = null;
        java.text.DateFormat sdf = new SimpleDateFormat();
        if (p_strDate != null && p_strDate.length() > 0)
        {
            returnDate = new java.sql.Date(sdf.parse(p_strDate).getTime());
        }
        return returnDate;
    }

    /**
     * util.Date型日期转化指定格式的字符串型日期 toStrDateFromUtilDateByFormat(new Date(),"yyyy-MM-dd HH:mm:ss");
     *
     * @param p_utilDate
     *            Date
     * @param p_format
     *            String 格式1:"yyyy-MM-dd" 格式2:"yyyy-MM-dd HH:mm:ss EE" 格式3:"yyyy年MM月dd日 hh:mm:ss EE" 说明: 年-月-日 时:分:秒 星期 注意MM/mm大小写
     * @return String
     * @Date: 2006-10-31
     */
    public static String toStrDateFromUtilDateByFormat(Date p_utilDate, String p_format) throws ParseException
    {
        String l_result = "";
        try
        {
            if (p_utilDate != null)
            {
                SimpleDateFormat sdf = new SimpleDateFormat(p_format);
                l_result = sdf.format(p_utilDate);
            }
        } catch (Exception ex)
        {
        }
        return l_result;
    }

    /**
     * util.Date型日期转化转化成Calendar日期
     *
     * @param p_utilDate
     *            Date
     * @return Calendar
     * @Date: 2006-10-31
     */
    public static Calendar toCalendarFromUtilDate(Date p_utilDate)
    {
        Calendar c = Calendar.getInstance();
        c.setTime(p_utilDate);
        return c;
    }

    /**
     * util.Date型日期转化sql.Date(年月日)型日期
     *
     * @Param: p_utilDate util.Date型日期
     * @Return: java.sql.Date sql.Date型日期
     * @Date: 2006-10-31
     */
    public static java.sql.Date toSqlDateFromUtilDate(Date p_utilDate)
    {
        java.sql.Date returnDate = null;
        if (p_utilDate != null)
        {
            returnDate = new java.sql.Date(p_utilDate.getTime());
        }
        return returnDate;
    }

    /**
     * util.Date型日期转化sql.Time(时分秒)型日期
     *
     * @Param: p_utilDate util.Date型日期
     * @Return: java.sql.Time sql.Time型日期
     * @Date: 2006-10-31
     */
    public static java.sql.Time toSqlTimeFromUtilDate(Date p_utilDate)
    {
        java.sql.Time returnDate = null;
        if (p_utilDate != null)
        {
            returnDate = new java.sql.Time(p_utilDate.getTime());
        }
        return returnDate;
    }

    /**
     * util.Date型日期转化sql.Date(时分秒)型日期
     *
     * @Param: p_utilDate util.Date型日期
     * @Return: java.sql.Timestamp sql.Timestamp型日期
     * @Date: 2006-10-31
     */
    public static java.sql.Timestamp toSqlTimestampFromUtilDate(Date p_utilDate)
    {
        java.sql.Timestamp returnDate = null;
        if (p_utilDate != null)
        {
            returnDate = new java.sql.Timestamp(p_utilDate.getTime());
        }
        return returnDate;
    }

    /**
     * sql.Date型日期转化util.Date型日期
     *
     * @Param: sqlDate sql.Date型日期
     * @Return: java.util.Date util.Date型日期
     * @Date: 2006-10-31
     */
    public static Date toUtilDateFromSqlDate(java.sql.Date p_sqlDate)
    {
        Date returnDate = null;
        if (p_sqlDate != null)
        {
            returnDate = new Date(p_sqlDate.getTime());
        }
        return returnDate;
    }

    // -----------------获取指定日期的年份，月份，日份，小时，分，秒，毫秒----------------------------
    /**
     * 获取指定日期的年份
     *
     * @param p_date
     *            util.Date日期
     * @return int 年份
     * @author zhuqx
     * @Date: 2006-10-31
     */
    public static int getYearOfDate(Date p_date)
    {
        Calendar c = Calendar.getInstance();
        c.setTime(p_date);
        return c.get(Calendar.YEAR);
    }

    /**
     * 获取指定日期的月份
     *
     * @param p_date
     *            util.Date日期
     * @return int 月份
     * @author zhuqx
     * @Date: 2006-10-31
     */
    public static int getMonthOfDate(Date p_date)
    {
        Calendar c = Calendar.getInstance();
        c.setTime(p_date);
        return c.get(Calendar.MONTH) + 1;
    }

    /**
     * 获取指定日期的日份
     *
     * @param p_date
     *            util.Date日期
     * @return int 日份
     * @author zhuqx
     * @Date: 2006-10-31
     */
    public static int getDayOfDate(Date p_date)
    {
        Calendar c = Calendar.getInstance();
        c.setTime(p_date);
        return c.get(Calendar.DAY_OF_MONTH);
    }

    public static int getDayOfYear(Date p_date)
    {
        Calendar c = Calendar.getInstance();
        c.setTime(p_date);
        return c.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * 返回星期几
     *
     * @param p_date
     * @return
     */
    public static int getWeekOfDate(Date p_date)
    {
        Calendar c = Calendar.getInstance();
        c.setTime(p_date);
        String s = "一";
        int ret = 1;
        switch (c.get(Calendar.DAY_OF_WEEK))
        {
        case 1 :
            s = "日";
            ret = 7;
            break;
        case 2 :
            s = "一";
            ret = 1;
            break;
        case 3 :
            s = "二";
            ret = 2;
            break;
        case 4 :
            s = "三";
            ret = 3;
            break;
        case 5 :
            s = "四";
            ret = 4;
            break;
        case 6 :
            s = "五";
            ret = 5;
            break;
        case 7 :
            s = "六";
            ret = 6;
            break;
        }
        // return "星期" + s;
        return ret;
    }

    /**
     * 返回星期几
     *
     * @param p_date
     * @return
     */
    public static String getWeekStrOfDate(Date p_date)
    {
        Calendar c = Calendar.getInstance();
        c.setTime(p_date);
        String s = "一";
        switch (c.get(Calendar.DAY_OF_WEEK))
        {
        case 1 :
            s = "日";
            break;
        case 2 :
            s = "一";
            break;
        case 3 :
            s = "二";
            break;
        case 4 :
            s = "三";
            break;
        case 5 :
            s = "四";
            break;
        case 6 :
            s = "五";
            break;
        case 7 :
            s = "六";
            break;
        }
        return "星期" + s;
    }

    /**
     * 获取指定日期的小时
     *
     * @param p_date
     *            util.Date日期
     * @return int 日份
     * @author zhuqx
     * @Date: 2006-10-31
     */
    public static int getHourOfDate(Date p_date)
    {
        Calendar c = Calendar.getInstance();
        c.setTime(p_date);
        return c.get(Calendar.HOUR_OF_DAY);
    }

    /**
     * 获取指定日期的分钟
     *
     * @param p_date
     *            util.Date日期
     * @return int 分钟
     * @author zhuqx
     * @Date: 2006-10-31
     */
    public static int getMinuteOfDate(Date p_date)
    {
        Calendar c = Calendar.getInstance();
        c.setTime(p_date);
        return c.get(Calendar.MINUTE);
    }

    /**
     * 获取指定日期的秒钟
     *
     * @param p_date
     *            util.Date日期
     * @return int 秒钟
     * @author zhuqx
     * @Date: 2006-10-31
     */
    public static int getSecondOfDate(Date p_date)
    {
        Calendar c = Calendar.getInstance();
        c.setTime(p_date);
        return c.get(Calendar.SECOND);
    }

    /**
     * 获取指定日期的毫秒
     *
     * @param p_date
     *            util.Date日期
     * @return long 毫秒
     * @author zhuqx
     * @Date: 2006-10-31
     */
    public static long getMillisOfDate(Date p_date)
    {
        Calendar c = Calendar.getInstance();
        c.setTime(p_date);
        return c.getTimeInMillis();
    }

    /** 获取指定日期0点0分0秒的时间戳 */
    public static long getMillisOfDateZero(Date p_date)
    {
        Calendar c = Calendar.getInstance();
        c.setTime(p_date);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    // -----------------获取当前/系统日期(指定日期格式)-----------------------------------------------------------------------------------
    /**
     * 获取指定日期格式当前日期的字符型日期
     *
     * @param p_format
     *            日期格式 格式1:"yyyy-MM-dd" 格式2:"yyyy-MM-dd HH:mm:ss EE" 格式3:"yyyy年MM月dd日 hh:mm:ss EE" 说明: 年-月-日 时:分:秒 星期 注意MM/mm大小写
     * @return String 当前时间字符串
     * @author zhuqx
     * @Date: 2006-10-31
     */
    public static String getNowOfDateByFormat(String p_format)
    {
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat(p_format);
        String dateStr = sdf.format(d);
        return dateStr;
    }

    /**
     * 获取指定日期格式系统日期的字符型日期
     *
     * @param p_format
     *            日期格式 格式1:"yyyy-MM-dd" 格式2:"yyyy-MM-dd HH:mm:ss EE" 格式3:"yyyy年MM月dd日 hh:mm:ss EE" 说明: 年-月-日 时:分:秒 星期 注意MM/mm大小写
     * @return String 系统时间字符串
     * @author zhuqx
     * @Date: 2006-10-31
     */
    public static String getSystemOfDateByFormat(String p_format)
    {
        long time = System.currentTimeMillis();
        Date d = new Date(time);
        SimpleDateFormat sdf = new SimpleDateFormat(p_format);
        String dateStr = sdf.format(d);
        return dateStr;
    }

    /**
     * 获取字符日期一个月的天数
     *
     * @param p_date
     * @return 天数
     * @author zhuqx
     */
    public static long getDayOfMonth(Date p_date) throws ParseException
    {
        int year = getYearOfDate(p_date);
        int month = getMonthOfDate(p_date) - 1;
        int day = getDayOfDate(p_date);
        int hour = getHourOfDate(p_date);
        int minute = getMinuteOfDate(p_date);
        int second = getSecondOfDate(p_date);
        Calendar l_calendar = new GregorianCalendar(year, month, day, hour, minute, second);
        return l_calendar.getActualMaximum(l_calendar.DAY_OF_MONTH);
    }

    // -----------------获取指定月份的第一天,最后一天
    // ---------------------------------------------------------------------------
    /**
     * 获取指定月份的第一天
     *
     * @param p_strDate
     *            指定月份
     * @param p_format
     *            日期格式
     * @return String 时间字符串
     * @author zhuqx
     * @Date: 2006-10-31
     */
    public static String getDateOfMonthBegin(String p_strDate, String p_format) throws ParseException
    {
        Date date = toUtilDateFromStrDateByFormat(p_strDate, p_format);
        return toStrDateFromUtilDateByFormat(date, "yyyy-MM") + "-01";
    }

    /**
     * 获取指定月份的最后一天
     *
     * @param p_strDate
     *            指定月份
     * @param p_format
     *            日期格式
     * @return String 时间字符串
     * @author zhuqx
     * @Date: 2006-10-31
     */
    public static String getDateOfMonthEnd(String p_strDate, String p_format) throws ParseException
    {
        Date date = toUtilDateFromStrDateByFormat(getDateOfMonthBegin(p_strDate, p_format), p_format);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        return toStrDateFromUtilDateByFormat(calendar.getTime(), p_format);
    }

    /**
     * 日期是否有效
     * 
     * @param year
     * @param month
     * @param day
     * @return
     */
    public static boolean birIsGood(int year, int month, int day)
    {
        if (year < 1000 || month == 0 || month > 12 || day == 0 || day > 31)
        {
            return false;
        }
        return true;
    }

    /**
     * 格式化为，分：秒
     * 
     * @param time
     *            1 毫秒 2秒
     * @return
     */
    public static String formatTime(int time)
    {
        if (time != 0)
        {
            StringBuilder sb = new StringBuilder();
            sb.append(time / (60 * 1000)).append(":");
            int millinSeconds = time % (60 * 1000);
            int seconds = (millinSeconds / 1000);
            if (seconds < 10)
            {
                sb.append("0" + seconds);
            } else
            {
                sb.append(seconds);
            }
            return sb.toString();
        } else
        {
            // 未知
            return "0:00";
        }

    }

    /** 毫秒时间转格式化 时分秒 2:30'50" 单位毫秒 */
    public static String formatTimeHMS(long time)
    {
        if (time > 0)
        {

            long hh = 3600000;
            long mm = 60000;
            long ss = 1000;
            long h = time / hh;// 小时
            long m = time % hh / mm;// 分钟
            long s = time % mm / ss;// 秒
            StringBuilder sb = new StringBuilder();
            if (h > 0)
            {
                sb.append(h).append(":");
            }
            if (m < 10)
                sb.append("0");
            sb.append(m).append("'");
            if (s < 10)
                sb.append("0");
            sb.append(s).append("\"");
            return sb.toString();
        } else
        {
            return "00'00\"";
        }
    }

    /** 毫秒时间转格式化 时分秒 2:30:50: 单位毫秒 */
    public static String formatTimeHMS2(long time)
    {
        if (time > 0)
        {

            long hh = 3600000;
            long mm = 60000;
            long ss = 1000;
            long h = time / hh;// 小时
            long m = time % hh / mm;// 分钟
            long s = time % mm / ss;// 秒
            StringBuilder sb = new StringBuilder();
            sb.append(h).append(":");
            if (m < 10)
                sb.append("0");
            sb.append(m).append(":");
            if (s < 10)
                sb.append("0");
            sb.append(s);
            return sb.toString();
        } else
        {
            return "00:00:00";
        }
    }

    /**
     * 格式化为,分秒钟
     * 
     * @param time
     *            1 毫秒 2秒
     * @return
     */
    public static String formatTime(long time)
    {
        if (time != 0)
        {
            StringBuilder sb = new StringBuilder();
            sb.append(time / (60 * 1000)).append(":");
            int millinSeconds = (int) (time % (60 * 1000));
            int seconds = (millinSeconds / 1000);
            if (seconds < 10)
            {
                sb.append("0" + seconds);
            } else
            {
                sb.append(seconds);
            }
            return sb.toString();
        } else
        {
            // 未知
            return "0:00";
        }

    }

    public static boolean isSameDay(long time1, long time2)
    {
        Calendar now = new GregorianCalendar();
        now.setTime(new Date(time1));
        Calendar other = new GregorianCalendar();
        other.setTime(new Date(time2));

        if (now.get(Calendar.YEAR) == other.get(Calendar.YEAR)
                && now.get(Calendar.DAY_OF_YEAR) == other.get(Calendar.DAY_OF_YEAR))
        {
            return true;
        }
        return false;
    }

    /**
     * 判断是否和今天是同一周
     * 
     * @param ti
     * @return
     */
    private static boolean isSameWeek(long ti)
    {
        Calendar now = new GregorianCalendar();
        Calendar other = new GregorianCalendar();
        other.setTime(new Date(ti));
        if (now.get(Calendar.YEAR) == other.get(Calendar.YEAR)
                && now.get(Calendar.WEEK_OF_YEAR) == other.get(Calendar.WEEK_OF_YEAR)
                && other.get(Calendar.DAY_OF_WEEK) != 1)
        {
            return true;
        }
        return false;
    }

    /**
     * 判断是否和今天是同一年
     * 
     * @param ti
     * @return
     */
    public static boolean isSameYear(long ti)
    {
        Calendar now = new GregorianCalendar();
        Calendar other = new GregorianCalendar();
        other.setTime(new Date(ti));
        if (now.get(Calendar.YEAR) == other.get(Calendar.YEAR))
        {
            return true;
        }
        return false;
    }

    /**
     * 格式化活动起始时间
     * 
     * @param timestamp
     * @return
     */
    public static String formatActivityTime(long timestamp)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date(timestamp);
        return sdf.format(date);
    }

    /**
     * 格式化签到时间
     * 
     * @param timestamp
     * @return
     */
    public static String formatCheckTime(long timestamp)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Date date = new Date(timestamp);
        return sdf.format(date);
    }

    public static String getNowHHmmss()
    {
        String ret = "";
        try
        {
            ret = TimeUtil.toStrDateFromUtilDateByFormat(new Date(), "HH:mm:ss");
        } catch (ParseException e)
        {
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * 格式化文章发布时间
     * 
     * @return
     */
    public static String formatPublishTime(long timestamp)
    {
        // 1分钟内显示刚刚，1小时内显示N分钟前，1天内显示N小时内，
        // 四天内显示格式为1天/2天/3天前，超过4天，显示为 16-5-26 15:30
        String ret = "";
        try
        {
            long currenttime = System.currentTimeMillis();
            long t = currenttime - timestamp;
            if (t > 0)
            {
                if (t < 60000)// 1分钟
                {
                    ret = "刚刚";
                } else if (t < 3600000)// 1小时内
                {
                    long m = t / 60000;
                    ret = m + "分钟前";
                } else if (t < 86400000)// 24小时内
                {
                    long h = t / 3600000;
                    ret = h + "小时前";
                } else if (t < 172800000)// 48小时内
                {
                    // "HH:mm"
                    ret = "昨天" + dateFormat_6.format(new Date(timestamp));
                } else if (t < 259200000)// 72小时
                {
                    // "HH:mm"
                    ret = "前天" + dateFormat_6.format(new Date(timestamp));
                }
            }
            if (StringUtil.isEmpty(ret))
            {
                if (isSameYear(timestamp))
                {
                    // 当前年 不显示年 "MM-dd HH:mm"
                    ret = dateFormat_3.format(new Date(timestamp));
                } else
                {
                    // 非当前年 显示年 "yyyy-MM-dd HH:mm"
                    ret = dateFormat_1.format(new Date(timestamp));
                }
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * 格式化文章发布时间
     *
     * @return
     */
    public static String formatPublishTimeNoHM(long timestamp)
    {
        // 3分钟内显示刚刚，1小时内显示N分钟前，1天内显示N小时内，
        // 四天内显示格式为1天/2天/3天前，超过4天，显示为 16-5-26 15：30
        String ret = "";
        try
        {
            long currenttime = System.currentTimeMillis();
            long t = currenttime - timestamp;
            if (t > 0)
            {
                if (t < 180000)// 3分钟
                {
                    ret = "刚刚";
                } else if (t < 3600000)// 一小时
                {
                    long m = t / 60000;
                    ret = m + "分钟前";
                } else if (t < 86400000)// 一天
                {
                    long h = t / 3600000;
                    ret = h + "小时前";
                } else if (t < 345600000)// 四天
                {
                    long d = t / 86400000;
                    ret = d + "天前";
                }
            }
            if (StringUtil.isEmpty(ret))
            {
                if (isSameYear(timestamp))
                {
                    ret = new SimpleDateFormat("MM-dd").format(new Date(timestamp));
                } else
                {
                    ret = new SimpleDateFormat("yyyy-MM-dd").format(new Date(timestamp));
                }
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return ret;
    }

    public static String formatColumnLastTime(long timestamp)
    {
        // 3分钟内的显示刚刚，
        // 1小时内的 N分钟前，
        // 1天内 N小时前，（今天）
        // 一天前的显示 昨天，
        // 两天前的显示 前天，
        // 1周以内的 前天开始用星期X，
        // 一周以上的用日期格式4.23，
        // 跨年的用年份+日期 16.4.23
        String ret = "";

        try
        {
            long currenttime = System.currentTimeMillis();
            // 当前时间差
            long t = currenttime - timestamp;
            if (t > 0)
            {
                if (t < 180000)// 3分钟
                {
                    ret = "刚刚";
                } else if (t < 3600000)// 一小时
                {
                    long m = t / 60000;
                    ret = m + "分钟前";
                } else
                {
                    // 今天零点
                    Calendar calendar = new GregorianCalendar();
                    calendar.setTime(new Date(currenttime));
                    int y = calendar.get(Calendar.YEAR);
                    int m = calendar.get(Calendar.MONTH);
                    int d = calendar.get(Calendar.DAY_OF_MONTH);
                    Date date_today_zero = new Date(y - 1900, m, d);

                    long today_zero = date_today_zero.getTime();
                    long yesterday_zero = today_zero - 86400000;
                    long beforeyesterday_zero = yesterday_zero - 86400000;

                    if (today_zero < timestamp)// 当天
                    {
                        long h = t / 3600000;
                        ret = h + "小时前";
                    } else if (yesterday_zero < timestamp)// 昨天
                    {
                        ret = "昨天";
                    } else if (beforeyesterday_zero < timestamp)// 前天
                    {
                        ret = "前天";
                    } else if (t < 604800000)// 七天内 一周之内
                    {
                        ret = getWeekStrOfDate(new Date(timestamp)); // 星期几
                    }
                }
            }

            if (StringUtil.isEmpty(ret))
            {
                if (isSameYear(timestamp))
                {
                    ret = new SimpleDateFormat("M.d").format(new Date(timestamp));
                } else
                {
                    ret = new SimpleDateFormat("yy.M.d").format(new Date(timestamp));
                }
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        return ret;
    }

}
