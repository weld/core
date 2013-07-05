package org.jboss.weld.tests.resources.producer.non.dependent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class ResourceProducer {

    @SuppressWarnings("unused")
    @Produces
    @ApplicationScoped
    @PersistenceContext(unitName = "pu1")
    private EntityManager entityManager;

}
