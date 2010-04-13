package org.jboss.weld.test.tomcat.lookup;


import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jboss.testharness.impl.packaging.*;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletResponse;

@Artifact(addCurrentPackage=false)
@IntegrationTest(runLocally=true)
@Resources({
   @Resource(source="context-servlet-injection.xml", destination="/META-INF/context.xml"),
   @Resource(source="web-servlet-injection.xml", destination="/WEB-INF/web.xml")
})
@Classes({
   RatServlet.class,
   Sewer.class
})
public class ServletInjectionTest extends AbstractWeldTest
{

   @Test
   public void testServletInjection() throws Exception 
   {
      HttpClient client = new HttpClient();
      HttpMethod method = new GetMethod(getContextPath() + "/rat");
      assert client.executeMethod(method) == HttpServletResponse.SC_OK;
   }
     

   
}
