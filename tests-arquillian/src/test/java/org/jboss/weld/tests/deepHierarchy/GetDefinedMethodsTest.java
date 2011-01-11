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
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.jboss.weld.introspector.WeldClass;
import org.jboss.weld.introspector.WeldMethod;
import org.jboss.weld.introspector.jlr.WeldClassImpl;
import org.jboss.weld.metadata.TypeStore;
import org.jboss.weld.resources.ClassTransformer;
import org.junit.Test;

/**
 * @author Marius Bogoevici
 */
public class GetDefinedMethodsTest
{
   @Test
   public void testGetDefinedMethods()
   {
      WeldClass<Child> type = WeldClassImpl.of(Child.class, new ClassTransformer(new TypeStore()));
      Collection<WeldMethod<?,? super Child>> weldMethods =  type.getWeldMethods();
      Set<String> methodNames = new HashSet<String>();
      for (WeldMethod<?, ? super Child> weldMethod : weldMethods)
      {
         methodNames.add(weldMethod.getName());
      }
      assertTrue(methodNames.contains("definedOnlyInChild"));
      assertTrue(methodNames.contains("definedOnlyInGrandParent"));
      assertTrue(methodNames.contains("overriddenInParent"));

   }
}
