package org.rdengine.util.inputfilter;

import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;

/**
 * 文本内容过滤，限制小数长度 Created by CCCMAX on 2019/5/8.
 */

public class NumberFloatLengthInputFilter implements InputFilter
{
    int maxFloatLength;

    public NumberFloatLengthInputFilter(int maxFloatLength)
    {
        this.maxFloatLength = maxFloatLength;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend)
    {
        if (dest.toString().contains("."))
        {
            int pointIndex = dest.toString().indexOf(".");// 小数点位置
            String[] array = dest.toString().split("\\.");
            if (array.length == 2)
            {
                String floatstr = array[1];// 获取小数点后内容
                if (!TextUtils.isEmpty(floatstr) && floatstr.length() >= maxFloatLength)
                {
                    if (dstart <= pointIndex)
                    {
                        // 判断当前光标是否输入在小数点前
                        return null;// 不做过滤操作
                    } else
                    {
                        return "";// 返回无内容
                    }
                }
            }
        } else if (source.toString().startsWith(".") && dstart == 0)
        {
            // 原文没有.
            // 如果输入内容 是.开头 、 插在头部 , 补0
            return "0" + source;
        }

        return null;
    }
}
