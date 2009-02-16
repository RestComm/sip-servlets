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


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.mobicents.sip.servlet.tooling.SipServletCorePlugin;


/**
 * @author cath
 */
public class FileUtil {
	/**
	 * Creates the file at the specified location from the given template with
	 * substitutions defined in the map.
	 * 
	 * @param folder the folder that the path is relative to
	 * @param path the file name to create relative to the folder
	 * @param template the path of the template to use, as retrievable via ServiceCreationPlugin.openStream()
	 * @param subs the substitution map to apply to the template
	 * @param monitor the progress monitor to update as the file is created
	 * @throws IOException if the file could not be created or the template could not be opened
	 * @throws CoreException if something else goes wrong ;-)
	 */
	
	public static IFile createFromTemplate(IContainer folder, IPath path, IPath template, Map subs, IProgressMonitor monitor) throws IOException, CoreException {
		
		IFile file = folder.getFile(path);
		InputStream stream = new StringSubstitutionInputStream(SipServletCorePlugin.getDefault().openStream(template), subs);

		if (file.exists())
			file.setContents(stream, true, true, monitor);
		else
			file.create(stream, true, monitor);
		
		stream.close();
		return file;
	}
	
	public static IFile createFromInputStream(IContainer folder, IPath path, InputStream stream, IProgressMonitor monitor) throws CoreException, IOException {
		
		IFile file = folder.getFile(path);
		
		if (file.exists())
			file.setContents(stream, true, true, monitor);
		else
			file.create(stream, true, monitor);
		
		stream.close();
		return file;		
	}
	
	public static File createFromInputStream(IPath dir, IPath path, InputStream is, IProgressMonitor monitor) throws IOException {

		IPath fullPath = dir.append(path);
		File file = fullPath.toFile();
		
		FileOutputStream os = new FileOutputStream(file);
		
		byte buf[] = new byte[2048];
		while (true) {
			int ret = is.read(buf);
			if (ret == -1) // EOF
				break;
			
			os.write(buf, 0, ret);			
		}
		
		is.close();
		os.close();
		return file;
	}
}
