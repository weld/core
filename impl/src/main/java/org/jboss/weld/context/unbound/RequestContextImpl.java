package org.jboss.weld.context.unbound;

import java.lang.annotation.Annotation;

import javax.enterprise.context.RequestScoped;

import org.jboss.weld.context.AbstractUnboundContext;
import org.jboss.weld.context.RequestContext;
import org.jboss.weld.context.beanstore.HashMapBeanStore;

public class RequestContextImpl extends AbstractUnboundContext implements RequestContext
{

   public RequestContextImpl()
   {
      super(false);
   }

   public Class<? extends Annotation> getScope()
   {
      return RequestScoped.class;
   }

   public void activate()
   {
      // Attach bean store (this context is unbound, so this can simply be thread-scoped
      setBeanStore(new HashMapBeanStore());
      super.activate();
   }
   
   @Override
   public void deactivate()
   {
      super.deactivate();
      // Dettach the bean store
      setBeanStore(null);
   }

}
