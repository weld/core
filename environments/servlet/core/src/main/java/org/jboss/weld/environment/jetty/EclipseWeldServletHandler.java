package org.jboss.weld.environment.jetty;


import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;
import org.jboss.weld.environment.servlet.logging.JettyLogger;

/**
 * @author <a href="mailto:matija.mazi@gmail.com">Matija Mazi</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @deprecated As of Jetty 7.2 Weld implements {@link org.eclipse.jetty.servlet.ServletContextHandler.Decorator} to inject servlets and filters
 */
@Deprecated
public class EclipseWeldServletHandler extends ServletHandler {

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
            JettyLogger.LOG.cantFindInjectior(injectable);
        } else {
            injector.inject(injectable);
        }
    }

    protected static void process(WebAppContext wac, boolean startNewHandler) throws Exception {
        EclipseWeldServletHandler wHandler = new EclipseWeldServletHandler(wac.getServletHandler(), wac.getServletContext());
        wac.setServletHandler(wHandler);
        wac.getSecurityHandler().setHandler(wHandler);

        if (startNewHandler) {
            wHandler.start();
        }

        Resource jettyEnv = null;
        Resource webInf = wac.getWebInf();
        if (webInf != null && webInf.exists()) {
            jettyEnv = webInf.addPath("jetty-env.xml");
        }
        if (jettyEnv == null || !(jettyEnv.exists())) {
            JettyLogger.LOG.missingJettyEnvXml();
        }
    }

    public static void process(WebAppContext wac) throws Exception {
        process(wac, false);
    }

    public static void process(ServletContext context) throws Exception {
        WebAppContext wac = WebAppContext.getCurrentWebAppContext();
        if (wac == null) {
            wac = findWAC(context);
        }

        if (wac != null) {
            process(wac, true);
        } else {
            JettyLogger.LOG.cantFindMatchingWebApplicationContext();
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
                            if (wac.getServletHandler() == servletHandler) {
                                return wac;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
}
