/**
 * 
 */
package org.jboss.webbeans.bean.standard;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.context.CreationalContext;
import javax.inject.manager.Manager;

import org.jboss.webbeans.ManagerImpl;

public class ManagerBean extends AbstractStandardBean<ManagerImpl>
{
   
   private static final Set<Type> TYPES = new HashSet<Type>(Arrays.asList(ManagerImpl.class, Manager.class));
   
   public static final ManagerBean of(ManagerImpl manager)
   {
      return new ManagerBean(manager);
   }
   
   protected ManagerBean(ManagerImpl manager)
   {
      super(manager);
   }

   public ManagerImpl create(CreationalContext<ManagerImpl> creationalContext)
   {
      return getManager();
   }

   @Override
   public Class<ManagerImpl> getType()
   {
      return ManagerImpl.class;
   }

   @Override
   public Set<Type> getTypes()
   {
      return TYPES;
   }

   public void destroy(ManagerImpl instance)
   {
      // No-op
   }
   
}