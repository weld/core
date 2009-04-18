package org.jboss.jsr299.tck.tests.implementation.enterprise.lifecycle;

import javax.context.CreationalContext;

public class MockCreationalContext<T> implements CreationalContext<T>
{

   public void push(T incompleteInstance)
   {

   }

}
