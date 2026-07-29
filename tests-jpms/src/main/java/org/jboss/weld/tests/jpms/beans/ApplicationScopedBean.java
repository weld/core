package org.jboss.weld.tests.jpms.beans;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ApplicationScopedBean {
    public String hello() {
        return "hello";
    }
}
