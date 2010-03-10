package org.jboss.weld.manager;

import javax.enterprise.context.spi.CreationalContext;

import org.jboss.weld.injection.InjectionContextImpl;
import org.jboss.weld.introspector.WeldClass;
import org.jboss.weld.util.Beans;

public class MessageDrivenInjectionTarget<T> extends SimpleInjectionTarget<T>
{

   public MessageDrivenInjectionTarget(WeldClass<T> type, BeanManagerImpl beanManager)
   {
      super(type, beanManager);
   }
   
   public void inject(final T instance, final CreationalContext<T> ctx)
   {
      new InjectionContextImpl<T>(beanManager, this, instance)
      {
         public void proceed()
         {
            Beans.injectFieldsAndInitializers(instance, ctx, beanManager, injectableFields, initializerMethods);
         }
      }.run();

   }   

}
