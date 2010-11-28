package org.jboss.weld.environment.servlet.test.deployment.structure;

import static org.jboss.weld.environment.servlet.test.util.Deployments.baseDeployment;
import static org.junit.Assert.assertEquals;

import javax.enterprise.inject.spi.BeanManager;

import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.environment.servlet.test.util.BeansXml;
import org.junit.Test;

public class DeploymentOrderingTestBase
{
   
   
   public static WebArchive deployment()
   {
      WebArchive war = baseDeployment().addPackage(DeploymentOrderingTestBase.class.getPackage()).addWebResource(new BeansXml().alternatives(Bar.class), "beans.xml").addWebResource(new BeansXml().alternatives(Garply.class), "classes/META-INF/beans.xml");
      System.out.println(war.toString(true));
      return war;
   }
   
   @Test
   public void testBeansXmlMerged(BeanManager beanManager)
   {
      assertEquals(Bar.class, beanManager.resolve(beanManager.getBeans(Foo.class)).getBeanClass());
      assertEquals(Garply.class, beanManager.resolve(beanManager.getBeans(Baz.class)).getBeanClass());
   }
   
}
