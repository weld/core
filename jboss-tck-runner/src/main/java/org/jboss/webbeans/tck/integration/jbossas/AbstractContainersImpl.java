package org.jboss.webbeans.tck.integration.jbossas;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
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

/**
 * 
 * @author jeffgenender
 * @author Pete Muir
 *
 */
public abstract class AbstractContainersImpl implements Configurable, Containers
{
   
   public static String JAVA_OPTS = "\"-Xms128m -Xmx512m -XX:MaxPermSize=256m -Dorg.jboss.resolver.warning=true -Dsun.rmi.dgc.client.gcInterval=3600000 -Dsun.rmi.dgc.server.gcInterval=3600000 -ea\"";
   
   public static final String JBOSS_HOME_PROPERTY_NAME = "jboss.home";
   public static final String JBOSS_AS_DIR_PROPERTY_NAME = "jboss-as.dir";
   public static final String JBOSS_BOOT_TIMEOUT_PROPERTY_NAME = "jboss.boot.timeout";
   
   private static Logger log = Logger.getLogger(AbstractContainersImpl.class);
   
   private Configuration configuration;
   protected String jbossHome;
   private String jbossHttpUrl;
   private boolean jbossWasStarted;
   private long bootTimeout;

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
      this.jbossHttpUrl = "http://" + configuration.getHost() + "/";
   }

   protected boolean checkJBossUp()
   {
      // Check that JBoss is up!
      try
      {
         URLConnection connection = new URL(jbossHttpUrl).openConnection();
         if (!(connection instanceof HttpURLConnection))
         {
            throw new IllegalStateException("Not an http connection! " + connection);
         }
         HttpURLConnection httpConnection = (HttpURLConnection) connection;
         httpConnection.connect();
         if (httpConnection.getResponseCode() != HttpURLConnection.HTTP_OK)
         {
            return false;
         }
      }
      catch (Exception e) 
      {
         return false;
      }
      log.info("Successfully connected to JBoss AS at " + jbossHttpUrl);
      return true;
   }
   
   public void setup() throws IOException
   {
      if (System.getProperty(JBOSS_AS_DIR_PROPERTY_NAME) != null)
      {
         File jbossAsDir = new File(System.getProperty(JBOSS_AS_DIR_PROPERTY_NAME));
         if (jbossAsDir.isDirectory())
         {
            File buildProperties = new File(jbossAsDir, "build.properties");
            if (buildProperties.exists())
            {
               System.getProperties().load(new FileReader(buildProperties));
            }
            File localBuildProperties = new File(jbossAsDir, "local.build.properties");
            if (localBuildProperties.exists())
            {
               System.getProperties().load(new FileReader(localBuildProperties));
            }
         }            
      }
      jbossHome = System.getProperty(JBOSS_HOME_PROPERTY_NAME);
      if (jbossHome == null)
      {
         throw new IllegalArgumentException("-D" + JBOSS_HOME_PROPERTY_NAME + " must be set");
      }
      else
      {
         log.info("JBoss Home set to " + jbossHome);
      }
      this.bootTimeout = Long.getLong(JBOSS_BOOT_TIMEOUT_PROPERTY_NAME, 120000);
      if (!checkJBossUp())
      {
         jbossWasStarted = true;
         launch(jbossHome, "run", "");
         log.info("Starting JBoss AS");
         // Wait for JBoss to come up
         long timeoutTime = System.currentTimeMillis() + bootTimeout;
         boolean interrupted = false;
         while (timeoutTime > System.currentTimeMillis())
         {
            if (checkJBossUp()) 
            {
               log.info("Started JBoss AS");
               return;
            }
            try
            {
               Thread.sleep(500);
            }
            catch (InterruptedException e)
            {
               interrupted = true;
            }
         }
         if (interrupted)
         {
            Thread.currentThread().interrupt();
         }
         // If we got this far something went wrong
         log.warn("Unable to connect to JBoss after " + bootTimeout + "ms, giving up!");
         launch(jbossHome, "shutdown", "-S");
      }
      else
      {
         return;
      }
      // After trying to start automatically, try the connection again
      if (!checkJBossUp())
      {
         throw new IllegalStateException("Error connecting to JBoss AS at " + jbossHttpUrl);
      }
   }
   
   public void cleanup() throws IOException
   {
      if (jbossWasStarted)
      {
         log.info("Shutting down JBoss AS");
         launch(jbossHome, "shutdown", "-S");
         log.info("Shut down JBoss AS");
      }
   }
   
   private static void launch(String jbossHome, String scriptFileName, String params) throws IOException
   {
      String osName = System.getProperty("os.name");
      Runtime runtime = Runtime.getRuntime();

      Process p = null;
      if (osName.startsWith("Windows")) 
      {
          String command[] = {
                "cmd.exe",
                "/C",
                "set JAVA_OPTS=" + JAVA_OPTS + " & cd " + jbossHome + "\\bin & " + scriptFileName + ".bat " + params
          };
          p = runtime.exec(command);
      }
      else 
      {
          String command[] = {
                "sh",
                "-c",
                "cd " + jbossHome + "/bin; JAVA_OPTS=" + JAVA_OPTS + " ./" + scriptFileName + ".sh " + params
                };
          p = runtime.exec(command);
      }
      dump(p.getErrorStream());
      dump(p.getInputStream());
   }

   protected static void dump(final InputStream is) 
   {
      new Thread(new Runnable() 
      {
          public void run() 
          {
             try 
             {
                DataOutputStream out = new DataOutputStream(new FileOutputStream("/tmp/jboss.log"));
                int c;
                while((c = is.read()) != -1) 
                {
                   out.writeByte(c);
                }
                is.close();
                out.close();
             }
             catch(IOException e) 
             {
                System.err.println("Error Writing/Reading Streams.");
             }
          }
      }).start();
   }

   
}