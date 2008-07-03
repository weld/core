package org.jboss.webbeans.injectable;

import javax.webbeans.Container;

import org.jboss.webbeans.util.JNDI;

public class JMSConstructor<T> implements ComponentConstructor<T>
{

   Class<? extends T> type;
   
   private String jndiName;
   
   public JMSConstructor(String jndiName)
   {
      this.jndiName = jndiName;
   }
   
   public T invoke(Container container)
   {
      return JNDI.lookup(jndiName, type);
   }

}
