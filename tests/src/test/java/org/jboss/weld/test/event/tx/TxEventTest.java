package org.jboss.weld.test.event.tx;


import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.Classes;
import org.jboss.testharness.impl.packaging.IntegrationTest;
import org.jboss.testharness.impl.packaging.Resource;
import org.jboss.testharness.impl.packaging.Resources;
import org.jboss.testharness.impl.packaging.war.WarArtifactDescriptor;
import org.testng.annotations.Test;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;

@Artifact(addCurrentPackage=false)
@IntegrationTest(runLocally=true)
@Resources({
   @Resource(source="faces-config.xml", destination="WEB-INF/faces-config.xml"),
   @Resource(source="web.xml", destination=WarArtifactDescriptor.WEB_XML_DESTINATION),
   @Resource(source="home.xhtml", destination="home.xhtml")
})
@Classes({
   Foo.class,
   Updated.class
})
public class TxEventTest extends AbstractHtmlUnitTest
{
   
   @Test(description="WBRI-401")
   public void testRequestContextLifecycle() throws Exception
   {
      WebClient webClient = new WebClient();
      HtmlPage home = webClient.getPage(getPath("/home.jsf"));
      HtmlSubmitInput beginConversationButton = getFirstMatchingElement(home, HtmlSubmitInput.class, "SaveButton");
      beginConversationButton.click();
   }
   
}