package com.yourcompany.www;

import java.io.Serial;
import java.time.LocalDate;
import java.util.*;

public class Jalali extends Calendar {

	// Constants
	public static final int AD = 1;
	static final int BCE = 0;
	static final int CE = 1;
	private static final int ONE_SECOND = 1000;
	private static final int ONE_MINUTE = 60 * ONE_SECOND;
	private static final int ONE_HOUR = 60 * ONE_MINUTE;
	private static final long ONE_DAY = 24 * ONE_HOUR;

	static final int[] MIN_VALUES = {
			BCE,        // ERA
			100,        // YEAR
			1,          // MONTH
			1,          // WEEK_OF_YEAR
			0,          // WEEK_OF_MONTH
			1,          // DAY_OF_MONTH
			1,          // DAY_OF_YEAR
			7,          // DAY_OF_WEEK
			1,          // DAY_OF_WEEK_IN_MONTH
			AM,         // AM_PM
			0,          // HOUR
			0,          // HOUR_OF_DAY
			0,          // MINUTE
			0,          // SECOND
			0,          // MILLISECOND
			-13 * ONE_HOUR, // ZONE_OFFSET (UNIX compatibility)
			0           // DST_OFFSET
	};

	static final int[] MAX_VALUES = {
			CE,         // ERA
			3000,       // YEAR
			11,         // MONTH
			53,         // WEEK_OF_YEAR
			6,          // WEEK_OF_MONTH
			31,         // DAY_OF_MONTH
			366,        // DAY_OF_YEAR
			6,          // DAY_OF_WEEK
			6,          // DAY_OF_WEEK_IN_MONTH
			PM,         // AM_PM
			11,         // HOUR
			23,         // HOUR_OF_DAY
			59,         // MINUTE
			59,         // SECOND
			999,        // MILLISECOND
			14 * ONE_HOUR, // ZONE_OFFSET
			ONE_HOUR    // DST_OFFSET
	};

	private static final int[] GREGORIAN_DAYS_IN_MONTH =
			{31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
	private static final int[] JALALI_DAYS_IN_MONTH =
			{31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29};

	private static final String[] MONTH_NAMES = {
			"فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
			"مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
	};

	private static final String[] WEEK_DAY_NAMES = {
			"شنبه", "یکشنبه", "دوشنبه", "سه شنبه", "چهارشنبه", "پنج شنبه", "جمعه"
	};
	@Serial
	private static final long serialVersionUID = -570906057422128282L;

	private TimeZone timeZone;
	private boolean isSetTime = false;
	private GregorianCalendar gregorianCalendar;

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
		this.timeZone = zone;
		Calendar calendar = Calendar.getInstance(zone, aLocale);
		YearMonthDay yearMonthDay = toJalali(
				new YearMonthDay(calendar.get(YEAR), calendar.get(MONTH), calendar.get(DATE))
		);
		set(yearMonthDay.getYear(), yearMonthDay.getMonth(), yearMonthDay.getDay());
		complete();
	}

	public Jalali(int year, int month, int dayOfMonth) {
		this(year, month, dayOfMonth, 0, 0, 0, 0);
	}

	public Jalali(int year, int month, int dayOfMonth, int hourOfDay, int minute) {
		this(year, month, dayOfMonth, hourOfDay, minute, 0, 0);
	}

	public Jalali(int year, int month, int dayOfMonth, int hourOfDay, int minute, int second) {
		this(year, month, dayOfMonth, hourOfDay, minute, second, 0);
	}

	public Jalali(int year, int month, int dayOfMonth, int hourOfDay,
	              int minute, int second, int millis) {
		super();
		set(YEAR, year);
		set(MONTH, month);
		set(DAY_OF_MONTH, dayOfMonth);

		if (hourOfDay >= 12 && hourOfDay <= 23) {
			set(AM_PM, PM);
			set(HOUR, hourOfDay - 12);
		} else {
			set(HOUR, hourOfDay);
			set(AM_PM, AM);
		}

		set(HOUR_OF_DAY, hourOfDay);
		set(MINUTE, minute);
		set(SECOND, second);
		set(MILLISECOND, millis);

		YearMonthDay gregorianDate = toGregorian(
				new YearMonthDay(fields[1], fields[2], fields[5])
		);
		gregorianCalendar = new GregorianCalendar(
				gregorianDate.getYear(), gregorianDate.getMonth(), gregorianDate.getDay(),
				hourOfDay, minute, second
		);
		time = gregorianCalendar.getTimeInMillis();
		isSetTime = true;
	}


	public static YearMonthDay toJalali(YearMonthDay gregorian) {
		validateMonth(gregorian.getMonth());

		int jalaliYear, jalaliMonth, jalaliDay;
		int gregorianDayNo, jalaliDayNo;
		int jalaliNP;
		int i;

		gregorian.setYear(gregorian.getYear() - 1600);
		gregorian.setDay(gregorian.getDay() - 1);

		gregorianDayNo = 365 * gregorian.getYear() + (int) Math.floor((gregorian.getYear() + 3) / 4)
				- (int) Math.floor((gregorian.getYear() + 99) / 100)
				+ (int) Math.floor((gregorian.getYear() + 399) / 400);

		for (i = 0; i < gregorian.getMonth(); ++i) {
			gregorianDayNo += GREGORIAN_DAYS_IN_MONTH[i];
		}

		if (gregorian.getMonth() > 1 && isGregorianLeapYear(gregorian.getYear())) {
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

		for (i = 0; i < 11 && jalaliDayNo >= JALALI_DAYS_IN_MONTH[i]; ++i) {
			jalaliDayNo -= JALALI_DAYS_IN_MONTH[i];
		}

		jalaliMonth = i;
		jalaliDay = jalaliDayNo + 1;

		return new YearMonthDay(jalaliYear, jalaliMonth, jalaliDay);
	}

	public static YearMonthDay toGregorian(YearMonthDay jalali) {
		validateMonth(jalali.getMonth());

		int gregorianYear, gregorianMonth, gregorianDay;
		int gregorianDayNo, jalaliDayNo;
		int leap;
		int i;

		jalali.setYear(jalali.getYear() - 979);
		jalali.setDay(jalali.getDay() - 1);

		jalaliDayNo = 365 * jalali.getYear() + (jalali.getYear() / 33) * 8
				+ (int) Math.floor(((jalali.getYear() % 33) + 3) / 4);
		for (i = 0; i < jalali.getMonth(); ++i) {
			jalaliDayNo += JALALI_DAYS_IN_MONTH[i];
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

		for (i = 0; gregorianDayNo >= GREGORIAN_DAYS_IN_MONTH[i] + ((i == 1 && leap == 1) ? 1 : 0); i++) {
			gregorianDayNo -= GREGORIAN_DAYS_IN_MONTH[i] + ((i == 1 && leap == 1) ? 1 : 0);
		}

		gregorianMonth = i;
		gregorianDay = gregorianDayNo + 1;

		return new YearMonthDay(gregorianYear, gregorianMonth, gregorianDay);
	}


	public static YearMonthDay toJalali(LocalDate gregorian) {
		return toJalali(new YearMonthDay(gregorian.getYear(), gregorian.getMonthValue() - 1, gregorian.getDayOfMonth()));
	}

	public static LocalDate toGregorianDate(YearMonthDay jalali) {
		YearMonthDay gregorian = toGregorian(jalali);
		return LocalDate.of(gregorian.year, gregorian.month + 1, gregorian.day);
	}

	public static YearMonthDay parseJalali(String jalaliDate) {
		String[] parts = jalaliDate.split("/");
		if (parts.length != 3) {
			throw new IllegalArgumentException("Invalid Jalali date format. Expected yyyy/mm/dd");
		}
		return new YearMonthDay(
				Integer.parseInt(parts[0]),
				Integer.parseInt(parts[1]) - 1,
				Integer.parseInt(parts[2])
		);
	}

	public static YearMonthDay fromDate(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return toJalali(new YearMonthDay(
				cal.get(Calendar.YEAR),
				cal.get(Calendar.MONTH),
				cal.get(Calendar.DAY_OF_MONTH))
		);
	}

	public static YearMonthDay fromLocalDate(LocalDate date) {
		return toJalali(date);
	}

	public YearMonthDay addDays(YearMonthDay jalaliDate, int daysToAdd) {
		LocalDate date = toGregorianDate(jalaliDate);
		date = daysToAdd > 0 ? date.plusDays(daysToAdd) : date.minusDays(Math.abs(daysToAdd));
		return toJalali(date);
	}

	public YearMonthDay addMonths(YearMonthDay jalaliDate, int monthsToAdd) {
		LocalDate date = toGregorianDate(jalaliDate);
		date = monthsToAdd > 0 ? date.plusMonths(monthsToAdd) : date.minusMonths(Math.abs(monthsToAdd));
		return toJalali(date);
	}

	public YearMonthDay addWeeks(YearMonthDay jalaliDate, int weeksToAdd) {
		LocalDate date = toGregorianDate(jalaliDate);
		date = weeksToAdd > 0 ? date.plusWeeks(weeksToAdd) : date.minusWeeks(Math.abs(weeksToAdd));
		return toJalali(date);
	}

	public YearMonthDay addYears(YearMonthDay jalaliDate, int yearsToAdd) {
		LocalDate date = toGregorianDate(jalaliDate);
		date = yearsToAdd > 0 ? date.plusYears(yearsToAdd) : date.minusYears(Math.abs(yearsToAdd));
		return toJalali(date);
	}

	public boolean isAfter(YearMonthDay jalaliDate1, YearMonthDay jalaliDate2) {
		return toGregorianDate(jalaliDate1).isAfter(toGregorianDate(jalaliDate2));
	}

	public boolean isBefore(YearMonthDay jalaliDate1, YearMonthDay jalaliDate2) {
		return toGregorianDate(jalaliDate1).isBefore(toGregorianDate(jalaliDate2));
	}

	public boolean isEqual(YearMonthDay jalaliDate1, YearMonthDay jalaliDate2) {
		return toGregorianDate(jalaliDate1).isEqual(toGregorianDate(jalaliDate2));
	}

	public static int lengthOfMonth(YearMonthDay jalaliDate) {
		if (jalaliDate.month < 6) {
			return 31;
		} else if (jalaliDate.month < 11) {
			return 30;
		}
		return isLeapYear(jalaliDate.year) ? 30 : 29;
	}

	public static int lengthOfYear(YearMonthDay jalaliDate) {
		return isLeapYear(jalaliDate.year) ? 366 : 365;
	}

	public static boolean isLeapYear(int year) {
		int remainder = year % 33;
		return remainder == 1 || remainder == 5 || remainder == 9 || remainder == 13 ||
				remainder == 17 || remainder == 22 || remainder == 26 || remainder == 30;
	}

	private static boolean isGregorianLeapYear(int year) {
		return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
	}

	public static String toLongString(YearMonthDay jalaliDate) {
		return String.format("%s %d %s %d",
				getDayOfWeekName(jalaliDate),
				jalaliDate.day,
				getMonthName(jalaliDate),
				jalaliDate.year
		);
	}

	public static String getDayOfWeekName(YearMonthDay jalaliDate) {
		int dayOfWeek = getDayOfWeek(jalaliDate);
		return WEEK_DAY_NAMES[dayOfWeek - 1];
	}

	public static String getMonthName(YearMonthDay jalaliDate) {
		return MONTH_NAMES[jalaliDate.month];
	}

	public static int getDayOfWeek(YearMonthDay jalaliDate) {
		YearMonthDay gregorian = toGregorian(jalaliDate);
		Calendar cal = new GregorianCalendar(gregorian.getYear(), gregorian.getMonth(), gregorian.getDay());
		return cal.get(Calendar.DAY_OF_WEEK);
	}

	public static int weekOfYear(int dayOfYear, int year) {
		YearMonthDay firstDay = new YearMonthDay(year, 0, 1);
		int firstDayOfWeek = getDayOfWeek(firstDay);

		dayOfYear += (firstDayOfWeek - 2) % 7;
		return (dayOfYear / 7) + 1;
	}

	// Calendar overrides
	@Override
	protected void computeTime() {
		if (!isTimeSet && !isSetTime) {
			Calendar cal = GregorianCalendar.getInstance(timeZone);

			setDefaultFieldsIfNotSet(cal);

			if (internalGet(HOUR_OF_DAY) >= 12 && internalGet(HOUR_OF_DAY) <= 23) {
				set(AM_PM, PM);
				set(HOUR, internalGet(HOUR_OF_DAY) - 12);
			} else {
				set(HOUR, internalGet(HOUR_OF_DAY));
				set(AM_PM, AM);
			}

			YearMonthDay gregorian = toGregorian(
					new YearMonthDay(internalGet(YEAR), internalGet(MONTH), internalGet(DAY_OF_MONTH))
			);
			gregorianCalendar = new GregorianCalendar(
					gregorian.getYear(), gregorian.getMonth(), gregorian.getDay(),
					internalGet(HOUR_OF_DAY), internalGet(MINUTE), internalGet(SECOND)
			);
			time = gregorianCalendar.getTimeInMillis();
		} else if (!isTimeSet && isSetTime) {
			if (internalGet(HOUR_OF_DAY) >= 12 && internalGet(HOUR_OF_DAY) <= 23) {
				set(AM_PM, PM);
				set(HOUR, internalGet(HOUR_OF_DAY) - 12);
			} else {
				set(HOUR, internalGet(HOUR_OF_DAY));
				set(AM_PM, AM);
			}

			gregorianCalendar = new GregorianCalendar();
			set(ZONE_OFFSET, timeZone.getRawOffset());
			set(DST_OFFSET, timeZone.getDSTSavings());

			YearMonthDay gregorian = toGregorian(
					new YearMonthDay(internalGet(YEAR), internalGet(MONTH), internalGet(DAY_OF_MONTH))
			);
			gregorianCalendar.set(
					gregorian.getYear(), gregorian.getMonth(), gregorian.getDay(),
					internalGet(HOUR_OF_DAY), internalGet(MINUTE), internalGet(SECOND)
			);
			time = gregorianCalendar.getTimeInMillis();
		}
	}

	@Override
	protected void computeFields() {
		boolean temp = isTimeSet;
		if (!areFieldsSet) {
			setMinimalDaysInFirstWeek(1);
			setFirstDayOfWeek(7);

			int dayOfYear = 0;
			for (int i = 0; i < fields[2]; i++) {
				dayOfYear += JALALI_DAYS_IN_MONTH[i];
			}
			dayOfYear += fields[5];
			set(DAY_OF_YEAR, dayOfYear);

			set(DAY_OF_WEEK, getDayOfWeek(
					new YearMonthDay(fields[1], fields[2], fields[5]))
			);

			set(DAY_OF_WEEK_IN_MONTH, (fields[5] - 1) / 7 + 1);

			set(WEEK_OF_YEAR, weekOfYear(fields[6], fields[1]));

			set(WEEK_OF_MONTH, weekOfYear(fields[6], fields[1]) -
					weekOfYear(fields[6] - fields[5], fields[1]) + 1);

			isTimeSet = temp;
		}
	}

	private void setDefaultFieldsIfNotSet(Calendar cal) {
		int[] fieldsToCheck = {HOUR_OF_DAY, HOUR, MINUTE, SECOND, MILLISECOND,
				ZONE_OFFSET, DST_OFFSET, AM_PM};
		int[] defaultValues = {
				cal.get(HOUR_OF_DAY), cal.get(HOUR), cal.get(MINUTE),
				cal.get(SECOND), cal.get(MILLISECOND), cal.get(ZONE_OFFSET),
				cal.get(DST_OFFSET), cal.get(AM_PM)
		};

		for (int i = 0; i < fieldsToCheck.length; i++) {
			if (!isSet(fieldsToCheck[i])) {
				set(fieldsToCheck[i], defaultValues[i]);
			}
		}
	}

	private static void validateMonth(int month) {
		if (month > 11 || month < -11) {
			throw new IllegalArgumentException("Month must be between 0 and 11");
		}
	}

	public static class YearMonthDay {
		private int year;
		private int month; // 0-based
		private int day;

		public YearMonthDay(int year, int month, int day) {
			this.year = year;
			this.month = month;
			this.day = day;
		}

		public YearMonthDay(String jalaliDate) {
			String[] parts = jalaliDate.split("/");
			if (parts.length != 3) {
				throw new IllegalArgumentException("Invalid Jalali date format. Expected yyyy/mm/dd");
			}
			this.year = Integer.parseInt(parts[0]);
			this.month = Integer.parseInt(parts[1]) - 1; // Convert to 0-based
			this.day = Integer.parseInt(parts[2]);
		}

		public int getYear() { return year; }
		public void setYear(int year) { this.year = year; }
		public int getMonth() { return month; }
		public void setMonth(int month) { this.month = month; }
		public int getDay() { return day; }
		public void setDay(int day) { this.day = day; }

		@Override
		public String toString() {
			return String.format("%04d/%02d/%02d", year, month + 1, day);
		}
	}

	@Override
	public void add(int field, int amount) {
		
		if (field == YEAR || field == MONTH || field == DAY_OF_MONTH) {
			YearMonthDay jalaliDate = new YearMonthDay(fields[YEAR], fields[MONTH], fields[DAY_OF_MONTH]);
			switch (field) {
				case YEAR:
					jalaliDate = addYears(jalaliDate, amount);
					break;
				case MONTH:
					jalaliDate = addMonths(jalaliDate, amount);
					break;
				case DAY_OF_MONTH:
					jalaliDate = addDays(jalaliDate, amount);
					break;
				default:
					jalaliDate = addDays(jalaliDate, amount);
			}
			set(YEAR, jalaliDate.getYear());
			set(MONTH, jalaliDate.getMonth());
			set(DAY_OF_MONTH, jalaliDate.getDay());
		} 
		complete();
	}

	@Override
	public void roll(int field, boolean up) {
		roll(field, up ? +1 : -1);
	}

	@Override
	public void roll(int field, int amount) {
		if (field == YEAR || field == MONTH || field == DAY_OF_MONTH) {
			YearMonthDay jalaliDate = new YearMonthDay(fields[YEAR], fields[MONTH], fields[DAY_OF_MONTH]);
			switch (field) {
				case YEAR:
					jalaliDate = addYears(jalaliDate, amount);
					break;
				case MONTH:
					jalaliDate = addMonths(jalaliDate, amount);
					break;
				case DAY_OF_MONTH:
					jalaliDate = addDays(jalaliDate, amount);
					break;
				default:
					jalaliDate = addYears(jalaliDate, amount);
			}
			set(YEAR, jalaliDate.getYear());
			set(MONTH, jalaliDate.getMonth());
			set(DAY_OF_MONTH, jalaliDate.getDay());
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

	@Override
	public void set(int field, int value) {
		if (field == YEAR || field == MONTH || field == DAY_OF_MONTH) {
			YearMonthDay jalaliDate = new YearMonthDay(fields[YEAR], fields[MONTH], fields[DAY_OF_MONTH]);
			switch (field) {
				case YEAR:
					jalaliDate.setYear(value);
					break;
				case MONTH:
					jalaliDate.setMonth(value);
					break;
				case DAY_OF_MONTH:
					jalaliDate.setDay(value);
					break;
				default:
					jalaliDate.setDay(value);
					
			}
			set(YEAR, jalaliDate.getYear());
			set(MONTH, jalaliDate.getMonth());
			set(DAY_OF_MONTH, jalaliDate.getDay());
		} else {
			super.set(field, value);
		}
	}
}
