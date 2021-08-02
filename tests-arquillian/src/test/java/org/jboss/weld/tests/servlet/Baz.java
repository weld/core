package org.jboss.weld.tests.servlet;

import jakarta.enterprise.context.Dependent;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

@Dependent
public class Baz implements ServletContextListener {

    public void contextDestroyed(ServletContextEvent sce) {
    }

    public void contextInitialized(ServletContextEvent sce) {
    }

}
