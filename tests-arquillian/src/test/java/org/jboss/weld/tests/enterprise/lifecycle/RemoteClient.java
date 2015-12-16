package org.jboss.weld.tests.enterprise.lifecycle;

import org.jboss.weld.context.RequestContext;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.test.util.Utils;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.jboss.weld.test.util.Utils.getActiveContext;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@WebServlet("/")
public class RemoteClient extends HttpServlet {

    @Inject
    BeanManagerImpl beanManager;

    @Inject
    GrossStadt frankfurt;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        // return on empty path request
        if (pathInfo == null)
            return;

        try {
            RequestContext requestContext = getActiveContext(beanManager, RequestContext.class);
            Bean<KleinStadt> stadtBean = Utils.getBean(beanManager, KleinStadt.class);
            if (pathInfo.equals("/request1")) {
                assertNotNull("Expected a bean for stateful session bean Kassel", stadtBean);
                CreationalContext<KleinStadt> creationalContext = beanManager.createCreationalContext(stadtBean);
                KleinStadt kassel = requestContext.get(stadtBean, creationalContext);
                stadtBean.destroy(kassel, creationalContext);

                assertTrue("Expected SFSB bean to be destroyed", frankfurt.isKleinStadtDestroyed());
                return;
            } else if (pathInfo.equals("/request2")) {
                KleinStadt kassel = requestContext.get(stadtBean);
                assertNull("SFSB bean should not exist after being destroyed", kassel);
                return;
            }
        } catch (AssertionError e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
}
