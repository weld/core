package org.jboss.jsr299.tck.tests.implementation.enterprise.lifecycle;

import javax.enterprise.context.spi.CreationalContext;

public class MockCreationalContext<T> implements CreationalContext<T>
{

   public void push(T incompleteInstance)
   {

   }
   
   public void release()
   {
      // TODO Auto-generated method stub
      
   }

}
