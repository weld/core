package org.jboss.webbeans.bean;

import java.util.Set;

import javax.context.Dependent;
import javax.inject.manager.Bean;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.injection.AnnotatedInjectionPoint;

public abstract class RIBean<T> extends Bean<T>
{

   private final ManagerImpl manager;
   
   protected RIBean(ManagerImpl manager)
   {
      super(manager);
      this.manager = manager;
   }
   
   @Override
   protected ManagerImpl getManager()
   {
      return manager;
   }
   
   public abstract Class<T> getType();
   
   public abstract boolean isSpecializing();
   
   public boolean isDependent()
   {
      return getScopeType().equals(Dependent.class);
   }
   
   public abstract boolean isProxyable();
   
   public abstract boolean isPrimitive();
   
   public abstract Set<AnnotatedInjectionPoint<?, ?>> getInjectionPoints();
   
   public abstract RIBean<?> getSpecializedBean();
   
}
