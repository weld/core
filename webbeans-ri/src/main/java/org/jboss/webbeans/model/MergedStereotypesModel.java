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
 * Meta model for the merged stereotype for a bean
 * @author pmuir
 *
 */
public class MergedStereotypesModel<T, E>
{

   private Map<Class<? extends Annotation>, Annotation> possibleDeploymentTypes;
   private Set<Annotation> possibleScopeTypes;
   private boolean beanNameDefaulted;
   private Set<Class<?>> requiredTypes;
   private Set<Class<? extends Annotation>> supportedScopes;
   private boolean isDeclaredInXml;
   
   public MergedStereotypesModel(AnnotatedItem<T, E> annotatedItem, AnnotatedItem<T, E> xmlAnnotatedItem, ManagerImpl manager)
   {
      possibleDeploymentTypes = new HashMap<Class<? extends Annotation>, Annotation>();
      possibleScopeTypes = new HashSet<Annotation>();
      requiredTypes = new HashSet<Class<?>>();
      supportedScopes = new HashSet<Class<? extends Annotation>>();
      
      if (xmlAnnotatedItem.getAnnotations(Stereotype.class).size() > 0)
      {
         merge(xmlAnnotatedItem.getAnnotations(Stereotype.class), manager);
         isDeclaredInXml = true;
      }
      else
      {
         merge(annotatedItem.getAnnotations(Stereotype.class), manager);
      }
      
   }
   
   private void merge(Set<Annotation> stereotypeAnnotations, ManagerImpl manager)
   {
      for (Annotation stereotypeAnnotation : stereotypeAnnotations)
      {
         // Retrieve and merge all metadata from stereotypes
         StereotypeModel<?> stereotype = manager.getModelManager().getStereotype(stereotypeAnnotation.annotationType());
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
         if (stereotype.isBeanNameDefaulted()) 
         {
            beanNameDefaulted = true;
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
   
   public boolean isBeanNameDefaulted()
   {
      return beanNameDefaulted;
   }
   
   public Set<Class<?>> getRequiredTypes()
   {
      return requiredTypes;
   }
   
   public Set<Class<? extends Annotation>> getSupportedScopes()
   {
      return supportedScopes;
   }
   
   public boolean isDeclaredInXml()
   {
      return isDeclaredInXml;
   }
   
}
