package org.jboss.webbeans.test.unit.xml.parser.schema;

import javax.inject.DefinitionException;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.Classes;
import org.jboss.testharness.impl.packaging.Resource;
import org.jboss.testharness.impl.packaging.Resources;
import org.jboss.webbeans.ejb.EjbDescriptorCache;
import org.jboss.webbeans.mock.MockXmlEnvironment;
import org.jboss.webbeans.test.AbstractWebBeansTest;
import org.jboss.webbeans.test.unit.xml.beans.Order;
import org.jboss.webbeans.xml.XmlEnvironment;
import org.jboss.webbeans.xml.XmlParser;
import org.testng.annotations.Test;

@Artifact
@Resources({
   @Resource(source="/org/jboss/webbeans/test/unit/xml/parser/schema/beans.xml", destination="WEB-INF/classes/org/jboss/webbeans/test/unit/xml/parser/schema/beans.xml" ),
   @Resource(source="/org/jboss/webbeans/test/unit/xml/parser/schema/not-valid-beans.xml", destination="WEB-INF/classes/org/jboss/webbeans/test/unit/xml/parser/schema/not-valid-beans.xml" )
})
@Classes({Order.class})
public class SchemaValidationTest extends AbstractWebBeansTest
{   
   @Test
   public void testTwoValidSchemas()
   {      
      XmlEnvironment parserEnv = new MockXmlEnvironment(getResources("beans.xml"), new EjbDescriptorCache());
      XmlParser parser = new XmlParser(parserEnv);
      parser.parse();
      
      assert parserEnv.getManager().resolveByType(Order.class).size() == 1;
   }
   
   @Test(expectedExceptions = DefinitionException.class)
   public void testOneSchemaNotValid()
   {      
      XmlEnvironment parserEnv = new MockXmlEnvironment(getResources("not-valid-beans.xml"), new EjbDescriptorCache());
      XmlParser parser = new XmlParser(parserEnv);
      parser.parse();
      
      assert false;
   }
}