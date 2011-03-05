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

import static org.jboss.netty.channel.Channels.*;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.http.HttpRequestEncoder;
import org.jboss.netty.handler.codec.http.HttpResponseDecoder;
import org.jboss.netty.handler.codec.rtsp.RtspRequestEncoder;
import org.jboss.netty.handler.codec.rtsp.RtspResponseDecoder;

/**
 * @author The Netty Project (netty-dev@lists.jboss.org)
 * @author Andy Taylor (andy.taylor@jboss.org)
 * @author Trustin Lee (trustin@gmail.com)
 * @author Vladimir Ralev (vladimir.ralev@jboss.org)
 * 
 */
public class RtspClientPipelineFactory implements ChannelPipelineFactory {
	RTSPStack rtsp;
	public RtspClientPipelineFactory(RTSPStack rtsp) {
		this.rtsp = rtsp;
	}
    public ChannelPipeline getPipeline() throws Exception {
        // Create a default pipeline implementation.
        ChannelPipeline pipeline = pipeline();
        pipeline.addLast("decoder", new RtspResponseDecoder());
        // Remove the following line if you don't want automatic content decompression.
        //pipeline.addLast("inflater", new HttpContentDecompressor());
        // Uncomment the following line if you don't want to handle HttpChunks.
        //pipeline.addLast("aggregator", new HttpChunkAggregator(1048576));
        pipeline.addLast("encoder", new RtspRequestEncoder());
        RtspResponseHandler rtspResponseHandler = new RtspResponseHandler();
        pipeline.addLast("handler", rtspResponseHandler);
        pipeline.getContext(rtspResponseHandler).setAttachment(rtsp);
        return pipeline;
    }
}
