package cn.navibeidou.beidou.Util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeStringUtil {

    public static String nowString() {
        SimpleDateFormat sDateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
        return sDateFormat.format(new java.util.Date()) + " ";
    }

    public static String nowStringLong() {
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        return sDateFormat.format(new java.util.Date()) + " ";
    }

    public static String longToDate(long longTime) {
        Date date = new Date(longTime);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

    public static String[] getBeforeSevenDay() {
        String[] arr = new String[7];
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = null;
        for (int i = 0; i < 7; i++) {
            c = Calendar.getInstance();
            c.add(Calendar.DAY_OF_MONTH, -i);
            arr[6 - i] = sdf.format(c.getTime());

        }
        return arr;
    }
}
