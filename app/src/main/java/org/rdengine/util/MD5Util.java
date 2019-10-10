package org.rdengine.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class MD5Util
{
    public static String getMd5(byte[] bytes)
    {
        try
        {
            MessageDigest localMessageDigest = MessageDigest.getInstance("MD5");
            localMessageDigest.update(bytes);
            String str = bytesToHexString(localMessageDigest.digest());
            return str;
        } catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static String getMd5(String str)
    {
        try
        {
            byte[] bytes = str.getBytes();
            MessageDigest localMessageDigest = MessageDigest.getInstance("MD5");
            localMessageDigest.update(bytes);
            String ret = bytesToHexString(localMessageDigest.digest());
            return ret;
        } catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static String bytesToHexString(byte[] bytes)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++)
        {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1)
            {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    public static String getMd5(File file)
    {
        String md5String = null;
        if (!file.exists())
        {
            return null;
        }
        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[1024];
        int len;
        try
        {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1)
            {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
        BigInteger bigInt = new BigInteger(1, digest.digest());
        md5String = bigInt.toString(16);
        return md5String;

    }

    /**
     * 利用java原生的摘要实现SHA256加密
     * 
     * @param str
     *            加密后的报文
     * @return
     */
    public static String getSHA256Str(String str)
    {
        MessageDigest messageDigest;
        String encodeStr = "";
        try
        {
            messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(str.getBytes("UTF-8"));
            encodeStr = bytesToHexString(messageDigest.digest());
        } catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        return encodeStr;
    }

    /**
     * hmacSHA256签名
     * @param data 数据
     * @param key 秘钥
     * @return 结果转16进制
     */
    public static String getHmacSHA256(String data, String key)
    {

        try
        {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");

            SecretKeySpec secret_key = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");

            sha256_HMAC.init(secret_key);

            byte[] array = sha256_HMAC.doFinal(data.getBytes("UTF-8"));

            StringBuilder sb = new StringBuilder();

            for (byte item : array)
            {

                sb.append(Integer.toHexString((item & 0xFF) | 0x100).substring(1, 3));

            }

            return sb.toString().toUpperCase();
        } catch (Exception ex)
        {
        }

        return null;
    }

}
