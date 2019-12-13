package org.jboss.weld.tests.resources.producer.non.dependent;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class ResourceProducer {

    @SuppressWarnings("unused")
    @Produces
    @ApplicationScoped
    @PersistenceContext(unitName = "pu1")
    private EntityManager entityManager;

}
