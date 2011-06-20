package org.jboss.weld.tests.metadata.scanning;

import static org.jboss.weld.tests.metadata.scanning.Utils.createBeansXml;

import javax.enterprise.inject.spi.BeanManager;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.tests.metadata.Qux;
import org.jboss.weld.tests.metadata.scanning.jboss.Baz;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class SystemPropertyValueInvertedActivationTest
{

   public static final String TEST1_PROPERTY = SystemPropertyValueInvertedActivationTest.class + ".test1";
   public static final String TEST1_VALUE = "meh1";
   public static final String TEST2_PROPERTY = SystemPropertyValueInvertedActivationTest.class + ".test2";
   public static final String TEST2_VALUE = "meh2";

   public static final Asset BEANS_XML = createBeansXml(
         "<weld:scan>" +
            "<weld:include name=\"" + Bar.class.getName() + "\">" +
               "<weld:if-system-property name=\"" + TEST1_PROPERTY + "\" value=\"!" + TEST1_VALUE + "\" />" +
            "</weld:include>" +
            "<weld:include name=\"" + Foo.class.getName() + "\">" +
               "<weld:if-system-property name=\"" + TEST2_PROPERTY + "\" value=\"!" + TEST2_VALUE + "\" />" +
            "</weld:include>" +
         "</weld:scan>");

   @Deployment
   public static Archive<?> deployment()
   {
      System.setProperty(TEST1_PROPERTY, TEST1_VALUE);
      System.setProperty(TEST2_PROPERTY, TEST2_PROPERTY);
      return ShrinkWrap.create(JavaArchive.class).addClass(Utils.class)
         .addClasses(Bar.class, Foo.class, Baz.class, Qux.class)
         .addAsManifestResource(BEANS_XML, "beans.xml");
   }

   @Test
   public void test(BeanManager beanManager)
   {
      assert beanManager.getBeans(Bar.class).size() == 0;
      assert beanManager.getBeans(Qux.class).size() == 0;
      assert beanManager.getBeans(Foo.class).size() == 1;
      assert beanManager.getBeans(Baz.class).size() == 0;
   }

}
