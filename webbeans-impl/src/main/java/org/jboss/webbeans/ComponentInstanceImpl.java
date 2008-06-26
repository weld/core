package org.jboss.webbeans;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.webbeans.BindingType;
import javax.webbeans.ComponentInstance;
import javax.webbeans.Container;
import javax.webbeans.DeploymentType;
import javax.webbeans.Named;
import javax.webbeans.ScopeType;
import javax.webbeans.Stereotype;

import org.jboss.webbeans.util.AnnotatedWebBean;

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
   private Set<Annotation> possibleDeploymentTypes;
   private Set<Annotation> possibleScopeTypes;
   private boolean componentNameDefaulted;
   private Set<Class<?>> requiredTypes;
   private Set<Class<? extends Annotation>> supportedScopes;
   
   public ComponentInstanceImpl(AnnotatedWebBean annotatedElement, ContainerImpl container)
   {
      initStereotypes(annotatedElement, container);
      initBindingTypes(annotatedElement);
      initComponentType(annotatedElement, container);
      initScopeType(annotatedElement);
      initName(annotatedElement);
      checkRequiredTypesImplemented(annotatedElement);
      checkScopeAllowed(annotatedElement);
      // TODO Interceptors
   }
   
   private void initStereotypes(AnnotatedWebBean annotatedElement, ContainerImpl container)
   {
      possibleDeploymentTypes = new HashSet<Annotation>();
      possibleScopeTypes = new HashSet<Annotation>();
      requiredTypes = new HashSet<Class<?>>();
      supportedScopes = new HashSet<Class<? extends Annotation>>();
      for (Annotation stereotypeAnnotation : annotatedElement.getAnnotations(Stereotype.class))
      {
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
   }
   
   private void checkScopeAllowed(AnnotatedWebBean annotatedClass)
   {
      if (supportedScopes.size() > 0)
      {
         if (!supportedScopes.contains(scopeType))
         {
            throw new RuntimeException("Scope " + scopeType + " is not an allowed by the component's stereotype");
         }
      }
   }
   
   private void checkRequiredTypesImplemented(AnnotatedWebBean annotatedClass)
   {
      if (requiredTypes.size() > 0)
      {
         // TODO This needs to check a lot more. Or we do through checking assignability
         List<Class> classes = Arrays.asList(annotatedClass.getAnnotatedClass().getInterfaces());
         if (!classes.containsAll(requiredTypes))
         {
            // TODO Ugh, improve this exception
            throw new RuntimeException("Not all required types are implemented");
         }
      }
   }

   private void initScopeType(AnnotatedWebBean annotatedElement)
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
      else if (possibleScopeTypes.size() == 1)
      {
         this.scopeType = possibleScopeTypes.iterator().next();
      }
      else if (possibleScopeTypes.size() > 0)
      {
         //TODO DO something
      }
      else
      {
         this.scopeType = new DependentBinding();
      }
   }

   private void initComponentType(AnnotatedWebBean annotatedElement, ContainerImpl container)
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
         this.componentType = getDeploymentType(container.getEnabledDeploymentTypes(), possibleDeploymentTypes);
      }
   }

   private void initBindingTypes(AnnotatedWebBean annotatedElement)
   {
      bindingTypes = annotatedElement.getAnnotations(BindingType.class);
      
      // Add the default binding if needed
      if (bindingTypes.size() == 0)
      {
         bindingTypes.add(new CurrentBinding());
      }
   }

   private void initName(AnnotatedWebBean annotatedElement)
   {
      if (annotatedElement.isAnnotationPresent(Named.class))
      {
         String name = annotatedElement.getAnnotation(Named.class).value();
         if ("".equals(name))
         {
            componentNameDefaulted = true;
         }
         else
         {
            componentNameDefaulted = false;
            this.name = name;
         }
      }
      if (componentNameDefaulted)
      {
         // TODO Write default name alogorithm
      }
   }
   
   public static Annotation getDeploymentType(List<Annotation> enabledDeploymentTypes, Set<Annotation> possibleDeploymentTypes)
   {
      List<Annotation> l = new ArrayList<Annotation>(enabledDeploymentTypes);
      l.retainAll(possibleDeploymentTypes);
      if (l.size() > 0)
      {
         return l.get(0);
      }
      else
      {
         return new ProductionBinding();
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
