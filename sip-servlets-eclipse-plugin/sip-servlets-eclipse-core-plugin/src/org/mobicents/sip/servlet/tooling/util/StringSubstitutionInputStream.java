package org.mobicents.sip.servlet.tooling.util;

/**
 *   Copyright 2005 Open Cloud Ltd.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

import java.io.*;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author cath
 * @author vralev
 */
public class StringSubstitutionInputStream extends InputStream {

	public StringSubstitutionInputStream(java.io.InputStream source, java.util.Map substitutions) {
		if (substitutions == null)
			throw new NullPointerException("substitutions may not be null");
		
		reader = new BufferedReader(new InputStreamReader(source));	
		subs = substitutions;			
	}
	
	public boolean markSupported() { return false; }
	
	public int read() throws IOException {
		// Read in a new line if need be.
		if (lastLine == null) {
			lastLine = reader.readLine();
			if (lastLine == null) // EOF and we don't have anything locally buffered.
				return -1;
						
			lastLine = lastLine + '\n';
			index = 0; // Set to beginning of line
			
			// Perform substitutions - non-iterative
			Set keys = subs.keySet();		
			Iterator iter = keys.iterator();
			while (iter.hasNext()) {
				String key = (String) iter.next();
				
				lastLine = lastLine.replaceAll("___" + key + "___", (String) subs.get(key));									
			}			
		}
		
		int character = (int) lastLine.charAt(index++);
		if (index >= lastLine.length()) {
			lastLine = null;
			index = -1;
		}
			
		return character;
	}

	private String lastLine = null;
	private int index = -1; // Index into the lastLine variable
	private final BufferedReader reader;
	private final Map subs;
	
}
