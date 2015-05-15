/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2014, Telestax Inc and individual contributors
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
 *
*/
package io.undertow.servlet.core;

import java.util.ArrayList;

import javax.servlet.ServletException;

import org.mobicents.as8.deployment.SIPWebContext;

import io.undertow.server.HttpHandler;
import io.undertow.servlet.api.Deployment;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletContainer;
import io.undertow.servlet.api.ThreadSetupAction;
import io.undertow.servlet.core.ConvergedDeploymentManagerImpl;
/**
 * @author alerant.appngin@gmail.com
 *
 */
public class JBossConvergedDeploymentManagerImpl extends ConvergedDeploymentManagerImpl implements DeploymentManager {
    private volatile State state = State.UNDEPLOYED;
    
    public JBossConvergedDeploymentManagerImpl(DeploymentInfo deployment, ServletContainer servletContainer) {
        super(deployment, servletContainer);
    }

    @Override
    public void stop() throws ServletException {
        super.stop();
        state = super.getState();
    }

    @Override
    public void undeploy() {
        super.undeploy();
        state = super.getState();
    }

    @Override
    public void deploy() {
        super.deploy();
        state = super.getState();
    }

    @Override
    public HttpHandler start()  throws ServletException {
        ThreadSetupAction.Handle handle = deployment.getThreadSetupAction().setup(null);
        try {
            deployment.getSessionManager().start();

            //we need to copy before iterating
            //because listeners can add other listeners
            ArrayList<Lifecycle> lifecycles = new ArrayList<>(deployment.getLifecycleObjects());
            for (Lifecycle object : lifecycles) {
                object.start();
            }
            HttpHandler root = deployment.getHandler();
            
            //we skip servlet init after our SIPWebContext initialized succesfully in order to be able to do sip injections to normal servlets 
            
            state = State.STARTED;
            return root;
        } finally {
            handle.tearDown();
        }
    }
    
    @Override
    protected Deployment createDeployment(DeploymentManager manager, DeploymentInfo deploymentInfo,
            ServletContainer servletContainer) {
        SIPWebContext result = new SIPWebContext(manager, deploymentInfo, servletContainer);
        
        
        return result;
    }
    
    @Override
    public State getState() {
        return state;
    }
}
