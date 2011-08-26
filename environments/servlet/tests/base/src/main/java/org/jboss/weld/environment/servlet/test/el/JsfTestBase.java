package org.jboss.weld.environment.servlet.test.el;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static org.jboss.weld.environment.servlet.test.util.Deployments.*;
import static org.junit.Assert.assertNotNull;

public abstract class JsfTestBase
{

   public static final Asset CHARLIE_XHTML = new ByteArrayAsset(("<jsp:root xmlns:jsp=\"http://java.sun.com/JSP/Page\" " +
   		"xmlns:h=\"http://java.sun.com/jsf/html\" " +
         "xmlns:f=\"http://java.sun.com/jsf/core\" " +
         "xmlns:s=\"http://jboss.com/products/seam/taglib\" " +
         "xmlns=\"http://www.w3.org/1999/xhtml\" " +
         "version=\"2.0\"> " +
         "<jsp:output doctype-root-element=\"html\" " +
              "doctype-public=\"-//W3C//DTD XHTML 1.0 Transitional//EN\" " +
              "doctype-system=\"http://www.w3c.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"/> " +
   "<jsp:directive.page contentType=\"text/html\"/> " +
   "<html> " +
   "<head /> " +
   "<body> " +
   "<f:view> " +
      "<h:outputText value=\"#{chicken.name}\" id=\"oldel\"/>" +
      "<h:outputText value=\"#{chicken.getName()}\" id=\"newel\"/>" +
   "</f:view> " +
  "</body> " +
  "</html> " +
"</jsp:root>").getBytes());

   public static WebArchive deployment()
   {
      return baseDeployment(FACES_WEB_XML)
         .add(CHARLIE_XHTML, "charlie.xhtml")
         .addAsWebResource(EMPTY_FACES_CONFIG_XML, "faces-config.xml")
         .addClass(Chicken.class);
   }

   @Test
   public void testELWithParameters(@ArquillianResource URL baseURL) throws Exception
   {
      WebClient client = new WebClient();
      HtmlPage page = client.getPage(new URL(baseURL, "charlie.jsf"));

      page.asXml();

      HtmlSpan oldel = getFirstMatchingElement(page, HtmlSpan.class, "oldel");
      assertNotNull(oldel);
      assertEquals("Charlie", oldel.asText());

      HtmlSpan newel = getFirstMatchingElement(page, HtmlSpan.class, "newel");
      assertNotNull(newel);
      assertEquals("Charlie", newel.asText());
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
