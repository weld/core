package org.jboss.webbeans.tck.integration.jbossas;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
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
import org.jboss.webbeans.tck.integration.jbossas.util.DeploymentProperties;

/**
 *
 * @author jeffgenender
 * @author Pete Muir
 *
 */
public abstract class AbstractContainersImpl implements Configurable, Containers
{

   public static String JAVA_OPTS = " -ea";

   public static final String JBOSS_HOME_PROPERTY_NAME = "jboss.home";
   public static final String JAVA_OPTS_PROPERTY_NAME = "java.opts";
   public static final String JBOSS_AS_DIR_PROPERTY_NAME = "jboss-as.dir";
   public static final String JBOSS_BOOT_TIMEOUT_PROPERTY_NAME = "jboss.boot.timeout";
   public static final String FORCE_RESTART_PROPERTY_NAME = "jboss.force.restart";
   public static final String MAX_DEPLOYMENTS_PROPERTY_NAME = "jboss.deployments.restart";
   public static final String SHUTDOWN_DELAY_PROPERTY_NAME = "jboss.shutdown.delay";

   private static Logger log = Logger.getLogger(AbstractContainersImpl.class);

   private final DeploymentProperties properties;

   private Configuration configuration;
   protected String jbossHome;
   private String jbossHttpUrl;
   private boolean jbossWasStarted;
   private long bootTimeout;
   private String javaOpts;

   private boolean forceRestart;

   protected int maxDeployments;

   private int jbossShutdownDelay;

   public AbstractContainersImpl()
   {
      this.properties = new DeploymentProperties();
   }

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

   protected boolean isJBossUp()
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
      String jbossAsPath = properties.getStringValue(JBOSS_AS_DIR_PROPERTY_NAME, "../jboss-as", false);
      if (jbossAsPath != null)
      {
         File jbossAsDir = new File(jbossAsPath);
         if (jbossAsDir.isDirectory())
         {
            File buildProperties = new File(jbossAsDir, "build.properties");
            if (buildProperties.exists())
            {
               loadProperties(buildProperties);
            }
            File localBuildProperties = new File(jbossAsDir, "local.build.properties");
            if (localBuildProperties.exists())
            {
               loadProperties(localBuildProperties);
            }
         }
      }
      jbossHome = properties.getStringValue(JBOSS_HOME_PROPERTY_NAME, null, true);
      javaOpts = properties.getStringValue(JAVA_OPTS_PROPERTY_NAME, "", false);
      javaOpts = javaOpts + JAVA_OPTS;
      File jbossHomeFile = new File(jbossHome);
      jbossHome = jbossHomeFile.getPath();
      log.info("Using JBoss instance in " + jbossHome + " at URL " + configuration.getHost());
      this.bootTimeout = properties.getLongValue(JBOSS_BOOT_TIMEOUT_PROPERTY_NAME, 240000, false);
      this.forceRestart = properties.getBooleanValue(FORCE_RESTART_PROPERTY_NAME, false, false);
      this.maxDeployments = properties.getIntValue(MAX_DEPLOYMENTS_PROPERTY_NAME, 25, false);
      this.jbossShutdownDelay = properties.getIntValue(SHUTDOWN_DELAY_PROPERTY_NAME, 15000, false); 
      restartJboss();
   }
   
   protected void restartJboss() throws IOException
   {
      if (forceRestart)
      {
         if (isJBossUp())
         {
            log.info("Shutting down JBoss instance as in force-restart mode");
            shutDownJBoss();
            try
            {
               Thread.sleep(jbossShutdownDelay);
            }
            catch (InterruptedException e)
            {
               Thread.currentThread().interrupt();
            }
         }
      }
      if (!isJBossUp())
      {
         jbossWasStarted = true;
         launch("run", "");
         log.info("Starting JBoss instance");
         // Wait for JBoss to come up
         long timeoutTime = System.currentTimeMillis() + bootTimeout;
         boolean interrupted = false;
         while (timeoutTime > System.currentTimeMillis())
         {
            if (isJBossUp())
            {
               log.info("Started JBoss instance");
               return;
            }
            try
            {
               Thread.sleep(200);
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
         log.warn("Unable to connect to JBoss instance after " + bootTimeout + "ms, giving up!");
         launch("shutdown", "-S");
         throw new IllegalStateException("Error connecting to JBoss instance");
      }
   }

   protected void loadProperties(File file) throws IOException
   {
	   InputStream is = new FileInputStream(file);
	   try
	   {
			System.getProperties().load(is);
	   }
	   finally
	   {
		   is.close();
	   }
   }

   public String getJbossHome()
   {
      return jbossHome;
   }

   public void cleanup() throws IOException
   {
      if (jbossWasStarted)
      {
         log.info("Shutting down JBoss instance");
         shutDownJBoss();
      }
   }

   private void shutDownJBoss() throws IOException
   {
      launch("shutdown", "-S");
      log.info("Shut down JBoss AS");
   }

   private void launch(String scriptFileName, String params) throws IOException
   {
      String osName = System.getProperty("os.name");
      Runtime runtime = Runtime.getRuntime();

      Process p = null;
      if (osName.startsWith("Windows"))
      {
          String command[] = {
                "cmd.exe",
                "/C",
                "set JAVA_OPTS=" + javaOpts + " & cd /D " + jbossHome + "\\bin & " + scriptFileName + ".bat " + params
          };
          p = runtime.exec(command);
      }
      else
      {
          String command[] = {
                "sh",
                "-c",
                "cd " + jbossHome + "/bin;JAVA_OPTS=\"" + javaOpts + "\" ./" + scriptFileName + ".sh " + params
                };
          p = runtime.exec(command);
      }
      dump(p.getErrorStream());
      dump(p.getInputStream());
   }

   protected void dump(final InputStream is)
   {
      new Thread(new Runnable()
      {
          public void run()
          {
             try
             {
                DataOutputStream out = new DataOutputStream(new FileOutputStream(configuration.getOutputDirectory() + File.separator + "jboss.log"));
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