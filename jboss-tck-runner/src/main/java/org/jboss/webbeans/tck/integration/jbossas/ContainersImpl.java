package org.jboss.webbeans.tck.integration.jbossas;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.log4j.Logger;
import org.jboss.jsr299.tck.api.Configurable;
import org.jboss.jsr299.tck.api.Configuration;
import org.jboss.jsr299.tck.spi.Containers;


public class ContainersImpl implements Containers, Configurable
{
   
   private Logger log = Logger.getLogger(ContainersImpl.class);
   
   public static final String JBOSS_HOME_PROPERTY_NAME = "jbossHome";
   
   private File deployDir;
   private Configuration configuration;
   
   private boolean validated;
   
   public void setConfiguration(Configuration configuration)
   {
      this.configuration = configuration;
      
     
   }
   
   protected void validate()
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
      
      // Check that JBoss is up!
      String url = "http://" + configuration.getHost() + "/";
      try
      {
         URLConnection connection = new URL(url).openConnection();
         if (!(connection instanceof HttpURLConnection))
         {
            throw new IllegalStateException("Not an http connection! " + connection);
         }
         HttpURLConnection httpConnection = (HttpURLConnection) connection;
         httpConnection.connect();
         if (httpConnection.getResponseCode() != HttpURLConnection.HTTP_OK)
         {
            throw new IllegalStateException("Error connecting to JBoss AS at " + url);
         }
      }
      catch (Exception e) 
      {
         throw new IllegalStateException("Cannot connect to JBoss AS", e);
      }
      log.info("Successfully connected to JBoss AS at " + url);
      
   }
   
   public ContainersImpl() throws MalformedURLException, IOException
   {
      
   }
   
   
   
   public void deploy(InputStream archive, String name) throws Exception
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
   
   private static void copy(InputStream inputStream, File file) throws IOException
   {
      OutputStream os = new FileOutputStream(file);
      try 
      {
         byte[] buf = new byte[1024];
         int i = 0;
         while ((i = inputStream.read(buf)) != -1) 
         {
             os.write(buf, 0, i);
         }
     } 
     finally 
     {
         os.close();
     }
   }
   
   public void undeploy(String name) throws Exception
   {
      File file = new File(deployDir, name);
      if (file.exists())
      {
         file.delete();
      }
      Thread.sleep(1000);
   }
   
}
