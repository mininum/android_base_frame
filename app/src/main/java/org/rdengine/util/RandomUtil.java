package org.rdengine.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by CCCMAX on 17/6/4.
 */

public class RandomUtil
{
    /**
     * 根据min和max随机生成一个范围在[min,max]的随机数，包括min和max
     * 
     * @param min
     * @param max
     * @return int
     */
    public static int getRandom(int min, int max)
    {
        Random random = new Random();
        return random.nextInt(max - min + 1) + min;
    }

    /**
     * 根据min和max随机生成count个不重复的随机数组
     * 
     * @param min
     * @param max
     * @param count
     * @return int[]
     */
    public static int[] getRandoms(int min, int max, int count)
    {
        int[] randoms = new int[count];
        List<Integer> listRandom = new ArrayList<Integer>();

        if (count > (max - min + 1))
        {
            return null;
        }

        // 将所有的可能出现的数字放进候选list
        for (int i = min; i <= max; i++)
        {
            listRandom.add(i);
        }

        // 从候选list中取出放入数组，已经被选中的就从这个list中移除
        for (int i = 0; i < count; i++)
        {
            int index = getRandom(0, listRandom.size() - 1);// 随机角标
            randoms[i] = listRandom.get(index);
            listRandom.remove(index);
        }

        return randoms;
    }
}
