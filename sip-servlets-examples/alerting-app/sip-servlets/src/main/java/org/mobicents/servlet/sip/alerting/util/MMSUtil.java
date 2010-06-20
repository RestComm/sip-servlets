package org.mobicents.servlet.sip.alerting.util;

import javax.media.mscontrol.MsControlFactory;

public class MMSUtil {
	public static MsControlFactory msControlFactory;
	public static MsControlFactory getMsControl() {
		try {
			return msControlFactory;
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
