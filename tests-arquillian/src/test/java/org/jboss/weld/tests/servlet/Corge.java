package org.jboss.weld.tests.servlet;

import jakarta.enterprise.context.Dependent;
import jakarta.servlet.ServletRequestEvent;
import jakarta.servlet.ServletRequestListener;

@Dependent
public class Corge implements ServletRequestListener {

    public void requestDestroyed(ServletRequestEvent sre) {
    }

    public void requestInitialized(ServletRequestEvent sre) {
    }

}
