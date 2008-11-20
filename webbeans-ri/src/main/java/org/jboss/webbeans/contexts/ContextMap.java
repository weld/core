package org.jboss.webbeans.contexts;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.webbeans.manager.Context;

import com.google.common.collect.ForwardingMap;

public class ContextMap extends
      ForwardingMap<Class<? extends Annotation>, List<Context>>
{

   private Map<Class<? extends Annotation>, List<Context>> delegate;

   public ContextMap()
   {
      delegate = new HashMap<Class<? extends Annotation>, List<Context>>();
   }

   public List<Context> get(Class<? extends Annotation> key)
   {
      return (List<Context>) super.get(key);
   }

   public DependentContext getBuiltInContext(Class<? extends Annotation> scopeType)
   {
      return (DependentContext) get(scopeType).iterator().next();
   }

   @Override
   protected Map<Class<? extends Annotation>, List<Context>> delegate()
   {
      return delegate;
   }
}