package org.jboss.weld.environment.servlet.test.util;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

public class Deployments
{
   public static final String CONTEXT_PATH = "http://localhost:8888/test";

   private static final String DEFAULT_WEB_XML_PREFIX = "<web-app> <servlet><servlet-name>ServletTestRunner</servlet-name><servlet-class>org.jboss.arquillian.protocol.servlet_3.ServletTestRunner</servlet-class></servlet> <servlet-mapping><servlet-name>ServletTestRunner</servlet-name><url-pattern>/ArquillianServletRunner</url-pattern></servlet-mapping> <listener><listener-class>org.jboss.weld.environment.servlet.Listener</listener-class></listener> <resource-env-ref><resource-env-ref-name>BeanManager</resource-env-ref-name><resource-env-ref-type>javax.enterprise.inject.spi.BeanManager</resource-env-ref-type></resource-env-ref> ";
   private static final String DEFAULT_WEB_XML_SUFFIX = "</web-app>";
   
   public static final Asset DEFAULT_WEB_XML = new ByteArrayAsset((DEFAULT_WEB_XML_PREFIX + DEFAULT_WEB_XML_SUFFIX).getBytes());

   public static WebArchive baseDeployment(BeansXml beansXml, Asset webXml)
   {
      return ShrinkWrap.create(WebArchive.class, "test.war")
         .addWebResource(beansXml, "beans.xml")
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
