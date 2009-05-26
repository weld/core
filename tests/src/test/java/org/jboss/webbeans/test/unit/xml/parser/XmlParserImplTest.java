package org.jboss.webbeans.test.unit.xml.parser;

import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.spi.Bean;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.Classes;
import org.jboss.testharness.impl.packaging.Resource;
import org.jboss.testharness.impl.packaging.Resources;
import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.ejb.EjbDescriptorCache;
import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.introspector.AnnotatedItem;
import org.jboss.webbeans.mock.MockXmlEnvironment;
import org.jboss.webbeans.test.AbstractWebBeansTest;
import org.jboss.webbeans.test.unit.xml.beans.Order;
import org.jboss.webbeans.xml.XmlEnvironment;
import org.jboss.webbeans.xml.XmlParser;

@Artifact
@Resources({
   @Resource(source="/org/jboss/webbeans/test/unit/xml/parser/user-defined-beans.xml", destination="WEB-INF/classes/org/jboss/webbeans/test/unit/xml/parser/user-defined-beans.xml" ),
   @Resource(source="/org/jboss/webbeans/test/unit/xml/parser/schema.xsd", destination="WEB-INF/classes/org/jboss/webbeans/test/unit/xml/parser/schema.xsd" ),
   @Resource(source="/org/jboss/webbeans/test/unit/xml/parser/namespace", destination="WEB-INF/classes/org/jboss/webbeans/test/unit/xml/parser/namespace" )
})
@Classes(
      value={Order.class, MockXmlEnvironment.class},
      packages={"org.jboss.webbeans.test.unit.xml.beans.annotationtype", "org.jboss.webbeans.test.unit.xml.parser.schema.foo"}
)
public class XmlParserImplTest extends AbstractWebBeansTest
{
//   @Test
   public void testParse()
   {
      XmlEnvironment parserEnv = new MockXmlEnvironment(getResources("beans.xml"), new EjbDescriptorCache());
      AnnotatedClass<?> aClass = parserEnv.loadClass("org.jboss.webbeans.test.unit.xml.beans.Order", Order.class);

      Set<URL> xmls = new HashSet<URL>();
      Iterable<URL> urls = getResources("user-defined-beans.xml");

      for (URL url : urls)
         xmls.add(url);      
      
      XmlParser parser = new XmlParser(parserEnv);
      parser.parse();      
      
      for (AnnotatedItem<?, ?> aElement : parserEnv.getClasses())
      {
         assert aElement.equals(aClass);
      }

      assert parserEnv.getClasses().size() == 1;
   }
   
//   @Test
   public void testDd()
   {      
      XmlEnvironment parserEnv = new MockXmlEnvironment(getResources("user-defined-beans.xml"), new EjbDescriptorCache());
      XmlParser parser = new XmlParser(parserEnv);
      parser.parse();
      
      ManagerImpl manager = parserEnv.getManager();
      
      Set<Bean<Order>> beansSet = manager.getBeans(Order.class);
      List<Class<? extends Annotation>> dTypes = manager.getEnabledDeploymentTypes();
      dTypes.size();
      for(Bean<Order> bean : beansSet)
      {
         Class<? extends Annotation> deploymentType = bean.getDeploymentType();
         System.out.println("after parsing: " + deploymentType);
      }
   }
}