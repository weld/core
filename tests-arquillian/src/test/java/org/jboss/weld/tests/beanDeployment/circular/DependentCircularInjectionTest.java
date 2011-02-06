package org.jboss.weld.tests.beanDeployment.circular;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.api.ShouldThrowException;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.exceptions.DeploymentException;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class DependentCircularInjectionTest
{
   @Deployment @ShouldThrowException(DeploymentException.class)
   public static JavaArchive createDeployment()
   {
      return ShrinkWrap.create(BeanArchive.class)
         .addClasses(Fish.class, Water.class);
   }
   
   @Test
   public void test()
   {

   }

}
