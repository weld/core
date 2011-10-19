package org.jboss.weld.environment.jetty;


import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.webapp.WebAppContext;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:matija.mazi@gmail.com">Matija Mazi</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class EclipseWeldServletHandler extends ServletHandler {
    private static final Logger log = Logger.getLogger(EclipseWeldServletHandler.class.getName());

    private ServletContext sco;
    private JettyWeldInjector injector;

    public EclipseWeldServletHandler(ServletHandler existingHandler, ServletContext servletContext) {
        sco = servletContext;
        setFilters(existingHandler.getFilters());
        setFilterMappings(existingHandler.getFilterMappings());
        setServlets(existingHandler.getServlets());
        setServletMappings(existingHandler.getServletMappings());
    }

    public Servlet customizeServlet(Servlet servlet) throws Exception {
        inject(servlet);
        return servlet;
    }

    public Filter customizeFilter(Filter filter) throws Exception {
        inject(filter);
        return filter;
    }

    protected void inject(Object injectable) {
        if (injector == null) {
            injector = (JettyWeldInjector) sco.getAttribute(AbstractJettyContainer.INJECTOR_ATTRIBUTE_NAME);
        }
        if (injector == null) {
            log.warning("Can't find Injector in the servlet context so injection is not available for " + injectable);
        } else {
            injector.inject(injectable);
        }
    }

    public static void process(WebAppContext wac) {
        EclipseWeldServletHandler wHanlder = new EclipseWeldServletHandler(wac.getServletHandler(), wac.getServletContext());
        wac.setServletHandler(wHanlder);
        wac.getSecurityHandler().setHandler(wHanlder);
    }

    public static void process(ServletContext context) {
        WebAppContext wac = findWAC(context);
        if (wac != null) {
            process(wac);
        } else {
            log.info("Cannot find matching WebApplicationContext, no default CDI support: use jetty-env.xml");
        }
    }

    protected static WebAppContext findWAC(ServletContext context) {
        if (context instanceof ContextHandler.Context) {
            ContextHandler.Context sContext = (ContextHandler.Context) context;
            ContextHandler contextHandler = sContext.getContextHandler();
            Handler handler = contextHandler.getHandler();
            if (handler instanceof ServletHandler) {
                ServletHandler servletHandler = (ServletHandler) handler;
                Server server = servletHandler.getServer();
                Handler serverHandler = server.getHandler();
                if (serverHandler instanceof HandlerCollection) {
                    HandlerCollection hc = (HandlerCollection) serverHandler;
                    for (Handler h : hc.getHandlers()) {
                        if (h instanceof WebAppContext) {
                            WebAppContext wac = (WebAppContext) h;
                            if (wac.getServletHandler() == servletHandler)
                                return wac;
                        }
                    }
                }
            }
        }
        return null;
    }
}
