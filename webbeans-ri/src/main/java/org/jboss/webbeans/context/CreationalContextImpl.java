package org.jboss.webbeans.context;

import javax.context.CreationalContext;

public class CreationalContextImpl<T> implements CreationalContext<T>
{
   
   public static final <T> CreationalContextImpl<T> newInstance()
   {
      return new CreationalContextImpl<T>();
   }
   
   public void push(T incompleteInstance)
   {
      // TODO Auto-generated method stub
      
   }
   
}
