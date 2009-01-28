package org.mobicents.ipbx.session.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {
	public static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";

	public static String nowString() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
		return sdf.format(cal.getTime());

	}

	public static String nowString(String dateFormat) {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		return sdf.format(cal.getTime());
	}

	public static Date now() {
		Calendar cal = Calendar.getInstance();
		return cal.getTime();
	}
}
