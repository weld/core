package org.jboss.webbeans.test.beans;

import javax.webbeans.Named;
import javax.webbeans.Observes;

import org.jboss.webbeans.test.annotations.Tame;

@Tame
@Named("Teddy")
public class TeaCupPomeranian
{
   public static class OversizedException extends RuntimeException
   {
      private static final long serialVersionUID = 1L;
      
   }
   
   public static class TooSmallException extends Exception
   {
      private static final long serialVersionUID = 1L;
   }
   
   public void observeSimpleEvent(@Observes String someEvent)
   {
      throw new OversizedException();
   }
   
   public void observeAnotherSimpleEvent(@Observes Integer someEvent) throws TooSmallException
   {
      throw new TooSmallException();
   }
}
