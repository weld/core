package org.jboss.webbeans;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import javax.webbeans.DuplicateBindingTypeException;
import javax.webbeans.Observable;

import org.jboss.webbeans.util.Reflections;

public class FacadeImpl<T> {

   protected final Set<? extends Annotation> bindingTypes;
   protected final ManagerImpl manager;
   protected final Class<T> type;

   /**
    * Validates the binding types
    * 
    * Removes @Observable from the list
    * 
    * @param annotations The annotations to validate
    * @return A set of binding type annotations (minus @Observable, if it was
    *         present)
    */
   protected static Set<Annotation> getBindingTypes(Annotation... annotations) {
      Set<Annotation> result = new HashSet<Annotation>();
      for (Annotation annotation : annotations)
      {
         if (!Reflections.isBindingType(annotation))
         {
            throw new IllegalArgumentException(annotation + " is not a binding type");
         }
         if (!annotation.annotationType().equals(Observable.class))
         {
            result.add(annotation);
         }
      }
      return result;
   }

   protected FacadeImpl(ManagerImpl manager, Class<T> eventType, Annotation... bindingTypes) {
      this.manager = manager;
      this.type = eventType;
      this.bindingTypes = getBindingTypes(bindingTypes);
    }

   /**
    * Validates the binding types and checks for duplicates among the annotations.
    * 
    * @param annotations The annotations to validate
    * @return A set of unique binding type annotations
    */
   protected Set<Annotation> checkBindingTypes(Annotation... annotations) {
      Set<Annotation> result = new HashSet<Annotation>();
      for (Annotation annotation : annotations)
      {
         if (!Reflections.isBindingType(annotation))
         {
            throw new IllegalArgumentException(annotation + " is not a binding type for " + this);
         }
         if (result.contains(annotation) || this.bindingTypes.contains(annotation))
         {
            throw new DuplicateBindingTypeException(annotation + " is already present in the bindings list for " + this);
         }
         result.add(annotation);
      }
      return result;
   }

   protected Annotation[] mergeBindings(Annotation... bindingTypes) {
      Set<Annotation> bindingParameters = checkBindingTypes(bindingTypes);
      bindingParameters.addAll(this.bindingTypes);
      Annotation[] bindings = bindingParameters.toArray(new Annotation[0]);
      return bindings;
   }

}