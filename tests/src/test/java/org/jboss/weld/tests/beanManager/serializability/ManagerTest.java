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
package org.jboss.weld.tests.beanManager.serializability;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.Packaging;
import org.jboss.weld.literal.DefaultLiteral;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.test.AbstractWeldTest;
import org.jboss.weld.test.Utils;
import org.testng.annotations.Test;

@Artifact
@Packaging
public class ManagerTest extends AbstractWeldTest
{
   
   private static final Set<Annotation> DEFAULT_QUALIFIERS = Collections.<Annotation>singleton(DefaultLiteral.INSTANCE);
   
   private static interface Dummy {}
   
   private static class DummyBean implements Bean<Dummy>
   {
      
      private static final Set<Type> TYPES = new HashSet<Type>();
      
      static
      {
         TYPES.add(Dummy.class);
         TYPES.add(Object.class);
      }

      public Set<Annotation> getQualifiers()
      {
         return DEFAULT_QUALIFIERS;
      }

      public Set<InjectionPoint> getInjectionPoints()
      {
         return Collections.emptySet();
      }

      public String getName()
      {
         return null;
      }

      public Class<? extends Annotation> getScope()
      {
         return Dependent.class;
      }

      public Set<Type> getTypes()
      {
         return TYPES;
      }

      public boolean isNullable()
      {
         return true;
      }

      public Dummy create(CreationalContext<Dummy> creationalContext)
      {
         return null;
      }

      public void destroy(Dummy instance, CreationalContext<Dummy> creationalContext)
      {
         
      }

      public Class<?> getBeanClass()
      {
         return Dummy.class;
      }

      public boolean isAlternative()
      {
         return false;
      }

      public Set<Class<? extends Annotation>> getStereotypes()
      {
         return Collections.emptySet();
      }
      
   }
   
   @Test
   public void testRootManagerSerializability() throws Exception
   {
      String rootManagerId = getCurrentManager().getId();
      BeanManagerImpl deserializedRootManager = (BeanManagerImpl) Utils.deserialize(Utils.serialize(getCurrentManager()));
      assert deserializedRootManager.getId().equals(rootManagerId);
      assert getCurrentManager().getBeans(Foo.class).size() == 1;
      assert deserializedRootManager.getBeans(Foo.class).size() == 1;
      assert getCurrentManager().getBeans(Foo.class).iterator().next().equals(deserializedRootManager.getBeans(Foo.class).iterator().next());
   }
   
   @Test
   public void testChildManagerSerializability() throws Exception
   {
      BeanManagerImpl childManager = getCurrentManager().createActivity();
      Bean<?> dummyBean = new DummyBean();
      childManager.addBean(dummyBean);
      String childManagerId = childManager.getId();
      BeanManagerImpl deserializedChildManager = (BeanManagerImpl) Utils.deserialize(Utils.serialize(childManager));
      assert deserializedChildManager.getId().equals(childManagerId);
      assert childManager.getBeans(Dummy.class).size() == 1;
      assert deserializedChildManager.getBeans(Dummy.class).size() == 1;
      assert childManager.getBeans(Dummy.class).iterator().next().equals(deserializedChildManager.getBeans(Dummy.class).iterator().next());
   }
   
   
   
}
