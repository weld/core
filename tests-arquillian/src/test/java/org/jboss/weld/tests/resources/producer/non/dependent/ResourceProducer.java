package org.jboss.weld.tests.resources.producer.non.dependent;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Dependent
public class ResourceProducer {

    @SuppressWarnings("unused")
    @Produces
    @ApplicationScoped
    @PersistenceContext(unitName = "pu1")
    private EntityManager entityManager;

}
