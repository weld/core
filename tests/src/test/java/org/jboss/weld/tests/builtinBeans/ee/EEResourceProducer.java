package org.jboss.weld.tests.builtinBeans.ee;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import javax.transaction.UserTransaction;

public class EEResourceProducer
{
   
   @Resource(mappedName="java:comp/UserTransaction")
   @Produced
   @Produces
   private UserTransaction transaction;
   
   @PersistenceContext
   @Produces
   @Produced
   private EntityManager entityManager;
   
   @PersistenceUnit
   @Produces
   @Produced
   private EntityManagerFactory entityManagerFactory;
   
   @EJB
   @Produces
   @Produced
   private HorseRemote horse;

}
