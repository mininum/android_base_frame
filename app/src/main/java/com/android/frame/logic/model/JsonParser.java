package com.android.frame.logic.model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public abstract class JsonParser
{
    public boolean delData = false;

    public abstract JsonParser jsonParse(JSONObject json);

    public static <T extends JsonParser> ArrayList<T> parseList(Class<T> clazz, JSONArray jsonarray)
    {
        ArrayList<T> ret = new ArrayList<>();
        if (jsonarray != null && jsonarray.length() > 0)
        {
            for (int i = 0; i < jsonarray.length(); i++)
            {
                JSONObject json = jsonarray.optJSONObject(i);
                if (json != null && json.length() > 0)
                {
                    try
                    {
                        JsonParser bean = clazz.newInstance();
                        bean = bean.jsonParse(json);
                        if (!bean.delData)
                            ret.add((T) bean);
                    } catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                }
            }
        }
        if (ret.size() == 0)
            return null;
        return ret;
    }

}