package org.jboss.webbeans.el;

import java.util.Iterator;

import javax.el.ELContext;
import javax.el.ELResolver;

public abstract class ForwardingELResolver extends ELResolver
{
   
   protected abstract ELResolver delegate();

   @Override
   public Class<?> getCommonPropertyType(ELContext context, Object base)
   {
      return delegate().getCommonPropertyType(context, base);
   }

   @Override
   public Iterator<?> getFeatureDescriptors(ELContext context, Object base)
   {
      return delegate().getFeatureDescriptors(context, base);
   }

   @Override
   public Class<?> getType(ELContext context, Object base, Object property)
   {
      return delegate().getType(context, base, property);
   }

   @Override
   public Object getValue(ELContext context, Object base, Object property)
   {
      return delegate().getValue(context, base, property);
   }

   @Override
   public boolean isReadOnly(ELContext context, Object base, Object property)
   {
      return delegate().isReadOnly(context, base, property);
   }

   @Override
   public void setValue(ELContext context, Object base, Object property, Object value)
   {
      delegate().setValue(context, base, property, value);
   }

}
