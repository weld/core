package org.jboss.weld.tests.servlet;

import jakarta.enterprise.context.Dependent;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

@Dependent
public class Qux implements HttpSessionListener {

    public void sessionCreated(HttpSessionEvent se) {

    }

    public void sessionDestroyed(HttpSessionEvent se) {
    }

}
