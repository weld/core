package org.jboss.weld.tests.util.annotated;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

/**
 * A type closure builder
 * 
 * @author Stuart Douglas
 * 
 */
class TestTypeClosureBuilder
{

   final Set<Type> types = new HashSet<Type>();

   public TestTypeClosureBuilder add(Class<?> beanType)
   {
      Class<?> c = beanType;
      do
      {
         types.add(c);
         c = c.getSuperclass();
      }
      while (c != null);
      for (Class<?> i : beanType.getInterfaces())
      {
         types.add(i);
      }
      return this;
   }

   public TestTypeClosureBuilder addInterfaces(Class<?> beanType)
   {
      for (Class<?> i : beanType.getInterfaces())
      {
         types.add(i);
      }
      return this;
   }

   public Set<Type> getTypes()
   {
      return types;
   }

}
