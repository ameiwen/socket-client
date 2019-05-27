package com.client.socket.utils;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
    public static final String YYYY_MM_DD_HH_MM = "yyyy-MM-dd HH:mm";
    public static final String YYYY_MM_DD_HH = "yyyy-MM-dd HH";
    public static final String HH_MM_SS = "HH:mm:ss";
    public static final String YYYY_MM_DD = "yyyy-MM-dd";
    public static final String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";
    public static final long ONE_DAY_MILLS = 86400000L;

    public DateUtil() {
    }

    public static String convertToString(Date date, String format) {
        if (date == null) {
            return null;
        } else {
            String s = null;

            try {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
                s = simpleDateFormat.format(date);
            } catch (Exception var4) {
            }

            return s;
        }
    }

    public static Date convertToDate(String s, String format) {
        if (StringUtils.isBlank(s)) {
            return null;
        } else {
            Date date = null;

            try {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
                date = simpleDateFormat.parse(s);
            } catch (Exception var4) {
            }

            return date;
        }
    }

    public static String getCurrentTime() {
        String dateString = null;

        try {
            Date currentTime = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            dateString = formatter.format(currentTime);
        } catch (Exception var3) {
            var3.printStackTrace();
        }

        return dateString;
    }

    public static String getCurrentTimeByFormat(String format) {
        String dateString = null;

        try {
            Date currentTime = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat(format);
            dateString = formatter.format(currentTime);
        } catch (Exception var4) {
            var4.printStackTrace();
        }

        return dateString;
    }
}