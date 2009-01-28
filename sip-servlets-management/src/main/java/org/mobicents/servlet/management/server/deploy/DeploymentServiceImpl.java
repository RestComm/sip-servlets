package org.mobicents.servlet.management.server.deploy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import org.mobicents.servlet.management.client.deploy.DeploymentService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class DeploymentServiceImpl  extends RemoteServiceServlet implements DeploymentService{

	private static String[] appExtensions = {"war", "sar", "sar2", "ear"};

	public void deploy(String application) {
		String serverHome = System.getProperty("jboss.server.home.dir");
		String jbossHome = System.getProperty("jboss.home.dir");
		String appToDeploy = jbossHome + "/examples/" + application;
		String deployFolder = serverHome + "/deploy/";
		String targetFile = deployFolder + application;
		copyFile(appToDeploy, targetFile);
	}

	public String[] getApplications(String directory) {
		String serverHome = System.getProperty("jboss.server.home.dir");
		String jbossHome = System.getProperty("jboss.home.dir");
		String deployFolder = serverHome + "/deploy/";
		String examplesFolder = jbossHome + "/examples/";
		String folder = null;
		if(directory.equals("examples")) {
			folder = examplesFolder;
		} else {
			folder = deployFolder;
		}
		
		ArrayList<String> result = new ArrayList<String>();
		File root = new File(folder);
		File[] files = root.listFiles();
		if(files != null) {
			for(int q=0; q<files.length; q++) {
				File file = files[q];
				String fileName = file.getName();
				boolean isApp = false;
				for(String ext: appExtensions) {
					if(fileName.endsWith(ext)) {
						isApp = true;
						break;
					}
				}
				if(isApp)
				{
					result.add(fileName);
				}
			}
		}
		return result.toArray(new String[]{});
	}
	
	private static boolean copyFile(String from, String to){
		try {
			File f1 = new File(from);
			File f2 = new File(to);
			InputStream in = new FileInputStream(f1);

			OutputStream out = new FileOutputStream(f2);

			byte[] buf = new byte[10000];
			int len;
			while ((len = in.read(buf)) > 0){
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		}
		catch(Exception ex){
			return false;
		}
		return true;
	}

	public boolean isJBoss() {
		String serverHome = System.getProperty("jboss.server.home.dir");
		if(serverHome != null) return true;
		return false;
	}
}
