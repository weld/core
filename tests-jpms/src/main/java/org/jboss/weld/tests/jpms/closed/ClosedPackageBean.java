package org.jboss.weld.tests.jpms.closed;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ClosedPackageBean {
    public String value() {
        return "closed";
    }
}
