/**
 * 
 */
package org.jboss.webbeans.test;

import org.testng.annotations.Test;

/**
 * Unit tests for the deferred event notification object used to delay
 * notification till the end of a transaction.
 * 
 * @author David Allen
 *
 */
public class DeferredEventNotificationTest
{

   /**
    * Test method for {@link org.jboss.webbeans.event.DeferredEventNotification#beforeCompletion()}.
    */
   @Test
   public final void testBeforeCompletion()
   {
      // When the transaction is committed, the beforeCompletion() method is
      // invoked which in turn invokes the observer.  Here the mock observer
      // is used to keep track of the event being fired.
   }

}
