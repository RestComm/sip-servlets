package org.mobicents.servlet.sip.example;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

import net.java.stun4j.StunAddress;
import net.java.stun4j.client.NetworkConfigurationDiscoveryProcess;

public class RTPPacketForwarder {
	String from;
	String to;
	int fromPort;
	int toPort;
	DatagramSocket fromSocket;
	boolean running;
	byte payloadType;
	long numPackets = 0;
	StunAddress stunAddress;
	
	Thread forwardingThread = new Thread() {
		public void run() {
			while(running) {
				DatagramPacket packet = new DatagramPacket(new byte[5000], 5000);
				try {
					fromSocket.receive(packet);
					packet.setSocketAddress(new InetSocketAddress(to, toPort));
					packet.getData()[1] = payloadType;
					fromSocket.send(packet);
					numPackets++;
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		}
	};
	public RTPPacketForwarder(String from, String to, int fromPort, int toPort, byte payloadType) {
		this.from = from;
		this.to = to;
		this.fromPort = fromPort;
		this.toPort = toPort;
		this.payloadType = payloadType;
	}
	
	public StunAddress getStunAddress() {
		return stunAddress;
	}
	
	public void start() {
		try {
			/*
			NetworkConfigurationDiscoveryProcess addressDiscovery = new NetworkConfigurationDiscoveryProcess(
					new StunAddress(from, fromPort), new StunAddress("stun.softjoys.com", 3478));
			addressDiscovery.start();
			this.stunAddress = addressDiscovery.determineAddress().getPublicAddress();
			addressDiscovery.shutDown();*/
			fromSocket = new DatagramSocket(new InetSocketAddress(from, fromPort));
			
			running = true;
			forwardingThread.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void stop() {
		running = false;
		forwardingThread.interrupt();
		fromSocket.close();
	}
	
	
}
