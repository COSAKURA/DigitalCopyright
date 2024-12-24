package com.digitalcopyright.utils;

import org.apache.commons.lang3.StringUtils;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Sakura
 */
public class DateUtils {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat DATETIME_WITH_MS_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    // 当前月份
    private static final int CURRENT_MONTH = Calendar.getInstance().get(Calendar.MONTH) + 1;

    /**
     * 获得当前日期 yyyy-MM-dd HH:mm:ss
     */
    public static String getCurrentTime() {
        return DATETIME_FORMAT.format(new Date());
    }

    /**
     * 获取系统当前时间戳
     */
    public static String getSystemTime() {
        return String.valueOf(System.currentTimeMillis());
    }

    /**
     * 获取当前日期 yyyy-MM-dd
     */
    public static String getDateByString() {
        return DATE_FORMAT.format(new Date());
    }

    /**
     * 得到两个时间差（单位：毫秒），格式 yyyy-MM-dd HH:mm:ss
     */
    public static long dateSubtraction(String start, String end) {
        try {
            Date startDate = DATETIME_FORMAT.parse(start);
            Date endDate = DATETIME_FORMAT.parse(end);
            return endDate.getTime() - startDate.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 得到两个时间差（单位：毫秒），输入为 Date 类型
     */
    public static long dateTogether(Date start, Date end) {
        return end.getTime() - start.getTime();
    }

    /**
     * 转化 long 值的日期为 yyyy-MM-dd HH:mm:ss.SSS 格式的日期
     */
    public static String transferLongToDate(String millSec) {
        Date date = new Date(Long.parseLong(millSec));
        return DATETIME_WITH_MS_FORMAT.format(date);
    }

    /**
     * 格式化日期，转换为 yyyy-MM-dd HH:mm:ss
     */
    public static String getOkDate(String date) {
        if (StringUtils.isEmpty(date)) {
            return null;
        }
        try {
            Date parsedDate = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.ENGLISH).parse(date);
            return DATETIME_FORMAT.format(parsedDate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取当前日期是一个星期的第几天（星期天返回 0）
     */
    public static int getDayOfWeek() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        return cal.get(Calendar.DAY_OF_WEEK) - 1;
    }

    /**
     * 判断当前时间是否在[startTime, endTime]区间，注意时间格式要一致
     * @param nowTime     当前时间
     * @param dateSection 时间区间   yyyy-MM-dd,yyyy-MM-dd
     */
    public static boolean isEffectiveDate(Date nowTime, String dateSection) {
        try {
            String[] times = dateSection.split(",");
            Date startTime = DATE_FORMAT.parse(times[0]);
            Date endTime = DATE_FORMAT.parse(times[1]);

            if (nowTime.equals(startTime) || nowTime.equals(endTime)) {
                return true;
            }

            Calendar nowCalendar = Calendar.getInstance();
            nowCalendar.setTime(nowTime);

            Calendar startCalendar = Calendar.getInstance();
            startCalendar.setTime(startTime);

            Calendar endCalendar = Calendar.getInstance();
            endCalendar.setTime(endTime);

            return (isSameDay(nowCalendar, startCalendar) || isSameDay(nowCalendar, endCalendar))
                    || (nowCalendar.after(startCalendar) && nowCalendar.before(endCalendar));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 判断两个日期是否是同一天
     */
    public static boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                && cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)
                && cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * 获取当前时间的时间戳
     */
    public static long getTimeByDate(String time) {
        try {
            Date date = DATETIME_FORMAT.parse(time);
            return date.getTime();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 获取当前小时，如：2020-10-3 17
     */
    public static String getCurrentHour() {
        GregorianCalendar calendar = new GregorianCalendar();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        return (hour < 10 ? getDateByString() + " 0" : getDateByString() + " ") + hour;
    }

    /**
     * 获取当前时间一个小时前
     */
    public static String getCurrentHourBefore() {
        GregorianCalendar calendar = new GregorianCalendar();
        int hour = calendar.get(Calendar.HOUR_OF_DAY) - 1;
        return (hour < 0 ? getBeforeDay() + " 23" : getDateByString() + " " + (hour < 10 ? "0" + hour : hour));
    }

    /**
     * 获取当前日期前一天
     */
    public static String getBeforeDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        return DATE_FORMAT.format(calendar.getTime());
    }

    /**
     * 获取最近七天
     */
    public static String getServen() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, -7);
        return DATE_FORMAT.format(c.getTime());
    }

    /**
     * 获取最近一个月
     */
    public static String getOneMonth() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, -1);
        return DATE_FORMAT.format(c.getTime());
    }

    /**
     * 获取最近三个月
     */
    public static String getThreeMonth() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, -3);
        return DATE_FORMAT.format(c.getTime());
    }

    /**
     * 获取最近一年
     */
    public static String getOneYear() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.YEAR, -1);
        return DATE_FORMAT.format(c.getTime());
    }

    /**
     * 获取今年月份数据
     */
    public static List<Integer> getMonthList() {
        List<Integer> list = new ArrayList<>();
        for (int i = 1; i <= CURRENT_MONTH; i++) {
            list.add(i);
        }
        return list;
    }

    /**
     * 获取当前年度季度列表
     */
    public static List<Integer> getQuartList() {
        int quart = CURRENT_MONTH / 3 + 1;
        List<Integer> list = new ArrayList<>();
        for (int i = 1; i <= quart; i++) {
            list.add(i);
        }
        return list;
    }

    public static void main(String[] args) {
        // 测试输出季度列表
        System.out.println(DateUtils.getQuartList());
    }
}
