package org.jboss.webbeans;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import javax.webbeans.Stereotype;

import org.jboss.webbeans.util.AnnotatedItem;

/**
 * Meta model for the merged stereotype for a component
 * @author pmuir
 *
 */
public class MergedComponentStereotypes
{

   private Set<Annotation> possibleDeploymentTypes;
   private Set<Annotation> possibleScopeTypes;
   private boolean componentNameDefaulted;
   private Set<Class<?>> requiredTypes;
   private Set<Class<? extends Annotation>> supportedScopes;
   
   public MergedComponentStereotypes(AnnotatedItem annotatedItem, AnnotatedItem xmlAnnotatedItem, ContainerImpl container)
   {
      possibleDeploymentTypes = new HashSet<Annotation>();
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
         StereotypeMetaModel stereotype = container.getStereotypeManager().getStereotype(stereotypeAnnotation.annotationType());
         if (stereotype.getDefaultDeploymentType() != null)
         {
            possibleDeploymentTypes.add(stereotype.getDefaultDeploymentType());
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
      
      if (this.possibleScopeTypes.size() > 1)
      {
         throw new RuntimeException("All stereotypes must specify the same scope OR a scope must be specified on the component");
      }
   }
   
   public Set<Annotation> getPossibleDeploymentTypes()
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
