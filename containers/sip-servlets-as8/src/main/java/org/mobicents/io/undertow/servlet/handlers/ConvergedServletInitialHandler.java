/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2015, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package org.mobicents.io.undertow.servlet.handlers;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.concurrent.Executor;

import javax.servlet.DispatcherType;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mobicents.io.undertow.servlet.spec.ConvergedHttpServletRequestFacade;
import org.mobicents.io.undertow.servlet.spec.ConvergedHttpServletResponseFacade;
import org.mobicents.servlet.sip.startup.ConvergedServletContextImpl;
import org.xnio.BufferAllocator;
import org.xnio.ByteBufferSlicePool;
import org.xnio.ChannelListener;
import org.xnio.Option;
import org.xnio.OptionMap;
import org.xnio.Pool;
import org.xnio.StreamConnection;
import org.xnio.XnioIoThread;
import org.xnio.XnioWorker;
import org.xnio.channels.ConnectedChannel;
import org.xnio.conduits.ConduitStreamSinkChannel;
import org.xnio.conduits.ConduitStreamSourceChannel;
import org.xnio.conduits.StreamSinkConduit;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.HttpUpgradeListener;
import io.undertow.server.SSLSessionInfo;
import io.undertow.server.ServerConnection;
import io.undertow.servlet.core.CompositeThreadSetupAction;
import io.undertow.servlet.core.ServletBlockingHttpExchange;
import io.undertow.servlet.handlers.ServletChain;
import io.undertow.servlet.handlers.ServletInitialHandler;
import io.undertow.servlet.handlers.ServletPathMatch;
import io.undertow.servlet.handlers.ServletPathMatches;
import io.undertow.servlet.handlers.ServletRequestContext;
import io.undertow.servlet.spec.HttpServletRequestImpl;
import io.undertow.servlet.spec.HttpServletResponseImpl;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import io.undertow.util.Protocols;
import io.undertow.util.RedirectBuilder;
import io.undertow.util.StatusCodes;

/**
 * This class extends io.undertow.servlet.handlers.ServletInitialHandler to create ConvergedServletRequestContext with
 * ConvergedHttpServletRequestFacade and ConvergedHttpServletResponseFacade instead of plain ServletRequestContex.
 *
 * @author kakonyi.istvan@alerant.hu
 * */
public class ConvergedServletInitialHandler extends ServletInitialHandler{
    private static final String HTTP2_UPGRADE_PREFIX = "h2";

    private final ConvergedServletContextImpl convergedServletContext;

    public ConvergedServletInitialHandler(final ServletPathMatches paths,final HttpHandler next,final CompositeThreadSetupAction setupAction,
            final ConvergedServletContextImpl servletContext) {
        super(paths, next, setupAction, servletContext.getDelegatedContext());
        this.convergedServletContext = servletContext;

    }

    @Override
    public void handleRequest(final HttpServerExchange exchange) throws Exception {
        ServletPathMatches paths=null;
        try{
            //lets get access of superclass private fields using reflection:
            Field pathsField = ServletInitialHandler.class.getDeclaredField("paths");
            pathsField.setAccessible(true);
            paths = (ServletPathMatches)pathsField.get(this);
            pathsField.setAccessible(false);
        }catch(NoSuchFieldException | IllegalAccessException e){
            throw new ServletException(e);
        }

        final String path = exchange.getRelativePath();
        if(isForbiddenPath(path)) {
            exchange.setResponseCode(StatusCodes.NOT_FOUND);
            return;
        }
        final ServletPathMatch info = paths.getServletHandlerByPath(path);
        //https://issues.jboss.org/browse/WFLY-3439
        //if the request is an upgrade request then we don't want to redirect
        //as there is a good chance the web socket client won't understand the redirect
        //we make an exception for HTTP2 upgrade requests, as this would have already be handled at
        //the connector level if it was going to be handled.
        String upgradeString = exchange.getRequestHeaders().getFirst(Headers.UPGRADE);
        boolean isUpgradeRequest = upgradeString != null && !upgradeString.startsWith(HTTP2_UPGRADE_PREFIX);
        if (info.getType() == ServletPathMatch.Type.REDIRECT && !isUpgradeRequest) {
            //UNDERTOW-89
            //we redirect on GET requests to the root context to add an / to the end
            exchange.setResponseCode(StatusCodes.TEMPORARY_REDIRECT);
            exchange.getResponseHeaders().put(Headers.LOCATION, RedirectBuilder.redirect(exchange, exchange.getRelativePath() + "/", true));
            return;
        } else if (info.getType() == ServletPathMatch.Type.REWRITE) {
            //this can only happen if the path ends with a /
            //otherwise there would be a rewrite instead
            exchange.setRelativePath(exchange.getRelativePath() + info.getRewriteLocation());
            exchange.setRequestURI(exchange.getRequestURI() + info.getRewriteLocation());
            exchange.setRequestPath(exchange.getRequestPath() + info.getRewriteLocation());
        }

        final HttpServletResponseImpl response = new HttpServletResponseImpl(exchange, convergedServletContext.getDelegatedContext());
        final HttpServletRequestImpl request = new HttpServletRequestImpl(exchange, convergedServletContext.getDelegatedContext());
        final ConvergedHttpServletResponseFacade convergedResponse = new ConvergedHttpServletResponseFacade(response,convergedServletContext);
        final ConvergedHttpServletRequestFacade convergedRequest = new ConvergedHttpServletRequestFacade(request,convergedServletContext);
        final ServletRequestContext servletRequestContext = new ConvergedServletRequestContext(convergedServletContext.getDeployment(), convergedRequest, convergedResponse, info);
        //set the max request size if applicable
        if (info.getServletChain().getManagedServlet().getMaxRequestSize() > 0) {
            exchange.setMaxEntitySize(info.getServletChain().getManagedServlet().getMaxRequestSize());
        }
        exchange.putAttachment(ServletRequestContext.ATTACHMENT_KEY, servletRequestContext);

        exchange.startBlocking(new ServletBlockingHttpExchange(exchange));
        servletRequestContext.setServletPathMatch(info);

        Executor executor = info.getServletChain().getExecutor();
        if (executor == null) {
            executor = convergedServletContext.getDeployment().getExecutor();
        }

        if (exchange.isInIoThread() || executor != null) {
            //either the exchange has not been dispatched yet, or we need to use a special executor
            exchange.dispatch(executor, new HttpHandler() {
                @Override
                public void handleRequest(final HttpServerExchange exchange) throws Exception {
                    if(System.getSecurityManager() == null) {
                        dispatchRequest(exchange, servletRequestContext, info.getServletChain(), DispatcherType.REQUEST);
                    } else {
                        //sometimes thread pools inherit some random
                        AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
                            @Override
                            public Object run() throws Exception{
                                dispatchRequest(exchange, servletRequestContext, info.getServletChain(), DispatcherType.REQUEST);
                                return null;
                            }
                        });
                    }
                }
            });
        } else {
            dispatchRequest(exchange, servletRequestContext, info.getServletChain(), DispatcherType.REQUEST);
        }
    }

    @Override
    public void dispatchMockRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        ServletPathMatches paths=null;
        try{
            //lets get access of superclass private fields using reflection:
            Field pathsField = ServletInitialHandler.class.getDeclaredField("paths");
            pathsField.setAccessible(true);
            paths = (ServletPathMatches)pathsField.get(this);
            pathsField.setAccessible(false);
        }catch(NoSuchFieldException | IllegalAccessException e){
            throw new ServletException(e);
        }

        final ByteBufferSlicePool bufferPool = new ByteBufferSlicePool(BufferAllocator.BYTE_BUFFER_ALLOCATOR, 1024, 1024);
        MockServerConnection connection = new MockServerConnection(bufferPool);
        HttpServerExchange exchange = new HttpServerExchange(connection);
        exchange.setRequestScheme(request.getScheme());
        exchange.setRequestMethod(new HttpString(request.getMethod()));
        exchange.setProtocol(Protocols.HTTP_1_0);
        exchange.setResolvedPath(request.getContextPath());
        String relative;
        if (request.getPathInfo() == null) {
            relative = request.getServletPath();
        } else {
            relative = request.getServletPath() + request.getPathInfo();
        }
        exchange.setRelativePath(relative);
        final ServletPathMatch info = paths.getServletHandlerByPath(request.getServletPath());
        final HttpServletResponseImpl oResponse = new HttpServletResponseImpl(exchange, convergedServletContext.getDelegatedContext());
        final HttpServletRequestImpl oRequest = new HttpServletRequestImpl(exchange, convergedServletContext.getDelegatedContext());
        final ServletRequestContext servletRequestContext = new ServletRequestContext(convergedServletContext.getDeployment(), oRequest, oResponse, info);
        servletRequestContext.setServletRequest(request);
        servletRequestContext.setServletResponse(response);
        //set the max request size if applicable
        if (info.getServletChain().getManagedServlet().getMaxRequestSize() > 0) {
            exchange.setMaxEntitySize(info.getServletChain().getManagedServlet().getMaxRequestSize());
        }
        exchange.putAttachment(ServletRequestContext.ATTACHMENT_KEY, servletRequestContext);

        exchange.startBlocking(new ServletBlockingHttpExchange(exchange));
        servletRequestContext.setServletPathMatch(info);

        try {
            dispatchRequest(exchange, servletRequestContext, info.getServletChain(), DispatcherType.REQUEST);
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new ServletException(e);
        }
    }

    //FIXME:kakonyii: This method is copied form the base class:
    private boolean isForbiddenPath(String path) {
        return path.equalsIgnoreCase("/meta-inf/")
            || path.regionMatches(true, 0, "/web-inf/", 0, "/web-inf/".length());
    }

    //FIXME:kakonyii: This method is copied form the base class:
    private void dispatchRequest(final HttpServerExchange exchange, final ServletRequestContext servletRequestContext, final ServletChain servletChain, final DispatcherType dispatcherType) throws Exception {
        HttpHandler next=null;
        try{
            //lets get access of superclass private fields using reflection:
            Field nextField = ServletInitialHandler.class.getDeclaredField("next");
            nextField.setAccessible(true);
            next = (HttpHandler)nextField.get(this);
            nextField.setAccessible(false);
        }catch(NoSuchFieldException | IllegalAccessException e){
            throw new ServletException(e);
        }

        servletRequestContext.setDispatcherType(dispatcherType);
        servletRequestContext.setCurrentServlet(servletChain);
        if (dispatcherType == DispatcherType.REQUEST || dispatcherType == DispatcherType.ASYNC) {
            super.handleFirstRequest(exchange, servletChain, servletRequestContext, servletRequestContext.getServletRequest(), servletRequestContext.getServletResponse());
        } else {
            next.handleRequest(exchange);
        }
    }

    //FIXME:kakonyii: This class is copied form the base class:
    private static class MockServerConnection extends ServerConnection {
        private final Pool<ByteBuffer> bufferPool;
        private SSLSessionInfo sslSessionInfo;

        private MockServerConnection(Pool<ByteBuffer> bufferPool) {
            this.bufferPool = bufferPool;
        }

        @Override
        public Pool<ByteBuffer> getBufferPool() {
            return bufferPool;
        }

        @Override
        public XnioWorker getWorker() {
            return null;
        }

        @Override
        public XnioIoThread getIoThread() {
            return null;
        }

        @Override
        public HttpServerExchange sendOutOfBandResponse(HttpServerExchange exchange) {
            throw new IllegalStateException();
        }

        @Override
        public void terminateRequestChannel(HttpServerExchange exchange) {

        }

        @Override
        public boolean isOpen() {
            return true;
        }

        @Override
        public boolean supportsOption(Option<?> option) {
            return false;
        }

        @Override
        public <T> T getOption(Option<T> option) throws IOException {
            return null;
        }

        @Override
        public <T> T setOption(Option<T> option, T value) throws IllegalArgumentException, IOException {
            return null;
        }

        @Override
        public void close() throws IOException {
        }

        @Override
        public SocketAddress getPeerAddress() {
            return null;
        }

        @Override
        public <A extends SocketAddress> A getPeerAddress(Class<A> type) {
            return null;
        }

        @Override
        public ChannelListener.Setter<? extends ConnectedChannel> getCloseSetter() {
            return null;
        }

        @Override
        public SocketAddress getLocalAddress() {
            return null;
        }

        @Override
        public <A extends SocketAddress> A getLocalAddress(Class<A> type) {
            return null;
        }

        @Override
        public OptionMap getUndertowOptions() {
            return OptionMap.EMPTY;
        }

        @Override
        public int getBufferSize() {
            return 1024;
        }

        @Override
        public SSLSessionInfo getSslSessionInfo() {
            return sslSessionInfo;
        }

        @Override
        public void setSslSessionInfo(SSLSessionInfo sessionInfo) {
            sslSessionInfo = sessionInfo;
        }

        @Override
        public void addCloseListener(CloseListener listener) {
        }

        @Override
        public StreamConnection upgradeChannel() {
            return null;
        }

        @Override
        public ConduitStreamSinkChannel getSinkChannel() {
            return null;
        }

        @Override
        public ConduitStreamSourceChannel getSourceChannel() {
            return new ConduitStreamSourceChannel(null, null);
        }

        @Override
        protected StreamSinkConduit getSinkConduit(HttpServerExchange exchange, StreamSinkConduit conduit) {
            return conduit;
        }

        @Override
        protected boolean isUpgradeSupported() {
            return false;
        }

        @Override
        protected void exchangeComplete(HttpServerExchange exchange) {
        }

        @Override
        protected void setUpgradeListener(HttpUpgradeListener upgradeListener) {
            //ignore
        }

        @Override
        protected void maxEntitySizeUpdated(HttpServerExchange exchange) {
        }
    }
}
