package org.mobicents.servlet.sip.example;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

public class RTSPStack {
	static RTSPStack instance = new RTSPStack();
	ExecutorService executor;
	public String uri = "rtsp://v2.cache7.c.youtube.com/CiILENy73wIaGQmakfOL9oMJzBMYDSANFEgGUgZ2aWRlb3MM/0/0/0/video.3gp";
	public Channel channel = null;
	public String sdp;
	
	ClientBootstrap clientBootstrap;

	Integer cseq = 1;
	
	public String session = null;
	
	public String videoPort;
	
	public String audioPort;
	
	private LinkedList<RTPPacketForwarder> rtpRemappers = new LinkedList<RTPPacketForwarder>();
	
	public void init() {
		executor = Executors.newFixedThreadPool(2);
		
		 clientBootstrap = new ClientBootstrap(
	            new NioClientSocketChannelFactory(
	                    executor,
	                    executor));
		 clientBootstrap.setPipelineFactory(new RtspClientPipelineFactory(this));
		 try {
			 URI rtspURI = new URI(uri);
			 String host = rtspURI.getHost();
			 int port = rtspURI.getPort();
			 ChannelFuture future = clientBootstrap.connect(
					 new InetSocketAddress(host, port>0?port:554));

			 future.addListener(new ChannelFutureListener() {
				 public void operationComplete(ChannelFuture arg0) throws Exception {
					 channel = arg0.getChannel();
					 sendOptions();
				 }
			 });
		 } catch (Exception e) {
			 e.printStackTrace();
		 }
	}
	
	public void destroy() {
		channel.disconnect();
		channel.close();
		clientBootstrap.releaseExternalResources();
		executor.shutdownNow();
		for(RTPPacketForwarder fwd:rtpRemappers) {
			fwd.stop();
		}
	}
	
	public void addCSeq(DefaultRtspRequest req) {
		req.setHeader("CSeq", cseq.toString());
		cseq++;
	}
	
	public void sendOptions() throws URISyntaxException {
		DefaultRtspRequest req = new DefaultRtspRequest(new RtspVersion("RTSP/1.0"), new RtspMethod("OPTIONS"), uri);
		addCSeq(req);
		channel.write(req);
	}
	
	public void sendDescribe() throws URISyntaxException {
		DefaultRtspRequest req = new DefaultRtspRequest(new RtspVersion("RTSP/1.0"), new RtspMethod("DESCRIBE"), uri);
		addCSeq(req);
		req.setHeader("Accept", "application/sdp");
		channel.write(req);
	}
	public void sendSetup(String track, String port) throws URISyntaxException {
		DefaultRtspRequest req = new DefaultRtspRequest(new RtspVersion("RTSP/1.0"), new RtspMethod("SETUP"), uri+"/" + track);
		addCSeq(req);
		req.addHeader("Transport", "RTP/AVP;unicast;client_port="+port);
		channel.write(req);
	}
	public void sendPlay() throws URISyntaxException {
		DefaultRtspRequest req = new DefaultRtspRequest(new RtspVersion("RTSP/1.0"), new RtspMethod("PLAY"), uri);
		addCSeq(req);
		req.addHeader("Session", session);
		req.addHeader("Range", "npt=0.000");
		channel.write(req);
	}
	public void sendTeardown() throws URISyntaxException {
		DefaultRtspRequest req = new DefaultRtspRequest(new RtspVersion("RTSP/1.0"), new RtspMethod("TEARDOWN"), uri);
		addCSeq(req);
		req.addHeader("Session", session);
		channel.write(req);
	}
	
	public void addRTPMapper(RTPPacketForwarder fwd) {
		rtpRemappers.add(fwd);
		fwd.start();
	}
	
	public static void main(String[] arg) {
		RTSPStack.instance.init();
	}
}
