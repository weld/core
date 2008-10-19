package org.jboss.webbeans.model;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.webbeans.Stereotype;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.introspector.AnnotatedItem;

/**
 * Meta model for the merged stereotype for a component
 * @author pmuir
 *
 */
public class MergedStereotypesModel<T, E>
{

   private Map<Class<? extends Annotation>, Annotation> possibleDeploymentTypes;
   private Set<Annotation> possibleScopeTypes;
   private boolean componentNameDefaulted;
   private Set<Class<?>> requiredTypes;
   private Set<Class<? extends Annotation>> supportedScopes;
   
   public MergedStereotypesModel(AnnotatedItem<T, E> annotatedItem, AnnotatedItem<T, E> xmlAnnotatedItem, ManagerImpl container)
   {
      possibleDeploymentTypes = new HashMap<Class<? extends Annotation>, Annotation>();
      possibleScopeTypes = new HashSet<Annotation>();
      requiredTypes = new HashSet<Class<?>>();
      supportedScopes = new HashSet<Class<? extends Annotation>>();
      
      // All stereotypes declared in java and xml are merged
      Set<Annotation> stereotypeAnnotations = new HashSet<Annotation>();
      stereotypeAnnotations.addAll(annotatedItem.getAnnotations(Stereotype.class));
      stereotypeAnnotations.addAll(xmlAnnotatedItem.getAnnotations(Stereotype.class));
      
      for (Annotation stereotypeAnnotation : stereotypeAnnotations)
      {
         // Retrieve and merge all metadata from stereotypes
         StereotypeModel<?> stereotype = container.getModelManager().getStereotype(stereotypeAnnotation.annotationType());
         if (stereotype == null)
         {
            throw new NullPointerException("Stereotype " + stereotypeAnnotation + " not registered with container");
         }
         if (stereotype.getDefaultDeploymentType() != null)
         {
            possibleDeploymentTypes.put(stereotype.getDefaultDeploymentType().annotationType(), stereotype.getDefaultDeploymentType());
         }
         if (stereotype.getDefaultScopeType() != null)
         {
            possibleScopeTypes.add(stereotype.getDefaultScopeType());
         }
         requiredTypes.addAll(stereotype.getRequiredTypes());
         supportedScopes.addAll(stereotype.getSupportedScopes());
         if (stereotype.isComponentNameDefaulted()) 
         {
            componentNameDefaulted = true;
         }
      }
   }
   
   public Map<Class<? extends Annotation>, Annotation> getPossibleDeploymentTypes()
   {
      return possibleDeploymentTypes;
   }
   
   public Set<Annotation> getPossibleScopeTypes()
   {
      return possibleScopeTypes;
   }
   
   public boolean isComponentNameDefaulted()
   {
      return componentNameDefaulted;
   }
   
   public Set<Class<?>> getRequiredTypes()
   {
      return requiredTypes;
   }
   
   public Set<Class<? extends Annotation>> getSupportedScopes()
   {
      return supportedScopes;
   }
   
}
