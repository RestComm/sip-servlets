/*
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.web.tomcat.service.session;

import gov.nist.javax.sip.SipProviderImpl;
import gov.nist.javax.sip.SipStackImpl;
import gov.nist.javax.sip.stack.SIPDialog;

import javax.sip.SipStack;

import org.apache.catalina.Container;
import org.apache.catalina.Engine;
import org.apache.catalina.Service;
import org.apache.catalina.connector.Connector;
import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession;
import org.mobicents.servlet.sip.core.session.SipSessionKey;
import org.mobicents.servlet.sip.message.SipFactoryImpl;
import org.mobicents.servlet.sip.startup.SipService;


/**
 * Common superclass of ClusteredSipSession types that use JBossCache
 * as their distributed cache.
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public abstract class JBossCacheClusteredSipSession extends ClusteredSipSession {
	private static transient final Logger logger = Logger.getLogger(JBossCacheClusteredSipSession.class);
	
	/**
	 * Our proxy to the cache.
	 */
	protected transient ConvergedJBossCacheService proxy_;
	   
	protected JBossCacheClusteredSipSession(SipSessionKey key,
			SipFactoryImpl sipFactoryImpl,
			MobicentsSipApplicationSession mobicentsSipApplicationSession) {
		super(key, sipFactoryImpl, mobicentsSipApplicationSession, 
				((JBossCacheSipManager)mobicentsSipApplicationSession.getSipContext().getSipManager()).getUseJK());
		int maxUnrep = ((JBossCacheSipManager)mobicentsSipApplicationSession.getSipContext().getSipManager()).getMaxUnreplicatedInterval() * 1000;
	    setMaxUnreplicatedInterval(maxUnrep);
		establishProxy();
	}
	
	/**
	 * Initialize fields marked as transient after loading this session from the
	 * distributed store
	 * 
	 * @param manager
	 *            the manager for this session
	 */
	public void initAfterLoad(JBossCacheSipManager manager) {
		sipFactory = manager.getSipFactoryImpl();
		if(proxy != null) {
			proxy.setSipFactoryImpl(sipFactory);
		}
		if(b2buaHelper != null) {
			b2buaHelper.setSipFactoryImpl(sipFactory);
			b2buaHelper.setSipManager(manager);
		}
		//inject the dialog into the available sip stacks
		if(logger.isDebugEnabled()) {
			logger.debug("dialog to inject " + sessionCreatingDialog);
			if(sessionCreatingDialog != null) {
				logger.debug("dialog id of the dialog to inject " + sessionCreatingDialog.getDialogId());
			}
		}
		if(sessionCreatingDialogId != null && sessionCreatingDialogId.length() > 0) {
			Container context = manager.getContainer();
			Container container = context.getParent().getParent();
			if(container instanceof Engine) {
				Service service = ((Engine)container).getService();
				if(service instanceof SipService) {
					Connector[] connectors = service.findConnectors();
					for (Connector connector : connectors) {
						SipStack sipStack = (SipStack)
							connector.getProtocolHandler().getAttribute(SipStack.class.getSimpleName());
						if(sipStack != null && sipStack.getSipProviders().hasNext()) {
							sessionCreatingDialog = ((SipStackImpl)sipStack).getDialog(sessionCreatingDialogId); 
							if(logger.isDebugEnabled()) {
								logger.debug("dialog injected " + sessionCreatingDialog);
							}
						}
					}
				}
			}
		}
		
		// Since attribute map may be transient, we may need to populate it
		// from the underlying store.
		populateAttributes();

		// Notify all attributes of type SipSessionActivationListener 
		this.activate();

		// We are no longer outdated vis a vis distributed cache
		clearOutdated();
	}

	/**
	 * Gets a reference to the JBossCacheService.
	 */
	protected void establishProxy() {
		if (proxy_ == null) {
			proxy_ = (ConvergedJBossCacheService)((JBossCacheSipManager) getSipApplicationSession().getSipContext().getSipManager()).getCacheService();

			// still null???
			if (proxy_ == null) {
				throw new RuntimeException(
						"JBossCacheClusteredSession: Cache service is null.");
			}
		}
	}

	protected abstract void populateAttributes();

	/**
	 * Override the superclass to additionally reset this class' fields.
	 * <p>
	 * <strong>NOTE:</strong> It is not anticipated that this method will be
	 * called on a ClusteredSession, but we are overriding the method to be
	 * thorough.
	 * </p>
	 */
//	public void recycle() {
//		super.recycle();
//
//		proxy_ = null;
//	}

	/**
	 * Increment our version and place ourself in the cache.
	 */
	public synchronized void processSessionRepl() {
		// Replicate the session.
		if (logger.isDebugEnabled()) {
			logger.debug("processSessionRepl(): session is dirty. Will increment "
					+ "version from: " + getVersion() + " and replicate.");
		}
		this.incrementVersion();
		proxy_.putSipSession(getId(), this);

		sessionAttributesDirty = false;
		sessionMetadataDirty = false;

		updateLastReplicated();
	}

	/**
	 * Overrides the superclass impl by doing nothing if <code>localCall</code>
	 * is <code>false</code>. The JBossCacheManager will already be aware of a
	 * remote invalidation and will handle removal itself.
	 */
	protected void removeFromManager(boolean localCall, boolean localOnly) {
		if (localCall) {
			super.removeFromManager(localCall, localOnly);
		}
	}

	protected Object removeAttributeInternal(String name, boolean localCall,
			boolean localOnly) {
		return removeJBossInternalAttribute(name, localCall, localOnly);
	}

	protected Object removeJBossInternalAttribute(String name) {
		throw new UnsupportedOperationException(
				"removeJBossInternalAttribute(String) "
						+ "is not supported by JBossCacheClusteredSession; use "
						+ "removeJBossInternalAttribute(String, boolean, boolean");
	}

	protected abstract Object removeJBossInternalAttribute(String name,
			boolean localCall, boolean localOnly);

}
