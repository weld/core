package org.jboss.webbeans.exceptions;

import javax.webbeans.DefinitionException;

public class NotAScopeException extends DefinitionException
{

   public NotAScopeException()
   {
      super();
   }

   public NotAScopeException(String message, Throwable throwable)
   {
      super(message, throwable);
   }

   public NotAScopeException(String message)
   {
      super(message);
   }

   public NotAScopeException(Throwable throwable)
   {
      super(throwable);
   }
   
}
