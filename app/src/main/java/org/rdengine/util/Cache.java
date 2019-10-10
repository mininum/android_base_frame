package org.rdengine.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class Cache
{
    /**
     * 保存在线数据
     * 
     * @param key
     * @param content
     * @return
     */
    public boolean storeOnlineData(String key, String content, String cache_path)
    {
        // 保存数据到缓存
        if (!StringUtil.isEmpty(content) && !StringUtil.isEmpty(cache_path))
        {
            byte[] data;
            try
            {
                data = content.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e1)
            {
                data = content.getBytes();
            }

            String fileName = String.valueOf(key.hashCode());
            File file = new File(cache_path.concat(fileName));
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
                return true;
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

        return false;
    }

    /**
     * 获取缓存的在线数据
     * 
     * @param key
     *            the key
     * @return the store data
     */
    public String getOnlineData(String key, String cache_path)
    {
        String ret = null;
        if (StringUtil.isEmpty(key) || StringUtil.isEmpty(cache_path))
        {
            return ret;
        }
        FileInputStream ins = null;
        ByteArrayOutputStream baos = null;
        String fileName = String.valueOf(key.hashCode());

        File file = new File(cache_path.concat(fileName));
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
}
