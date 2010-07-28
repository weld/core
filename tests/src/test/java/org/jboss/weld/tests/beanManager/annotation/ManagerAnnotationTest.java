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
package org.jboss.weld.tests.beanManager.annotation;

import javax.persistence.PersistenceContext;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

@Artifact
public class ManagerAnnotationTest extends AbstractWeldTest
{
   
   @Test(description="WELD-299")
   public void testIsQualifier() throws Exception
   {
      assert !getCurrentManager().isQualifier(PersistenceContext.class);
   }
   
   @Test(description="WELD-299")
   public void testIsInterceptorBinding() throws Exception
   {
      assert !getCurrentManager().isInterceptorBinding(PersistenceContext.class);
   }
   
   @Test(description="WELD-299")
   public void testIsNormalScope() throws Exception
   {
      assert !getCurrentManager().isNormalScope(PersistenceContext.class);
   }
   
   @Test(description="WELD-299")
   public void testIsPassivatingScope() throws Exception
   {
      assert !getCurrentManager().isPassivatingScope(PersistenceContext.class);
   }
   
   @Test(description="WELD-299")
   public void testIsScope() throws Exception
   {
      assert !getCurrentManager().isScope(PersistenceContext.class);
   }
   
   @Test(description="WELD-299")
   public void testIsStereotype() throws Exception
   {
      assert !getCurrentManager().isStereotype(PersistenceContext.class);
   }
   
   
}
