package org.jboss.webbeans;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.Set;

import javax.webbeans.BindingType;
import javax.webbeans.ComponentInstance;
import javax.webbeans.Container;
import javax.webbeans.Current;
import javax.webbeans.DeploymentType;
import javax.webbeans.Named;
import javax.webbeans.ScopeType;
import javax.webbeans.Stereotype;

import org.jboss.webbeans.util.EnhancedAnnotatedElement;

/**
 * Web Beans Component meta model
 * 
 * @author Pete Muir
 * 
 */
public class ComponentInstanceImpl<T> extends ComponentInstance<T>
{
   
   private Set<Annotation> bindingTypes;
   private Annotation componentType;
   private String name;
   private Annotation scopeType;
   private Set<Annotation> stereotypes;
   
   public ComponentInstanceImpl(EnhancedAnnotatedElement annotatedElement)
   {
      initSterotypes(annotatedElement);
      initBindingTypes(annotatedElement);
      initComponentType(annotatedElement);
      initScopeType(annotatedElement);
      initName(annotatedElement);
   }
   
   private void initSterotypes(EnhancedAnnotatedElement annotatedElement)
   {
      this.stereotypes = annotatedElement.getAnnotations(Stereotype.class);
   }

   private void initScopeType(EnhancedAnnotatedElement annotatedElement)
   {
      Set<Annotation> scopes = annotatedElement.getAnnotations(ScopeType.class);
      if (scopes.size() > 1)
      {
         throw new RuntimeException("At most one scope may be specified");
      }
      else if (scopes.size() == 1)
      {
         this.scopeType = scopes.iterator().next();
      }
      else
      {
         // TODO Look at sterotypes
      }
   }

   private void initComponentType(EnhancedAnnotatedElement annotatedElement)
   {
      Set<Annotation> deploymentTypes = annotatedElement.getAnnotations(DeploymentType.class);
      if (deploymentTypes.size() > 1)
      {
         throw new RuntimeException("At most one deployment type may be specified");
      }
      else if (deploymentTypes.size() == 1)
      {
         this.componentType = deploymentTypes.iterator().next();
      }
      else
      {
         // TODO Look at sterotypes
      }
   }

   private void initBindingTypes(EnhancedAnnotatedElement annotatedElement)
   {
      bindingTypes = annotatedElement.getAnnotations(BindingType.class);
      
      // Add the default binding if needed
      if (bindingTypes.size() == 0)
      {
         bindingTypes.add(new CurrentBinding());
      }
   }

   private void initName(EnhancedAnnotatedElement annotatedElement)
   {
      if (annotatedElement.isAnnotationPresent(Named.class))
      {
         String name = annotatedElement.getAnnotation(Named.class).value();
         if ("".equals(name))
         {
            // TODO write default name algorithm
            
         }
         this.name = name;
      }
   }

   @Override
   public T create(Container container)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public void destroy(Container container, Object instance)
   {
      // TODO Auto-generated method stub
      
   }

   @Override
   public Set<Annotation> getBindingTypes()
   {
      return bindingTypes;
   }

   @Override
   public Annotation getComponentType()
   {
      return componentType;
   }

   @Override
   public String getName()
   {
      return name;
   }

   @Override
   public Set<Class> getTypes()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public Annotation getScopeType()
   {
      return scopeType;
   }

}
