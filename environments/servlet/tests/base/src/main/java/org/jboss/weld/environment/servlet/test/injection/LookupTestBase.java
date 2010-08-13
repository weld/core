package org.jboss.weld.environment.servlet.test.injection;



import static org.jboss.weld.environment.servlet.test.util.Deployments.baseDeployment;

import javax.enterprise.inject.spi.BeanManager;

import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;

public class LookupTestBase
{
   
   public static WebArchive deployment()
   {
      return baseDeployment().addClasses(Mouse.class, Vole.class, LookupTestBase.class);
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
