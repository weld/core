package org.jboss.weld.environment.servlet.test.el;

import static org.jboss.arquillian.api.RunModeType.AS_CLIENT;
import static org.jboss.weld.environment.servlet.test.util.TomcatDeployments.CONTEXT_XML;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.api.Run;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.runner.RunWith;

@Run(AS_CLIENT)
@RunWith(Arquillian.class)
public class JsfTest extends JsfTestBase
{

   @Deployment
   public static WebArchive deployment()
   {
      return JsfTestBase.deployment().add(CONTEXT_XML, "META-INF/context.xml");
   }
   
   @Override
   protected String getPath(String page)
   {
      return "http://localhost:8888/test" + page;
   }
   
}
