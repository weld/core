package org.jboss.webbeans.test.integration;

import javax.transaction.Status;
import javax.transaction.UserTransaction;
import javax.webbeans.AnnotationLiteral;
import javax.webbeans.Current;

import org.jboss.webbeans.test.AbstractEjbEmbeddableTest;
import org.jboss.webbeans.test.mock.MockWebBeanDiscovery;
import org.testng.annotations.Test;

public class TransactionIntegrationTest extends AbstractEjbEmbeddableTest
{
   
   @Test
   public void testTransactionLookup() throws Exception
   {
      new RunInDependentContext()
      {

         @Override
         protected void execute() throws Exception
         {
            webBeansBootstrap.setWebBeanDiscovery(new MockWebBeanDiscovery());
            webBeansBootstrap.boot();
            UserTransaction userTransaction = manager.getInstanceByType(UserTransaction.class, new AnnotationLiteral<Current>() {});
            assert userTransaction.getStatus() == Status.STATUS_NO_TRANSACTION;
         }

      
      }.run();
         
   }
   
   @Test
   public void testWBTransactionLookup() throws Exception
   {
      new RunInDependentContext()
      {

         @Override
         protected void execute() throws Exception
         {
            webBeansBootstrap.setWebBeanDiscovery(new MockWebBeanDiscovery());
            webBeansBootstrap.boot();
            org.jboss.webbeans.transaction.UserTransaction userTransaction = manager.getInstanceByType(org.jboss.webbeans.transaction.UserTransaction.class, new AnnotationLiteral<Current>() {});
            assert userTransaction.isNoTransaction();
         }

      
      }.run();
         
   }
   
}
