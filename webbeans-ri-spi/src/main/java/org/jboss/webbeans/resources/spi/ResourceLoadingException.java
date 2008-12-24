/**
 * 
 */
package org.jboss.webbeans.resources.spi;

import javax.webbeans.ExecutionException;

public class ResourceLoadingException extends ExecutionException
{

   public ResourceLoadingException()
   {
      super();
   }

   public ResourceLoadingException(String message, Throwable throwable)
   {
      super(message, throwable);
   }

   public ResourceLoadingException(String message)
   {
      super(message);
   }

   public ResourceLoadingException(Throwable throwable)
   {
      super(throwable);
   }
   
}