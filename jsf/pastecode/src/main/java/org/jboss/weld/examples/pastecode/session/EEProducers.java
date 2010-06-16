package org.jboss.weld.examples.pastecode.session;

import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class EEProducers
{
   
   @SuppressWarnings("unused")
   @PersistenceContext(unitName = "pastecodeDatabase")
   @Produces
   private EntityManager em;
   
}
