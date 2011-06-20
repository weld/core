package org.jboss.weld.tests.jsf;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class JsfTest
{

   @Deployment
   public static WebArchive deployment()
   {
      return ShrinkWrap.create(WebArchive.class, "test.war")
         .addPackage(JsfTest.class.getPackage())
         .addAsWebResource(JsfTest.class.getPackage(), "faces-config.xml", "faces-config.xml")
         .addAsWebResource(JsfTest.class.getPackage(), "web.xml", "web.xml")
         .addAsWebResource(new ByteArrayAsset(new byte[0]), "beans.xml");
   }

   @Test
   // WELD-492
   public void testExtendsUiComponent(Garply garply)
   {
      Assert.assertNotNull(garply);
   }

}
