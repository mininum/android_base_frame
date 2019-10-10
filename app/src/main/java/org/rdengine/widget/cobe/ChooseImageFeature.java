package org.rdengine.widget.cobe;

import java.util.ArrayList;
import java.util.List;

public class ChooseImageFeature
{
    /**
     * 选中的图片路径
     */
    public ArrayList<String> choosePathList = new ArrayList<String>();
    /**
     * 选中的图当前查看第几个
     */
    public int index = 0;

    /**
     * 最多可以选多少张图
     */
    public int max_count = 9;

    public ChooseImageCallback mChooseImageCallback;

    public ChooseImageFeature(int maxcount, ChooseImageCallback callback)
    {
        max_count = maxcount;
        mChooseImageCallback = callback;
    }

    public ChooseImageFeature(ArrayList<String> pathlist, int index, int maxcount, ChooseImageCallback callback)
    {
        choosePathList = pathlist;
        this.index = index;
        max_count = maxcount;
        mChooseImageCallback = callback;
    }

    public interface ChooseImageCallback
    {
        void onFinsh(List<String> pathlist);
    }
}
