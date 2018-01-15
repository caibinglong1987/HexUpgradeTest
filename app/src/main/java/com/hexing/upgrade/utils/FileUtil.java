package com.hexing.upgrade.utils;

import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author caibinglong
 *         date 2018/1/3.
 *         desc desc
 */

public class FileUtil {

    public static byte[] getBytesFromFile(File file) throws IOException {
        // Get the size of the file
        long length = file.length();

        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.
        if (length > Integer.MAX_VALUE) {
            // File is too large
            throw new IOException("File is too large!");
        }

        if (length == 0) {
            return null;
        }

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int) length];

        // Read in the bytes
        int offset = 0;
        int numRead = 0;

        InputStream is = new FileInputStream(file);
        try {
            while (offset < bytes.length
                    && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead;
            }
        } finally {
            is.close();
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file " + file.getName());
        }
        return bytes;
    }


    /**
     * InputStream --> File
     *
     * @param ins
     * @param file
     */
    public static File inputStreamToFile(InputStream ins, File file) {
        OutputStream os = null;
        try {
            os = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();

            return file;
        }
        int bytesRead = 0;
        byte[] buffer = new byte[8192];
        try {
            while ((bytesRead = ins.read(buffer, 0, 8192)) != -1) {
                os.write(buffer, 0, bytesRead);
            }

            os.close();
            ins.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    /**
     * 按行读取文件
     *
     * @param file file
     */
    public static List<String> readFileOnLine(File file) {
        List<String> dataList = new ArrayList<>();
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (inputStream == null) {
            LogUtils.d("读取文本内容", "文件解析失败");
            return dataList;
        }
        StringBuilder strBuilder = new StringBuilder();
        InputStreamReader streamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(streamReader);
        String strLine;
        try {
            //通过read line按行读取
            while ((strLine = bufferedReader.readLine()) != null) {
                //strLine就是一行的内容
                if (!TextUtils.isEmpty(strLine)) {
                    strBuilder.append(strLine);
                    dataList.add(strBuilder.toString());
                    strBuilder = new StringBuilder();
                }
            }
            bufferedReader.close();
            streamReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dataList;
    }

}
