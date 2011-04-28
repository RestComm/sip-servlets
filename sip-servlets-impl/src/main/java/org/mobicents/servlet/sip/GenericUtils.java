/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.mobicents.servlet.sip;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Generic utils not related to SIP.
 * 
 * @author vralev
 *
 */
public class GenericUtils {
	
	/**
	 * Convert byte array to string
	 * 
	 * @param data
	 * @return
	 */
	public static String convertToHex(byte[] data) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < data.length; i++) {
			int halfbyte = (data[i] >>> 4) & 0x0F;
			int two_halfs = 0;
			do {
				if ((0 <= halfbyte) && (halfbyte <= 9))
					buf.append((char) ('0' + halfbyte));
				else
					buf.append((char) ('a' + (halfbyte - 10)));
				halfbyte = data[i] & 0x0F;
			} while(two_halfs++ < 1);
		}
		return buf.toString();
	}
	
	public static String reduceHash(String hash, int maxChars) {
		if(hash.length() > maxChars) {
			return hash.substring(0, maxChars);
		} else {
			return hash;
		}
	}
	
	/**
	 * Compute hash value of a string
	 * 
	 * @param input
	 * @return
	 */
	public static String hashString(String input) {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA");
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalArgumentException("The SHA Algorithm could not be found", e);
		}
		byte[] bytes = input.getBytes();
		md.update(bytes);
		String hashed =  convertToHex(md.digest());
		hashed = reduceHash(hashed, 8);
		return hashed;
	}
}
