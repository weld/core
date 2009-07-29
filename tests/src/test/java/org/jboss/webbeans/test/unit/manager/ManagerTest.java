package org.jboss.webbeans.test.unit.manager;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.deployment.Production;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.Packaging;
import org.jboss.webbeans.BeanManagerImpl;
import org.jboss.webbeans.literal.CurrentLiteral;
import org.jboss.webbeans.test.AbstractWebBeansTest;
import org.testng.annotations.Test;

@Artifact
@Packaging
public class ManagerTest extends AbstractWebBeansTest
{
   
   private static final Set<Annotation> DEFAULT_BINDINGS = new HashSet<Annotation>();
   
   static
   {
      DEFAULT_BINDINGS.add(new CurrentLiteral());
   }
   
   private static interface Dummy {}
   
   private static class DummyBean implements Bean<Dummy>
   {
      
      private static final Set<Type> TYPES = new HashSet<Type>();
      
      static
      {
         TYPES.add(Dummy.class);
         TYPES.add(Object.class);
      }

      public Set<Annotation> getBindings()
      {
         return DEFAULT_BINDINGS;
      }

      public Class<? extends Annotation> getDeploymentType()
      {
         return Production.class;
      }

      public Set<InjectionPoint> getInjectionPoints()
      {
         return Collections.emptySet();
      }

      public String getName()
      {
         return null;
      }

      public Class<? extends Annotation> getScopeType()
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

      public boolean isPolicy()
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
      Integer rootManagerId = getCurrentManager().getId();
      BeanManagerImpl deserializedRootManager = (BeanManagerImpl) deserialize(serialize(getCurrentManager()));
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
      Integer childManagerId = childManager.getId();
      BeanManagerImpl deserializedChildManager = (BeanManagerImpl) deserialize(serialize(childManager));
      assert deserializedChildManager.getId().equals(childManagerId);
      assert childManager.getBeans(Dummy.class).size() == 1;
      assert deserializedChildManager.getBeans(Dummy.class).size() == 1;
      assert childManager.getBeans(Dummy.class).iterator().next().equals(deserializedChildManager.getBeans(Dummy.class).iterator().next());
   }
   
   
   
}
