/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
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

package org.mobicents.servlet.sip.core.timers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSessionKey;
import org.mobicents.timers.PeriodicScheduleStrategy;
import org.mobicents.timers.TimerTaskData;

/**
 * @author jean.deruelle@gmail.com
 * 
 */
public class TimerServiceTaskData extends TimerTaskData implements Serializable {

	private static final Logger logger = Logger.getLogger(TimerServiceTaskData.class);

	private transient Serializable data;
	private transient byte[] dataBytes;

	private MobicentsSipApplicationSessionKey key;

	private long delay;

	public TimerServiceTaskData(Serializable id, long startTime, long period,
			PeriodicScheduleStrategy periodicScheduleStrategy) {
		super(id, startTime, period, periodicScheduleStrategy);
	}

	/**
	 * @param key
	 *            the key to set
	 */
	public void setKey(MobicentsSipApplicationSessionKey key) {
		this.key = key;
	}

	/**
	 * @return the key
	 */
	public MobicentsSipApplicationSessionKey getKey() {
		return key;
	}

	/**
	 * @param delay
	 *            the delay to set
	 */
	public void setDelay(long delay) {
		this.delay = delay;
	}

	/**
	 * @return the delay
	 */
	public long getDelay() {
		return delay;
	}

	/**
	 * 
	 * @return
	 */
	public Serializable getData() {
		if(data != null) {
			return data;
		}
		if (dataBytes != null) {			
			deserializeData();
		}
		return data;
	}
	
	/**
	 * 
	 * @param data
	 */
	public void setData(Serializable data) {
		this.data = data;
		this.dataBytes = null;
	}
	
	// http://code.google.com/p/sipservlets/issues/detail?id=90
	
	private void serializeData() {
		ObjectOutputStream out = null;
		ByteArrayOutputStream bos = null;
		try {
			bos = new ByteArrayOutputStream() ;
			out = new ObjectOutputStream(bos) ;
			out.writeObject(data);
			out.close();
			dataBytes = bos.toByteArray();
			out = null;
			bos = null;
		}
		catch (Exception e) {
			logger.error("failed to serialize timer app data "+data+" , it won't be available in another cluster node",e);
		}
		finally {
			if (out != null) {
				try {
					out.close();
				}
				catch (Exception e) {
					if(logger.isDebugEnabled()) {
						logger.debug(e.getMessage(),e);
					}
				}
			}
			else {
				if (bos != null) {
					try {
						bos.close();
					}
					catch (Exception e) {
						if(logger.isDebugEnabled()) {
							logger.debug(e.getMessage(),e);
						}
					}
				}
			}
		}
	}
	
	private void deserializeData() {
		ObjectInputStream in = null;
		ByteArrayInputStream bis = null;
		try {
			bis = new ByteArrayInputStream(dataBytes) ;
			in = new CustomObjectInputStream(bis) ;
			data = (Serializable) in.readObject();
			in.close();
			in = null;
			bis = null;
		}
		catch (Exception e) {
			logger.error("failed to deserialize timer app data: "+e.getMessage(),e);
		}
		finally {
			if (in != null) {
				try {
					in.close();
				}
				catch (Exception e) {
					if(logger.isDebugEnabled()) {
						logger.debug(e.getMessage(),e);
					}					
				}
			}
			else {
				if (bis != null) {
					try {
						bis.close();
					}
					catch (Exception e) {
						if(logger.isDebugEnabled()) {
							logger.debug(e.getMessage(),e);
						}
					}
				}
			}
		}
	}
	
	private static class CustomObjectInputStream extends ObjectInputStream {

		public CustomObjectInputStream(InputStream in) throws IOException {
			super(in);
		}
		
		@Override
		protected Class<?> resolveClass(ObjectStreamClass desc)
				throws IOException, ClassNotFoundException {
			return Thread.currentThread().getContextClassLoader().loadClass(desc.getName());			
		}
	}
	
	private void readObject(java.io.ObjectInputStream in)
			throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		int length = in.readInt();
		if (length > 0) {
			dataBytes = new byte[length];
			in.readFully(dataBytes);
		}
	}

	private void writeObject(java.io.ObjectOutputStream out)
			throws IOException {
		out.defaultWriteObject();
		if (dataBytes == null && data != null) {
			serializeData();
		}
		if(dataBytes == null) {
			out.writeInt(0);
		}
		else {
			out.writeInt(dataBytes.length);
			out.write(dataBytes);
		}		
	}
}