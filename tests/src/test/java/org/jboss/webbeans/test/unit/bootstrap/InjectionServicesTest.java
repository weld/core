package org.jboss.webbeans.test.unit.bootstrap;

import java.util.Arrays;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.jboss.webbeans.injection.spi.InjectionServices;
import org.jboss.webbeans.mock.MockEELifecycle;
import org.jboss.webbeans.mock.TestContainer;
import org.testng.annotations.Test;

public class InjectionServicesTest
{
   
   @Test
   public void testInjectionOfTarget()
   {
      TestContainer container = new TestContainer(new MockEELifecycle(), Arrays.asList(Foo.class, Bar.class), null);
      CheckableInjectionServices ijs = new CheckableInjectionServices();
      container.getDeployment().getArchive().getServices().add(InjectionServices.class, ijs);
      container.startContainer();
      container.ensureRequestActive();
      
      BeanManager manager = container.getBeanManager();
      
      Bean<? extends Object> bean = manager.resolve(manager.getBeans(Foo.class));
      ijs.reset();
      Foo foo = (Foo) manager.getReference(bean, Foo.class, manager.createCreationalContext(bean));
      
      assert ijs.isBefore();
      assert ijs.isAfter();
      assert ijs.isInjectedAfter();
      assert ijs.isInjectionTargetCorrect();
      
      assert foo.getBar() != null;
      assert foo.getMessage().equals("hi!");
      
      
      container.stopContainer();
   }
   
}
