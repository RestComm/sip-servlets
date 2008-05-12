package org.mobicents.servlet.sip.example;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;

import org.w3c.dom.Document;

import com.facebook.api.FacebookException;
import com.facebook.api.FacebookXmlRestClient;
import com.facebook.api.ProfileField;

public class FacebookUtils {
	   
	static final String PHONE_START_TAG = "Phone[";
	static final String PHONE_END_TAG = "]";
	
	public static String getUserPhone(String userId, FacebookXmlRestClient client) {
		String aboutMe = getUserField(userId, ProfileField.ABOUT_ME, client);
		if(aboutMe == null) return null;
		
		int start = aboutMe.indexOf(PHONE_START_TAG); 
		if(start<0) return null;
		
		start += PHONE_START_TAG.length();
		int end = aboutMe.indexOf(PHONE_END_TAG, start);
		if(end<0) return null;
		
		String phoneNumber = aboutMe.substring(start, end);
		if(!phoneNumber.startsWith("sip:")) {
			phoneNumber = phoneNumber.replace('.', ' ').replace('-', ' ').replace('+', ' ');
			String rewritePhone = "";
			for(int q=0; q<phoneNumber.length(); q++) {
				if(phoneNumber.charAt(q)!=' ') {
					rewritePhone += phoneNumber.charAt(q);
				}
			}
			phoneNumber = rewritePhone;
		}
		return phoneNumber;
	}
	
	public static String getCurrentUserPhone(FacebookXmlRestClient client) {
		String phone = null;
		try {
			Document doc = client.data_getUserPreference(0);
			phone = doc.getElementsByTagName("data_getUserPreference_response").item(0).getTextContent();
		} catch (Exception e) {
			
		}

		if(phone == null || phone.length()<1) {
			try {
				phone = getUserPhone(new Integer(client.users_getLoggedInUser()).toString(), client);
			} catch (FacebookException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	
		return phone;
	}
	
	public static String setCurrentUserPhone(String value, FacebookXmlRestClient client) {
		try {
			client.data_setUserPreference(new Integer(0), value);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String getUserField(String userId, ProfileField field, FacebookXmlRestClient client) {
		try {
			ArrayList<Integer> userIds = new ArrayList<Integer>();
			EnumSet<ProfileField> fields = EnumSet.of(field);
			userIds.add(new Integer(userId));
			fields.add(field);
			Document doc = client.users_getInfo(userIds, fields);
			String text = doc.getElementsByTagName(field.toString()).item(0).getTextContent();
			return text;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
