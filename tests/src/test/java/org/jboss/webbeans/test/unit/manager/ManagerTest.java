package org.jboss.webbeans.test.unit.manager;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.deployment.Production;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.Packaging;
import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.bean.BaseBean;
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
   
   private static class DummyBean extends BaseBean<Dummy>
   {
      
      private static final Set<Type> TYPES = new HashSet<Type>();
      
      static
      {
         TYPES.add(Dummy.class);
         TYPES.add(Object.class);
      }

      protected DummyBean(BeanManager beanManager)
      {
         super(beanManager);
      }

      @Override
      public Set<Annotation> getBindings()
      {
         return DEFAULT_BINDINGS;
      }

      @Override
      public Class<? extends Annotation> getDeploymentType()
      {
         return Production.class;
      }

      @Override
      public Set<InjectionPoint> getInjectionPoints()
      {
         return Collections.emptySet();
      }

      @Override
      public String getName()
      {
         return null;
      }

      @Override
      public Class<? extends Annotation> getScopeType()
      {
         return Dependent.class;
      }

      @Override
      public Set<Type> getTypes()
      {
         return TYPES;
      }

      @Override
      public boolean isNullable()
      {
         return true;
      }

      @Override
      public boolean isSerializable()
      {
         return false;
      }

      public Dummy create(CreationalContext<Dummy> creationalContext)
      {
         return null;
      }

      public void destroy(Dummy instance)
      {
         
      }
      
   }
   
   @Test
   public void testRootManagerSerializability() throws Exception
   {
      Integer rootManagerId = getCurrentManager().getId();
      ManagerImpl deserializedRootManager = (ManagerImpl) deserialize(serialize(getCurrentManager()));
      assert deserializedRootManager.getId().equals(rootManagerId);
      assert getCurrentManager().getBeans(Foo.class).size() == 1;
      assert deserializedRootManager.getBeans(Foo.class).size() == 1;
      assert getCurrentManager().getBeans(Foo.class).iterator().next().equals(deserializedRootManager.getBeans(Foo.class).iterator().next());
   }
   
   @Test
   public void testChildManagerSerializability() throws Exception
   {
      ManagerImpl childManager = getCurrentManager().createActivity();
      BaseBean<?> dummyBean = new DummyBean(childManager);
      childManager.addBean(dummyBean);
      Integer childManagerId = childManager.getId();
      ManagerImpl deserializedChildManager = (ManagerImpl) deserialize(serialize(childManager));
      assert deserializedChildManager.getId().equals(childManagerId);
      assert childManager.getBeans(Dummy.class).size() == 1;
      assert deserializedChildManager.getBeans(Dummy.class).size() == 1;
      assert childManager.getBeans(Dummy.class).iterator().next().equals(deserializedChildManager.getBeans(Dummy.class).iterator().next());
   }
   
   
   
}
