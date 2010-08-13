package org.jboss.weld.environment.servlet.test.util;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

public class Deployments
{

   public static final String WELD_SERVLET_ARTIFACT_NAME = "org.jboss.weld.servlet:weld-servlet";
   public static final String CONTEXT_PATH = "http://localhost:8888/test";

   public static final Asset ARQUILLIAN_WEB_XML = new ByteArrayAsset("<web-app> <servlet><servlet-name>ServletTestRunner</servlet-name><servlet-class>org.jboss.arquillian.protocol.servlet_3.ServletTestRunner</servlet-class></servlet> <servlet-mapping><servlet-name>ServletTestRunner</servlet-name><url-pattern>/ArquillianServletRunner</url-pattern></servlet-mapping> <listener><listener-class>org.jboss.weld.environment.servlet.Listener</listener-class></listener> </web-app>".getBytes());

   public static WebArchive baseDeployment(BeansXml beansXml, Asset webXml)
   {
      return ShrinkWrap.create(WebArchive.class, "test.war").addWebResource(beansXml, "beans.xml").addLibrary(MavenArtifactResolver.resolve(WELD_SERVLET_ARTIFACT_NAME)).setWebXML(webXml);
   }
   
   public static WebArchive baseDeployment(BeansXml beansXml)
   {
      return baseDeployment(beansXml, ARQUILLIAN_WEB_XML);
   }
   
   public static WebArchive baseDeployment()
   {
      return baseDeployment(new BeansXml(), ARQUILLIAN_WEB_XML);
   }
   
   public static WebArchive baseDeployment(Asset webXml)
   {
      return baseDeployment(new BeansXml(), webXml);
   }

}
