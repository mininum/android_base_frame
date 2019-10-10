package org.rdengine.runtime;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import org.rdengine.log.DLOG;
import org.rdengine.util.StringUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * 手机内存存储工具 The Class PreferenceHelper.
 */
public class PreferenceHelper
{

    /** The m ph. */
    private volatile static PreferenceHelper mPH;

    /** The m ctx. */
    private Context mCtx;

    /** The sync obj. */
    private static Object syncObj = new Object();

    /** The edit. */
    private Editor edit = null;

    /** 本地存储 The Constant SHARE_NAME. */
    final static String SHARE_NAME = "mti_wallet_share_preference";

    /**
     * Instantiates a new preference helper.
     * 
     * @param ctx
     *            the ctx
     */
    public PreferenceHelper(Context ctx)
    {
        mCtx = ctx;
    }

    /**
     * Ins.
     * 
     * @return the preference helper
     */
    public static PreferenceHelper ins()
    {
        if (mPH == null)
        {
            synchronized (PreferenceHelper.class)
            {
                if (mPH == null)
                    mPH = new PreferenceHelper(RT.application);
            }
        }
        return mPH;
    }

    /**
     * Gets the preference.
     * 
     * @return the preference
     */
    public SharedPreferences getPreference()
    {
        return getMCtx().getSharedPreferences(SHARE_NAME, Activity.MODE_PRIVATE);
    }

    private Context getMCtx()
    {
        if (mCtx == null)
        {
            mCtx = RT.application;
        }
        return mCtx;
    }

    /**
     * 提交修改的数据，storeShereData之后需要调用commit才能真正保存数据.
     * 
     * @return true, if successful
     */
    public boolean commit()
    {
        boolean ret = false;
        synchronized (syncObj)
        {
            if (edit != null)
            {
                ret = edit.commit();
                if (ret)
                {
                    edit = null;
                }
            }
        }
        return ret;
    }

    /**
     * Checks if is commit.
     * 
     * @return true, if is commit
     */
    public boolean isCommit()
    {
        return (edit == null);
    }

    /**
     * 获取缓存数据.
     * 
     * @param key
     *            the key
     * @param type
     *            the type
     * @return the string share data
     */
    public String getStringShareData(String key, boolean type)
    {
        String data = getShareData(key, type);
        if (data == null)
        {
            return "";
        }
        return data;
    }

    public String getStringShareData(String key, String defValue)
    {
        if (getMCtx() == null)
            return defValue;
        String ret = getPreference().getString(key, defValue);
        if (StringUtil.isEmpty(ret))
        {
            return defValue;
        } else
        {
            return ret;
        }
    }

    /**
     * Gets the share data.
     * 
     * @param key
     *            the key
     * @param type
     *            true从缓存preference取数据
     * @return the share data
     */
    public String getShareData(String key, boolean type)
    {
        String ret = null;
        if (key == null || getMCtx() == null)
        {
            return ret;
        }
        if (type)
        {

            String tmp = getPreference().getString(key, null);

            if (tmp != null)
            {
                return tmp;
            }
        } else
        {
            FileInputStream ins = null;
            ByteArrayOutputStream baos = null;
            String fileName = String.valueOf(key.hashCode());

            File file = new File(RT.defaultCache.concat("/").concat(fileName));
            if (file.exists())
            {
                try
                {
                    ins = new FileInputStream(file);
                    byte[] buffer = new byte[4096];
                    int len = 0;
                    baos = new ByteArrayOutputStream();
                    while ((len = ins.read(buffer)) != -1)
                    {
                        baos.write(buffer, 0, len);
                        baos.flush();
                    }
                    return new String(baos.toByteArray());
                } catch (FileNotFoundException e)
                {
                    e.printStackTrace();
                    return null;
                } catch (IOException e)
                {
                    e.printStackTrace();
                    return null;
                } finally
                {
                    if (ins != null)
                    {
                        try
                        {
                            ins.close();
                        } catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                    if (baos != null)
                    {
                        try
                        {
                            baos.close();
                        } catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
            } else
            {
                // 文件不存在则数据不存在
                return null;
            }

        }

        return ret;
    }

    /**
     * 获得文件内容返回字节数组
     * 
     * @param key
     *            the key
     * @return the share data bytes
     */
    public byte[] getShareDataBytes(String key)
    {
        byte[] ret = null;
        if (key == null || getMCtx() == null)
        {
            return ret;
        }
        FileInputStream ins = null;
        ByteArrayOutputStream baos = null;
        String fileName = String.valueOf(key.hashCode());
        File file = new File(RT.defaultCache.concat("/").concat(fileName));
        if (file.exists())
        {
            try
            {
                ins = new FileInputStream(file);
                byte[] buffer = new byte[4096];
                int len = 0;
                baos = new ByteArrayOutputStream();
                while ((len = ins.read(buffer)) != -1)
                {
                    baos.write(buffer, 0, len);
                    baos.flush();
                }
                return baos.toByteArray();
            } catch (FileNotFoundException e)
            {
                e.printStackTrace();
                return null;
            } catch (IOException e)
            {
                e.printStackTrace();
                return null;
            } finally
            {
                if (ins != null)
                {
                    try
                    {
                        ins.close();
                    } catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
                if (baos != null)
                {
                    try
                    {
                        baos.close();
                    } catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        } else
        {
            // 文件不存在则数据不存在
            return null;
        }

    }

    /**
     * Gets the boolean share data.
     * 
     * @param key
     *            the key
     * @param defValue
     *            the def value
     * @return the boolean share data
     */
    public boolean getBooleanShareData(String key, boolean defValue)
    {
        if (getMCtx() == null)
            return defValue;
        return getPreference().getBoolean(key, defValue);
    }

    /**
     * Gets the int share data.
     * 
     * @param key
     *            the key
     * @param defValue
     *            the def value
     * @return the int share data
     */
    public int getIntShareData(String key, int defValue)
    {
        if (getMCtx() == null)
            return defValue;
        return getPreference().getInt(key, defValue);
    }

    /**
     * Gets the long share data.
     * 
     * @param key
     *            the key
     * @param defValue
     *            the def value
     * @return the long share data
     */
    public long getLongShareData(String key, long defValue)
    {
        if (getMCtx() == null)
            return defValue;
        return getPreference().getLong(key, defValue);
    }

    /**
     * Store int share data.
     * 
     * @param key
     *            the key
     * @param value
     *            the value
     * @return true, if successful
     */
    public boolean storeIntShareData(String key, int value)
    {
        boolean ret = false;
        if (getMCtx() != null)
        {
            synchronized (syncObj)
            {
                if (edit == null)
                {
                    edit = getPreference().edit();
                }
                edit.putInt(key, value);
                ret = true;
            }
        }
        return ret;
    }

    public void storeLongShareData(String key, long value)
    {
        if (getMCtx() != null)
        {
            synchronized (syncObj)
            {
                if (edit == null)
                {
                    edit = getPreference().edit();
                }
                edit.putLong(key, value);
            }
        }
    }

    /**
     * 保存到本地数据，如果是键值对数据需要调用commit方法才可以真正写数据 保存批量键值对数据时候通过commit方法一次写入本地文件. 需要与#commit()方法同时使用
     * 
     * @param key
     *            the key
     * @param data
     *            the data
     * @param type
     *            为true表示保存的键值对，false表示保存到单个文件
     * @return true, if successful
     */
    public boolean storeShareData(String key, byte[] data, boolean type)
    {
        boolean ret = true;
        if (key == null)
            return false;
        if (type)
        {
            if (getMCtx() != null)
            {
                synchronized (syncObj)
                {
                    if (edit == null)
                    {
                        edit = getPreference().edit();
                    }
                    try
                    {
                        edit.putString(key, new String(data, "utf-8"));
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                        ret = false;
                    }
                }

            }
        } else
        {
            // 保存数据到缓存
            File file = new File(RT.defaultCache);
            if (file.exists())
            {
                String fileName = String.valueOf(key.hashCode());
                file = new File(file.getAbsolutePath().concat("/").concat(fileName));
                if (!file.exists())
                {
                    try
                    {
                        file.createNewFile();
                    } catch (IOException e)
                    {
                        e.printStackTrace();
                        return false;
                    }
                }
                FileOutputStream out = null;
                try
                {
                    out = new FileOutputStream(file, false);
                    out.write(data);
                    out.flush();
                } catch (Exception e)
                {
                    e.printStackTrace();
                    return false;
                } finally
                {
                    if (out != null)
                    {
                        try
                        {
                            out.close();
                        } catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return ret;
    }

    public static class FileP
    {
        String filePath;
    }

    public boolean existKey(String key, boolean type, FileP fp)
    {
        if (type)
        {
            if (getMCtx() != null)
            {
                return getPreference().contains(key);

            }

        } else
        {
            File file = new File(RT.defaultCache);
            if (file.exists())
            {

                String fileName = file.getAbsolutePath().concat("/").concat(String.valueOf(key.hashCode()));
                file = new File(fileName);
                if (file.exists() && file.isFile())
                {
                    if (fp != null)
                    {
                        fp.filePath = fileName;
                    } else
                    {
                        fp = new FileP();
                        fp.filePath = fileName;
                    }
                    return true;
                }
            }

        }
        return false;
    }

    /**
     * Store share data.
     * 
     * @param key
     *            the key
     * @param data
     *            the data
     * @return true, if successful
     */
    public boolean storeShareData(String key, byte[] data)
    {
        return storeShareData(key, data, true);
    }

    public void storeShareStringData(String key, String data)
    {
        if (getMCtx() != null)
        {
            synchronized (syncObj)
            {
                if (edit == null)
                {
                    edit = getPreference().edit();
                }
                edit.putString(key, data);
            }
        }
    }

    /**
     * Store boolean share data.
     * 
     * @param key
     *            the key
     * @param value
     *            the value
     * @return true, if successful
     */
    public boolean storeBooleanShareData(String key, boolean value)
    {
        boolean ret = false;
        if (getMCtx() != null)
        {
            synchronized (syncObj)
            {
                if (edit == null)
                {
                    edit = getPreference().edit();
                }
                edit.putBoolean(key, value);
                ret = true;
            }
        }
        return ret;
    }

    /**
     * 删除相关tag，如，keyPrefix=abcdef将删除abcdef为前缀的选项 注意不要误删别的数据
     * 
     * @param keyPrefix
     *            the string
     */
    public void clearDatasWithTag(String keyPrefix)
    {
        if (StringUtil.isEmpty(keyPrefix))
        {
            return;
        }
        if (getMCtx() != null)
        {
            synchronized (syncObj)
            {
                if (edit == null)
                {
                    edit = getPreference().edit();
                }
                Map<String, ?> map = getPreference().getAll();
                Set<String> keys = map.keySet();
                boolean isRemove = false;
                for (String key : keys)
                {
                    if (key.startsWith(keyPrefix))
                    {
                        edit.remove(key);
                        isRemove = true;
                        if (RT.DEBUG)
                        {
                            DLOG.e("cccmax", "remove key:" + key);
                        }
                    }
                }
                if (isRemove)
                {
                    edit.commit();
                }
            }
        }

    }
}
