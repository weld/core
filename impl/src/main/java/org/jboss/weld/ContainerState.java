package org.jboss.weld;

/**
 * Container status
 * @author pmuir
 *
 */
public enum ContainerState
{
   /**
    * The container has not been started
    */
   STOPPED(false),
   /**
    * The container is starting
    */
   STARTING(false),
   /**
    * The container has started and beans have been deployed
    */
   INITIALIZED(true),
   /**
    * The deployment has been validated
    */
   VALIDATED(true),
   /**
    * The container has been shutdown
    */
   SHUTDOWN(false);
   
   private ContainerState(boolean available)
   {
      this.available = available;
   }
   
   final boolean available;
   
   /**
    * Whether the container is available for use
    * 
    * @return
    */
   public boolean isAvailable()
   {
      return available;
   }
}