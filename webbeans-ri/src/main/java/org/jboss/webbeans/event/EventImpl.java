package org.jboss.webbeans.event;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.webbeans.BindingType;
import javax.webbeans.Current;
import javax.webbeans.DuplicateBindingTypeException;
import javax.webbeans.Event;
import javax.webbeans.Observer;
import javax.webbeans.manager.Manager;

/**
 * Implementation of the {@link Event} interface used for the container provided
 * Web Bean to be injected for an observable event. See section 7.4 of the JSR
 * for more details on how this bean is provided by the container and used.
 * 
 * @author David Allen
 * 
 */
public class EventImpl<T> implements Event<T>
{
   private Collection<? extends Annotation> eventBindings;
   private Class<T> eventType;

   // The current WB manager
   @Current
   protected Manager webBeansManager;

   /**
    * Used to set the event bindings for this type of event after it is
    * constructed with the default constructor.
    * 
    * @param eventBindings
    *           Annotations that are bindings for the event
    */
   public void setEventBindings(Annotation... eventBindings)
   {
      // TODO Use constructor injection
      Set<Annotation> newEventBindings = new HashSet<Annotation>();
      addAnnotationBindings(newEventBindings, eventBindings);
      this.eventBindings = newEventBindings;
   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.webbeans.Event#fire(java.lang.Object,
    * java.lang.annotation.Annotation[])
    */
   public void fire(T event, Annotation... bindings)
   {
      // Combine the annotations passed here with the annotations (event
      // bindings)
      // specified on the @Observable object.
      Set<Annotation> eventBindings = new HashSet<Annotation>();
      eventBindings.addAll(this.getBindingTypes());
      // eventBindings.addAll(Arrays.asList(bindings));
      addAnnotationBindings(eventBindings, bindings);

      // Invoke the container method to fire the event per 7.2
      webBeansManager
            .fireEvent(event, eventBindings.toArray(new Annotation[0]));
   }

   public void observe(Observer<T> observer, Annotation... bindings)
   {
      // Register the observer with the web beans manager
      Class<T> eventArgumentClazz = null;
      try
      {
         // TODO Fix me:  Reflection can only get type variables, not the arguments used elsewhere
         Field eventTypeField = this.getClass().getDeclaredField("eventType");
         ParameterizedType genericType = (ParameterizedType) eventTypeField.getGenericType();
         Type[] eventTypeArguments = genericType.getActualTypeArguments();
         eventArgumentClazz = (Class<T>) eventTypeArguments[0];
      } catch (SecurityException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (NoSuchFieldException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      
      Set<Annotation> eventBindings = new HashSet<Annotation>();
      eventBindings.addAll(this.getBindingTypes());
      addAnnotationBindings(eventBindings, bindings);
      webBeansManager.addObserver(observer, eventArgumentClazz, bindings);
   }

   /**
    * Adds each of the annotation bindings to the set, but if any binding
    * already exists in the set, a {@link DuplicateBindingTypeException} is
    * thrown.
    * 
    * @param bindingsSet
    *           The set of annotation binding objects
    * @param bindings
    *           An array of annotation bindings to add to the set
    * @throws DuplicateBindingTypeException
    *            if any of bindings are duplicates
    * @throws IllegalArgumentException
    *            if any annotation is not a binding type
    */
   private void addAnnotationBindings(Set<Annotation> bindingsSet,
         Annotation[] bindings)
   {
      if (bindings != null)
      {
         Set<Class<? extends Annotation>> bindingTypes = new HashSet<Class<? extends Annotation>>();
         // Add the bindings types that are already in the set being added to.  This will
         // provide detection of duplicates across construction and later invocations.
         for (Annotation annotation : bindingsSet)
         {
            bindingTypes.add(annotation.annotationType());
         }
         
         // Now go through the new annotations being added to make sure these are binding
         // types and are not duplicates
         for (Annotation annotation : bindings)
         {
            // Check that the binding type is indeed a binding type
            Annotation[] bindingAnnotations = annotation.annotationType()
                  .getAnnotations();
            boolean isBindingType = false;
            for (Annotation bindingAnnotation : bindingAnnotations)
            {
               if (bindingAnnotation.annotationType().equals(BindingType.class))
               {
                  isBindingType = true;
               }
            }
            if (!isBindingType)
               throw new IllegalArgumentException("Annotation " + annotation
                     + " is not a binding type");

            // Check that no binding type was specified more than once in the
            // annotations
            if (bindingTypes.contains(annotation.annotationType()))
            {
               throw new DuplicateBindingTypeException();
            } else
            {
               bindingTypes.add(annotation.annotationType());
            }
         }
         bindingsSet.addAll(Arrays.asList(bindings));
      }

   }

   private Collection<? extends Annotation> getBindingTypes()
   {
      // Get the binding types directly from the model for the bean
      return this.eventBindings;
   }

   // TODO Remove the setter for the manager once WB injection is working
   public void setManager(Manager manager)
   {
      this.webBeansManager = manager;
   }

}
