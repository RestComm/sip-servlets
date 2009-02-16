package org.mobicents.sip.servlet.tooling.util;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
import org.eclipse.jst.j2ee.web.componentcore.util.WebArtifactEdit;
import org.eclipse.jst.javaee.web.WebApp;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;
import org.mobicents.sip.servlet.tooling.SipServletCorePlugin;
import org.osgi.framework.Bundle;
/**
 * 
 * @author <a href="mailto:stefan.sik@omxgroup.com">Stefan Sik</a>
 * @author <a href="mailto:vralev@redhat.com">Vladimir Ralev</a>
 *
 */
public final class EclipseUtils
{

    public static IFolder getWebInfLibDir( final IProject pj )
    {
        final IVirtualComponent vc = ComponentCore.createComponent( pj );
        final IVirtualFolder vf = vc.getRootFolder().getFolder( "WEB-INF/lib" );
        return (IFolder) vf.getUnderlyingFolder();
    }
    
    public static IFolder getWebInfDir( final IProject pj )
    {
        final IVirtualComponent vc = ComponentCore.createComponent( pj );
        final IVirtualFolder vf = vc.getRootFolder().getFolder( "WEB-INF" );
        return (IFolder) vf.getUnderlyingFolder();
    }
    
    public static IFolder getWebInfTldDir( final IProject pj )
    {
        final IVirtualComponent vc = ComponentCore.createComponent( pj );
        final IVirtualFolder vf = vc.getRootFolder().getFolder( "WEB-INF/tld" );
        return (IFolder) vf.getUnderlyingFolder();
    }

    /**
     * Copies a resource from within the FormGen plugin to a destination in
     * the workspace.
     *
     * @param src the path of the resource within the plugin
     * @param dest the destination path within the workspace
     */

    public static void copyFromPlugin( final IPath src,
                                       final IFile dest )

        throws CoreException

    {
        try
        {
        	final Bundle bundle = SipServletCorePlugin.getDefault().getBundle();
            final InputStream in = FileLocator.openStream( bundle, src, false );
            dest.create( in, true, null );
        }
        catch( IOException e )
        {
            // throw new CoreException( FormGenPlugin.createErrorStatus( e.getMessage(), e ) );
        }
    }
    
    public static IProgressMonitor getMonitor(IProgressMonitor m)
    {
    	return m != null ? m: new NullProgressMonitor();
    }

}