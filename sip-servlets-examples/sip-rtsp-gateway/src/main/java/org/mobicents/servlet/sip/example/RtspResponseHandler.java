/*
 * Copyright 2009 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.mobicents.servlet.sip.example;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpResponse;

/**
 * @author Vladimir Ralev (vladimir.ralev@jboss.org)
 *
 */
public class RtspResponseHandler extends SimpleChannelUpstreamHandler {
	private static final Logger logger = Logger.getLogger(RtspResponseHandler.class.getCanonicalName());
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    	RTSPStack rtsp = (RTSPStack) ctx.getAttachment();
    	if(e.getMessage() instanceof HttpResponse) {
    		HttpResponse response = (HttpResponse) e.getMessage();
    		String session = response.getHeader("Session");
    		if(session != null) {
    			rtsp.session = session;
    		}
    		ChannelBuffer buff = response.getContent();
    		if(buff != null) {
    			String sdp = new String(buff.array());
    			logger.info(">>>>>______SDP"+sdp);
    			if(sdp.length()>10) {
    				rtsp.sdp = sdp;
    			}
    		}
    		String cseq = response.getHeader("CSeq");
    		if(cseq.equals("1")) {
    			rtsp.sendDescribe();
    		} else if(cseq.equals("2")) {
    			rtsp.sendSetup("trackID=0",rtsp.videoPort);
    		} else if(cseq.equals("3")) {
    			rtsp.sendSetup("trackID=1",rtsp.audioPort);
    		} else if(cseq.equals("4")) {
    			rtsp.sendPlay();
    		} else if(cseq.equals("5")) {
    		}
    		
    	}
    	
    }

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		logger.log(Level.SEVERE, "Error", e.getCause());
	}

}
