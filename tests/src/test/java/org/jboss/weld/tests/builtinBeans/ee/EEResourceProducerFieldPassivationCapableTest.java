package org.jboss.weld.tests.builtinBeans.ee;

import static org.jboss.weld.tests.builtinBeans.ee.Checker.checkEntityManager;
import static org.jboss.weld.tests.builtinBeans.ee.Checker.checkEntityManagerFactory;
import static org.jboss.weld.tests.builtinBeans.ee.Checker.checkRemoteEjb;
import static org.jboss.weld.tests.builtinBeans.ee.Checker.checkUserTransaction;

import java.lang.annotation.Annotation;

import javax.enterprise.util.AnnotationLiteral;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.IntegrationTest;
import org.jboss.testharness.impl.packaging.Packaging;
import org.jboss.testharness.impl.packaging.PackagingType;
import org.jboss.testharness.impl.packaging.Resource;
import org.jboss.weld.test.AbstractWeldTest;
import org.jboss.weld.test.Utils;
import org.testng.annotations.Test;

@Artifact
@IntegrationTest
@Packaging(PackagingType.EAR)
@Resource(source = "persistence.xml", destination = "META-INF/persistence.xml")
public class EEResourceProducerFieldPassivationCapableTest extends AbstractWeldTest
{
   
   private static final Annotation PRODUCED = new AnnotationLiteral<Produced>() {};
   
   @Test
   public void testResource() throws Throwable
   {
      UserTransaction userTransaction = getReference(UserTransaction.class, PRODUCED);
      UserTransaction userTransaction1 = Utils.deserialize(Utils.serialize(userTransaction));
      assert checkUserTransaction(userTransaction1);
   }
   
   @Test
   public void testEntityManager() throws Throwable
   {
      EntityManager entityManager = getReference(EntityManager.class, PRODUCED);
      EntityManager entityManager1 = Utils.deserialize(Utils.serialize(entityManager));
      assert checkEntityManager(entityManager1);
   }
   
   @Test
   public void testEntityManagerFactory() throws Throwable
   {
      EntityManagerFactory entityManagerFactory = getReference(EntityManagerFactory.class, PRODUCED);
      EntityManagerFactory entityManagerFactory1 = Utils.deserialize(Utils.serialize(entityManagerFactory));
      assert checkEntityManagerFactory(entityManagerFactory1);
   }
   
   @Test
   public void testRemoteEjb() throws Throwable
   {
      HorseRemote horse = getReference(HorseRemote.class, PRODUCED);
      HorseRemote horse1 = Utils.deserialize(Utils.serialize(horse));
      assert checkRemoteEjb(horse1);
   }
   
   @Test
   public void testAllOnBean() throws Throwable
   {
      EEResourceConsumer consumer = getReference(EEResourceConsumer.class);
      consumer.check();
      EEResourceConsumer consumer1 = Utils.deserialize(Utils.serialize(consumer));
      consumer1.check();
   }

}
