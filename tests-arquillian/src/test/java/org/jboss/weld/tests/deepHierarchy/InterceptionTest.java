/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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

package org.jboss.weld.tests.deepHierarchy;

import static org.junit.Assert.assertEquals;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.tests.category.Integration;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * @author Marius Bogoevici
 */

@Category(Integration.class)
@RunWith(Arquillian.class)
public class InterceptionTest
{
   @Deployment
   public static Archive<?> deploy()
   {
      return ShrinkWrap.create(BeanArchive.class)
            .intercept(TestInterceptor.class)
            .addPackage(InterceptionTest.class.getPackage());
   }

   @Test
   public void testChildMethodIntercepted(Child child) throws Exception
   {
      TestInterceptor.interceptedMethodNames.clear();
      child.definedOnlyInChild();
      assertEquals(1, TestInterceptor.interceptedMethodNames.size());
      assertEquals("definedOnlyInChild", TestInterceptor.interceptedMethodNames.get(0));
   }

   @Test
   public void testGrandParentMethodIntercepted(Child child) throws Exception
   {
      TestInterceptor.interceptedMethodNames.clear();
      child.definedOnlyInGrandParent();
      assertEquals(1, TestInterceptor.interceptedMethodNames.size());
      assertEquals("definedOnlyInGrandParent", TestInterceptor.interceptedMethodNames.get(0));
   }

   @Test
   public void testOverridenMethodIntercepted(Child child) throws Exception
   {
      TestInterceptor.interceptedMethodNames.clear();
      child.overriddenInParent();
      assertEquals(1, TestInterceptor.interceptedMethodNames.size());
      assertEquals("overriddenInParent", TestInterceptor.interceptedMethodNames.get(0));
   }
}
