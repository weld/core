package org.jboss.webbeans.test.unit.manager;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.context.CreationalContext;
import javax.context.Dependent;
import javax.inject.Production;
import javax.inject.manager.Bean;
import javax.inject.manager.InjectionPoint;
import javax.inject.manager.Manager;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.Packaging;
import org.jboss.webbeans.ManagerImpl;
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
   
   private static class DummyBean extends Bean<Dummy>
   {
      
      private static final Set<Type> TYPES = new HashSet<Type>();
      
      static
      {
         TYPES.add(Dummy.class);
         TYPES.add(Object.class);
      }

      protected DummyBean(Manager manager)
      {
         super(manager);
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
      assert getCurrentManager().resolveByType(Foo.class).size() == 1;
      assert deserializedRootManager.resolveByType(Foo.class).size() == 1;
      assert getCurrentManager().resolveByType(Foo.class).iterator().next().equals(deserializedRootManager.resolveByType(Foo.class).iterator().next());
   }
   
   @Test
   public void testChildManagerSerializability() throws Exception
   {
      ManagerImpl childManager = getCurrentManager().createActivity();
      Bean<?> dummyBean = new DummyBean(childManager);
      childManager.addBean(dummyBean);
      Integer childManagerId = childManager.getId();
      ManagerImpl deserializedChildManager = (ManagerImpl) deserialize(serialize(childManager));
      assert deserializedChildManager.getId().equals(childManagerId);
      assert childManager.resolveByType(Dummy.class).size() == 1;
      assert deserializedChildManager.resolveByType(Dummy.class).size() == 1;
      assert childManager.resolveByType(Dummy.class).iterator().next().equals(deserializedChildManager.resolveByType(Dummy.class).iterator().next());
   }
   
   
   
}
