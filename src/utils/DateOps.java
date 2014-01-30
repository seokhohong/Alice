package utils;

import java.util.Calendar;
import java.util.Date;

public class DateOps 
{
	public static Calendar dateToCalendar(Date date)
	{ 
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal;
	}
}
