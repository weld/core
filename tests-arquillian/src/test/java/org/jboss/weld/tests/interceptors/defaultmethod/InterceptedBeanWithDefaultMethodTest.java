package org.jboss.weld.tests.interceptors.defaultmethod;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class InterceptedBeanWithDefaultMethodTest {

   @Inject
   private Foo foo;

   @Deployment
   public static Archive<?> deploy() {
      return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(InterceptedBeanWithDefaultMethodTest.class)).intercept(FooInterceptor.class).addPackage(InterceptedBeanWithDefaultMethodTest.class.getPackage());
   }

   /*
   * description = "WELD-2405"
   */
   @Test
   public void testDefaultMethodInvocationOnInterceptedBean() {
      Assert.assertTrue(foo.isIntercepted());
   }
}
