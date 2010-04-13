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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

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
   
   private static final Logger log = LoggerFactory.getLogger(ServletInjectionTest.class);
   
   @Override
   @BeforeClass
   public void beforeClass() throws Throwable
   {
      super.beforeClass();
   }
   
   
   @Test
   public void testServletInjection() throws Exception 
   {
      HttpClient client = new HttpClient();
      HttpMethod method = new GetMethod(getContextPath() + "/rat");
      assert client.executeMethod(method) == HttpServletResponse.SC_OK;
   }
     

   
}
