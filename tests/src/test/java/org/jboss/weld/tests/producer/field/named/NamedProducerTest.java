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

package org.jboss.weld.tests.producer.field.named;

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

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

/**
 * <p>Check what happens when session.invalidate() is called.</p>
 * 
 * @author Pete Muir
 *
 */
@Artifact(addCurrentPackage=false)
@Classes({User.class, NewUserAction.class, Employee.class, SaveAction.class})
@IntegrationTest(runLocally=true)
@Resources({
   @Resource(destination=WarArtifactDescriptor.WEB_XML_DESTINATION, source="web.xml"),
   @Resource(destination="view.xhtml", source="view.xhtml"),
   @Resource(destination="home.xhtml", source="home.xhtml"),
   @Resource(destination="/WEB-INF/faces-config.xml", source="faces-config.xml")
})
public class NamedProducerTest extends AbstractWeldTest
{
   
   @Test(description = "forum post")
   public void testNamedProducerWorks() throws Exception
   {
      WebClient client = new WebClient();
      client.setThrowExceptionOnFailingStatusCode(false);
      
      HtmlPage page = client.getPage(getPath("/view.jsf"));
      // Check the page rendered ok
      assert getFirstMatchingElement(page, HtmlSubmitInput.class, "saveButton") != null;
   }
   
   @Test(description = "WELD-404")
   public void testNamedProducerFieldLoosesValues() throws Exception
   {
      WebClient client = new WebClient();
      
      HtmlPage page = client.getPage(getPath("/home.jsf"));
      // Check the page rendered ok
      HtmlSubmitInput saveButton = getFirstMatchingElement(page, HtmlSubmitInput.class, "saveButton");
      HtmlTextInput employeeFieldName = getFirstMatchingElement(page, HtmlTextInput.class, "employeeFieldName");
      HtmlTextInput employeeMethodName = getFirstMatchingElement(page, HtmlTextInput.class, "employeeMethodName");
      assert employeeFieldName != null;
      assert employeeMethodName != null;
      assert saveButton != null;
      employeeFieldName.setValueAttribute("Pete");
      employeeMethodName.setValueAttribute("Gavin");
      saveButton.click();
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
