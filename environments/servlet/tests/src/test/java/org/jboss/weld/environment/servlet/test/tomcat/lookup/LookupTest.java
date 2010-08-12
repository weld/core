package org.jboss.weld.environment.servlet.test.tomcat.lookup;



import javax.enterprise.inject.spi.BeanManager;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.environment.servlet.test.util.DeploymentDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class LookupTest
{
   
   @Deployment
   public static WebArchive deployment()
   {
      return DeploymentDescriptor.deployment().addClasses(Mouse.class, Vole.class, LookupTest.class);
   }
   
   @Test
   public void testManagerInJndi(Mouse mouse, BeanManager beanManager) throws Exception 
   {
      assert mouse.getManager() != null;
      assert mouse.getManager().equals(beanManager);
   }
     
   @Test
   public void testResource(Vole vole, BeanManager beanManager) throws Exception 
   {
      assert vole.getManager() != null;
      assert vole.getManager().equals(beanManager);
   }
   
}
