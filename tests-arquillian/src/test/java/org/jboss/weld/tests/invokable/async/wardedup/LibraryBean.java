package org.jboss.weld.tests.invokable.async.wardedup;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class LibraryBean {
    public String ping() {
        return "pong";
    }
}
