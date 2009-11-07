package org.jboss.weld.tests.builtinBeans.ee;

import static org.jboss.weld.tests.builtinBeans.ee.Checker.checkEntityManager;
import static org.jboss.weld.tests.builtinBeans.ee.Checker.checkEntityManagerFactory;
import static org.jboss.weld.tests.builtinBeans.ee.Checker.checkRemoteEjb;
import static org.jboss.weld.tests.builtinBeans.ee.Checker.checkUserTransaction;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;

@SessionScoped
public class EEResourceConsumer implements Serializable
{
   
   @Inject @Produced UserTransaction userTransaction;
   @Inject @Produced EntityManager entityManager;
   @Inject @Produced EntityManagerFactory entityManagerFactory;
   @Inject @Produced HorseRemote horse;
   
   public void check()
   {
      assert checkUserTransaction(userTransaction);
      assert checkEntityManager(entityManager);
      assert checkEntityManagerFactory(entityManagerFactory);
      assert checkRemoteEjb(horse);
   }

}
