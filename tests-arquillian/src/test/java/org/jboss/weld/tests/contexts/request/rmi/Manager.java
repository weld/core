package org.jboss.weld.tests.contexts.request.rmi;

import javax.ejb.Stateless;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;

@Stateless
public class Manager {
    @Produces
    @RequestScoped
    @My
    public Config getCurrentConfig() {
        return new Config();
    }
}
