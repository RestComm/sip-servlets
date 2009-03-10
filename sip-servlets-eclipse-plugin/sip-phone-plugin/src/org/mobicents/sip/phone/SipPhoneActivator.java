package org.mobicents.sip.phone;

import java.util.Hashtable;

import net.java.sip.communicator.impl.configuration.ConfigurationActivator;
import net.java.sip.communicator.impl.fileaccess.FileAccessActivator;
import net.java.sip.communicator.impl.media.MediaActivator;
import net.java.sip.communicator.impl.netaddr.NetaddrActivator;
import net.java.sip.communicator.impl.protocol.ProtocolProviderActivator;
import net.java.sip.communicator.impl.protocol.sip.ProtocolProviderFactorySipImpl;
import net.java.sip.communicator.impl.protocol.sip.SipActivator;
import net.java.sip.communicator.impl.resources.ResourceManagementActivator;
import net.java.sip.communicator.service.protocol.Call;
import net.java.sip.communicator.service.protocol.OperationSetBasicTelephony;
import net.java.sip.communicator.service.protocol.ProtocolNames;
import net.java.sip.communicator.service.protocol.ProtocolProviderFactory;
import net.java.sip.communicator.service.protocol.ProtocolProviderService;
import net.java.sip.communicator.service.protocol.SecurityAuthority;
import net.java.sip.communicator.service.protocol.UserCredentials;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * The activator class controls the plug-in life cycle
 */
public class SipPhoneActivator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "sip_phone_plugin";

	// The shared instance
	private static SipPhoneActivator plugin;
	
	/**
	 * The constructor
	 */
	public SipPhoneActivator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		

	}
	
  

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static SipPhoneActivator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
	

}
