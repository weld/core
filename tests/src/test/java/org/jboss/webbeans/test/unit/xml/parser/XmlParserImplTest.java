package org.jboss.webbeans.test.unit.xml.parser;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.inject.DeploymentException;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.io.SAXReader;
import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.Classes;
import org.jboss.testharness.impl.packaging.Resource;
import org.jboss.testharness.impl.packaging.Resources;
import org.jboss.webbeans.ejb.EjbDescriptorCache;
import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.introspector.AnnotatedItem;
import org.jboss.webbeans.mock.MockXmlEnvironment;
import org.jboss.webbeans.test.AbstractWebBeansTest;
import org.jboss.webbeans.test.unit.xml.beans.Order;
import org.jboss.webbeans.test.unit.xml.beans.TestBean;
import org.jboss.webbeans.test.unit.xml.beans.TestDeploymentType;
import org.jboss.webbeans.xml.ParseXmlHelper;
import org.jboss.webbeans.xml.XmlConstants;
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
}

	/*
<Beans xmlns="urn:java:ee" xmlns:myapp="urn:java:org.jboss.webbeans.test.unit.xml.beans"
       xmlns:test="urn:java:org.jboss.webbeans.test.unit.xml.parser"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="urn:java:ee http://java.sun.com/jee/ee/schema-1.0.xsd
                           urn:java:com.mydomain.myapp http://mydomain.com/myapp/schema-1.2.xsd">
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
