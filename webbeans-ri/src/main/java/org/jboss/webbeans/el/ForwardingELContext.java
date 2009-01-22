package org.jboss.webbeans.el;

import java.util.Locale;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.FunctionMapper;
import javax.el.VariableMapper;

public abstract class ForwardingELContext extends ELContext
{
   
   protected abstract ELContext delgate();
   
   @Override
   public ELResolver getELResolver()
   {
      return delgate().getELResolver();
   }
   
   @Override
   public FunctionMapper getFunctionMapper()
   {
      return delgate().getFunctionMapper();
   }
   
   @Override
   public VariableMapper getVariableMapper()
   {
      return delgate().getVariableMapper();
   }
   
   @Override
   public boolean equals(Object obj)
   {
      return delgate().equals(obj);
   }
   
   @SuppressWarnings("unchecked")
   @Override
   public Object getContext(Class key)
   {
      return delgate().getContext(key);
   }
   
   @Override
   public Locale getLocale()
   {
      return delgate().getLocale();
   }
   
   @Override
   public int hashCode()
   {
      return delgate().hashCode();
   }
   
   @Override
   public boolean isPropertyResolved()
   {
      return delgate().isPropertyResolved();
   }
   
   @SuppressWarnings("unchecked")
   @Override
   public void putContext(Class key, Object contextObject)
   {
      delgate().putContext(key, contextObject);
   }
   
   @Override
   public void setLocale(Locale locale)
   {
      delgate().setLocale(locale);
   }
   
   @Override
   public void setPropertyResolved(boolean resolved)
   {
      delgate().setPropertyResolved(resolved);
   }
   
   @Override
   public String toString()
   {
      return delgate().toString();
   }
}
