package org.jboss.webbeans.test.unit.xml.parser;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.Classes;
import org.jboss.testharness.impl.packaging.Resource;
import org.jboss.testharness.impl.packaging.Resources;
import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.introspector.AnnotatedItem;
import org.jboss.webbeans.mock.MockXmlEnvironment;
import org.jboss.webbeans.test.unit.AbstractWebBeansTest;
import org.jboss.webbeans.test.unit.xml.beans.Order;
import org.jboss.webbeans.test.unit.xml.beans.TestBean;
import org.jboss.webbeans.test.unit.xml.beans.TestDeploymentType;
import org.jboss.webbeans.xml.ParseXmlHelper;
import org.jboss.webbeans.xml.XmlEnvironment;
import org.jboss.webbeans.xml.XmlParser;
import org.testng.annotations.Test;

@Artifact
@Resources({
   @Resource(source="/org/jboss/webbeans/test/unit/xml/parser/user-defined-beans.xml", destination="WEB-INF/classes/org/jboss/webbeans/test/unit/xml/parser/user-defined-beans.xml" )
})
@Classes({Order.class})
public class XmlParserImplTest extends AbstractWebBeansTest
{
   //@Test
   public void testParse()
   {
      XmlEnvironment parserEnv = new MockXmlEnvironment(getResources("beans.xml"));
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
   
   //@Test
   public void testParceNamespaceFile()
   {      
      String urn = "urn:java:org.jboss.webbeans.test.unit.xml.parser";
      
      File f = ParseXmlHelper.loadNamespaceFile(urn);
      List<String> packages = ParseXmlHelper.parseNamespaceFile(f);
      assert packages.size() == 2;
   }
   
   //@Test
   public void testLoadClassByURN()
   {      
      String urn = "urn:java:org.jboss.webbeans.test.unit.xml.parser";
      String beanName = "TestBean";
      String deploymentTypeName = "TestDeploymentType";
      
      Class<?> beanClass = ParseXmlHelper.loadClassByURN(urn, beanName);
      Class<?> deploymentTypeClass = ParseXmlHelper.loadClassByURN(urn, deploymentTypeName);
      
      assert beanClass.equals(TestBean.class);
      assert deploymentTypeClass.equals(TestDeploymentType.class);
   }
}

	/*
	<Beans xmlns="urn:java:ee" xmlns:myapp="urn:java:org.jboss.webbeans.test.unit.xml.parser"
	xmlns:test="urn:java:org.jboss.webbeans.test.unit.xml">
	<Deploy>
		<Standard />
		<Production />
		<test:AnotherDeploymentType />
	</Deploy>
	<myapp:Order>
		<ConversationScoped />
		<myapp:PaymentProcessor>
			<myapp:Asynchronous />
		</myapp:PaymentProcessor>
		<myapp:User />
	</myapp:Order>
	<myapp:Login>
		<ConversationScoped />
		<BindingType />
	</myapp:Login>
	</Beans>
	*/
