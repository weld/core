package org.jboss.webbeans.tck.integration.jbossas;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;


public class FileSystemContainersImpl extends AbstractContainersImpl
{
   
   private static Logger log = Logger.getLogger(FileSystemContainersImpl.class);
   
   private File deployDir;
   
   public FileSystemContainersImpl() throws IOException
   {
      deployDir = new File(jbossHome, "server/default/deploy");
      if (!deployDir.isDirectory())
      {
         throw new IllegalArgumentException(deployDir.getPath() + " is not a directory"); 
      }
      log.info("Deploying TCK artifacts to " + deployDir.getPath());
   }
   
   public void deploy(InputStream archive, String name) throws IOException
   {
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
