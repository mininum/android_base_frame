package org.rdengine.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by CCCMAX on 17/7/21.
 */

public class DataUtil
{

    /**
     * 数据分组 一行多个
     *
     * @param resources
     *            数据 ArrayList[Object]
     * @param groupCount
     *            一行有几个 最少2个
     * @param keepRemainder
     *            保留剩余的不够一行的数据
     * @return ArrayList[ArrayList[Object]]
     */
    public static ArrayList makeResourceGridGroup(List resources, int groupCount, boolean keepRemainder)
    {
        if (groupCount < 2)
            groupCount = 2;

        ArrayList ret = new ArrayList();

        if (resources != null && resources.size() > 0)
        {
            ArrayList group = new ArrayList();
            for (Object res : resources)
            {
                if (group == null)
                {
                    group = new ArrayList();
                }

                if (group.size() < groupCount)
                {
                    // group中没装满 就添加数据
                    group.add(res);
                }

                if (group.size() == groupCount)
                {
                    // group装满后 group添加到ret中，并清除group指针
                    ret.add(group);
                    group = null;
                }
            }
            if (group != null && group.size() > 0)
                ret.add(group);

            // 不保留不足一行的数据
            if (!keepRemainder && ret.size() > 0 && groupCount != Integer.MAX_VALUE)
            {
                ArrayList lastGroup = (ArrayList) ret.get(ret.size() - 1);
                if (lastGroup != null && lastGroup.size() < groupCount)
                    ret.remove(lastGroup);
            }
        }

        if (ret.size() > 0)
            return ret;

        return null;
    }
}
