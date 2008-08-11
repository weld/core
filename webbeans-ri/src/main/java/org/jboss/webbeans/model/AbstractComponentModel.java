package org.jboss.webbeans.model;

import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.logging.Logger;

import javax.webbeans.BindingType;

import org.jboss.webbeans.bindings.CurrentBinding;
import org.jboss.webbeans.injectable.ComponentConstructor;
import org.jboss.webbeans.introspector.AnnotatedItem;
import org.jboss.webbeans.util.LoggerUtil;

public abstract class AbstractComponentModel<T>
{
   
   public static final String LOGGER_NAME = "componentModel";
   
   private static Logger log = LoggerUtil.getLogger(LOGGER_NAME);

   protected static Set<Annotation> initBindingTypes(AnnotatedItem annotatedItem, AnnotatedItem xmlAnnotatedItem)
   {
      Set<Annotation> xmlBindingTypes = xmlAnnotatedItem.getAnnotations(BindingType.class);
      if (xmlBindingTypes.size() > 0)
      {
         // TODO support producer expression default binding type
         log.finest("Using binding types " + xmlBindingTypes + " specified in XML");
         return xmlBindingTypes;
      }
      
      Set<Annotation> bindingTypes = annotatedItem.getAnnotations(BindingType.class);
      
      if (bindingTypes.size() == 0)
      {
         log.finest("Adding default @Current binding type");
         bindingTypes.add(new CurrentBinding());
      }
      else
      {
         log.finest("Using binding types " + bindingTypes + " specified by annotations");
      }
      return bindingTypes;
   }

   public abstract Set<Annotation> getBindingTypes();

   public abstract Annotation getScopeType();

   protected abstract Class<? extends T> getType();

   public abstract ComponentConstructor<T> getConstructor();

   public abstract Annotation getDeploymentType();

   public abstract String getName();

}