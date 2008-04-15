package org.mobicents.servlet.sip.security;
/**
 * This class is just a data structure to contain auth info.
 * @author root
 *
 */
public class AuthInfoEntry {
	public String userName;
	public String password;
	public int statusCode;
	
	public AuthInfoEntry(int statusCode, String userName, String password) {
		this.statusCode = statusCode;
		this.userName = userName;
		this.password = password;
	}
	
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public int getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}
}
