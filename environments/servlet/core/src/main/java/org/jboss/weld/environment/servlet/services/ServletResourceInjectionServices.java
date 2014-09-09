package org.jboss.weld.environment.servlet.services;

import org.jboss.weld.environment.servlet.logging.WeldServletLogger;
import org.jboss.weld.injection.spi.ResourceInjectionServices;
import org.jboss.weld.injection.spi.helpers.AbstractResourceServices;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public abstract class ServletResourceInjectionServices extends AbstractResourceServices implements ResourceInjectionServices {

    private Context context;

    public ServletResourceInjectionServices() {
        try {
            context = new InitialContext();
        } catch (NamingException e) {
            throw WeldServletLogger.LOG.errorCreatingJNDIContext(e);
        }
    }

    @Override
    protected Context getContext() {
        return context;
    }

}
