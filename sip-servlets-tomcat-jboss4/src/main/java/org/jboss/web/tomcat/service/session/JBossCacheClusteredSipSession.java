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

import gov.nist.javax.sip.SipStackImpl;

import java.text.ParseException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.sip.SipStack;

import org.apache.catalina.Container;
import org.apache.catalina.Engine;
import org.apache.catalina.Service;
import org.apache.catalina.connector.Connector;
import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.core.session.DistributableSipManager;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession;
import org.mobicents.servlet.sip.core.session.SessionManagerUtil;
import org.mobicents.servlet.sip.core.session.SipSessionKey;
import org.mobicents.servlet.sip.message.B2buaHelperImpl;
import org.mobicents.servlet.sip.message.SipFactoryImpl;
import org.mobicents.servlet.sip.proxy.ProxyImpl;
import org.mobicents.servlet.sip.startup.SipService;


/**
 * Common superclass of ClusteredSipSession types that use JBossCache
 * as their distributed cache.
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public abstract class JBossCacheClusteredSipSession extends ClusteredSipSession {
	private static final Logger logger = Logger.getLogger(JBossCacheClusteredSipSession.class);
	
	/**
	 * Our proxy to the cache.
	 */
	protected transient ConvergedJBossCacheService proxy_;		
	   
	protected JBossCacheClusteredSipSession(SipSessionKey key,
			SipFactoryImpl sipFactoryImpl,
			MobicentsSipApplicationSession mobicentsSipApplicationSession, JBossCacheManager jBossCacheManager) {
		super(key, sipFactoryImpl, mobicentsSipApplicationSession, 
				jBossCacheManager.getUseJK());
		int maxUnrep = jBossCacheManager.getMaxUnreplicatedInterval() * 1000;
	    setMaxUnreplicatedInterval(maxUnrep);
		establishProxy(jBossCacheManager);
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
		// Since attribute map may be transient, we may need to populate it
		// from the underlying store.
		populateMetaData();
		if(proxy != null) {
			proxy.setSipFactoryImpl(sipFactory);
		}
		if(b2buaHelper != null) {
			b2buaHelper.setSipFactoryImpl(sipFactory);
			b2buaHelper.setSipManager(manager);
		}
		//inject the dialog into the available sip stacks
		if(logger.isDebugEnabled()) {
			logger.debug("dialog to inject " + sessionCreatingDialogId);
			if(sessionCreatingDialogId != null) {
				logger.debug("dialog id of the dialog to inject " + sessionCreatingDialogId);
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
						if(sipStack != null) {
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
	 * @param jBossCacheManager 
	 */
	protected void establishProxy(JBossCacheManager jBossCacheManager) {
		if (proxy_ == null) {
			proxy_ = (ConvergedJBossCacheService) jBossCacheManager.getCacheService();

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

	protected void populateMetaData() {
		final String sipAppSessionId = sipApplicationSessionKey.getId();
		final String sipSessionId = getHaId();				
		Long ct = (Long) proxy_.getSipSessionMetaData(sipAppSessionId, sipSessionId, CREATION_TIME);
		if(ct != null) {
			creationTime = ct;
		}
		Integer ip = (Integer) proxy_.getSipSessionMetaData(sipAppSessionId, sipSessionId, INVALIDATION_POLICY);
		if(ip != null) {
			invalidationPolicy = ip;
		}
		handlerServlet = (String) proxy_.getSipSessionMetaData(sipAppSessionId, sipSessionId, HANDLER);
		Boolean valid = (Boolean) proxy_.getSipSessionMetaData(sipAppSessionId, sipSessionId, IS_VALID);
		if(valid != null) {
			setValid(valid);
		}
		state = (State) proxy_.getSipSessionMetaData(sipAppSessionId, sipSessionId, STATE);
		Long cSeq = (Long) proxy_.getSipSessionMetaData(sipAppSessionId, sipSessionId, CSEQ);
		if(cSeq != null) {
			cseq = cSeq;
		} 
		Boolean iwr = (Boolean) proxy_.getSipSessionMetaData(sipAppSessionId, sipSessionId, INVALIDATE_WHEN_READY);		
		if(iwr != null) {
			invalidateWhenReady = iwr;
		} 
		Boolean rti = (Boolean) proxy_.getSipSessionMetaData(sipAppSessionId, sipSessionId, READY_TO_INVALIDATE);		
		if(rti != null) {
			readyToInvalidate = rti;
		} 
		sessionCreatingDialogId = (String) proxy_.getSipSessionMetaData(sipAppSessionId, sipSessionId, DIALOG_ID);
		proxy = (ProxyImpl) proxy_.getSipSessionMetaData(sipAppSessionId, sipSessionId, PROXY);
		
		Integer size = (Integer) proxy_.getSipSessionMetaData(sipAppSessionId, sipSessionId, B2B_SESSION_SIZE);
		String[][] sessionArray = (String[][])proxy_.getSipSessionMetaData(sipAppSessionId, sipSessionId, B2B_SESSION_MAP);
		if(logger.isDebugEnabled()) {
			logger.debug("b2bua session array size = " + size + ", value = " + sessionArray);
		}
		if(size != null && sessionArray != null) {
			Map<SipSessionKey, SipSessionKey> sessionMap = new ConcurrentHashMap<SipSessionKey, SipSessionKey>();
			for (int i = 0; i < size; i++) {
				String key = sessionArray[0][i];
				String value = sessionArray[1][i];
				try {
					SipSessionKey sipSessionKeyKey = SessionManagerUtil.parseSipSessionKey(key);
					SipSessionKey sipSessionKeyValue = SessionManagerUtil.parseSipSessionKey(value);
					sessionMap.put(sipSessionKeyKey, sipSessionKeyValue);
				} catch (ParseException e) {
					logger.warn("couldn't parse a deserialized sip session key from the B2BUA", e);
				}
			}
			if(b2buaHelper == null) {
				b2buaHelper = new B2buaHelperImpl();
			}
			b2buaHelper.setSessionMap(sessionMap);
		}
		isNew = false;
	}
	
	/**
	 * Increment our version and place ourself in the cache.
	 */
	public void processSessionRepl() {		
		// Replicate the session.
		final String sipAppSessionKey = sipApplicationSessionKey.getId();
		final String sipSessionKey = getHaId();
		if(isNew) {
			proxy_.putSipSessionMetaData(sipAppSessionKey, sipSessionKey, CREATION_TIME, creationTime);
			proxy_.putSipSessionMetaData(sipAppSessionKey, sipSessionKey, INVALIDATION_POLICY, invalidationPolicy);
			isNew = false;
		}
					
		if(sessionMetadataDirty) {
			if (logger.isDebugEnabled()) {
				logger.debug("processSessionRepl(): session metadata is dirty.");
			}			
			for (Entry<String, Object> entry : metaModifiedMap_.entrySet()) {
				proxy_.putSipSessionMetaData(sipAppSessionKey, sipSessionKey, entry.getKey(), entry.getValue());
			}
			metaModifiedMap_.clear();
						
			if(proxy != null) {											
				proxy_.putSipSessionMetaData(sipAppSessionKey, sipSessionKey, PROXY, proxy);
			}
			
			if(b2buaHelper != null) {
				final Map<SipSessionKey, SipSessionKey> sessionMap = b2buaHelper.getSessionMap();
				final int size = sessionMap.size();
				final String[][] sessionArray = new String[2][size];
				int i = 0;
				for (Entry<SipSessionKey, SipSessionKey> entry : sessionMap.entrySet()) {
					sessionArray [0][i] = entry.getKey().toString(); 
					sessionArray [1][i] = entry.getValue().toString();
					i++;
				}
				proxy_.putSipSessionMetaData(sipAppSessionKey, sipSessionKey,B2B_SESSION_SIZE, size);
				if(logger.isDebugEnabled()) {
					logger.debug("storing b2bua session array " + sessionArray);
				}
				proxy_.putSipSessionMetaData(sipAppSessionKey, sipSessionKey, B2B_SESSION_MAP, sessionArray);
			}
		}
		this.incrementVersion();
		proxy_.putSipSession(sipSessionKey, this);

		sessionAttributesDirty = false;
		sessionMetadataDirty = false;
		sessionLastAccessTimeDirty = false;

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
