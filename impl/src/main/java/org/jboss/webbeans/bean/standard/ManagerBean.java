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

import org.jboss.webbeans.RootManager;

public class ManagerBean extends AbstractStandardBean<RootManager>
{
   
   private static final Set<Type> TYPES = new HashSet<Type>(Arrays.asList(RootManager.class, Manager.class));
   
   public static final ManagerBean of(RootManager manager)
   {
      return new ManagerBean(manager);
   }
   
   protected ManagerBean(RootManager manager)
   {
      super(manager);
   }

   public RootManager create(CreationalContext<RootManager> creationalContext)
   {
      return getManager();
   }

   @Override
   public Class<RootManager> getType()
   {
      return RootManager.class;
   }

   @Override
   public Set<Type> getTypes()
   {
      return TYPES;
   }

   public void destroy(RootManager instance)
   {
      // No-op
   }
   
   @Override
   public boolean isSerializable()
   {
      return true;
   }
   
   @Override
   public String toString()
   {
      return "Built-in javax.inject.manager.Manager bean";
   }
   
   
}