package org.mobicents.sip.servlet.tooling;

import java.util.HashMap;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
import org.eclipse.wst.common.project.facet.core.IDelegate;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.mobicents.sip.servlet.tooling.util.EclipseUtils;
import org.mobicents.sip.servlet.tooling.util.FileUtil;

@SuppressWarnings("restriction")
public final class SipServletFacetAction implements IDelegate
{
	
	private void createFolder(IFolder root, String folder) {
		String[] strings = folder.split("/");
		for(String token : strings) {
			if(!token.equals("")) {
				root = root.getFolder(token);
				try {
					root.create(false, true, null);
				} catch (CoreException e) {
				}
			}
		}
	}
	
    public void execute( final IProject pj,
                         final IProjectFacetVersion fv,
                         final Object config,
                         final IProgressMonitor monitor )

        throws CoreException

    {
        monitor.beginTask( "Install Sip Servlets Facet", 2 );
        SipServletApplicationConfig cfg = (SipServletApplicationConfig) config;
        try
        {
        	
        	

            final IFolder webInfLib = EclipseUtils.getWebInfLibDir( pj );
            final IFolder webInf = EclipseUtils.getWebInfDir( pj );

            EclipseUtils.copyFromPlugin( new Path( "libs/sip-servlets-spec-1.1.8.jar" ),
                                  webInfLib.getFile( "sip-servlets-spec-1.1.8.jar" ) );
            
            HashMap<String, String> subs = new HashMap<String, String>();
            
            monitor.worked(1);
            String servletClassFile = cfg.getMainServletClass().replace('.', '/') + ".java";
            int last = servletClassFile.lastIndexOf('/');
            String servletClassName = cfg.getMainServletClass().substring(last+1);
            String servletClassPackagePath = servletClassFile.substring(0, last);
            String servletClassPackage = servletClassPackagePath.replace('/', '.');
        	IFolder rootFolder = (IFolder) J2EEProjectUtilities.getSourceFolderOrFirst(pj, "");
        	createFolder(rootFolder, servletClassPackagePath);
        	
        	monitor.worked(1);
        	subs.put("APP_NAME", cfg.getAppName());
        	subs.put("APP_DESCRIPTION", cfg.getDescription());
        	subs.put("MAIN_SERVLET_NAME", cfg.getMainServletName());
        	subs.put("MAIN_SERVLET_DESCRIPTION", cfg.getMainServletDesciption());
        	subs.put("MAIN_SERVLET_CLASS", cfg.getMainServletClass());
        	subs.put("MAIN_SERVLET_PACKAGE", servletClassPackage);
        	subs.put("MAIN_SERVLET_CLASSNAME", servletClassName);
        	
        	FileUtil.createFromTemplate(webInf, new Path("sip.xml"),
        			new Path("/templates/sip.xml.template"), subs, monitor);
        	Path p = new Path(servletClassFile);
        	
        	
        	FileUtil.createFromTemplate(rootFolder, p,
        			new Path("/templates/main.servlet.java.template"),
        			subs,
        			monitor);

            monitor.worked( 1 );
        } catch (Exception e) {
			throw new RuntimeException(e);
		}
        finally
        {
            monitor.done();
        }
    }
}
