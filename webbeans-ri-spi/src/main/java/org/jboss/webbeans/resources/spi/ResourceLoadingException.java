/**
 * 
 */
package org.jboss.webbeans.resources.spi;

import javax.inject.ExecutionException;


/**
 * Exception thrown when errors occur while loading resource
 * 
 * @author Pete Muir
 *
 */
public class ResourceLoadingException extends ExecutionException
{
   private static final long serialVersionUID = 1L;

   /**
    * Constructor
    */
   public ResourceLoadingException()
   {
      super();
   }

   /**
    * Constructor
    * 
    * @param message The message
    * @param throwable The exception
    */
   public ResourceLoadingException(String message, Throwable throwable)
   {
      super(message, throwable);
   }

   /**
    * Constructor
    * 
    * @param message The message
    */
   public ResourceLoadingException(String message)
   {
      super(message);
   }

   /**
    * Constructor
    * 
    * @param throwable The exception
    */
   public ResourceLoadingException(Throwable throwable)
   {
      super(throwable);
   }
   
}