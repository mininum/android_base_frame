package com.android.frame.logic.model;

/**
 * Created by CCCMAX on 2019/5/20.
 */
public enum Gender
{
    MALE(1), FEMALE(2), UNKNOW(3);

    int num;

    Gender(int num)
    {
        this.num = num;
    }

    public static Gender findItem(int num)
    {
        for (Gender gender : Gender.values())
        {
            if (gender.num == num)
                return gender;
        }
        return UNKNOW;
    }
}
