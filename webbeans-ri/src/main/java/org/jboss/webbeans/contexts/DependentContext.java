package org.jboss.webbeans.contexts;

import javax.webbeans.ContextNotActiveException;
import javax.webbeans.Dependent;
import javax.webbeans.manager.Bean;

/**
 * The dependent context
 * 
 * @author Nicklas Karlsson
 */
public class DependentContext extends PrivateContext
{

   public DependentContext()
   {
      super(Dependent.class);
      // TODO starts as non-active?
      setActive(false);
   }

   @Override
   public <T> T get(Bean<T> bean, boolean create)
   {
      if (!isActive())
      {
         throw new ContextNotActiveException();
      }
      // Dependent contexts don't really use any BeanMap storage
      return create == false ? null : bean.create();
   }   
   
   @Override
   public String toString()
   {
      return "Dependent context";
   }   
}
