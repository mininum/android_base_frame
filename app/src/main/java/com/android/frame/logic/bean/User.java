package com.android.frame.logic.bean;


import com.android.frame.logic.model.JsonParser;

import org.json.JSONObject;

/**
 * Created by CCCMAX on 2019/5/20.
 */

public class User extends JsonParser
{

    public int id;
    public String nickname;
    public int gender = SexType.UNKMOW;
    public long birthday;
    public String signture;
    public String avatar;
    public long createtime;
    public long lastlogin;
    public String phone;
    public String password;
    public String  certificated;
    public String game_certificated;
    public boolean have_fund_pwd;

    public String session;

    @Override
    public User jsonParse(JSONObject json)
    {
        try
        {
            id = json.optInt("id");
            nickname = json.optString("nickname");
            gender = json.optInt("gender", SexType.UNKMOW);
            birthday = json.optLong("birthday");
            signture = json.optString("signture");
            avatar = json.optString("avatar");
            createtime = json.optLong("createtime");
            lastlogin = json.optLong("lastlogin");
            phone = json.optString("phone");
            password = json.optString("password");
            certificated = json.optString("certificated");
            game_certificated = json.optString("game_certificated");
            have_fund_pwd = json.optBoolean("have_fund_pwd");

            session = json.optString("session",session);
        } catch (Exception ex)
        {
        }
        return this;
    }

    public JSONObject toJson()
    {
        JSONObject json = new JSONObject();
        try
        {

            json.put("uid", id);
            json.put("nickname",nickname);
            json.put("gender",gender);
            json.put("birthday",birthday);
            json.put("signture",signture);
            json.put("avatar",avatar);
            json.put("createtime",createtime);
            json.put("lastlogin",lastlogin);
            json.put("phone", phone);
            json.put("password",password);
            json.put("certificated",certificated);
            json.put("game_certificated",game_certificated);
            json.put("have_fund_pwd",have_fund_pwd);
            json.put("session",session);
        } catch (Exception ex)
        {
        }
        return json;
    }


    public String toJsonString()
    {
        return toJson().toString();
    }

    public interface  SexType{
        //男
        int MAN = 1;

        //女
        int WOMAN = 2;

        //未知
        int UNKMOW = 3;
    }


}
