package org.jboss.webbeans.bean;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.model.StereotypeModel;

/**
 * Meta model for the merged stereotype for a bean
 * @author pmuir
 *
 */
public class MergedStereotypes<T, E>
{

   private Map<Class<? extends Annotation>, Annotation> possibleDeploymentTypes;
   private Set<Annotation> possibleScopeTypes;
   private boolean beanNameDefaulted;
   private Set<Class<?>> requiredTypes;
   private Set<Class<? extends Annotation>> supportedScopes;
   
   public MergedStereotypes(Set<Annotation> stereotypeAnnotations, ManagerImpl manager)
   {
      possibleDeploymentTypes = new HashMap<Class<? extends Annotation>, Annotation>();
      possibleScopeTypes = new HashSet<Annotation>();
      requiredTypes = new HashSet<Class<?>>();
      supportedScopes = new HashSet<Class<? extends Annotation>>();
      merge(stereotypeAnnotations, manager);
   }
   
   protected void merge(Set<Annotation> stereotypeAnnotations, ManagerImpl manager)
   {
      for (Annotation stereotypeAnnotation : stereotypeAnnotations)
      {
         // Retrieve and merge all metadata from stereotypes
         StereotypeModel<?> stereotype = manager.getModelManager().getStereotype(stereotypeAnnotation.annotationType());
         if (stereotype == null)
         {
            throw new IllegalStateException("Stereotype " + stereotypeAnnotation + " not registered with container");
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
      return false;
   }
   
}
