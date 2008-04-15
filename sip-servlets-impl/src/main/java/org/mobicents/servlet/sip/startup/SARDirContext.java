/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mobicents.servlet.sip.startup;

import java.io.File;
import java.util.Hashtable;
import java.util.zip.ZipFile;

import org.apache.naming.resources.WARDirContext;
/**
 * SAR Directory Context implementation. 
 * It extends the Tomcat WARDirContext since it can contains converged apps 
 *
 */
public class SARDirContext extends WARDirContext {

	/**
	 * 
	 */
	public SARDirContext() {
		super();
	}

	/**
	 * @param env
	 */
	public SARDirContext(Hashtable env) {
		super(env);		
	}

	/**
	 * @param base
	 * @param entries
	 */
	public SARDirContext(ZipFile base, Entry entries) {
		super(base, entries);		
	}

	@Override
	public void setDocBase(String docBase) {		
		// Validate the format of the proposed document root
		if (docBase == null)
			throw new IllegalArgumentException(sm.getString("resources.null"));
		if (!(docBase.endsWith(".sar")))
			throw new IllegalArgumentException(sm
					.getString("warResources.notWar"));

		// Calculate a File object referencing this document base directory
		File base = new File(docBase);

		// Validate that the document base is an existing directory
		if (!base.exists() || !base.canRead() || base.isDirectory())
			throw new IllegalArgumentException(sm.getString(
					"warResources.invalidWar", docBase));
		try {
			this.base = new ZipFile(base);
		} catch (Exception e) {
			throw new IllegalArgumentException(sm.getString(
					"warResources.invalidWar", e.getMessage()));
		}
		// Change the document root property
        this.docBase = docBase;

		loadEntries();		
	}
}
