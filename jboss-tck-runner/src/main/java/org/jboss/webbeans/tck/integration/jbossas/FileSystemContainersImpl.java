package org.jboss.webbeans.tck.integration.jbossas;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.jboss.jsr299.tck.api.Configurable;
import org.jboss.jsr299.tck.spi.Containers;


public class FileSystemContainersImpl extends AbstractContainersImpl
{
   
   private static Logger log = Logger.getLogger(FileSystemContainersImpl.class);
   
   public static final String JBOSS_HOME_PROPERTY_NAME = "jbossHome";
   
   private File deployDir;
   
   public FileSystemContainersImpl() throws IOException
   {
      String jbossHome = System.getProperty(JBOSS_HOME_PROPERTY_NAME);
      if (jbossHome == null)
      {
         throw new IllegalArgumentException("-DjbossHome must be set");
      }
      deployDir = new File(jbossHome, "server/default/deploy");
      if (!deployDir.isDirectory())
      {
         throw new IllegalArgumentException(deployDir.getPath() + " is not a directory"); 
      }
      log.info("Deploying TCK artifacts to " + deployDir.getPath());
   }
   
   public void deploy(InputStream archive, String name) throws IOException
   {
      if (!validated)
      {
         validate();
      }
      File file = new File(deployDir, name);
      log.info("Deploying test " + name);
      file.createNewFile();
      copy(archive, file);
   }
   
   public void undeploy(String name) throws IOException
   {
      File file = new File(deployDir, name);
      if (file.exists())
      {
         file.delete();
      }
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
   
}
