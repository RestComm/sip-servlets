package org.jboss.mobicents.seam.util;

import javax.media.mscontrol.MsControlFactory;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class MMSUtil {
	public static MsControlFactory msControlFactory;
	public static String audioFilePath;
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
