package org.jboss.webbeans.model;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Set;

import javax.webbeans.Current;
import javax.webbeans.Dependent;
import javax.webbeans.ScopeType;

import org.jboss.webbeans.ContainerImpl;
import org.jboss.webbeans.injectable.ComponentConstructor;
import org.jboss.webbeans.injectable.JMSConstructor;
import org.jboss.webbeans.introspector.AnnotatedItem;
import org.jboss.webbeans.introspector.SimpleAnnotatedItem;
import org.jboss.webbeans.util.Reflections;

// TODO Work out what to return on methods we don't support

public class JmsComponentModel<T> extends AbstractComponentModel<T>
{

   // TODO Work out how to handle xml elements which don't correspond to annotations
   
   private String jndiName;
   private ComponentConstructor<T> constructor;
   private Set<Annotation> bindingTypes;
   private Annotation scopeType;
   
   @SuppressWarnings("unchecked")
   public JmsComponentModel(AnnotatedItem xmlAnnotatedItem, ContainerImpl container)
   {
      AnnotatedItem annotatedItem = new SimpleAnnotatedItem(new HashMap<Class<? extends Annotation>, Annotation>());
      bindingTypes = initBindingTypes(annotatedItem, xmlAnnotatedItem);
      //scopeType = new DependentBiding();
      checkBindingTypesAllowed(bindingTypes, null);
      checkScopeAllowed(xmlAnnotatedItem, null);
      // TODO Initialize queue. topic
      this.constructor = new JMSConstructor<T>(jndiName);
   }

   @SuppressWarnings("unchecked")
   protected static void checkBindingTypesAllowed(Set<Annotation> bindingTypes,
         String type)
   {
      if (bindingTypes.size() == 0)
      {
         throw new RuntimeException("Must declare at least one binding type for JMS Component " + type);
      }
      if (Reflections.annotationSetMatches(bindingTypes, Current.class))
      {
         throw new RuntimeException("Cannot declared the binding type @Current for JMS Component " + type);
      }
      
   }

   protected static <T> Class<? extends T> initType(AnnotatedItem xmlAnnotatedItem)
   {
      // TODO return TopicPublisher if its a topic or QueueSession if its a queue
      return null;
   }
   
   protected static void checkScopeAllowed(AnnotatedItem xmlAnnotatedItem, Class<?> type)
   {
      Set<Annotation> scopeTypeAnnotations = xmlAnnotatedItem.getAnnotations(ScopeType.class);
      if (scopeTypeAnnotations.size() > 0 && ! scopeTypeAnnotations.iterator().next().annotationType().equals(Dependent.class))
      {
         throw new RuntimeException("JMS component may only have scope @Dependent for " + type);
      }
   }
   
   @Override
   public ComponentConstructor<T> getConstructor()
   {
      return constructor;
   }
   
   @Override
   public Set<Annotation> getBindingTypes()
   {
      return bindingTypes;
   }
   
   @Override
   protected Class<? extends T> getType()
   {
      return null;
   }
   
   public String getJndiName()
   {
      return jndiName;
   }
   
   @Override
   public Annotation getScopeType()
   {
      return scopeType;
   }

   @Override
   public Annotation getDeploymentType()
   {
      return null;
   }

   @Override
   public String getName()
   {
      return null;
   }
   
}
