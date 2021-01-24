package me.cqp.JRbot.Utils.misc;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {
    public static String getDateStr(int daysdelta){
        Date d = new Date();
        Instant t = d.toInstant();
        t.plus(daysdelta, ChronoUnit.DAYS);
        d = Date.from(t);
        return new SimpleDateFormat("yyyy-MM-dd").format(d);
    }

    public static String getDateStr(int year,int month,int day){
        Calendar c = Calendar.getInstance();
        c.set(year, month, day);
        Date d = c.getTime();
        return new SimpleDateFormat("yyyy-MM-dd").format(d);
    }

    public static String getDateStr() {
        Date current = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(current);
    }

    public static String getCurrentTime() {
        Date current = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(current);
    }

    public static String formatTime(long seconds) {
        long days = seconds / (24 * 3600);
        long year1 = days / 365;
        long day1 = days % 365;
        long hour1 = seconds % (24 * 3600) / 3600;
        long minute1 = seconds % 3600 / 60;
        long second1 = seconds % 60;


        ArrayList<String> list = new ArrayList<String>() {{
            add(year1 + "年");
            add(day1 + "天");
            add(hour1 + "小时");
            add(minute1 + "分钟");
            add(second1 + "秒");
        }};

        for (int i = 0; i < list.size(); i++) {
            String c = list.get(i);
            String repl = c.replaceAll("年|天|小时|分钟|秒","");
            if (Long.parseUnsignedLong(repl) == 0) {
                list.remove(c);
                i--;
            }
        }
        return String.join("", list);
    }
}
