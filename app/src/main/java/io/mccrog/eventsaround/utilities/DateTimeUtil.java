package io.mccrog.eventsaround.utilities;

import java.text.DateFormatSymbols;
import java.util.Calendar;

public class DateTimeUtil {

    private DateTimeUtil() {
    }

    public static String getFormattedDateString(int year, int month, int day) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getMonth(month))
                .append(" ").append(day);

        if (!isCurrentYear(year))
            stringBuilder.append(", ").append(year);

        return stringBuilder.toString();
    }

    public static String getFormattedTimeString(int hour, int minute) {
        StringBuilder stringBuilder = new StringBuilder();

        if (hour < 10) {
            stringBuilder.append(0);
        }

        stringBuilder.append(hour).append(" : ");

        if (minute < 10) {
            stringBuilder.append(0);
        }

        stringBuilder.append(minute);
        return stringBuilder.toString();
    }

    private static String getMonth(int month) {
        return new DateFormatSymbols().getMonths()[month];
    }

    public static String getFormattedDateTimeResourcesString(String dateTimeResourcesString, long dateTimeInMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dateTimeInMillis);

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        String date = getFormattedDateString(year, month, day);
        String time = getFormattedTimeString(hour, minute);

        return String.format(dateTimeResourcesString, date, time);
    }

    public static long getCurrentDateTimeInMillis() {
        return Calendar.getInstance().getTimeInMillis();
    }

    private static boolean isCurrentYear(int year) {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        return currentYear == year;
    }

}
