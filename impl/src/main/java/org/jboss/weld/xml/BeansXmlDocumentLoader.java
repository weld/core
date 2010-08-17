/**
 * 
 */
package org.jboss.weld.xml;

import static org.jboss.weld.logging.messages.XmlMessage.CONFIGURATION_ERROR;
import static org.jboss.weld.logging.messages.XmlMessage.LOAD_ERROR;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jboss.weld.exceptions.IllegalArgumentException;
import org.w3c.dom.Document;

abstract class BeansXmlDocumentLoader
{

   private final URL beansXml;

   BeansXmlDocumentLoader(URL beansXml)
   {
      this.beansXml = beansXml;
   }

   protected abstract Document loadDocument(InputStream in);

   public Document run()
   {
      InputStream is = null;
      try
      {
         is = openStream();
         return loadDocument(is);
      }
      finally
      {
         closeStream(is);
      }
   }

   private void closeStream(InputStream in)
   {
      if (in != null)
      {
         try
         {
            in.close();
         }
         catch (IOException e)
         {
            throw new IllegalStateException("Error closing stream " + in);
         }
      }
   }

   private InputStream openStream()
   {
      if (beansXml == null)
      {
         throw new org.jboss.weld.exceptions.IllegalStateException(LOAD_ERROR, "URL: null");
      }
      try
      {
         return beansXml.openStream();
      }
      catch (IOException e)
      {
         throw new org.jboss.weld.exceptions.IllegalStateException(LOAD_ERROR, e, beansXml.toString());
      }
   }

   protected boolean isDocumentEmpty(InputStream in)
   {
      try
      {
         return in.available() == 0;
      }
      catch (IOException e)
      {
         throw new IllegalArgumentException(LOAD_ERROR, e, beansXml.toString());
      }
   }

   protected DocumentBuilder createDocumentBuilder()
   {
      try
      {
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         factory.setNamespaceAware(true);
         return factory.newDocumentBuilder();
      }
      catch (ParserConfigurationException e)
      {
         throw new org.jboss.weld.exceptions.IllegalStateException(CONFIGURATION_ERROR, e);
      }
   }
}