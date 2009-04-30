package org.jboss.webbeans.test.unit.xml.parser.schema;

import javax.inject.DefinitionException;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.Classes;
import org.jboss.testharness.impl.packaging.Resource;
import org.jboss.testharness.impl.packaging.Resources;
import org.jboss.webbeans.ejb.EjbDescriptorCache;
import org.jboss.webbeans.mock.MockXmlEnvironment;
import org.jboss.webbeans.test.AbstractWebBeansTest;
import org.jboss.webbeans.test.unit.xml.parser.schema.foo.Order;
import org.jboss.webbeans.xml.XmlEnvironment;
import org.jboss.webbeans.xml.XmlParser;
import org.testng.annotations.Test;

@Artifact
@Resources({
   @Resource(source="/org/jboss/webbeans/test/unit/xml/parser/schema/beans.xml", destination="WEB-INF/classes/org/jboss/webbeans/test/unit/xml/parser/schema/beans.xml" ),
   @Resource(source="/org/jboss/webbeans/test/unit/xml/parser/schema/not-valid-beans.xml", destination="WEB-INF/classes/org/jboss/webbeans/test/unit/xml/parser/schema/not-valid-beans.xml" ),
   @Resource(source="/org/jboss/webbeans/test/unit/xml/parser/schema/namespace", destination="WEB-INF/classes/org/jboss/webbeans/test/unit/xml/parser/schema/namespace" ),
   @Resource(source="/org/jboss/webbeans/test/unit/xml/parser/schema/schema.xsd", destination="WEB-INF/classes/org/jboss/webbeans/test/unit/xml/parser/schema/schema.xsd" ),
   @Resource(source="/org/jboss/webbeans/test/unit/xml/parser/schema/valid/schema.xsd", destination="WEB-INF/classes/org/jboss/webbeans/test/unit/xml/parser/schema/valid/schema.xsd" )
})
@Classes(
      value={Order.class, MockXmlEnvironment.class},
      packages={"org.jboss.webbeans.test.unit.xml.beans.annotationtype", "org.jboss.webbeans.test.unit.xml.parser.schema.foo"}
)
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
      
      assert false : "file '/org/jboss/webbeans/test/unit/xml/parser/schema/not-valid-beans.xml' matching '/org/jboss/webbeans/test/unit/xml/parser/schema/schema.xsd'";
   }
}