package org.jboss.weld.context.bound;

import java.lang.annotation.Annotation;
import java.util.Map;

import javax.enterprise.context.SessionScoped;

import org.jboss.weld.context.AbstractBoundContext;
import org.jboss.weld.context.beanstore.MapBeanStore;
import org.jboss.weld.context.beanstore.NamingScheme;
import org.jboss.weld.context.beanstore.SimpleNamingScheme;

public class BoundSessionContextImpl extends AbstractBoundContext<Map<String, Object>> implements BoundSessionContext
{

   private static final String IDENTIFIER = BoundSessionContextImpl.class.getName();

   private final NamingScheme namingScheme;

   public BoundSessionContextImpl()
   {
      super(false);
      this.namingScheme = new SimpleNamingScheme(BoundSessionContext.class.getName());
   }

   public Class<? extends Annotation> getScope()
   {
      return SessionScoped.class;
   }

   public boolean associate(Map<String, Object> storage)
   {
      if (getBeanStore() == null)
      {
         setBeanStore(new MapBeanStore(namingScheme, storage));
         storage.put(IDENTIFIER, IDENTIFIER);
         return true;
      }
      else
      {
         return false;
      }
   }

   public boolean dissociate(Map<String, Object> storage)
   {
      if (storage.containsKey(IDENTIFIER))
      {
         storage.remove(IDENTIFIER);
         setBeanStore(null);
         return true;
      }
      else
      {
         return false;
      }
   }
   
   @Override
   public void invalidate()
   {
      super.invalidate();
   }

}
