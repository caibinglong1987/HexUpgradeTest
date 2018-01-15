package com.hexing.upgrade.utils;

/**
 * @author by HEC271
 *         on 2018/1/11.
 */

public class StringUtil {
    /**
     * 转换 ascii 码
     *
     * @param str 字符串
     * @return 返回ascii
     */
    public static String parseAscii(String str) {
        StringBuilder sb = new StringBuilder();
        byte[] bs = str.getBytes();
        for (int i = 0; i < bs.length; i++) {
            sb.append(toHex(bs[i]));
        }
        return sb.toString();
    }

    public static String parseAscii(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++)
            sb.append(toHex(bytes[i]));
        return sb.toString();
    }

    public static String toHex(int n) {
        StringBuilder sb = new StringBuilder();
        if (n / 16 == 0) {
            return toHexUtil(n);
        } else {
            String t = toHex(n / 16);
            int nn = n % 16;
            sb.append(t).append(toHexUtil(nn));
        }
        return sb.toString();
    }

    /**
     * 转换 16进制 数据
     *
     * @param len     长度
     * @param haveOne true 一个字节  false 2个字节
     * @return 16进制数据
     */
    public static String getHex(int len, boolean haveOne) {
        String result;
        if (haveOne) {//一个字节
            result = Integer.toHexString(len);
            if (result.length() == 1) {
                result = "0" + result;
            }
        } else { //2个字节
            result = Integer.toHexString(len);
            for (int i = result.length(); i < 4; i++) {
                result = "0" + result;
            }
        }
        return result;
    }

    private static String toHexUtil(int n) {
        String rt = "";
        switch (n) {
            case 10:
                rt += "A";
                break;
            case 11:
                rt += "B";
                break;
            case 12:
                rt += "C";
                break;
            case 13:
                rt += "D";
                break;
            case 14:
                rt += "E";
                break;
            case 15:
                rt += "F";
                break;
            default:
                rt += n;
        }
        return rt;
    }

    /**
     * ascii 转换 16进制
     *
     * @param str ascii 值
     * @return 16进制 字符
     */
    public static String convertStringToHex(String str) {
        char[] chars = str.toCharArray();
        StringBuffer hex = new StringBuffer();
        for (int i = 0; i < chars.length; i++) {
            hex.append(Integer.toHexString((int) chars[i]));
        }
        return hex.toString();
    }

    /**
     * 转换 ascii
     *
     * @param hex 16进制数据
     * @return 返回 ascii
     */
    public static String convertHexToString(String hex) {

        StringBuilder sb = new StringBuilder();
        StringBuilder temp = new StringBuilder();
        for (int i = 0; i < hex.length() - 1; i += 2) {
            //grab the hex in pairs
            String output = hex.substring(i, (i + 2));
            //convert hex to decimal
            int decimal = Integer.parseInt(output, 16);
            //convert the decimal to character
            sb.append((char) decimal);
            temp.append(decimal);
        }
        return sb.toString();
    }
}
