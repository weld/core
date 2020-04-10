package org.jboss.weld.tests.resources;

import jakarta.enterprise.inject.Produces;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;

public class JPAResourceProducerManagedBean_InstanceField {
    @Produces
    @PersistenceUnit(unitName = "pu1")
    @ProducedViaInstanceFieldOnManagedBean
    public EntityManagerFactory customerDatabasePersistenceUnit1;
}
