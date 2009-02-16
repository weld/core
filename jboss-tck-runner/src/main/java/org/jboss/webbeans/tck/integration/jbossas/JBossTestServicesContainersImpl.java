package org.jboss.webbeans.tck.integration.jbossas;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.jboss.jsr299.tck.api.DeploymentException;
import org.jboss.test.JBossTestServices;


public class JBossTestServicesContainersImpl extends AbstractContainersImpl
{
   
   private Logger log = Logger.getLogger(JBossTestServicesContainersImpl.class);
   
   private final JBossTestServices testServices;
   private final File tmpdir;
   
   public JBossTestServicesContainersImpl() throws Exception
   {
      this.testServices = new JBossTestServices(JBossTestServicesContainersImpl.class);
      testServices.setUpLogging();
      testServices.init();
      tmpdir = new File(System.getProperty("java.io.tmpdir"), "org.jboss.webbeans.tck.integration.jbossas");
      tmpdir.mkdir();
      tmpdir.deleteOnExit();
   }
   
   public void deploy(InputStream archiveStream, String name) throws DeploymentException, IOException
   {
      File archive = new File(tmpdir, name);
      archive.deleteOnExit();
      copy(archiveStream, archive);
      try
      {
         testServices.deploy(getTmpArchiveName(name));
      }
      catch (Exception e)
      {
         throw new DeploymentException("Error deploying " + name, e);
      } 
   }
   
   public void undeploy(String name) throws IOException
   {
      try
      {
         testServices.undeploy(getTmpArchiveName(name));
      }
      catch (Exception e)
      {
         throw new IOException("Error undeploying " + name, e);
      }
   }
   
   private String getTmpArchiveName(String name)
   {
      File file = new File(tmpdir, name);
      return file.toURI().toString();
   }
   
}
