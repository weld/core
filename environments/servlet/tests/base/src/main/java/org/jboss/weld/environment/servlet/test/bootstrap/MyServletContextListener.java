package org.jboss.weld.environment.servlet.test.bootstrap;

import static org.jboss.weld.environment.servlet.test.bootstrap.EventHolder.events;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

public class MyServletContextListener implements ServletContextListener {

    public void contextDestroyed(ServletContextEvent sce) {
        // TODO Auto-generated method stub

    }

    public void contextInitialized(ServletContextEvent sce) {
        events.add(sce);
    }

}
