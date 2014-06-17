package org.jboss.weld.environment.servlet.test.injection;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class BatServletContextListener extends BatListener implements ServletContextListener {

    public void contextInitialized(ServletContextEvent sce) {
        if (isSewerNameOk()) {
            sce.getServletContext().setAttribute(BAT_ATTRIBUTE_NAME, Boolean.TRUE);
        }
    }

    public void contextDestroyed(ServletContextEvent sce) {
        isSewerNameOk();
    }

}
