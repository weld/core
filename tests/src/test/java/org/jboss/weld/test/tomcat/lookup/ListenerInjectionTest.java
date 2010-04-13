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

@Artifact(addCurrentPackage=false)
@IntegrationTest(runLocally=true)
@Resources({
   @Resource(source="context-servlet-injection.xml", destination="/META-INF/context.xml"),
   @Resource(source="web-listener-injection.xml", destination="/WEB-INF/web.xml")
})
@Classes({
   BatListener.class,
   BatServlet.class,
   Sewer.class
})
public class ListenerInjectionTest extends AbstractWeldTest
{
   // This test currently fails showing that injection into Listeners doesn't
   // work in Tomcat standalone.
//   @Test
   public void testListenerInjection() throws Exception
   {
      HttpClient client = new HttpClient();
      HttpMethod method = new GetMethod(getContextPath() + "/bat");
      int sc = client.executeMethod(method);
      System.out.println("sc = " + sc);
      assert sc == HttpServletResponse.SC_OK;
   }
}
