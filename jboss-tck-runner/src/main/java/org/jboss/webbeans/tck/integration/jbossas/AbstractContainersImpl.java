package org.jboss.webbeans.tck.integration.jbossas;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.apache.log4j.Logger;
import org.jboss.jsr299.tck.api.Configurable;
import org.jboss.jsr299.tck.api.Configuration;
import org.jboss.jsr299.tck.spi.Containers;

public abstract class AbstractContainersImpl implements Configurable, Containers
{
   
   private static Logger log = Logger.getLogger(AbstractContainersImpl.class);
   
   private Configuration configuration;
   protected boolean validated;

   protected static void copy(InputStream inputStream, File file) throws IOException
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

   public void setConfiguration(Configuration configuration)
   {
      this.configuration = configuration;
   }

   protected void validate()
   {
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

   public AbstractContainersImpl()
   {
      super();
   }
   
}