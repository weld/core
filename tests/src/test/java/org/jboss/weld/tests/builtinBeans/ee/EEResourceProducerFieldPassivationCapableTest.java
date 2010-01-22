/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
