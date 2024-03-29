package org.rdengine.util;

import android.content.ClipData;
import android.content.Context;
import android.graphics.Paint;
import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import org.rdengine.runtime.RT;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil
{
    /** 正则验证密码 必须包含大写小写数字，可以输入大写小写数字符号空格 6-20位 */
    public static final String REGEX_PASSWORD_1 = "^[0-9a-zA-Z]{6,15}$";
    /** 正则 账号 支持4-18位字母、数字、下划线,首位必须是字母 */
    public static final String REGEX_ACCOUNT = "^(?=.*^[a-zA-Z])\\w{4,18}$";
    /** 正则 手机号*/
    public static final String REGEX_PHONE = "^1[3|4|5|7|8][0-9]\\d{4,8}$";
    /** 钱包地址正则 */
    public static final String REGEX_WALLET_ADDRESS = "^[0-9a-zA-Z_-]+$";
    /** 正则 身份证号 */
    public static final String REGEX_IDENTITY ="\\d{17}[\\d|x|X]";


    /**
     * 判断字符是否为空
     * 
     * @param str
     * @return boolean
     */
    public static boolean isEmpty(String str)
    {
        if (str == null || "".equals(trim(str)) || "null".equalsIgnoreCase(str))
            return true;
        else return false;
    }

    public static boolean isNotEmpty(String str)
    {
        return !isEmpty(str);
    }

    public static String trim(String str)
    {
        if (str != null)
            try
            {
                return str.replaceAll("[ |　]", " ").trim();
            } catch (Exception e)
            {
            }
        return str;
    }

    /**
     * 验证是否是 合法邮箱格式
     * 
     * @param email
     * @return boolean
     */
    public static boolean isIegalEmail(String email)
    {
        return Pattern.compile("\\w+([-_.]\\w+)*@\\w+([-_.]\\w+)*\\.\\w+([-_.]\\w+)*").matcher(email).matches();
    }

    /**
     * 验证是否存在中文字符
     * 
     * @param str
     * @return boolean
     */
    public static boolean isContainsChineseCharacter(String str)
    {
        return Pattern.compile("[.@\\w]*[\u4e00-\u9fa5]+[.@\\w]*").matcher(str).matches();
    }

    /**
     * <![CDATA[]]>
     * 
     * @param s
     * @return
     */
    public static String getCDATA(String s)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("<![CDATA[").append(s).append("]]>");
        return sb.toString();
    }

    /**
     * string 转int，吃掉exception
     * 
     * @param s
     * @return
     */
    public static int formatInt(String s)
    {
        int id = -1;
        try
        {
            id = Integer.valueOf(s);
        } catch (NumberFormatException ex)
        {
            id = -1;
        }

        return id;
    }

    /**
     * 返回MB
     * 
     * @param size
     * @return
     */
    public static String formatFileSize(long size)
    {
        try
        {
            double d = size / (1024 * 1024 * 1024);
            DecimalFormat df2 = new DecimalFormat("#,###,###,##0.00");
            double dd2dec = new Double(df2.format(d)).doubleValue();
            return String.valueOf(dd2dec);
        } catch (Throwable e)
        {
            return "";
        }
    }

    /**
     * 替换指定字符串
     * 
     * @param input
     * @param search
     * @param replacement
     * @return string
     */
    public static String replace(String input, String search, String replacement)
    {
        int pos = input.indexOf(search);
        if (pos != -1)
        {
            StringBuilder buffer = new StringBuilder();
            int lastPos = 0;
            do
            {
                buffer.append(input.substring(lastPos, pos)).append(replacement);
                lastPos = pos + search.length();
                pos = input.indexOf(search, lastPos);
            } while (pos != -1);
            buffer.append(input.substring(lastPos));
            input = buffer.toString();
        }
        return input;
    }

    /**
     * 汉字转码方法
     * 
     * @param text
     *            源字符串
     * @return 转码后的字符串
     */
    public static String encode(String text)
    {
        char[] utfBytes = text.toCharArray();
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < utfBytes.length; i++)
        {
            if (utfBytes[i] == '&')
            {
                result.append("&amp;");
                continue;
            }
            if (utfBytes[i] == '<')
            {
                result.append("&lt;");
                continue;
            }
            if (utfBytes[i] == '>')
            {
                result.append("&gt;");
                continue;
            }
            if (utfBytes[i] == '\"')
            {
                result.append("&quot;");
                continue;
            }
            if (utfBytes[i] == '\'')
            {
                result.append("&apos;");
                continue;
            }
            String hexB = Integer.toHexString(utfBytes[i]);
            if (hexB.length() == 2 && utfBytes[i] > 127)
            {
                result.append("&#x").append("00").append(hexB).append(";");
            } else if (hexB.length() > 2)
            {
                result.append("&#x").append(hexB).append(";");
            } else
            {
                result.append(utfBytes[i]);
            }
        }
        if (result.length() == 0)
        {
            result.append(" ");
        }
        return result.toString();
    }

    public static String HtmlEncode(String text)
    {
        String ret = text.replace("&", "&amp;");
        ret = ret.replace("<", "&lt;");
        ret = ret.replace(">", "&gt;");
        ret = ret.replace("\"", "&quot;");
        ret = ret.replace("\'", "&apos;");
        ret = ret.replace(" ", "&nbsp;");
        ret = ret.replace("\n", "<br/>");
        return ret;
    }

    /**
     * 特殊字符的转义，例如：&#XD;表示回车 <br>
     * 把转义符转换成正常字符
     * 
     * @param s
     * @return
     */
    public static String characterDecode(String s)
    {
        String t;
        Character ch;
        int tmpPos, i;

        int maxPos = s.length();
        StringBuilder sb = new StringBuilder(maxPos);
        int curPos = 0;
        while (curPos < maxPos)
        {
            char c = s.charAt(curPos++);
            if (c == '&')
            {
                tmpPos = curPos;
                if (tmpPos < maxPos)
                {
                    char d = s.charAt(tmpPos++);
                    if (d == '#')
                    {
                        if (tmpPos < maxPos)
                        {
                            d = s.charAt(tmpPos++);
                            if ((d == 'x') || (d == 'X'))
                            {
                                if (tmpPos < maxPos)
                                {
                                    d = s.charAt(tmpPos++);
                                    if (isHexDigit(d))
                                    {
                                        while (tmpPos < maxPos)
                                        {
                                            d = s.charAt(tmpPos++);
                                            if (!isHexDigit(d))
                                            {
                                                if (d == ';')
                                                {
                                                    t = s.substring(curPos + 2, tmpPos - 1);
                                                    try
                                                    {
                                                        i = Integer.parseInt(t, 16);
                                                        if ((i >= 0) && (i < 65536))
                                                        {
                                                            c = (char) i;
                                                            curPos = tmpPos;
                                                        }
                                                    } catch (NumberFormatException e)
                                                    {
                                                    }
                                                }
                                                break;
                                            }
                                        }
                                    }
                                }
                            } else if (isDigit(d))
                            {
                                while (tmpPos < maxPos)
                                {
                                    d = s.charAt(tmpPos++);
                                    if (!isDigit(d))
                                    {
                                        if (d == ';')
                                        {
                                            t = s.substring(curPos + 1, tmpPos - 1);
                                            try
                                            {
                                                i = Integer.parseInt(t);
                                                if ((i >= 0) && (i < 65536))
                                                {
                                                    c = (char) i;
                                                    curPos = tmpPos;
                                                }
                                            } catch (NumberFormatException e)
                                            {
                                            }
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    } else if (isLetter(d))
                    {
                        while (tmpPos < maxPos)
                        {
                            d = s.charAt(tmpPos++);
                            if (!isLetterOrDigit(d))
                            {
                                if (d == ';')
                                {
                                    t = s.substring(curPos, tmpPos - 1);
                                    ch = (Character) charTable.get(t);
                                    if (ch != null)
                                    {
                                        c = ch.charValue();
                                        curPos = tmpPos;
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
            }
            sb.append(c);
        }
        return sb.toString();
    }

    public static final HashMap<String, Character> charTable;

    static
    {
        // 定义特殊字符
        charTable = new HashMap<String, Character>();
        charTable.put("quot", Character.valueOf((char) 34));
        charTable.put("amp", Character.valueOf((char) 38));
        charTable.put("apos", Character.valueOf((char) 39));
        charTable.put("lt", Character.valueOf((char) 60));
        charTable.put("gt", Character.valueOf((char) 62));
        charTable.put("nbsp", Character.valueOf((char) 160));
        charTable.put("iexcl", Character.valueOf((char) 161));
        charTable.put("cent", Character.valueOf((char) 162));
        charTable.put("pound", Character.valueOf((char) 163));
        charTable.put("curren", Character.valueOf((char) 164));
        charTable.put("yen", Character.valueOf((char) 165));
        charTable.put("brvbar", Character.valueOf((char) 166));
        charTable.put("sect", Character.valueOf((char) 167));
        charTable.put("uml", Character.valueOf((char) 168));
        charTable.put("copy", Character.valueOf((char) 169));
        charTable.put("ordf", Character.valueOf((char) 170));
        charTable.put("laquo", Character.valueOf((char) 171));
        charTable.put("not", Character.valueOf((char) 172));
        charTable.put("shy", Character.valueOf((char) 173));
        charTable.put("reg", Character.valueOf((char) 174));
        charTable.put("macr", Character.valueOf((char) 175));
        charTable.put("deg", Character.valueOf((char) 176));
        charTable.put("plusmn", Character.valueOf((char) 177));
        charTable.put("sup2", Character.valueOf((char) 178));
        charTable.put("sup3", Character.valueOf((char) 179));
        charTable.put("acute", Character.valueOf((char) 180));
        charTable.put("micro", Character.valueOf((char) 181));
        charTable.put("para", Character.valueOf((char) 182));
        charTable.put("middot", Character.valueOf((char) 183));
        charTable.put("cedil", Character.valueOf((char) 184));
        charTable.put("sup1", Character.valueOf((char) 185));
        charTable.put("ordm", Character.valueOf((char) 186));
        charTable.put("raquo", Character.valueOf((char) 187));
        charTable.put("frac14", Character.valueOf((char) 188));
        charTable.put("frac12", Character.valueOf((char) 189));
        charTable.put("frac34", Character.valueOf((char) 190));
        charTable.put("iquest", Character.valueOf((char) 191));
        charTable.put("Agrave", Character.valueOf((char) 192));
        charTable.put("Aacute", Character.valueOf((char) 193));
        charTable.put("Acirc", Character.valueOf((char) 194));
        charTable.put("Atilde", Character.valueOf((char) 195));
        charTable.put("Auml", Character.valueOf((char) 196));
        charTable.put("Aring", Character.valueOf((char) 197));
        charTable.put("AElig", Character.valueOf((char) 198));
        charTable.put("Ccedil", Character.valueOf((char) 199));
        charTable.put("Egrave", Character.valueOf((char) 200));
        charTable.put("Eacute", Character.valueOf((char) 201));
        charTable.put("Ecirc", Character.valueOf((char) 202));
        charTable.put("Euml", Character.valueOf((char) 203));
        charTable.put("Igrave", Character.valueOf((char) 204));
        charTable.put("Iacute", Character.valueOf((char) 205));
        charTable.put("Icirc", Character.valueOf((char) 206));
        charTable.put("Iuml", Character.valueOf((char) 207));
        charTable.put("ETH", Character.valueOf((char) 208));
        charTable.put("Ntilde", Character.valueOf((char) 209));
        charTable.put("Ograve", Character.valueOf((char) 210));
        charTable.put("Oacute", Character.valueOf((char) 211));
        charTable.put("Ocirc", Character.valueOf((char) 212));
        charTable.put("Otilde", Character.valueOf((char) 213));
        charTable.put("Ouml", Character.valueOf((char) 214));
        charTable.put("times", Character.valueOf((char) 215));
        charTable.put("Oslash", Character.valueOf((char) 216));
        charTable.put("Ugrave", Character.valueOf((char) 217));
        charTable.put("Uacute", Character.valueOf((char) 218));
        charTable.put("Ucirc", Character.valueOf((char) 219));
        charTable.put("Uuml", Character.valueOf((char) 220));
        charTable.put("Yacute", Character.valueOf((char) 221));
        charTable.put("THORN", Character.valueOf((char) 222));
        charTable.put("szlig", Character.valueOf((char) 223));
        charTable.put("agrave", Character.valueOf((char) 224));
        charTable.put("aacute", Character.valueOf((char) 225));
        charTable.put("acirc", Character.valueOf((char) 226));
        charTable.put("atilde", Character.valueOf((char) 227));
        charTable.put("auml", Character.valueOf((char) 228));
        charTable.put("aring", Character.valueOf((char) 229));
        charTable.put("aelig", Character.valueOf((char) 230));
        charTable.put("ccedil", Character.valueOf((char) 231));
        charTable.put("egrave", Character.valueOf((char) 232));
        charTable.put("eacute", Character.valueOf((char) 233));
        charTable.put("ecirc", Character.valueOf((char) 234));
        charTable.put("euml", Character.valueOf((char) 235));
        charTable.put("igrave", Character.valueOf((char) 236));
        charTable.put("iacute", Character.valueOf((char) 237));
        charTable.put("icirc", Character.valueOf((char) 238));
        charTable.put("iuml", Character.valueOf((char) 239));
        charTable.put("eth", Character.valueOf((char) 240));
        charTable.put("ntilde", Character.valueOf((char) 241));
        charTable.put("ograve", Character.valueOf((char) 242));
        charTable.put("oacute", Character.valueOf((char) 243));
        charTable.put("ocirc", Character.valueOf((char) 244));
        charTable.put("otilde", Character.valueOf((char) 245));
        charTable.put("ouml", Character.valueOf((char) 246));
        charTable.put("divide", Character.valueOf((char) 247));
        charTable.put("oslash", Character.valueOf((char) 248));
        charTable.put("ugrave", Character.valueOf((char) 249));
        charTable.put("uacute", Character.valueOf((char) 250));
        charTable.put("ucirc", Character.valueOf((char) 251));
        charTable.put("uuml", Character.valueOf((char) 252));
        charTable.put("yacute", Character.valueOf((char) 253));
        charTable.put("thorn", Character.valueOf((char) 254));
        charTable.put("yuml", Character.valueOf((char) 255));
        charTable.put("OElig", Character.valueOf((char) 338));
        charTable.put("oelig", Character.valueOf((char) 339));
        charTable.put("Scaron", Character.valueOf((char) 352));
        charTable.put("scaron", Character.valueOf((char) 353));
        charTable.put("fnof", Character.valueOf((char) 402));
        charTable.put("circ", Character.valueOf((char) 710));
        charTable.put("tilde", Character.valueOf((char) 732));
        charTable.put("Alpha", Character.valueOf((char) 913));
        charTable.put("Beta", Character.valueOf((char) 914));
        charTable.put("Gamma", Character.valueOf((char) 915));
        charTable.put("Delta", Character.valueOf((char) 916));
        charTable.put("Epsilon", Character.valueOf((char) 917));
        charTable.put("Zeta", Character.valueOf((char) 918));
        charTable.put("Eta", Character.valueOf((char) 919));
        charTable.put("Theta", Character.valueOf((char) 920));
        charTable.put("Iota", Character.valueOf((char) 921));
        charTable.put("Kappa", Character.valueOf((char) 922));
        charTable.put("Lambda", Character.valueOf((char) 923));
        charTable.put("Mu", Character.valueOf((char) 924));
        charTable.put("Nu", Character.valueOf((char) 925));
        charTable.put("Xi", Character.valueOf((char) 926));
        charTable.put("Omicron", Character.valueOf((char) 927));
        charTable.put("Pi", Character.valueOf((char) 928));
        charTable.put("Rho", Character.valueOf((char) 929));
        charTable.put("Sigma", Character.valueOf((char) 931));
        charTable.put("Tau", Character.valueOf((char) 932));
        charTable.put("Upsilon", Character.valueOf((char) 933));
        charTable.put("Phi", Character.valueOf((char) 934));
        charTable.put("Chi", Character.valueOf((char) 935));
        charTable.put("Psi", Character.valueOf((char) 936));
        charTable.put("Omega", Character.valueOf((char) 937));
        charTable.put("alpha", Character.valueOf((char) 945));
        charTable.put("beta", Character.valueOf((char) 946));
        charTable.put("gamma", Character.valueOf((char) 947));
        charTable.put("delta", Character.valueOf((char) 948));
        charTable.put("epsilon", Character.valueOf((char) 949));
        charTable.put("zeta", Character.valueOf((char) 950));
        charTable.put("eta", Character.valueOf((char) 951));
        charTable.put("theta", Character.valueOf((char) 952));
        charTable.put("iota", Character.valueOf((char) 953));
        charTable.put("kappa", Character.valueOf((char) 954));
        charTable.put("lambda", Character.valueOf((char) 955));
        charTable.put("mu", Character.valueOf((char) 956));
        charTable.put("nu", Character.valueOf((char) 957));
        charTable.put("xi", Character.valueOf((char) 958));
        charTable.put("omicron", Character.valueOf((char) 959));
        charTable.put("pi", Character.valueOf((char) 960));
        charTable.put("rho", Character.valueOf((char) 961));
        charTable.put("sigmaf", Character.valueOf((char) 962));
        charTable.put("sigma", Character.valueOf((char) 963));
        charTable.put("tau", Character.valueOf((char) 964));
        charTable.put("upsilon", Character.valueOf((char) 965));
        charTable.put("phi", Character.valueOf((char) 966));
        charTable.put("chi", Character.valueOf((char) 967));
        charTable.put("psi", Character.valueOf((char) 968));
        charTable.put("omega", Character.valueOf((char) 969));
        charTable.put("thetasym", Character.valueOf((char) 977));
        charTable.put("upsih", Character.valueOf((char) 978));
        charTable.put("piv", Character.valueOf((char) 982));
        charTable.put("ensp", Character.valueOf((char) 8194));
        charTable.put("emsp", Character.valueOf((char) 8195));
        charTable.put("thinsp", Character.valueOf((char) 8201));
        charTable.put("zwnj", Character.valueOf((char) 8204));
        charTable.put("zwj", Character.valueOf((char) 8205));
        charTable.put("lrm", Character.valueOf((char) 8206));
        charTable.put("rlm", Character.valueOf((char) 8207));
        charTable.put("ndash", Character.valueOf((char) 8211));
        charTable.put("mdash", Character.valueOf((char) 8212));
        charTable.put("lsquo", Character.valueOf((char) 8216));
        charTable.put("rsquo", Character.valueOf((char) 8217));
        charTable.put("sbquo", Character.valueOf((char) 8218));
        charTable.put("ldquo", Character.valueOf((char) 8220));
        charTable.put("rdquo", Character.valueOf((char) 8221));
        charTable.put("bdquo", Character.valueOf((char) 8222));
        charTable.put("dagger", Character.valueOf((char) 8224));
        charTable.put("Dagger", Character.valueOf((char) 8225));
        charTable.put("bull", Character.valueOf((char) 8226));
        charTable.put("hellip", Character.valueOf((char) 8230));
        charTable.put("permil", Character.valueOf((char) 8240));
        charTable.put("prime", Character.valueOf((char) 8242));
        charTable.put("Prime", Character.valueOf((char) 8243));
        charTable.put("lsaquo", Character.valueOf((char) 8249));
        charTable.put("rsaquo", Character.valueOf((char) 8250));
        charTable.put("oline", Character.valueOf((char) 8254));
        charTable.put("frasl", Character.valueOf((char) 8260));
        charTable.put("euro", Character.valueOf((char) 8364));
        charTable.put("image", Character.valueOf((char) 8465));
        charTable.put("weierp", Character.valueOf((char) 8472));
        charTable.put("real", Character.valueOf((char) 8476));
        charTable.put("trade", Character.valueOf((char) 8482));
        charTable.put("alefsym", Character.valueOf((char) 8501));
        charTable.put("larr", Character.valueOf((char) 8592));
        charTable.put("uarr", Character.valueOf((char) 8593));
        charTable.put("rarr", Character.valueOf((char) 8594));
        charTable.put("darr", Character.valueOf((char) 8595));
        charTable.put("harr", Character.valueOf((char) 8596));
        charTable.put("crarr", Character.valueOf((char) 8629));
        charTable.put("lArr", Character.valueOf((char) 8656));
        charTable.put("uArr", Character.valueOf((char) 8657));
        charTable.put("rArr", Character.valueOf((char) 8658));
        charTable.put("dArr", Character.valueOf((char) 8659));
        charTable.put("hArr", Character.valueOf((char) 8660));
        charTable.put("forall", Character.valueOf((char) 8704));
        charTable.put("part", Character.valueOf((char) 8706));
        charTable.put("exist", Character.valueOf((char) 8707));
        charTable.put("empty", Character.valueOf((char) 8709));
        charTable.put("nabla", Character.valueOf((char) 8711));
        charTable.put("isin", Character.valueOf((char) 8712));
        charTable.put("notin", Character.valueOf((char) 8713));
        charTable.put("ni", Character.valueOf((char) 8715));
        charTable.put("prod", Character.valueOf((char) 8719));
        charTable.put("sum", Character.valueOf((char) 8721));
        charTable.put("minus", Character.valueOf((char) 8722));
        charTable.put("lowast", Character.valueOf((char) 8727));
        charTable.put("radic", Character.valueOf((char) 8730));
        charTable.put("prop", Character.valueOf((char) 8733));
        charTable.put("infin", Character.valueOf((char) 8734));
        charTable.put("ang", Character.valueOf((char) 8736));
        charTable.put("and", Character.valueOf((char) 8743));
        charTable.put("or", Character.valueOf((char) 8744));
        charTable.put("cap", Character.valueOf((char) 8745));
        charTable.put("cup", Character.valueOf((char) 8746));
        charTable.put("int", Character.valueOf((char) 8747));
        charTable.put("there4", Character.valueOf((char) 8756));
        charTable.put("sim", Character.valueOf((char) 8764));
        charTable.put("cong", Character.valueOf((char) 8773));
        charTable.put("asymp", Character.valueOf((char) 8776));
        charTable.put("ne", Character.valueOf((char) 8800));
        charTable.put("equiv", Character.valueOf((char) 8801));
        charTable.put("le", Character.valueOf((char) 8804));
        charTable.put("ge", Character.valueOf((char) 8805));
        charTable.put("sub", Character.valueOf((char) 8834));
        charTable.put("sup", Character.valueOf((char) 8835));
        charTable.put("nsub", Character.valueOf((char) 8836));
        charTable.put("sube", Character.valueOf((char) 8838));
        charTable.put("supe", Character.valueOf((char) 8839));
        charTable.put("oplus", Character.valueOf((char) 8853));
        charTable.put("otimes", Character.valueOf((char) 8855));
        charTable.put("perp", Character.valueOf((char) 8869));
        charTable.put("sdot", Character.valueOf((char) 8901));
        charTable.put("lceil", Character.valueOf((char) 8968));
        charTable.put("rceil", Character.valueOf((char) 8969));
        charTable.put("lfloor", Character.valueOf((char) 8970));
        charTable.put("rfloor", Character.valueOf((char) 8971));
        charTable.put("lang", Character.valueOf((char) 9001));
        charTable.put("rang", Character.valueOf((char) 9002));
        charTable.put("loz", Character.valueOf((char) 9674));
        charTable.put("spades", Character.valueOf((char) 9824));
        charTable.put("clubs", Character.valueOf((char) 9827));
        charTable.put("hearts", Character.valueOf((char) 9829));
        charTable.put("diams", Character.valueOf((char) 9830));
    }

    private static boolean isLetterOrDigit(char c)
    {
        return isLetter(c) || isDigit(c);
    }

    private static boolean isHexDigit(char c)
    {
        return isHexLetter(c) || isDigit(c);
    }

    private static boolean isLetter(char c)
    {
        return ((c >= 'a') && (c <= 'z')) || ((c >= 'A') && (c <= 'Z'));
    }

    private static boolean isHexLetter(char c)
    {
        return ((c >= 'a') && (c <= 'f')) || ((c >= 'A') && (c <= 'F'));
    }

    private static boolean isDigit(char c)
    {
        return (c >= '0') && (c <= '9');
    }

    // HTML is very particular about what constitutes white space.
    public static boolean isWhitespace(char ch)
    {
        return (ch == '\u0020') || (ch == '\r') || (ch == '\n') || (ch == '\u0009') || (ch == '\u000c')
                || (ch == '\u200b');
    }

    /**
     * 替换掉文件名中的怪异字符集
     * 
     * @param fileName
     * @return
     */
    public static String validFileName(String fileName)
    {
        if (fileName == null)
            return "";
        /**
         * ?@#$&()\|;'"<>+-/
         */
        fileName = fileName.replace("?", "");
        fileName = fileName.replace("@", "");
        fileName = fileName.replace("#", "");
        fileName = fileName.replace("$", "");
        fileName = fileName.replace("&", "");
        fileName = fileName.replace("(", "");
        fileName = fileName.replace(")", "");
        fileName = fileName.replace("|", "");
        fileName = fileName.replace(";", "");
        fileName = fileName.replace("'", "");
        fileName = fileName.replace("\"", "");
        fileName = fileName.replace("<", "");
        fileName = fileName.replace(">", "");
        fileName = fileName.replace("+", "");
        fileName = fileName.replace("-", "");
        fileName = fileName.replace("/", "");
        fileName = fileName.replace("\\", "");
        fileName = fileName.replace("..", "");
        return fileName;
    }

    /**
     * 获取正在播放歌曲的url中的pid
     * 
     * @param url
     * @return
     */
    public static String getPid(String url)
    {
        String pid = url;
        int len = pid.indexOf("PID=");
        if (len > 0)
        {
            pid = pid.substring(len + 4);
            int endLen = pid.indexOf("&");
            if (endLen > 0)
            {
                pid = pid.substring(0, endLen);
            }
        } else
        {
            pid = " ";
        }
        return pid;
    }

    /**
     * 替换掉字符串中选项 如:"mp3:amr:aac"中去掉"amr"
     * 
     * @param target
     *            被替换的字符串
     * @param src
     *            原字符串
     * @param sep
     *            分割符
     * @param type
     *            1转换小写，2转换大写
     * @return 结果字符串
     */
    public static String removeItemFromStr(String target, String src, String sep, int type)
    {
        if (src != null && target != null && sep != null)
        {
            if (type == 1)
            {
                src = src.toLowerCase();
                target = target.toLowerCase();
            } else if (type == 2)
            {
                src = src.toUpperCase();
                target = target.toUpperCase();
            } else
            {
                // not ignore cases
            }
            if (target.equals(src))
            {
                return "";
            } else
            {
                int idx = src.indexOf(sep.concat(target).concat(sep));
                if (idx != -1)
                {
                    src = src.replace(sep.concat(target).concat(sep), sep);
                } else
                {
                    if (src.startsWith(target.concat(sep)))
                    {
                        src = src.substring(target.length() + sep.length());
                    } else if (src.endsWith(sep.concat(target)))
                    {
                        src = src.substring(0, src.length() - (target.length() + sep.length()));
                    }
                }
            }
        }
        return src;
    }

    /**
     * 整数按除数得小数点的商 如：1234 按1024 转多少 K,保留小数点2位. formatIntToFloat(1234,1024,2)
     * 
     * @param src
     *            原整数
     * @param mode
     *            除数
     * @param decimal
     *            小数点位数
     * @return the float
     */
    public static float formatIntToFloat(int src, int mode, int decimal)
    {
        float ret = src * 1.0f;
        ret = ret / mode;
        BigDecimal b = new BigDecimal(ret);
        ret = b.setScale(decimal, BigDecimal.ROUND_HALF_UP).floatValue();
        return ret;
    }

    public static ArrayList<String> splitTextViewText(String txt, Paint paint, int lines, int... maxWidths)
    {

        ArrayList<String> list = new ArrayList<String>();
        if (isEmpty(txt))
        {
            return list;
        }
        int istart = 0;
        char ch;
        int w = 0;
        boolean hasAppended = false;
        int lineCount = 0;
        int len = txt.length();
        int widthLen = maxWidths.length;
        StringBuilder[] sb = new StringBuilder[lines];
        for (int i = 0; i < len; i++)
        {
            ch = txt.charAt(i);
            float[] widths = new float[1];
            String srt = String.valueOf(ch);
            paint.getTextWidths(srt, widths);

            if (ch == '\n' || ch == '\r')
            {
                lineCount++;
                // list.add(txt.substring(istart, i));
                istart = i + 1;
                w = 0;
            } else
            {
                w += (int) (Math.ceil(widths[0]));
                if (w > maxWidths[lineCount < widthLen ? lineCount : 0])
                {
                    lineCount++;
                    // list.add(txt.substring(istart, i));
                    istart = i;
                    i--;
                    w = 0;
                } else
                {
                    if (sb[lineCount] == null)
                    {
                        sb[lineCount] = new StringBuilder();
                    }
                    sb[lineCount].append(ch);

                    if (i == (len - 1))
                    {
                        lineCount++;
                        // list.add(txt.substring(istart, len));
                    }
                }
            }
            if (lineCount == lines)
            {
                if (i < (len - 1))
                {
                    hasAppended = true;
                }
                break;
            }
        }
        istart = 0;
        while (true)
        {
            if (sb[istart] != null)
            {
                list.add(sb[istart].toString());
            }
            istart++;
            if (istart >= lines)
                break;
        }
        int size = list.size();
        if (size == 0)
        {
            return list;
        }
        String last = list.get(size - 1);
        // if (!txt.endsWith(last) && lineCount > 1)
        // {
        // if (last.length() > 3)
        // {
        // last = last.substring(0, last.length() - 3).concat("...");
        // list.set(list.size() - 1, last);
        //
        // }
        // }
        int l = last.length();
        if (size < lines)
        {
            list.add("...");
        } else
        {
            if (hasAppended && l > 3)
            {
                last = last.substring(0, l - 3).concat("...");
                list.set(size - 1, last);
            }
        }
        return list;
    }

    // public static ArrayList<String> splitTextViewText(String txt, Paint p, int[] maxWidths, int lines)
    // {
    //
    // ArrayList<String> list = new ArrayList<String>();
    // if (isEmpty(txt))
    // {
    // return list;
    // }
    // int istart = 0;
    // char ch;
    // int w = 0;
    // int lineCount = 0;
    // int len = txt.length();
    // for (int i = 0; i < len; i++)
    // {
    // ch = txt.charAt(i);
    // float[] widths = new float[1];
    // String srt = String.valueOf(ch);
    // p.getTextWidths(srt, widths);
    //
    // if (ch == '\n' || ch == '\r')
    // {
    // lineCount++;
    // list.add(txt.substring(istart, i));
    // istart = i + 1;
    // w = 0;
    // } else
    // {
    // w += (int) (Math.ceil(widths[0]));
    // if (w > maxWidths[lineCount])
    // {
    // lineCount++;
    // list.add(txt.substring(istart, i));
    // istart = i;
    // i--;
    // w = 0;
    // } else
    // {
    // if (i == (len - 1))
    // {
    // lineCount++;
    // list.add(txt.substring(istart, len));
    // }
    // }
    // }
    // if (lineCount == lines)
    // {
    // break;
    // }
    // }
    // if (list.size() == 0)
    // {
    // return list;
    // }
    // String last = list.get(list.size() - 1);
    // if (!txt.endsWith(last) && lineCount > 1)
    // {
    // if (last.length() > 3)
    // {
    // last = last.substring(0, last.length() - 3).concat("...");
    // list.set(list.size() - 1, last);
    //
    // }
    // }
    //
    // return list;
    // }

    /** 从字符串中过滤11位的手机号码 去掉+86神马的 */
    public static String filterPhoneNumber(String phn)
    {
        if (isEmpty(phn))
        {
            return "";
        }
        String p_num = phn;
        if (p_num.length() >= 11)// 大于等于11位
        {
            p_num = StringUtil.replace(phn, "+86", "");// 电话号码去掉+86 如果有的话
            p_num = p_num.replace("+", "");// 电话号码去掉+ 如果有的话
            if (p_num.length() > 11)
                p_num = p_num.substring(p_num.length() - 10);// 号码大于11位 就只取后面的11个数
        } else
        // 小于11位 将就用吧 有几位是几位
        {

        }
        return p_num;
    }

    /**
     * 判断11位数字手机号
     * 
     * @param str
     * @return
     */
    public static boolean isPhoneNumber(String str)
    {
        try
        {
            Pattern p = null;
            Matcher m = null;
            boolean b = false;
            p = Pattern.compile("^[1][3,4,5,7,8][0-9]{9}$"); // 验证手机号
            m = p.matcher(str);
            b = m.matches();
            return b;
        } catch (Exception e)
        {
        }
        return false;
    }

    /**
     * 自动分割文本
     * 
     * @param content
     *            需要分割的文本
     * @param p
     *            画笔，用来根据字体测量文本的宽度
     * @param width
     *            最大的可显示像素（一般为控件的宽度）
     * @return 一个字符串数组，保存每行的文本
     */
    public static String[] autoSplit(String content, Paint p, float width)
    {
        int length = content.length();
        float textWidth = p.measureText(content);
        if (textWidth <= width)
        {
            return new String[]
            { content };
        }

        int start = 0, end = 1, i = 0;
        int lines = (int) Math.ceil(textWidth / width); // 计算行数
        String[] lineTexts = new String[lines];
        while (start < length)
        {
            if (p.measureText(content, start, end) > width)
            { // 文本宽度超出控件宽度时
                lineTexts[i++] = (String) content.subSequence(start, end);
                start = end;
            }
            if (end == length)
            { // 不足一行的文本
                lineTexts[i] = (String) content.subSequence(start, end);
                break;
            }
            end += 1;
        }
        return lineTexts;
    }

    public static int getLineCount(String content, Paint p, float width)
    {
        int count = 0;
        String[] strs = autoSplit(content, p, width);
        count = strs.length;
        if (strs.length == 1)
        {
            int length = content.length();
            for (int i = 0; i < length; i++)
            {
                char ch = content.charAt(i);
                if (ch == '\n' || ch == '\r')
                {
                    ++count;
                }
            }
        }
        return count;
    }

    /** 字符串从头开始 截取出限定长度的部分 中文长度 最多X个汉字的长度。。。 */
    public static String getSubStringByCNlength(String in, int CNmaxlength)
    {
        if (StringUtil.isEmpty(in))
        {
            return "";
        }
        int cnLength = 0;
        int enLength = 0;
        String a;
        StringBuffer retbuff = new StringBuffer();
        for (int i = 0; i < in.length(); i++)
        {
            // String a = in.substring(i, i + 1);
            a = String.valueOf(in.charAt(i));
            byte[] b = a.getBytes();
            if (b.length < 2)
            {
                enLength++;
            } else
            {
                cnLength++;
            }

            int Length = cnLength + (enLength + 1) / 2;
            if (Length == CNmaxlength)
            {
                return retbuff.append(a).toString();
            } else if (Length > CNmaxlength)
            {
                return retbuff.delete(retbuff.length() - 1, retbuff.length()).toString();
            }
            retbuff.append(a);
        }
        return in;
    }

    /**
     * 计算字节长度， 英文数字 +1，汉字+2
     * 
     * @param in
     * @return
     */
    public static int getBytelength(String in)
    {
        if (in == null)
        {
            return 0;
        }
        int cnLength = 0;
        int enLength = 0;
        String a;
        for (int i = 0; i < in.length(); i++)
        {
            a = String.valueOf(in.charAt(i));
            byte[] b = a.getBytes();
            if (b.length < 2)
            {
                enLength++;
            } else
            {
                cnLength++;
            }
        }
        int Length = cnLength * 2 + enLength;
        return Length;
    }

    /**
     * 计算汉字长度， 汉字+1，英文数字+0.5，取整
     *
     * @param in
     * @return
     */
    public static int getCNlength(String in)
    {
        if (in == null)
        {
            return 0;
        }
        int cnLength = 0;
        int enLength = 0;
        String a;
        for (int i = 0; i < in.length(); i++)
        {
            a = String.valueOf(in.charAt(i));
            byte[] b = a.getBytes();
            if (b.length < 2)
            {
                enLength++;
            } else
            {
                cnLength++;
            }
        }
        int Length = cnLength + (enLength + 1) / 2;
        return Length;
    }

    public static byte[] getBytes(String str)
    {
        return getBytes(str, null);
    }

    public static byte[] getBytes(String str, String charsetName)
    {
        byte[] ret = null;
        if (isEmpty(str))
            return ret;
        try
        {
            if (!isEmpty(charsetName))
            {
                ret = str.getBytes(charsetName);
            } else
            {
                ret = str.getBytes("utf-8");
            }
        } catch (UnsupportedEncodingException e)
        {
            ret = str.getBytes();
        }
        return ret;
    }

    /** 字节转对应对应存储单位 最大到TB 最小KB */
    public static String getUnitBySize(long num)
    {
        String ret = "";
        try
        {
            DecimalFormat df = new DecimalFormat("0.##");
            if (num >= 1099511627776L)// TB
            {
                ret = df.format((float) num / 1099511627776L).concat("TB");
            } else if (num >= 1073741824)// GB
            {
                ret = df.format((float) num / 1073741824).concat("GB");
            } else if (num >= 1048576)// MB
            {
                ret = df.format((float) num / 1048576).concat("MB");
            } else
            /* if(num >= 1024) */// KB
            {
                ret = df.format((float) num / 1024).concat("KB");
            }
            // else //B
            // {
            // ret =""+num+"B";
            // }
        } catch (Exception e)
        {
        }
        return ret;
    }

    /**
     * 清理字符串中的HTML标签
     * 
     * @param source
     * @return
     */
    public static String clearHtml(String source)
    {
        if (!isEmpty(source))
            try
            {
                Spanned spanned = Html.fromHtml(source);
                source = spanned.toString();
            } catch (Exception e)
            {
            }
        return source;
    }

    /**
     * 判断字符串是否包含html的标签 <>
     * 
     * @param source
     * @return
     */
    public static boolean hasHtmlTag(String source)
    {
        if (!isEmpty(source))
            try
            {
                String regex = "<(/|[a-zA-Z0-9])[^>]+>";
                Pattern p = Pattern.compile(regex);
                Matcher m = p.matcher(source);
                boolean has = m.find();
                return has;
            } catch (Exception e)
            {
            }
        return false;
    }

    public static String formatBigNumber(double number)
    {
        // return formatBigNumber(number, 1, true);
        return formatBigNumber(number, 2);
    }

    public static String formatBigNumber(double number, int floatLength)
    {
        return formatBigNumber(number, floatLength, true, false);
    }

    /**
     * 数字转化成 万，百万，亿, 小数精确显示不取舍
     * 
     * @param number
     * @param floatLength
     *            保留小数点后几位
     * @param userBigUnit
     *            是否使用大单位缩进
     * @param patchO
     *            小数位是否补0
     * @return
     */
    public static String formatBigNumber(double number, int floatLength, boolean userBigUnit, boolean patchO)
    {
        String ret = "";
        double divisor = 1;
        String unit = "";
        try
        {
            if (Double.isNaN(number) || Double.isInfinite(number))
                number = 0;
            number = new BigDecimal(number).doubleValue();
        } catch (Exception ex)
        {
        }
        if (userBigUnit)
        {
            if (number >= 100000000)
            {
                divisor = 100000000;
                unit = "亿";
            }
            // else if (number >= 1000000)
            // {
            // divisor = 1000000;
            // unit = "百万";
            // }
            else if (number >= 10000)
            {
                divisor = 10000;
                unit = "w";
            }
        }
        double a = number / divisor; // 1.83
        // 小数点后 # 可以隐藏多余的0， 0 不隐藏多余的0
        DecimalFormat df = userBigUnit ? new DecimalFormat("###################.###########")
                : new DecimalFormat("#,###.###########");
        String ssss = df.format(a);

        if (ssss.contains("."))
        {
            int index0 = ssss.indexOf(".");
            if (patchO && floatLength > 0)
            {
                // 补0
                int cccfloatLength = ssss.length() - (index0 + 1);
                if (cccfloatLength < floatLength)
                {
                    StringBuilder sb = new StringBuilder(ssss);
                    for (int i = 0; i < floatLength - cccfloatLength; i++)
                    {
                        sb.append("0");
                    }
                    ssss = sb.toString();
                }
            }

            int end = index0 + 1 + floatLength;
            end = Math.min(end, ssss.length());
            ssss = ssss.substring(0, end);

            if (ssss.endsWith("."))
            {
                ssss = ssss.substring(0, ssss.length() - 1);

            }

        } else if (patchO && floatLength > 0)
        {
            // 补0
            StringBuilder sb = new StringBuilder(ssss);
            sb.append(".");
            for (int i = 0; i < floatLength; i++)
            {
                sb.append("0");
            }
            ssss = sb.toString();
        } else
        {

            if (patchO && floatLength > 0)
            {
                StringBuilder sb = new StringBuilder(ssss);
                sb.append(".");
                for (int i = 0; i < floatLength; i++)
                {
                    sb.append("0");
                }
                ssss = sb.toString();
            }

        }

        ret = ssss + unit;

        return ret;
    }

    /**
     * 格式化未读数
     * 
     * @param number
     * @return
     */
    public static String formatUnreadNumber(long number)
    {
        // // 1000以内显示数字，1000以上显示成1000+，2000+，1w+
        // String ret = "";
        // int divisor = 1;
        // String unit = "";
        // if (number >= 10000)
        // {
        // divisor = 10000;
        // unit = "w";
        // } else if (number >= 1000)
        // {
        // divisor = 1000;
        // unit = "000";
        // } else
        // {
        // ret = "" + number;
        // }
        //
        // if (divisor > 1)
        // {
        // long a = number / divisor;
        // ret = a + unit + "+";
        // }
        // return ret;

        // 999以上显示...
        // if (number > 999)
        // {
        // return "…";
        // }
        // return "" + number;

        // 99以上显示99+
        if (number > 99)
        {
            return "99+";
        }
        return "" + number;
    }

    /**
     * 复制字符串到剪贴板
     * 
     * @param context
     * @param content
     */
    public static void copyString(Context context, String content)
    {
        if (content == null)
            return;
        int sdk = android.os.Build.VERSION.SDK_INT;
        if (sdk < 11)
        {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context
                    .getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(content.trim());
        } else
        {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context
                    .getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("", content.trim());
            clipboard.setPrimaryClip(clip);
        }
    }

    /**
     * 复制字符串到剪贴板
     *
     * @param context
     */
    public static String readClipboardString(Context context)
    {
        int sdk = android.os.Build.VERSION.SDK_INT;
        if (sdk < 11)
        {
            try
            {
                android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context
                        .getSystemService(Context.CLIPBOARD_SERVICE);
                CharSequence text = clipboard.getText();
                return text.toString();
            } catch (Exception ex)
            {
            }
        } else
        {
            try
            {
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context
                        .getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData cd = clipboard.getPrimaryClip();
                // if (cd.getDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN))
                ClipData.Item item = cd.getItemAt(0);
                CharSequence text = item.getText();
                return text.toString();
            } catch (Exception ex)
            {
            }
        }
        return null;
    }

    /**
     * 文字关键词高亮
     * 
     * @param str
     * @param keyword
     * @return
     */
    public static CharSequence makeLightText(String str, String keyword, String color)
    {
        try
        {
            if (!isEmpty(str) && !isEmpty(keyword) && str.contains(keyword))
            {
                str = str.replaceAll("\\n", "<br/>");
                str = str.replaceAll(keyword, "<font color='" + color + "'>" + keyword + "</font>");
                CharSequence ret = Html.fromHtml(str);
                return ret;
            }
        } catch (Exception e)
        {
        }
        return str;
    }

    public static void makeLightText(Context context, Spannable spannableString, String keyword, int start, int color)
    {
        if (context == null || spannableString == null || isEmpty(keyword))
            return;

        String pa = keyword;
        Pattern pattern = Pattern.compile(pa);
        Matcher matcher = pattern.matcher(spannableString);
        while (matcher.find())
        {
            final String key = matcher.group();
            if (matcher.start() < start)
            {
                continue;
            }
            int end = matcher.start() + key.length();
            spannableString.setSpan(new ForegroundColorSpan(color), matcher.start(), end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            if (end < spannableString.length())
            { // 如果整个字符串还未验证完，则继续。。
                makeLightText(context, spannableString, keyword, end, color);
            }
            break;
        }
    }

    /**
     * 将异常信息转化成字符串
     * 
     * @param t
     * @return
     * @throws IOException
     */
    public static String exceptionToString(Throwable t)
    {
        if (t == null)
            return null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try
        {
            t.printStackTrace(new PrintStream(baos));
        } finally
        {
            try
            {
                baos.close();
            } catch (Exception e)
            {
            }
        }
        return baos.toString();
    }

    /**
     * 返回单个字符串，若匹配到多个的话就返回第一个，方法与getSubUtil一样
     * 
     * @param soap
     * @param rgex
     * @return
     */
    public static String getSubUtilSimple(String soap, String rgex)
    {
        try
        {
            Pattern pattern = Pattern.compile(rgex);// 匹配的模式
            Matcher m = pattern.matcher(soap);
            while (m.find())
            {
                return m.group(0);
            }
        } catch (Exception ex)
        {
        }
        return "";
    }

    public static String getRandomStringFromArray(int stringArrayID)
    {
        try
        {
            String[] array = RT.application.getResources().getStringArray(stringArrayID);
            int index = new Random().nextInt(array.length);
            return array[index];
        } catch (Exception e)
        {
        }
        return null;
    }

    /**
     * 替换数字字符串中间的几位，自动计算<br>
     * 例如手机号 提出按成 138****9999
     *
     * @param numberStr
     *            数字字符串
     * @param replacement
     *            替换符
     * @return
     */
    public static String replaceIntermediateNumbers(String numberStr, char replacement)
    {
        try
        {
            numberStr = numberStr.trim();
            int length = numberStr.length();
            int s = 3;
            int m = 4;
            int e = 4;

            int a = length / 3;
            if (a < length / 3.0F)
                a++;
            m = a;

            int b = (length - m) / 2;
            if (b < (length - m) / 2.0F)
                b++;
            e = b;

            s = length - m - e;

            String r = "(\\d{" + s + "})\\d{" + m + "}(\\d{" + e + "})";
            String rc = "";
            for (int i = 0; i < m; i++)
            {
                rc += replacement;
            }
            String ret = numberStr.replaceAll(r, "$1" + rc + "$2");
            return ret;
        } catch (Exception ex)
        {
            return numberStr;
        }
    }

    /**
     * 价格格式化 ，整数保留1位小数， 小数保留2位小数
     * 
     * @param s
     *            金额
     * @return 格式后的金额
     */
    public static String orderPriceFormat(String s)
    {
        if (s == null || s.length() < 1)
        {
            return "";
        }
        double num = Double.parseDouble(s);

        // 取整数
        long numLong = Long.parseLong(new DecimalFormat("#").format(num));

        if (numLong == num)
        {
            // 传入数值 是整数
            return numLong + ".0";
        } else
        {
            // 传入数值是小数
            DecimalFormat formater = new DecimalFormat("0.00");
            return formater.format(num);
        }
    }

    /**
     * 格式化货币 ，每3位用,分隔，小数位有几位显示几位
     * 
     * @param num
     * @return
     */
    public static String formatMoneyExact(double num)
    {
        // "###################.###########" 精确显示double的格式化方法，会有进位
        String numstr = new DecimalFormat("#,###.###########").format(num);
        return numstr;
    }

    public static String formatLongNumber(double num)
    {
        // "###################.###########" 精确显示double的格式化方法
        // 14.399999999999999 这种 会进位成14.4
        String numstr = new DecimalFormat("###################.###########").format(num);
        return numstr;
    }

    public static void main(String... arg)
    {
        System.out.println(formatBigNumber(1232353456526.314489843752345d));
        System.out.println(formatBigNumber(1232353456526.399999999999999d));
        System.out.println(formatMoneyExact(50.09999847412d));
        System.out.println(formatLongNumber(1232353456526.399999999999999d));
    }
}
