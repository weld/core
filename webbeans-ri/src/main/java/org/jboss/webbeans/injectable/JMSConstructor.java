package org.jboss.webbeans.injectable;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.util.JNDI;

public class JMSConstructor<T> implements BeanConstructor<T, Object>
{

   Class<? extends T> type;
   
   private String jndiName;
   
   public JMSConstructor(String jndiName)
   {
      this.jndiName = jndiName;
   }
   
   public T invoke(ManagerImpl manager)
   {
      return JNDI.lookup(jndiName, type);
   }

   public Object getAnnotatedItem()
   {
      // TODO Auto-generated method stub
      return null;
   }
   
   public T invoke(ManagerImpl manager, Object instance)
   {
      return invoke(manager);
   }

}
