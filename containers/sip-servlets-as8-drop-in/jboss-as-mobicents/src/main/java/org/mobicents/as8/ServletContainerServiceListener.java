package org.mobicents.as8;

import io.undertow.servlet.api.JBossConvergedServletContainer;

import java.lang.reflect.Field;
import java.util.concurrent.Semaphore;

import org.jboss.msc.service.AbstractServiceListener;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceListener;
import org.wildfly.extension.undertow.ServletContainerService;

@SuppressWarnings({ "deprecation", "rawtypes" })
public class ServletContainerServiceListener extends AbstractServiceListener implements ServiceListener {
    private static final String SERVLETCONTAINERFIELDNAME = "servletContainer";
    private ServletContainerService service = null;
    private transient Semaphore semaphore = new Semaphore(-1);
    private static ServletContainerServiceListener listener = null;

    public static synchronized ServletContainerServiceListener newInstance() {
        if (listener == null) {
            listener = new ServletContainerServiceListener();
        }
        return listener;
    }

    // singleton
    private ServletContainerServiceListener() {
    }

    public void setService(ServletContainerService service) {
        if (this.service == null) {
            this.service = service;
        } else {
            // TODO error
        }
    }

    public void acquireSemaphore() {
        semaphore.acquireUninterruptibly();
    }

    public void releaseSemaphore() {
        semaphore.release();
    }

    private void releaseMaxSemaphore() {
        semaphore.release(Integer.MAX_VALUE);
    }

    @Override
    public void transition(final ServiceController controller, final ServiceController.Transition transition) {
        if (transition == ServiceController.Transition.STARTING_to_UP) {
            
            while (service.getServletContainer() == null) {
                // TODO timeout
            }
            Class serviceClass = service.getClass();

            for (Field field : serviceClass.getDeclaredFields()) {
                if (ServletContainerServiceListener.SERVLETCONTAINERFIELDNAME.equals(field.getName())) {
                    field.setAccessible(true);
                    try {
                        field.set(service, JBossConvergedServletContainer.Factory.newInstance());
                    } catch (IllegalArgumentException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    field.setAccessible(false);
                }
            }
            this.releaseMaxSemaphore();
       }
    }
}