package org.jboss.weld.test.tomcat.lookup;



import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.Classes;
import org.jboss.testharness.impl.packaging.IntegrationTest;
import org.jboss.testharness.impl.packaging.Resource;
import org.jboss.testharness.impl.packaging.Resources;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

@Artifact(addCurrentPackage=false)
@IntegrationTest(runLocally=true)
@Resources({
   @Resource(source="context-servlet-injection.xml", destination="/META-INF/context.xml"),
   @Resource(source="web-filter-injection.xml", destination="/WEB-INF/web.xml")
})
@Classes({
   CatFilter.class,
   Sewer.class
})
public class FilterInjectionTest extends AbstractWeldTest
{
   @Test
   public void testFilterInjection() throws Exception
   {
      HttpClient client = new HttpClient();
      HttpMethod method = new GetMethod(getContextPath() + "/cat");
      assert client.executeMethod(method) == HttpServletResponse.SC_OK;
   }
}
