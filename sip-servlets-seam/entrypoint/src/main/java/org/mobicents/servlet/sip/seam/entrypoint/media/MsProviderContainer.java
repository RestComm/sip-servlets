package org.mobicents.servlet.sip.seam.entrypoint.media;

import org.mobicents.mscontrol.MsProvider;
import org.mobicents.mscontrol.impl.MsProviderImpl;

public class MsProviderContainer {
	public static MsProvider msProvider = new MsProviderImpl();
}
