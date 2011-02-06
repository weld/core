package org.jboss.weld.environment.servlet.test.util;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

public class Deployments
{
   public static final String DEFAULT_WEB_XML_PREFIX = "<web-app> <listener><listener-class>org.jboss.weld.environment.servlet.Listener</listener-class></listener> <resource-env-ref><resource-env-ref-name>BeanManager</resource-env-ref-name><resource-env-ref-type>javax.enterprise.inject.spi.BeanManager</resource-env-ref-type></resource-env-ref> ";
   public static final String DEFAULT_WEB_XML_SUFFIX = "</web-app>";
   
   public static final Asset DEFAULT_WEB_XML = new ByteArrayAsset((DEFAULT_WEB_XML_PREFIX + DEFAULT_WEB_XML_SUFFIX).getBytes());
   
   public static final Asset EMPTY_FACES_CONFIG_XML = new ByteArrayAsset("<faces-config version=\"2.0\" xmlns=\"http://java.sun.com/xml/ns/javaee\"></faces-config>".getBytes());
   
   public static final Asset FACES_WEB_XML = new ByteArrayAsset((DEFAULT_WEB_XML_PREFIX + "<listener><listener-class>com.sun.faces.config.ConfigureListener</listener-class></listener> <context-param><param-name>javax.faces.DEFAULT_SUFFIX</param-name><param-value>.xhtml</param-value></context-param> <servlet><servlet-name>Faces Servlet</servlet-name><servlet-class>javax.faces.webapp.FacesServlet</servlet-class><load-on-startup>1</load-on-startup></servlet> <servlet-mapping><servlet-name>Faces Servlet</servlet-name><url-pattern>*.jsf</url-pattern></servlet-mapping> " + DEFAULT_WEB_XML_SUFFIX).getBytes());

   public static WebArchive baseDeployment(BeansXml beansXml, Asset webXml)
   {
      return ShrinkWrap.create(WebArchive.class)
         .addAsWebInfResource(beansXml, "beans.xml")
         .setWebXML(webXml);
   }
   
   public static WebArchive baseDeployment(BeansXml beansXml)
   {
      return baseDeployment(beansXml, DEFAULT_WEB_XML);
   }
   
   public static WebArchive baseDeployment()
   {
      return baseDeployment(new BeansXml(), DEFAULT_WEB_XML);
   }
   
   public static WebArchive baseDeployment(Asset webXml)
   {
      return baseDeployment(new BeansXml(), webXml);
   }
   
   /**
    * Inserts the extension into the end of the default web.xml (just before closing web-app)
    * 
    * @param extension
    * @return
    */
   public static String extendDefaultWebXml(String extension)
   {
      return DEFAULT_WEB_XML_PREFIX + extension + DEFAULT_WEB_XML_SUFFIX;
   }

}
