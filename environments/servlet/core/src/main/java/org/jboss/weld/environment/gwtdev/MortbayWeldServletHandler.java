package org.jboss.weld.environment.gwtdev;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;

import org.jboss.weld.environment.jetty.AbstractJettyContainer;
import org.jboss.weld.environment.jetty.JettyWeldInjector;
import org.jboss.weld.environment.servlet.logging.JettyLogger;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.handler.HandlerCollection;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.resource.Resource;

/**
 * @author <a href="mailto:matija.mazi@gmail.com">Matija Mazi</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class MortbayWeldServletHandler extends ServletHandler {

    private ServletContext sco;
    private JettyWeldInjector injector;

    public MortbayWeldServletHandler(ServletHandler existingHandler, ServletContext servletContext) {
        sco = servletContext;
        setFilters(existingHandler.getFilters());
        setFilterMappings(existingHandler.getFilterMappings());
        setServlets(existingHandler.getServlets());
        setServletMappings(existingHandler.getServletMappings());
    }

    @Override
    public Servlet customizeServlet(Servlet servlet) throws Exception {
        inject(servlet);
        return servlet;
    }

    @Override
    public Filter customizeFilter(Filter filter) throws Exception {
        inject(filter);
        return filter;
    }

    protected void inject(Object injectable) {
        if (injector == null) {
            injector = (JettyWeldInjector) sco.getAttribute(AbstractJettyContainer.INJECTOR_ATTRIBUTE_NAME);
        }
        if (injector == null) {
            JettyLogger.LOG.cantFindInjector(injectable);
        } else {
            injector.inject(injectable);
        }
    }

    protected static void process(WebAppContext wac, boolean startNewHandler) throws Exception {
        MortbayWeldServletHandler wHanlder = new MortbayWeldServletHandler(wac.getServletHandler(), wac.getServletContext());
        wac.setServletHandler(wHanlder);
        wac.getSecurityHandler().setHandler(wHanlder);

        if (startNewHandler) {
            wHanlder.start();
        }

        Resource jettyEnv = null;
        Resource webInf = wac.getWebInf();
        if (webInf != null && webInf.exists()) {
            jettyEnv = webInf.addPath("jetty-env.xml");
        }
        if (jettyEnv == null || !(jettyEnv.exists())) {
            JettyLogger.LOG.missingJettyEnv();
        }
    }

    public static void process(WebAppContext wac) throws Exception {
        process(wac, false);
    }

    public static void process(ServletContext context) throws Exception {
        WebAppContext wac = (WebAppContext) WebAppContext.getCurrentWebAppContext();
        if (wac == null) {
            wac = findWAC(context);
        }

        if (wac != null) {
            process(wac, true);
        } else {
            JettyLogger.LOG.cantFindWebApplicationContext();
        }
    }

    protected static WebAppContext findWAC(ServletContext context) {
        if (context instanceof ContextHandler.SContext) {
            ContextHandler.SContext sContext = (ContextHandler.SContext) context;
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
