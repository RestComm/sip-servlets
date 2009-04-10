package org.mobicents.sip.phone.views;

import java.util.Hashtable;

import net.java.sip.communicator.impl.audionotifier.AudioNotifierActivator;
import net.java.sip.communicator.impl.configuration.ConfigurationActivator;
import net.java.sip.communicator.impl.fileaccess.FileAccessActivator;
import net.java.sip.communicator.impl.media.MediaActivator;
import net.java.sip.communicator.impl.netaddr.NetaddrActivator;
import net.java.sip.communicator.impl.protocol.ProtocolProviderActivator;
import net.java.sip.communicator.impl.protocol.sip.ProtocolProviderFactorySipImpl;
import net.java.sip.communicator.impl.protocol.sip.SipAccountID;
import net.java.sip.communicator.impl.protocol.sip.SipActivator;
import net.java.sip.communicator.impl.resources.ResourceManagementActivator;
import net.java.sip.communicator.service.audionotifier.AudioNotifierService;
import net.java.sip.communicator.service.protocol.AccountID;
import net.java.sip.communicator.service.protocol.Call;
import net.java.sip.communicator.service.protocol.OperationSetBasicTelephony;
import net.java.sip.communicator.service.protocol.ProtocolNames;
import net.java.sip.communicator.service.protocol.ProtocolProviderFactory;
import net.java.sip.communicator.service.protocol.ProtocolProviderService;
import net.java.sip.communicator.util.Logger;

import org.mobicents.sip.phone.SipPhoneActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;


public class SipCommunicatorOSGIBootstrap {
	private static final Logger logger = Logger.getLogger(SipCommunicatorOSGIBootstrap.class);
	
	private ProtocolProviderFactorySipImpl protocolProviderFactorySipImpl;

	private ProtocolProviderService protocolProviderService;
	private BundleContext context;

	public SipCommunicatorOSGIBootstrap(BundleContext context) {
		this.context = context;
		try {
			initSipCommunicator(context);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void initSipCommunicator(BundleContext context) throws Exception {
		this.context = context;
		SipActivator sipActivator = new SipActivator();
		ResourceManagementActivator resourceManagementActivator = new ResourceManagementActivator();
		ProtocolProviderActivator protocolProviderActivator = new ProtocolProviderActivator();
		MediaActivator mediaActivator = new MediaActivator();
		FileAccessActivator fileAccessActivator = new FileAccessActivator();
		ConfigurationActivator confActivator = new ConfigurationActivator();
		NetaddrActivator netaddrActivator = new NetaddrActivator();
		AudioNotifierActivator audioNotifierActivator = new AudioNotifierActivator();

		sipActivator.start(context);
		fileAccessActivator.start(context);
		confActivator.start(context);
		mediaActivator.start(context);
		resourceManagementActivator.start(context);
		protocolProviderActivator.start(context);
		netaddrActivator.start(context);
		audioNotifierActivator.start(context);

		protocolProviderFactorySipImpl = sipActivator.getProtocolProviderFactory();
	}

	public void setUp(Hashtable props) {
		try{
			protocolProviderFactorySipImpl.uninstallAccount(protocolProviderService.getAccountID());
		} catch (Exception e) {}
		protocolProviderFactorySipImpl.installAccount("test", props);
		
		//find the protocol providers exported for the two accounts
		ServiceReference[] sipProviderRefs;
		try {
			sipProviderRefs = context.getServiceReferences(
					ProtocolProviderService.class.getName(),
					"(&"
					+"("+ProtocolProviderFactory.PROTOCOL+"="+ProtocolNames.SIP+")"
					+"("+ProtocolProviderFactory.USER_ID+"="
					+ "test" +")"
					+")");

			protocolProviderService = (ProtocolProviderService)context.getService(sipProviderRefs[0]);
		} catch (InvalidSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static Hashtable createSipAccountProperties(String userId, String displayName, String password,
			String serverAddress, String serverPort, String proxyAddress, String proxyPort)
	{
		Hashtable table = new Hashtable();

		table.put(ProtocolProviderFactory.USER_ID, userId);

		table.put(ProtocolProviderFactory.DISPLAY_NAME, displayName);

		table.put(ProtocolProviderFactory.PASSWORD, password);

		table.put(ProtocolProviderFactory.SERVER_ADDRESS, serverAddress);

		if(serverPort != null)
		{
			table.put(ProtocolProviderFactory.SERVER_PORT, serverPort);
		}

		table.put(ProtocolProviderFactory.PROXY_ADDRESS, proxyAddress);

		if(proxyPort != null)
		{
			table.put(ProtocolProviderFactory.PROXY_PORT, proxyPort);
		}

		return table;
	}

	public ProtocolProviderFactorySipImpl getProtocolProviderFactorySipImpl() {
		return protocolProviderFactorySipImpl;
	}

	public ProtocolProviderService getProtocolProviderService() {
		return protocolProviderService;
	}

	public BundleContext getContext() {
		return context;
	}
	
	private static AudioNotifierService audioNotifierService;
	public static AudioNotifierService getAudioNotifier() {
		if (audioNotifierService == null) {
			ServiceReference serviceReference = SipPhoneActivator.getDefault().getBundle().getBundleContext()
			.getServiceReference(AudioNotifierService.class.getName());

			audioNotifierService = (AudioNotifierService) SipPhoneActivator.getDefault().getBundle().getBundleContext()
			.getService(serviceReference);
		}

		return audioNotifierService;
	}
	
	

}
