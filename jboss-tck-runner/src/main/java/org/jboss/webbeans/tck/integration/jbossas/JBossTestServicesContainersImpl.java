package org.jboss.webbeans.tck.integration.jbossas;
import java.io.File;
import java.io.InputStream;

import org.apache.log4j.Logger;
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
   
   public void deploy(InputStream archiveStream, String name) throws Exception
   {
      File archive = new File(tmpdir, name);
      archive.deleteOnExit();
      copy(archiveStream, archive);
      testServices.deploy(getTmpArchiveName(name)); 
   }
   
   public void undeploy(String name) throws Exception
   {
      testServices.undeploy(getTmpArchiveName(name));
      try
      {
         // Give the app a chance to undeploy
         Thread.sleep(1000);
      }
      catch (InterruptedException e)
      {
         Thread.currentThread().interrupt();
      }
   }
   
   private String getTmpArchiveName(String name)
   {
      File file = new File(tmpdir, name);
      return file.toURI().toString();
   }
   
}
