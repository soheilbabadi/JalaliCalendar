package com.yourcompany.www;

import java.time.*;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

public class Jalali extends Calendar {

    public static final int AD = 1;
    static final int BCE = 0;
    static final int CE = 1;
    private static final int ONE_SECOND = 1000;
    private static final int ONE_MINUTE = 60 * ONE_SECOND;
    private static final int ONE_HOUR = 60 * ONE_MINUTE;
    static final int[] MIN_VALUES = {
            BCE,        // ERA
            100,        // YEAR
            1,    // MONTH
            1,        // WEEK_OF_YEAR
            0,        // WEEK_OF_MONTH
            1,        // DAY_OF_MONTH
            1,        // DAY_OF_YEAR
            7,        // DAY_OF_WEEK
            1,        // DAY_OF_WEEK_IN_MONTH
            AM,        // AM_PM
            0,        // HOUR
            0,        // HOUR_OF_DAY
            0,        // MINUTE
            0,        // SECOND
            0,        // MILLISECOND
            -13 * ONE_HOUR,    // ZONE_OFFSET (UNIX compatibility)
            0        // SUMMER
    };

    static final int[] MAX_VALUES = {
            CE,        // ERA
            3000,    // YEAR
            11,    // MONTH
            53,        // WEEK_OF_YEAR
            6,        // WEEK_OF_MONTH
            31,        // DAY_OF_MONTH
            366,        // DAY_OF_YEAR
            6,    // DAY_OF_WEEK
            6,        // DAY_OF_WEEK_IN
            PM,        // AM_PM
            11,        // HOUR
            23,        // HOUR_OF_DAY
            59,        // MINUTE
            59,        // SECOND
            999,        // MILLISECOND
            14 * ONE_HOUR,    // ZONE_OFFSET
            ONE_HOUR    //  SUMMER
    };
    private static final long ONE_DAY = 24 * ONE_HOUR;
    public static int[] gregorianDaysInMonth = {31, 28, 31, 30, 31,
            30, 31, 31, 30, 31, 30, 31};
    public static int[] jalaliDaysInMonth = {31, 31, 31, 31, 31, 31,
            30, 30, 30, 30, 30, 29};
    private static TimeZone timeZone = TimeZone.getDefault();
    private static boolean isSetTime = false;
    private GregorianCalendar cal;


    public Jalali() {
        this(TimeZone.getDefault(), Locale.getDefault());
    }

    public Jalali(TimeZone zone) {
        this(zone, Locale.getDefault());
    }

    public Jalali(Locale aLocale) {
        this(TimeZone.getDefault(), aLocale);
    }

    public Jalali(TimeZone zone, Locale aLocale) {

        super(zone, aLocale);
        timeZone = zone;
        Calendar calendar = Calendar.getInstance(zone, aLocale);

        YearMonthDay yearMonthDay = new YearMonthDay(calendar.get(YEAR), calendar.get(MONTH), calendar.get(DATE));
        yearMonthDay = getJalali(yearMonthDay);
        set(yearMonthDay.getYear(), yearMonthDay.getMonth(), yearMonthDay.getDay());
        complete();

    }

    public Jalali(int year, int month, int dayOfMonth) {
        this(year, month, dayOfMonth, 0, 0, 0, 0);
    }

    public Jalali(int year, int month, int dayOfMonth, int hourOfDay,
                  int minute) {
        this(year, month, dayOfMonth, hourOfDay, minute, 0, 0);
    }

    public Jalali(int year, int month, int dayOfMonth, int hourOfDay,
                  int minute, int second) {
        this(year, month, dayOfMonth, hourOfDay, minute, second, 0);
    }

    public Jalali(int year, int month, int dayOfMonth,
                  int hourOfDay, int minute, int second, int millis) {
        super();

        this.set(YEAR, year);
        this.set(MONTH, month);
        this.set(DAY_OF_MONTH, dayOfMonth);

        if (hourOfDay >= 12 && hourOfDay <= 23) {

            this.set(AM_PM, PM);
            this.set(HOUR, hourOfDay - 12);
        } else {
            this.set(HOUR, hourOfDay);
            this.set(AM_PM, AM);
        }

        this.set(HOUR_OF_DAY, hourOfDay);
        this.set(MINUTE, minute);
        this.set(SECOND, second);

        this.set(MILLISECOND, millis);

        YearMonthDay yearMonthDay = getGregorian(new YearMonthDay(fields[1], fields[2], fields[5]));
        cal = new GregorianCalendar(yearMonthDay.getYear(), yearMonthDay.getMonth(), yearMonthDay.getDay(), hourOfDay,
                minute, second);
        time = cal.getTimeInMillis();

        isSetTime = true;
    }

    public static YearMonthDay getJalali(YearMonthDay gregorian) {

        if (gregorian.getMonth() > 11 || gregorian.getMonth() < -11) {
            throw new IllegalArgumentException();
        }
        int jalaliYear;
        int jalaliMonth;
        int jalaliDay;

        int gregorianDayNo, jalaliDayNo;
        int jalaliNP;
        int i;

        gregorian.setYear(gregorian.getYear() - 1600);
        gregorian.setDay(gregorian.getDay() - 1);

        gregorianDayNo = 365 * gregorian.getYear() + (int) Math.floor((gregorian.getYear() + 3) / 4)
                - (int) Math.floor((gregorian.getYear() + 99) / 100)
                + (int) Math.floor((gregorian.getYear() + 399) / 400);
        for (i = 0; i < gregorian.getMonth(); ++i) {
            gregorianDayNo += gregorianDaysInMonth[i];
        }

        if (gregorian.getMonth() > 1 && ((gregorian.getYear() % 4 == 0 && gregorian.getYear() % 100 != 0)
                || (gregorian.getYear() % 400 == 0))) {
            ++gregorianDayNo;
        }

        gregorianDayNo += gregorian.getDay();

        jalaliDayNo = gregorianDayNo - 79;

        jalaliNP = (int) Math.floor(jalaliDayNo / 12053);
        jalaliDayNo = jalaliDayNo % 12053;

        jalaliYear = 979 + 33 * jalaliNP + 4 * (jalaliDayNo / 1461);
        jalaliDayNo = jalaliDayNo % 1461;

        if (jalaliDayNo >= 366) {
            jalaliYear += (int) Math.floor((jalaliDayNo - 1) / 365);
            jalaliDayNo = (jalaliDayNo - 1) % 365;
        }

        for (i = 0; i < 11 && jalaliDayNo >= jalaliDaysInMonth[i]; ++i) {
            jalaliDayNo -= jalaliDaysInMonth[i];
        }
        jalaliMonth = i;
        jalaliDay = jalaliDayNo + 1;

        return new YearMonthDay(jalaliYear, jalaliMonth, jalaliDay);
    }

    public static YearMonthDay getJalali(LocalDate gregorian) {
        YearMonthDay yearMonthDay = new YearMonthDay(gregorian.getYear(), gregorian.getMonthValue(), gregorian.getDayOfMonth());
        return getJalali(yearMonthDay);
    }

    public static LocalDate getGregorianDate(YearMonthDay jalali) {
        YearMonthDay yearMonthDay = getGregorian(jalali);
        LocalDate date = LocalDate.of(yearMonthDay.year, yearMonthDay.month, yearMonthDay.day);
        return date;
    }


    public static YearMonthDay getGregorian(YearMonthDay jalali) {
        if (jalali.getMonth() > 11 || jalali.getMonth() < -11) {
            throw new IllegalArgumentException();
        }

        int gregorianYear;
        int gregorianMonth;
        int gregorianDay;

        int gregorianDayNo, jalaliDayNo;
        int leap;

        int i;
        jalali.setYear(jalali.getYear() - 979);
        jalali.setDay(jalali.getDay() - 1);

        jalaliDayNo = 365 * jalali.getYear() + (jalali.getYear() / 33) * 8
                + (int) Math.floor(((jalali.getYear() % 33) + 3) / 4);
        for (i = 0; i < jalali.getMonth(); ++i) {
            jalaliDayNo += jalaliDaysInMonth[i];
        }

        jalaliDayNo += jalali.getDay();

        gregorianDayNo = jalaliDayNo + 79;

        gregorianYear = 1600 + 400 * (int) Math.floor(gregorianDayNo / 146097);
        gregorianDayNo = gregorianDayNo % 146097;

        leap = 1;
        if (gregorianDayNo >= 36525) {
            gregorianDayNo--;
            gregorianYear += 100 * (int) Math.floor(gregorianDayNo / 36524);
            gregorianDayNo = gregorianDayNo % 36524;

            if (gregorianDayNo >= 365) {
                gregorianDayNo++;
            } else {
                leap = 0;
            }
        }

        gregorianYear += 4 * (int) Math.floor(gregorianDayNo / 1461);
        gregorianDayNo = gregorianDayNo % 1461;

        if (gregorianDayNo >= 366) {
            leap = 0;

            gregorianDayNo--;
            gregorianYear += (int) Math.floor(gregorianDayNo / 365);
            gregorianDayNo = gregorianDayNo % 365;
        }

        for (i = 0; gregorianDayNo >= gregorianDaysInMonth[i] + ((i == 1 && leap == 1) ? i : 0); i++) {
            gregorianDayNo -= gregorianDaysInMonth[i] + ((i == 1 && leap == 1) ? i : 0);
        }
        gregorianMonth = i;
        gregorianDay = gregorianDayNo + 1;

        return new YearMonthDay(gregorianYear, gregorianMonth, gregorianDay);

    }

    public static int weekOfYear(int dayOfYear, int year) {
        switch (getDayOfWeek(Jalali.getGregorian(new YearMonthDay(year, 0, 1)))) {
            case 2:
                dayOfYear++;
                break;
            case 3:
                dayOfYear += 2;
                break;
            case 4:
                dayOfYear += 3;
                break;
            case 5:
                dayOfYear += 4;
                break;
            case 6:
                dayOfYear += 5;
                break;
            case 7:
                dayOfYear--;
                break;
        }
        dayOfYear = (int) Math.floor(dayOfYear / 7);
        return dayOfYear + 1;
    }

    public static int getDayOfWeek(YearMonthDay yearMonthDay) {

        Calendar cal = new GregorianCalendar(yearMonthDay.getYear(), yearMonthDay.getMonth(), yearMonthDay.getDay());
        return cal.get(DAY_OF_WEEK);


    }

    public static String getDayOfWeekName(YearMonthDay yearMonthDay) {

        Calendar cal = new GregorianCalendar(yearMonthDay.getYear(), yearMonthDay.getMonth(), yearMonthDay.getDay());
        var dOW = cal.get(DAY_OF_WEEK);
        String result = "";
        switch (dOW) {
            case 7:
                result = "شنبه";
                break;
            case 1:
                result = "یکشنبه";
                break;
            case 2:
                result = "دوشنبه";
                break;
            case 3:
                result = "سه شنبه";
                break;
            case 4:
                result = "چهارشنبه";
                break;
            case 5:
                result = "پنج شنبه";
                break;
            case 6:
                result = "جمعه";
                break;
            default:
                result = "";
                break;

        }
        return result;


    }

    public static boolean isLeapYear(int year) {

        return year % 33 == 1 || year % 33 == 5 || year % 33 == 9 || year % 33 == 13 ||
                year % 33 == 17 || year % 33 == 22 || year % 33 == 26 || year % 33 == 30;
    }

    public  YearMonthDay addDays(YearMonthDay jalaliDate, int valueToAdd) {

        LocalDate date;
        if (valueToAdd > 0) {
            date = getGregorianDate(jalaliDate).plusDays(valueToAdd);
        } else {
            date = getGregorianDate(jalaliDate).minusDays(Math.abs(valueToAdd));
        }
        return getJalali(date);

    }

    public  YearMonthDay addMonths(YearMonthDay jalaliDate, int valueToAdd) {

        LocalDate date;
        if (valueToAdd > 0) {
            date = getGregorianDate(jalaliDate).plusMonths(valueToAdd);
        } else {
            date = getGregorianDate(jalaliDate).minusMonths(Math.abs(valueToAdd));
        }
        return getJalali(date);
    }

    public  YearMonthDay addWeeks(YearMonthDay jalaliDate, int valueToAdd) {

        LocalDate date;
        if (valueToAdd > 0) {
            date = getGregorianDate(jalaliDate).plusWeeks(valueToAdd);
        } else {
            date = getGregorianDate(jalaliDate).minusWeeks(Math.abs(valueToAdd));
        }
        return getJalali(date);
    }


    public  YearMonthDay addYears(YearMonthDay jalaliDate, int valueToAdd) {
        LocalDate date;
        if (valueToAdd > 0) {
            date = getGregorianDate(jalaliDate).plusYears(valueToAdd);
        } else {
            date = getGregorianDate(jalaliDate).minusYears(Math.abs(valueToAdd));
        }
        return getJalali(date);

    }

    public  boolean isAfter(YearMonthDay jalaliDate1, YearMonthDay jalaliDate2) {
        LocalDate date1 = getGregorianDate(jalaliDate1);
        LocalDate date2 = getGregorianDate(jalaliDate2);
        return date1.isAfter(date2);

    }

    public  boolean isBefore(YearMonthDay jalaliDate1, YearMonthDay jalaliDate2) {
        LocalDate date1 = getGregorianDate(jalaliDate1);
        LocalDate date2 = getGregorianDate(jalaliDate2);
        return date1.isBefore(date2);
    }

    public  boolean isEqual(YearMonthDay jalaliDate1, YearMonthDay jalaliDate2) {
        LocalDate date1 = getGregorianDate(jalaliDate1);
        LocalDate date2 = getGregorianDate(jalaliDate2);
        return date1.isEqual(date2);
    }

    public static int lengthOfMonth(YearMonthDay jalaliDate) {
        if (jalaliDate.month < 7) {
            return 31;

        } else if (jalaliDate.month < 12) {
            return 30;
        }
        if (jalaliDate.month == 12 && isLeapYear(jalaliDate.year) == true) {
            return 30;
        } else {
            return 29;
        }


    }

    public static int lengthOfYear(YearMonthDay jalaliDate) {

        if (isLeapYear(jalaliDate.year) == true) return 366;
        return 365;
    }

    public static String toLongString(YearMonthDay jalaliDate) {
        String dOW = getDayOfWeekName(jalaliDate);
        String mN = getMonthName(jalaliDate);
        return dOW + " " + jalaliDate.day + " " + getMonthName(jalaliDate) + " " + jalaliDate.year;
    }

    public static String toString(YearMonthDay jalaliDate) {
        return jalaliDate.year + "/" + jalaliDate.month + "/" + jalaliDate.day;
    }

    public  long dateDiff(YearMonthDay firstDate, YearMonthDay secondDate) {
        LocalDate geo1 = getGregorianDate(firstDate);
        LocalDate geo2 = getGregorianDate(secondDate);
        return Duration.between(geo1, geo2).toDays();
    }

    public static Date getDate(String geoDate) {
        LocalDate parse;
        try {
            parse = LocalDate.parse(geoDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S"));
        } catch (Exception e) {
            parse = LocalDate.parse(geoDate, DateTimeFormatter.ofPattern( "E MMM dd HH:mm:ss z uuuu" ));
        }
       return Date.from(parse.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }


    public static LocalDate getLocalDate(String geoDate) {
        LocalDate parse;
        try {
            parse = LocalDate.parse(geoDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S"));
        } catch (Exception e) {
            parse = LocalDate.parse(geoDate, DateTimeFormatter.ofPattern( "E MMM dd HH:mm:ss z uuuu" ));
        }
        return LocalDate.from(parse.atStartOfDay(ZoneId.systemDefault()).toInstant());

    }
    public static String getMonthName(YearMonthDay jalaliDate) {
        String result = "";
        switch (jalaliDate.month) {
            case 1:
                result = "فروردین";
                break;
            case 2:
                result = "اردیبهشت";
                break;
            case 3:
                result = "خرداد";
                break;
            case 4:
                result = "تیر";
                break;
            case 5:
                result = "مرداد";
                break;
            case 6:
                result = "شهریور";
                break;
            case 7:
                result = "مهر";
                break;
            case 8:
                result = "آبان";
                break;
            case 9:
                result = "آذر";
                break;
            case 10:
                result = "دی";
                break;
            case 11:
                result = "بهمن";
                break;
            case 12:
                result = "اسفند";
                break;
            default:
                result = "";
                break;

        }
        return result;

    }

    @Override
    protected void computeTime() {

        if (!isTimeSet && !isSetTime) {
            Calendar cal = GregorianCalendar.getInstance(timeZone);
            if (!isSet(HOUR_OF_DAY)) {
                super.set(HOUR_OF_DAY, cal.get(HOUR_OF_DAY));
            }
            if (!isSet(HOUR)) {
                super.set(HOUR, cal.get(HOUR));
            }
            if (!isSet(MINUTE)) {
                super.set(MINUTE, cal.get(MINUTE));
            }
            if (!isSet(SECOND)) {
                super.set(SECOND, cal.get(SECOND));
            }
            if (!isSet(MILLISECOND)) {
                super.set(MILLISECOND, cal.get(MILLISECOND));
            }
            if (!isSet(ZONE_OFFSET)) {
                super.set(ZONE_OFFSET, cal.get(ZONE_OFFSET));
            }
            if (!isSet(DST_OFFSET)) {
                super.set(DST_OFFSET, cal.get(DST_OFFSET));
            }
            if (!isSet(AM_PM)) {
                super.set(AM_PM, cal.get(AM_PM));
            }

            if (internalGet(HOUR_OF_DAY) >= 12 && internalGet(HOUR_OF_DAY) <= 23) {
                super.set(AM_PM, PM);
                super.set(HOUR, internalGet(HOUR_OF_DAY) - 12);
            } else {
                super.set(HOUR, internalGet(HOUR_OF_DAY));
                super.set(AM_PM, AM);
            }

            YearMonthDay yearMonthDay = getGregorian(new YearMonthDay(internalGet(YEAR), internalGet(MONTH), internalGet(DAY_OF_MONTH)));
            cal.set(yearMonthDay.getYear(), yearMonthDay.getMonth(), yearMonthDay.getDay()
                    , internalGet(HOUR_OF_DAY), internalGet(MINUTE), internalGet(SECOND));
            time = cal.getTimeInMillis();

        } else if (!isTimeSet && isSetTime) {
            if (internalGet(HOUR_OF_DAY) >= 12 && internalGet(HOUR_OF_DAY) <= 23) {
                super.set(AM_PM, PM);
                super.set(HOUR, internalGet(HOUR_OF_DAY) - 12);
            } else {
                super.set(HOUR, internalGet(HOUR_OF_DAY));
                super.set(AM_PM, AM);
            }
            cal = new GregorianCalendar();
            super.set(ZONE_OFFSET, timeZone.getRawOffset());
            super.set(DST_OFFSET, timeZone.getDSTSavings());
            YearMonthDay yearMonthDay = getGregorian(new YearMonthDay(internalGet(YEAR), internalGet(MONTH), internalGet(DAY_OF_MONTH)));
            cal.set(yearMonthDay.getYear(), yearMonthDay.getMonth(), yearMonthDay.getDay(), internalGet(HOUR_OF_DAY),
                    internalGet(MINUTE), internalGet(SECOND));
            time = cal.getTimeInMillis();
        }
    }

    public void set(int field, int value) {
        switch (field) {
            case DATE: {
                super.set(field, 0);
                add(field, value);
                break;
            }
            case MONTH: {
                if (value > 11) {
                    super.set(field, 11);
                    add(field, value - 11);
                } else if (value < 0) {
                    super.set(field, 0);
                    add(field, value);
                } else {
                    super.set(field, value);
                }
                break;
            }
            case DAY_OF_YEAR: {
                if (isSet(YEAR) && isSet(MONTH) && isSet(DAY_OF_MONTH)) {
                    super.set(YEAR, internalGet(YEAR));
                    super.set(MONTH, 0);
                    super.set(DATE, 0);
                    add(field, value);
                } else {
                    super.set(field, value);
                }
                break;
            }
            case WEEK_OF_YEAR: {
                if (isSet(YEAR) && isSet(MONTH) && isSet(DAY_OF_MONTH)) {
                    add(field, value - get(WEEK_OF_YEAR));
                } else {
                    super.set(field, value);
                }
                break;
            }
            case WEEK_OF_MONTH: {
                if (isSet(YEAR) && isSet(MONTH) && isSet(DAY_OF_MONTH)) {
                    add(field, value - get(WEEK_OF_MONTH));
                } else {
                    super.set(field, value);
                }
                break;
            }
            case DAY_OF_WEEK: {
                if (isSet(YEAR) && isSet(MONTH) && isSet(DAY_OF_MONTH)) {
                    add(DAY_OF_WEEK, value % 7 - get(DAY_OF_WEEK));
                } else {
                    super.set(field, value);
                }
                break;
            }
            case HOUR_OF_DAY:
            case HOUR:
            case MINUTE:
            case SECOND:
            case MILLISECOND:
            case ZONE_OFFSET:
            case DST_OFFSET: {
                if (isSet(YEAR) && isSet(MONTH) && isSet(DATE) && isSet(HOUR) && isSet(HOUR_OF_DAY) &&
                        isSet(MINUTE) && isSet(SECOND) && isSet(MILLISECOND)) {
                    cal = new GregorianCalendar();
                    YearMonthDay yearMonthDay = getGregorian(new YearMonthDay(internalGet(YEAR), internalGet(MONTH), internalGet(DATE)));
                    cal.set(yearMonthDay.getYear(), yearMonthDay.getMonth(), yearMonthDay.getDay(), internalGet(HOUR_OF_DAY), internalGet(MINUTE),
                            internalGet(SECOND));
                    cal.set(field, value);
                    yearMonthDay = getJalali(new YearMonthDay(cal.get(YEAR), cal.get(MONTH), cal.get(DATE)));
                    super.set(YEAR, yearMonthDay.getYear());
                    super.set(MONTH, yearMonthDay.getMonth());
                    super.set(DATE, yearMonthDay.getDay());
                    super.set(HOUR_OF_DAY, cal.get(HOUR_OF_DAY));
                    super.set(MINUTE, cal.get(MINUTE));
                    super.set(SECOND, cal.get(SECOND));

                } else {
                    super.set(field, value);
                }
                break;
            }


            default: {
                super.set(field, value);
            }
        }
    }

    @Override
    protected void computeFields() {
        boolean temp = isTimeSet;
        if (!areFieldsSet) {
            setMinimalDaysInFirstWeek(1);
            setFirstDayOfWeek(7);

            //Day_Of_Year
            int dayOfYear = 0;
            int index = 0;

            while (index < fields[2]) {
                dayOfYear += jalaliDaysInMonth[index++];
            }
            dayOfYear += fields[5];
            super.set(DAY_OF_YEAR, dayOfYear);
            //***

            //Day_of_Week
            super.set(DAY_OF_WEEK, getDayOfWeek(getGregorian(new YearMonthDay(fields[1], fields[2], fields[5]))));
            //***

            //Day_Of_Week_In_Month
            if (0 < fields[5] && fields[5] < 8) {
                super.set(DAY_OF_WEEK_IN_MONTH, 1);
            }

            if (7 < fields[5] && fields[5] < 15) {
                super.set(DAY_OF_WEEK_IN_MONTH, 2);
            }

            if (14 < fields[5] && fields[5] < 22) {
                super.set(DAY_OF_WEEK_IN_MONTH, 3);
            }

            if (21 < fields[5] && fields[5] < 29) {
                super.set(DAY_OF_WEEK_IN_MONTH, 4);
            }

            if (28 < fields[5] && fields[5] < 32) {
                super.set(DAY_OF_WEEK_IN_MONTH, 5);
            }
            //***

            //Week_Of_Year
            super.set(WEEK_OF_YEAR, weekOfYear(fields[6], fields[1]));
            //***

            //Week_Of_Month
            super.set(WEEK_OF_MONTH, weekOfYear(fields[6], fields[1]) - weekOfYear(fields[6] - fields[5], fields[1]) + 1);
            //

            isTimeSet = temp;
        }
    }

    @Override
    public void add(int field, int amount) {

        if (field == MONTH) {
            amount += get(MONTH);
            add(YEAR, amount / 12);
            super.set(MONTH, amount % 12);
            if (get(DAY_OF_MONTH) > jalaliDaysInMonth[amount % 12]) {
                super.set(DAY_OF_MONTH, jalaliDaysInMonth[amount % 12]);
                if (get(MONTH) == 11 && isLeapYear(get(YEAR))) {
                    super.set(DAY_OF_MONTH, 30);
                }
            }
            complete();

        } else if (field == YEAR) {

            super.set(YEAR, get(YEAR) + amount);
            if (get(DAY_OF_MONTH) == 30 && get(MONTH) == 11 && !isLeapYear(get(YEAR))) {
                super.set(DAY_OF_MONTH, 29);
            }

            complete();
        } else {
            YearMonthDay yearMonthDay = getGregorian(new YearMonthDay(get(YEAR), get(MONTH), get(DATE)));
            Calendar gc = new GregorianCalendar(yearMonthDay.getYear(), yearMonthDay.getMonth(), yearMonthDay.getDay(),
                    get(HOUR_OF_DAY), get(MINUTE), get(SECOND));
            gc.add(field, amount);
            yearMonthDay = getJalali(new YearMonthDay(gc.get(YEAR), gc.get(MONTH), gc.get(DATE)));
            super.set(YEAR, yearMonthDay.getYear());
            super.set(MONTH, yearMonthDay.getMonth());
            super.set(DATE, yearMonthDay.getDay());
            super.set(HOUR_OF_DAY, gc.get(HOUR_OF_DAY));
            super.set(MINUTE, gc.get(MINUTE));
            super.set(SECOND, gc.get(SECOND));
            complete();
        }

    }

    @Override
    public void roll(int field, boolean up) {
        roll(field, up ? +1 : -1);
    }

    @Override
    public void roll(int field, int amount) {
        if (amount == 0) {
            return;
        }

        if (field < 0 || field >= ZONE_OFFSET) {
            throw new IllegalArgumentException();
        }

        complete();

        switch (field) {
            case AM_PM: {
                if (amount % 2 != 0) {
                    if (internalGet(AM_PM) == AM) {
                        fields[AM_PM] = PM;
                    } else {
                        fields[AM_PM] = AM;
                    }
                    if (get(AM_PM) == AM) {
                        super.set(HOUR_OF_DAY, get(HOUR));
                    } else {
                        super.set(HOUR_OF_DAY, get(HOUR) + 12);
                    }
                }
                break;
            }
            case YEAR: {
                super.set(YEAR, internalGet(YEAR) + amount);
                if (internalGet(MONTH) == 11 && internalGet(DAY_OF_MONTH) == 30 && !isLeapYear(internalGet(YEAR))) {
                    super.set(DAY_OF_MONTH, 29);
                }
                break;
            }
            case MINUTE: {
                int unit = 60;
                int m = (internalGet(MINUTE) + amount) % unit;
                if (m < 0) {
                    m += unit;
                }
                super.set(MINUTE, m);
                break;
            }
            case SECOND: {
                int unit = 60;
                int s = (internalGet(SECOND) + amount) % unit;
                if (s < 0) {
                    s += unit;
                }
                super.set(SECOND, s);
                break;
            }
            case MILLISECOND: {
                int unit = 1000;
                int ms = (internalGet(MILLISECOND) + amount) % unit;
                if (ms < 0) {
                    ms += unit;
                }
                super.set(MILLISECOND, ms);
                break;
            }

            case HOUR: {
                super.set(HOUR, (internalGet(HOUR) + amount) % 12);
                if (internalGet(HOUR) < 0) {
                    fields[HOUR] += 12;
                }
                if (internalGet(AM_PM) == AM) {
                    super.set(HOUR_OF_DAY, internalGet(HOUR));
                } else {
                    super.set(HOUR_OF_DAY, internalGet(HOUR) + 12);
                }

                break;
            }
            case HOUR_OF_DAY: {
                fields[HOUR_OF_DAY] = (internalGet(HOUR_OF_DAY) + amount) % 24;
                if (internalGet(HOUR_OF_DAY) < 0) {
                    fields[HOUR_OF_DAY] += 24;
                }
                if (internalGet(HOUR_OF_DAY) < 12) {
                    fields[AM_PM] = AM;
                    fields[HOUR] = internalGet(HOUR_OF_DAY);
                } else {
                    fields[AM_PM] = PM;
                    fields[HOUR] = internalGet(HOUR_OF_DAY) - 12;
                }

            }
            case MONTH: {
                int mon = (internalGet(MONTH) + amount) % 12;
                if (mon < 0) {
                    mon += 12;
                }
                super.set(MONTH, mon);

                int monthLen = jalaliDaysInMonth[mon];
                if (internalGet(MONTH) == 11 && isLeapYear(internalGet(YEAR))) {
                    monthLen = 30;
                }
                if (internalGet(DAY_OF_MONTH) > monthLen) {
                    super.set(DAY_OF_MONTH, monthLen);
                }
                break;
            }
            case DAY_OF_MONTH: {
                int unit = 0;
                if (0 <= get(MONTH) && get(MONTH) <= 5) {
                    unit = 31;
                }
                if (6 <= get(MONTH) && get(MONTH) <= 10) {
                    unit = 30;
                }
                if (get(MONTH) == 11) {
                    if (isLeapYear(get(YEAR))) {
                        unit = 30;
                    } else {
                        unit = 29;
                    }
                }
                int d = (get(DAY_OF_MONTH) + amount) % unit;
                if (d < 0) {
                    d += unit;
                }
                super.set(DAY_OF_MONTH, d);
                break;

            }
            case WEEK_OF_YEAR: {
                break;
            }
            case DAY_OF_YEAR: {
                int unit = (isLeapYear(internalGet(YEAR)) ? 366 : 365);
                int dayOfYear = (internalGet(DAY_OF_YEAR) + amount) % unit;
                dayOfYear = (dayOfYear > 0) ? dayOfYear : dayOfYear + unit;
                int month = 0, temp = 0;
                while (dayOfYear > temp) {
                    temp += jalaliDaysInMonth[month++];
                }
                super.set(MONTH, --month);
                super.set(DAY_OF_MONTH, jalaliDaysInMonth[internalGet(MONTH)] - (temp - dayOfYear));
                break;
            }
            case DAY_OF_WEEK: {
                int index = amount % 7;
                if (index < 0) {
                    index += 7;
                }
                int i = 0;
                while (i != index) {
                    if (internalGet(DAY_OF_WEEK) == FRIDAY) {
                        add(DAY_OF_MONTH, -6);
                    } else {
                        add(DAY_OF_MONTH, +1);
                    }
                    i++;
                }
                break;
            }

            default:
                throw new IllegalArgumentException();
        }

    }

    @Override
    public int getMinimum(int field) {
        return MIN_VALUES[field];
    }

    @Override
    public int getMaximum(int field) {
        return MAX_VALUES[field];
    }

    @Override
    public int getGreatestMinimum(int field) {
        return MIN_VALUES[field];
    }

    @Override
    public int getLeastMaximum(int field) {
        return MAX_VALUES[field];
    }


    public static class YearMonthDay {

        private int year;
        private int month;
        private int day;
        public YearMonthDay(int year, int month, int day) {
            this.year = year;
            this.month = month;
            this.day = day;
        }
        public YearMonthDay(String jalaliDate){
            Pattern pattern = Pattern.compile("/");
            String[] result = pattern.split(jalaliDate);
            this.year=Integer.parseInt(result[0]);
            this.month=Integer.parseInt(result[1]);
            this.day =Integer.parseInt(result[2]);

        }

        public int getYear() {
            return year;
        }

        public void setYear(int year) {
            this.year = year;
        }

        public int getMonth() {
            return month;
        }

        public void setMonth(int month) {
            this.month = month;
        }

        public int getDay() {
            return day;
        }

        public void setDay(int day) {
            this.day = day;
        }

        public String toString() {
            return getYear() + "/" + getMonth() + "/" + getDay();
        }
    }
}