package org.jboss.weld.tests.resources;

import javax.enterprise.inject.Produces;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

public class JPAResourceProducerManagedBean_InstanceField {
    @Produces
    @PersistenceUnit(unitName = "pu1")
    @ProducedViaInstanceFieldOnManagedBean
    public EntityManagerFactory customerDatabasePersistenceUnit1;
}
