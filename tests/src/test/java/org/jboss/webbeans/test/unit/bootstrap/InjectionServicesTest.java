package org.jboss.webbeans.test.unit.bootstrap;

import java.util.Arrays;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.jboss.webbeans.injection.spi.InjectionServices;
import org.jboss.webbeans.mock.MockEELifecycle;
import org.testng.annotations.Test;

public class InjectionServicesTest
{
   
   @Test
   public void testDiscoverFails()
   {
      MockEELifecycle lifecycle = new MockEELifecycle();
      lifecycle.getDeployment().getArchive().setBeanClasses(Arrays.asList(Foo.class, Bar.class));
      CheckableInjectionServices ijs = new CheckableInjectionServices();
      lifecycle.getDeployment().getArchive().getServices().add(InjectionServices.class, ijs);
      lifecycle.initialize();
      lifecycle.beginApplication();
      lifecycle.beginSession();
      lifecycle.beginRequest();
      
      BeanManager manager = lifecycle.getBootstrap().getManager(lifecycle.getDeployment().getArchive());
      
      Bean<? extends Object> bean = manager.resolve(manager.getBeans(Foo.class));
      ijs.reset();
      Foo foo = (Foo) manager.getReference(bean, Foo.class, manager.createCreationalContext(bean));
      
      assert ijs.isBefore();
      assert ijs.isAfter();
      assert ijs.isInjectedAfter();
      assert ijs.isInjectionTargetCorrect();
      
      assert foo.getBar() != null;
      assert foo.getMessage().equals("hi!");
      
      
      lifecycle.endRequest();
      lifecycle.endSession();
      lifecycle.endApplication();
   }
   
}
