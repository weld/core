package org.jboss.webbeans.test.unit.xml.parser;

import java.net.URL;
import java.util.HashSet;
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
import org.jboss.webbeans.util.xml.XmlParserImpl;
import org.jboss.webbeans.xml.XmlEnvironmentImpl;
import org.testng.annotations.Test;

@Artifact
@Resources({
   @Resource(source="/org/jboss/webbeans/test/unit/xml/user-defined-beans.xml", destination="WEB-INF/classes/org/jboss/webbeans/test/unit/xml/user-defined-beans.xml" )
})
@Classes({Order.class})
public class XmlParserImplTest extends AbstractWebBeansTest
{
   @Test
   public void testParse()
   {
      XmlEnvironmentImpl parserEnv = new MockXmlEnvironment(getResources("beans.xml"));
      AnnotatedClass<?> aClass = parserEnv.loadClass("org.jboss.webbeans.test.unit.xml.beans.Order", Order.class);

      Set<URL> xmls = new HashSet<URL>();
      Iterable<URL> urls = getResources("user-defined-beans.xml");

      for (URL url : urls)
         xmls.add(url);      
      
      XmlParserImpl parser = new XmlParserImpl(parserEnv);
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
