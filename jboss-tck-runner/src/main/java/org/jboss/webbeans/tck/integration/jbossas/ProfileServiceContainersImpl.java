package org.jboss.webbeans.tck.integration.jbossas;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.naming.InitialContext;

import org.apache.log4j.Logger;
import org.jboss.deployers.client.spi.IncompleteDeploymentException;
import org.jboss.deployers.spi.management.deploy.DeploymentManager;
import org.jboss.deployers.spi.management.deploy.DeploymentProgress;
import org.jboss.deployers.spi.management.deploy.DeploymentStatus;
import org.jboss.jsr299.tck.api.DeploymentException;
import org.jboss.managed.api.ManagedDeployment.DeploymentPhase;
import org.jboss.profileservice.spi.ProfileKey;
import org.jboss.profileservice.spi.ProfileService;
import org.jboss.virtual.VFS;

public class ProfileServiceContainersImpl extends AbstractContainersImpl
{

   private Logger log = Logger.getLogger(ProfileServiceContainersImpl.class);

   private final List<String> failedUndeployments;

   private DeploymentManager deploymentManager;
   private final File tmpdir;


   public ProfileServiceContainersImpl() throws Exception
   {
      tmpdir = new File(System.getProperty("java.io.tmpdir"), "org.jboss.webbeans.tck.integration.jbossas");
      tmpdir.mkdir();
      tmpdir.deleteOnExit();
      this.failedUndeployments = new ArrayList<String>();
   }


   @Override
   public void setup() throws IOException
   {
      super.setup();
      try
      {
		 initDeploymentManager();
	  }
      catch (Exception e)
	  {
		 IOException ioe = new IOException();
		 ioe.initCause(e);
	     throw ioe;
	  }
   }

   public void deploy(InputStream archiveStream, String name) throws DeploymentException, IOException
   {
      if (deploymentManager == null)
      {
         throw new IllegalStateException("setup() has not been called!");
      }
      Exception failure = null;
      try
      {
         File archive = new File(tmpdir, name);
         archive.deleteOnExit();
         copy(archiveStream, archive);
         DeploymentProgress distribute = deploymentManager.distribute(name, DeploymentPhase.APPLICATION, archive.toURI().toURL(), true);
         distribute.run();
         DeploymentProgress progress = deploymentManager.start(DeploymentPhase.APPLICATION, name);
         progress.run();
         DeploymentStatus status = progress.getDeploymentStatus();
         if (status.isFailed())
         {
            failure = status.getFailure();
            undeploy(name);
         }
      }
      catch (Exception e)
      {
		   IOException ioe = new IOException();
		   ioe.initCause(e);
	      throw ioe;
      }
      if (failure != null)
      {
         if (failure.getCause() instanceof IncompleteDeploymentException)
         {
            IncompleteDeploymentException incompleteDeploymentException = (IncompleteDeploymentException) failure.getCause();
            for (Entry<String, Throwable> entry : incompleteDeploymentException.getIncompleteDeployments().getContextsInError().entrySet())
            {
               if (entry.getKey().endsWith(name + "/_WebBeansBootstrap"))
               {
                  throw new DeploymentException(entry.getValue());
               }
            }
         }
         throw new DeploymentException(failure);
      }
   }

   public void undeploy(String name) throws IOException
   {
      try
      {
         DeploymentProgress stopProgress = deploymentManager.stop(DeploymentPhase.APPLICATION, name);
         stopProgress.run();

         DeploymentProgress undeployProgress = deploymentManager.undeploy(DeploymentPhase.APPLICATION, name);
         undeployProgress.run();
         if (undeployProgress.getDeploymentStatus().isFailed())
         {
        	 failedUndeployments.add(name);
         }
      }
      catch (Exception e)
      {
		 IOException ioe = new IOException();
		 ioe.initCause(e);
	     throw ioe;
      }
   }

   /**
    * Obtain the Deployment Manager
    * @throws Exception
    */
   protected void initDeploymentManager() throws Exception
   {
      String profileName = "default";
      InitialContext ctx = new InitialContext();
      ProfileService ps = (ProfileService) ctx.lookup("ProfileService");
      deploymentManager = ps.getDeploymentManager();
      ProfileKey defaultKey = new ProfileKey(profileName);
      deploymentManager.loadProfile(defaultKey, false);
      // Init the VFS to setup the vfs* protocol handlers
      VFS.init();
   }

   @Override
   public void cleanup() throws IOException
   {
	  super.cleanup();
	  List<String> remainingDeployments = new ArrayList<String>();
	  for (String name : failedUndeployments)
	  {
		  try
		  {
			  DeploymentProgress undeployProgress = deploymentManager.undeploy(DeploymentPhase.APPLICATION, name);
		      undeployProgress.run();
		      if (undeployProgress.getDeploymentStatus().isFailed())
		      {
		    	  remainingDeployments.add(name);
		      }
		  }
		  catch (Exception e)
		  {
			 IOException ioe = new IOException();
			 ioe.initCause(e);
		     throw ioe;
		  }
	   }
	  if (remainingDeployments.size() > 0)
	  {
		  //log.error("Failed to undeploy these artifacts: " + remainingDeployments);
	  }
   }

}
