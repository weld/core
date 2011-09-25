package org.jboss.weld.tests.resources;

import javax.enterprise.inject.Produces;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

public class JPAResourceProducerManagedBean_StaticField {
    @Produces
    @PersistenceUnit(unitName = "pu1")
    @ProducedViaStaticFieldOnManagedBean
    public static EntityManagerFactory customerDatabasePersistenceUnit1;
}
