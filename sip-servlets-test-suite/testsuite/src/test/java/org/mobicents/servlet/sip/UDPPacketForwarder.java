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

package org.mobicents.servlet.sip;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.LinkedList;

public class UDPPacketForwarder {
	int fromPort;
	int toPort;
	String bindAddress;
	boolean running;
	DatagramSocket fromSocket;
	Thread worker;
	public LinkedList<String> sipMessages;
	public HashSet<String> sipMessageWithoutRetrans;
	public UDPPacketForwarder(int fromPort, int toPort, String bind) {
		this.fromPort = fromPort;
		this.toPort = toPort;
		this.bindAddress = bind;
		sipMessages = new LinkedList<String>();
		sipMessageWithoutRetrans = new HashSet<String>();
	}
	
	public void start() {
		try {
			fromSocket = new DatagramSocket(fromPort, InetAddress.getByName(bindAddress));
			running = true;
			worker = new Thread() {
				@Override
				public void run() {
					while(running) {
						DatagramPacket packet = new DatagramPacket(new byte[3000], 3000);
						try {
							fromSocket.receive(packet);
							String sipMessage = new String(packet.getData());
							sipMessages.add(sipMessage);
							sipMessageWithoutRetrans.add(sipMessage);
							packet.setPort(toPort);
							fromSocket.send(packet);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					
				}
			};
			worker.start();
		} catch (Exception e) {
			throw new RuntimeException("Error", e);
		}
	}
	
	public void stop() {
		running = false;
		try {
			worker.interrupt();
			fromSocket.close();
		} catch (Exception e) {
			//e.printStackTrace();
		}
		
	}
}
