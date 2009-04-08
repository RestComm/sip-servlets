package org.mobicents.servlet.sip.seam.entrypoint.media;

import org.mobicents.mscontrol.MsProvider;
import org.mobicents.mscontrol.impl.MsProviderImpl;

/**
 * Central place to hold the media provider. It's a singleton and we need to reference it independetly.
 * @author vralev
 *
 */
public class MsProviderContainer {
	public static MsProvider msProvider = new MsProviderImpl();
}
