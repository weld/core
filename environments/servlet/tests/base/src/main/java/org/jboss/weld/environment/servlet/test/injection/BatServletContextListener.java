package org.jboss.weld.environment.servlet.test.injection;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

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
