package com.netflix.eureka.time;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {
	public static final String YYYYMMDD = "yyyyMMdd";
	public static final String YYYY_MM_DD = "yyyy-MM-dd";
	public static final String YMDHMS24 = "yyyy-MM-dd HH:mm:ss";
	
	public static final String HHMMSS = "HHmmss";
	public static final String HH_MM_SS = "HH:mm:ss";

    private static final int MODIFY_TRUNCATE = 0;
    private static final int MODIFY_ROUND = 1;
    private static final int MODIFY_CEILING = 2;
    
    public static final int SEMI_MONTH = 1001;

    private static final int[][] fields = {
            {Calendar.MILLISECOND},
            {Calendar.SECOND},
            {Calendar.MINUTE},
            {Calendar.HOUR_OF_DAY, Calendar.HOUR},
            {Calendar.DATE, Calendar.DAY_OF_MONTH, Calendar.AM_PM 
                /* Calendar.DAY_OF_YEAR, Calendar.DAY_OF_WEEK, Calendar.DAY_OF_WEEK_IN_MONTH */
            },
            {Calendar.MONTH, SEMI_MONTH},
            {Calendar.YEAR},
            {Calendar.ERA}};
	
	public static SimpleDateFormat getDateFormat(String dateType) {
		return new SimpleDateFormat(dateType);
	}
	
	/**
	 * 将当前时间转换成指定格式
	 * 
	 * @param dateType
	 * @return
	 */
	public static String getCurrentTimeString(String dateType) {
		Calendar c = Calendar.getInstance();
		Date date = c.getTime();
		return getTimeString(date, dateType);
	}
	
	/**
	 * 获取指定日期、指定格式化的字符串
	 * 
	 * @param date
	 * @param dateType
	 * @return
	 */
	public static String getTimeString(Date date, String dateType) {
		SimpleDateFormat dateFormat = getDateFormat(dateType);
		String dataStr = null;
		try {
			dataStr = dateFormat.format(date);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dataStr;
	}
	
	private static Date add(Date date, int calendarField, int amount) {
        if (date == null) {
            throw new IllegalArgumentException("The date must not be null");
        }
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(calendarField, amount);
        return c.getTime();
    }
	
    public static Date addYears(Date date, int amount) {
        return add(date, Calendar.YEAR, amount);
    }

    public static Date addMonths(Date date, int amount) {
        return add(date, Calendar.MONTH, amount);
    }
    
    public static Date addWeeks(Date date, int amount) {
        return add(date, Calendar.WEEK_OF_YEAR, amount);
    }

    public static Date addDays(Date date, int amount) {
        return add(date, Calendar.DAY_OF_MONTH, amount);
    }

    public static Date addHours(Date date, int amount) {
        return add(date, Calendar.HOUR_OF_DAY, amount);
    }

    public static Date addMinutes(Date date, int amount) {
        return add(date, Calendar.MINUTE, amount);
    }

    public static Date addSeconds(Date date, int amount) {
        return add(date, Calendar.SECOND, amount);
    }

    public static Date addMilliseconds(Date date, int amount) {
        return add(date, Calendar.MILLISECOND, amount);
    }
	
    public static Date truncate(Date date, int field) {
        if (date == null) {
            throw new IllegalArgumentException("The date must not be null");
        }
        Calendar gval = Calendar.getInstance();
        gval.setTime(date);
        modify(gval, field, MODIFY_TRUNCATE);
        return gval.getTime();
    }
    
	public static Date getDateEnd(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.set(Calendar.HOUR_OF_DAY, 23);
		c.set(Calendar.MINUTE, 59);
		c.set(Calendar.SECOND, 59);
		Date dateZero = c.getTime();
		return dateZero;
	}
	
	public static Date getDateStart(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		Date dateZero = c.getTime();
		return dateZero;
	}
	
	public static Date str2date(String strdate, String format) {
		if (null == strdate || "".equals(strdate)) {
			return null;
		}
		if (format == null || "".equals(format)) {
			return null;
		}
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		if(strdate.length() < format.length()) {
			if(strdate.split("-").length == 3) {
				strdate = strdate.replace(" ", "");
				if(strdate.length() == YYYY_MM_DD.length()) {
					formatter = new SimpleDateFormat(YYYY_MM_DD);
				}
			}
			if(strdate.split(":").length == 3) {
				strdate = strdate.replace(" ", "");
				if(strdate.length() == YYYY_MM_DD.length()) {
					formatter = new SimpleDateFormat(HH_MM_SS);
				}
				String replenish = new SimpleDateFormat(YYYY_MM_DD).format(new Date());
				strdate = String.format("%s %s", replenish, strdate);
			}
		}
		Date date = null;
		try {
			date = formatter.parse(strdate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}
	
	public static int compareTheMonth(Date to, Date from) {
		int co = to.compareTo(from);
		if(co == 0) {
			return 0;
		}
		
		Calendar c_to = Calendar.getInstance();
		c_to.setTime(to);
		
		Calendar c_from = Calendar.getInstance();
		c_from.setTime(from);
		
		if(co == 1) {
			int result = c_to.get(Calendar.MONTH) - c_from.get(Calendar.MONTH);
	        int month = (c_to.get(Calendar.YEAR) - c_from.get(Calendar.YEAR)) * 12;
	        return Math.abs(month + result); 
		}
		
		int result = c_from.get(Calendar.MONTH) - c_to.get(Calendar.MONTH);
        int month = (c_from.get(Calendar.YEAR) - c_to.get(Calendar.YEAR)) * 12;
        return Math.abs(month + result); 
	}
	
	public static int compareTheDay(Date to, Date from) {
		int co = to.compareTo(from);
		if(co == 0) {
			return 0;
		}
		
		Calendar c_to = Calendar.getInstance();
		c_to.setTime(to);
		
		Calendar c_from = Calendar.getInstance();
		c_from.setTime(from);
		
		if(co == 1) {
			long result = c_to.getTimeInMillis() - c_from.getTimeInMillis();
	        return Long.valueOf(result / (60 * 60 * 1000 * 24)).intValue(); 
		}
		
		long result = c_from.getTimeInMillis() - c_to.getTimeInMillis();
		return Long.valueOf(result / (60 * 60 * 1000 * 24)).intValue(); 
	}
	
	private static void modify(Calendar val, int field, int modType) {
        if (val.get(Calendar.YEAR) > 280000000) {
            throw new ArithmeticException("Calendar value too large for accurate calculations");
        }
        
        if (field == Calendar.MILLISECOND) {
            return;
        }

        // ----------------- Fix for LANG-59 ---------------------- START ---------------
        // see http://issues.apache.org/jira/browse/LANG-59
        //
        // Manually truncate milliseconds, seconds and minutes, rather than using
        // Calendar methods.

        Date date = val.getTime();
        long time = date.getTime();
        boolean done = false;

        // truncate milliseconds
        int millisecs = val.get(Calendar.MILLISECOND);
        if (MODIFY_TRUNCATE == modType || millisecs < 500) {
            time = time - millisecs;
        }
        if (field == Calendar.SECOND) {
            done = true;
        }

        // truncate seconds
        int seconds = val.get(Calendar.SECOND);
        if (!done && (MODIFY_TRUNCATE == modType || seconds < 30)) {
            time = time - (seconds * 1000L);
        }
        if (field == Calendar.MINUTE) {
            done = true;
        }

        // truncate minutes
        int minutes = val.get(Calendar.MINUTE);
        if (!done && (MODIFY_TRUNCATE == modType || minutes < 30)) {
            time = time - (minutes * 60000L);
        }

        // reset time
        if (date.getTime() != time) {
            date.setTime(time);
            val.setTime(date);
        }
        // ----------------- Fix for LANG-59 ----------------------- END ----------------

        boolean roundUp = false;
        for (int[] aField : fields) {
            for (int element : aField) {
                if (element == field) {
                    //This is our field... we stop looping
                    if (modType == MODIFY_CEILING || (modType == MODIFY_ROUND && roundUp)) {
                        if (field == SEMI_MONTH) {
                            //This is a special case that's hard to generalize
                            //If the date is 1, we round up to 16, otherwise
                            //  we subtract 15 days and add 1 month
                            if (val.get(Calendar.DATE) == 1) {
                                val.add(Calendar.DATE, 15);
                            } else {
                                val.add(Calendar.DATE, -15);
                                val.add(Calendar.MONTH, 1);
                            }
// ----------------- Fix for LANG-440 ---------------------- START ---------------
                        } else if (field == Calendar.AM_PM) {
                            // This is a special case
                            // If the time is 0, we round up to 12, otherwise
                            //  we subtract 12 hours and add 1 day
                            if (val.get(Calendar.HOUR_OF_DAY) == 0) {
                                val.add(Calendar.HOUR_OF_DAY, 12);
                            } else {
                                val.add(Calendar.HOUR_OF_DAY, -12);
                                val.add(Calendar.DATE, 1);
                            }
// ----------------- Fix for LANG-440 ---------------------- END ---------------
                        } else {
                            //We need at add one to this field since the
                            //  last number causes us to round up
                            val.add(aField[0], 1);
                        }
                    }
                    return;
                }
            }
            //We have various fields that are not easy roundings
            int offset = 0;
            boolean offsetSet = false;
            //These are special types of fields that require different rounding rules
            switch (field) {
                case SEMI_MONTH:
                    if (aField[0] == Calendar.DATE) {
                        //If we're going to drop the DATE field's value,
                        //  we want to do this our own way.
                        //We need to subtrace 1 since the date has a minimum of 1
                        offset = val.get(Calendar.DATE) - 1;
                        //If we're above 15 days adjustment, that means we're in the
                        //  bottom half of the month and should stay accordingly.
                        if (offset >= 15) {
                            offset -= 15;
                        }
                        //Record whether we're in the top or bottom half of that range
                        roundUp = offset > 7;
                        offsetSet = true;
                    }
                    break;
                case Calendar.AM_PM:
                    if (aField[0] == Calendar.HOUR_OF_DAY) {
                        //If we're going to drop the HOUR field's value,
                        //  we want to do this our own way.
                        offset = val.get(Calendar.HOUR_OF_DAY);
                        if (offset >= 12) {
                            offset -= 12;
                        }
                        roundUp = offset >= 6;
                        offsetSet = true;
                    }
                    break;
            }
            if (!offsetSet) {
                int min = val.getActualMinimum(aField[0]);
                int max = val.getActualMaximum(aField[0]);
                //Calculate the offset from the minimum allowed value
                offset = val.get(aField[0]) - min;
                //Set roundUp if this is more than half way between the minimum and maximum
                roundUp = offset > ((max - min) / 2);
            }
            //We need to remove this field
            if (offset != 0) {
                val.set(aField[0], val.get(aField[0]) - offset);
            }
        }
        throw new IllegalArgumentException("The field " + field + " is not supported");
    }
}
