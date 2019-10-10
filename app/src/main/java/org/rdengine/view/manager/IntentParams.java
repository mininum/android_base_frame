package org.rdengine.view.manager;

import java.util.HashMap;

/**
 * 同进程内intent传递参数，用此工具intent传key 接收端用key获取内存对象<br>
 * <ul>
 * Intent intent = new Intent(.....);<br>
 * String key = IntentParams.getInstance().createKey(context.getClass(), clazz);<br>
 * IntentParams.getInstance().put(key, mViewParam);<br>
 * intent.putExtra(IntentParams.INTENT_PARAMS_KEY, key);<br>
 * </localPath>
 * 
 * @author CCCMAX
 */
public class IntentParams
{
    public static final String INTENT_PARAMS_KEY = "intent_params_key";

    private HashMap<String, ViewParam> map = null;

    private static IntentParams params;

    private IntentParams()
    {
        map = new HashMap<String, ViewParam>();
    }

    public static IntentParams getInstance()
    {
        if (params == null)
        {
            params = new IntentParams();
        }
        return params;
    }

    /**
     * 创建一个key
     * 
     * @param cls1
     * @param cls2
     * @return
     */
    public String createKey(Class<?> cls1, Class<?> cls2)
    {
        return cls1.getName() + "_" + cls2.getName() + "_" + System.currentTimeMillis();
    }

    public void put(String key, ViewParam params)
    {
        map.put(key, params);
    }

    /**
     * 获取值
     * 
     * @param key
     * @return
     */
    public ViewParam get(String key)
    {
        return map.get(key);
    }

    /**
     * 删除已经用过的 在获取使用之后一定要删除
     * 
     * @param key
     */
    public void remove(String key)
    {
        map.remove(key);
    }

}