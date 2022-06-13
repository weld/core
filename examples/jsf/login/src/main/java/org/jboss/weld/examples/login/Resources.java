package org.jboss.weld.examples.login;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.logging.Logger;

@ApplicationScoped
public class Resources {

    // Expose an entity manager using the resource producer pattern
    @SuppressWarnings("unused")
    @PersistenceContext
    @Produces
    private EntityManager em;

    @Produces
    Logger getLogger(InjectionPoint ip) {
        String category = ip.getMember().getDeclaringClass().getName();
        return Logger.getLogger(category);
    }

}
