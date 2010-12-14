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
