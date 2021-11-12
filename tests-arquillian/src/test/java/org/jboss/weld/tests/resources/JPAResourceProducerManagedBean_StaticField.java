package org.jboss.weld.tests.resources;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;

@Dependent
public class JPAResourceProducerManagedBean_StaticField {
    @Produces
    @PersistenceUnit(unitName = "pu1")
    @ProducedViaStaticFieldOnManagedBean
    public static EntityManagerFactory customerDatabasePersistenceUnit1;
}
