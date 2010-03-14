/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.weld.tests.contexts.sessionInvalidation;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Set;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.Classes;
import org.jboss.testharness.impl.packaging.IntegrationTest;
import org.jboss.testharness.impl.packaging.Resource;
import org.jboss.testharness.impl.packaging.Resources;
import org.jboss.testharness.impl.packaging.war.WarArtifactDescriptor;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;

/**
 * <p>Check what happens when session.invalidate() is called.</p>
 * 
 * @author Pete Muir
 *
 */
@Artifact(addCurrentPackage=false)
@Classes({Storm.class,SomeBean.class})
@IntegrationTest(runLocally=true)
@Resources({
   @Resource(destination=WarArtifactDescriptor.WEB_XML_DESTINATION, source="web.xml"),
   @Resource(destination="storm.jspx", source="storm.jsf"),
   @Resource(destination="/WEB-INF/faces-config.xml", source="faces-config.xml")
})
public class InvalidateSessionTest extends AbstractWeldTest
{
   @Test(description = "WELD-380, WELD-403")
   public void testInvalidateSessionCalled() throws Exception
   {
      WebClient client = new WebClient();
      client.setThrowExceptionOnFailingStatusCode(true);
      
      HtmlPage page = client.getPage(getPath("/storm.jsf"));
      HtmlSubmitInput invalidateSessionButton = getFirstMatchingElement(page, HtmlSubmitInput.class, "invalidateSessionButton");
      page = invalidateSessionButton.click();
      HtmlInput inputField = getFirstMatchingElement(page, HtmlInput.class, "prop");
      assert Storm.PROPERTY_VALUE.equals(inputField.getValueAttribute());
      
      // Make another request to verify that the session bean value is not the
      // one from the previous invalidated session.
      page = client.getPage(getPath("/storm.jsf"));
      inputField = getFirstMatchingElement(page, HtmlInput.class, "prop");
      assert SomeBean.DEFAULT_PROPERTY_VALUE.equals(inputField.getValueAttribute());
   }

   @Test(description="WELD-461")
   public void testNoDoubleDestructionOnExternalRedirect() throws Exception
   {
	   WebClient client = new WebClient();
	   HtmlPage page = client.getPage(getPath("/storm.jsf"));
	   HtmlSubmitInput button = getFirstMatchingElement(page, HtmlSubmitInput.class, "redirectButton");
	   button.click();
   }
   
   protected <T> Set<T> getElements(HtmlElement rootElement, Class<T> elementClass)
   {
     Set<T> result = new HashSet<T>();
     
     for (HtmlElement element : rootElement.getAllHtmlChildElements())
     {
        result.addAll(getElements(element, elementClass));
     }
     
     if (elementClass.isInstance(rootElement))
     {
        result.add(elementClass.cast(rootElement));
     }
     return result;
     
   }
 
   protected <T extends HtmlElement> T getFirstMatchingElement(HtmlPage page, Class<T> elementClass, String id)
   {
     
     Set<T> inputs = getElements(page.getBody(), elementClass);
     for (T input : inputs)
     {
         if (input.getId().contains(id))
         {
            return input;
         }
     }
     return null;
   }
   
}
