package org.jboss.webbeans.model;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.webbeans.BindingType;
import javax.webbeans.DeploymentType;
import javax.webbeans.Named;
import javax.webbeans.ScopeType;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.bindings.CurrentAnnotationLiteral;
import org.jboss.webbeans.bindings.DependentAnnotationLiteral;
import org.jboss.webbeans.bindings.ProductionAnnotationLiteral;
import org.jboss.webbeans.injectable.ComponentConstructor;
import org.jboss.webbeans.injectable.InjectableMethod;
import org.jboss.webbeans.introspector.AnnotatedItem;
import org.jboss.webbeans.util.LoggerUtil;

public abstract class AbstractComponentModel<T, E>
{
   
   public static final String LOGGER_NAME = "componentModel";
   
   private static Logger log = LoggerUtil.getLogger(LOGGER_NAME);
   
   private Set<Annotation> bindingTypes;
   protected String name;
   protected Annotation scopeType;
   private MergedStereotypesModel<T, E> mergedStereotypes;
   protected Annotation deploymentType;
   protected Class<T> type;
   protected InjectableMethod<?> removeMethod;
   private Set<Class> apiTypes;
   
   protected void init(ManagerImpl container)
   {
      mergedStereotypes = new MergedStereotypesModel<T, E>(getAnnotatedItem(), getXmlAnnotatedItem(), container);
      initType();
      log.fine("Building Web Bean component metadata for " +  getType());
      initBindingTypes();
      initName();
      initDeploymentType(container);
      checkDeploymentType();
      initScopeType();
      initApiTypes();
   }
   
   protected abstract void initType();
   
   protected void initApiTypes()
   {
      apiTypes = getTypeHierachy(getType());
   }
   
   protected Set<Class> getTypeHierachy(Class clazz)
   {
      Set<Class> classes = new HashSet<Class>();
      if (!(clazz == null || clazz == Object.class))
      {
         classes.add(clazz);
         classes.addAll(getTypeHierachy(clazz.getSuperclass()));
         for (Class<?> c : clazz.getInterfaces())
         {
            classes.addAll(getTypeHierachy(c));
         }
      }
      return classes;
   }

   protected abstract AnnotatedItem<E> getAnnotatedItem();
   
   protected abstract AnnotatedItem<E> getXmlAnnotatedItem();

   protected void initBindingTypes()
   {
      Set<Annotation> xmlBindingTypes = getXmlAnnotatedItem().getAnnotations(BindingType.class);
      if (xmlBindingTypes.size() > 0)
      {
         // TODO support producer expression default binding type
         log.finest("Using binding types " + xmlBindingTypes + " specified in XML");
         this.bindingTypes= xmlBindingTypes;
         return;
      }
      else
      {
         Set<Annotation> bindingTypes = getAnnotatedItem().getAnnotations(BindingType.class);
         
         if (bindingTypes.size() == 0)
         {
            log.finest("Adding default @Current binding type");
            bindingTypes.add(new CurrentAnnotationLiteral());
         }
         else
         {
            log.finest("Using binding types " + bindingTypes + " specified by annotations");
         }
         this.bindingTypes = bindingTypes;
         return;
      }
   }
   
   /**
    * Return the scope of the component
    */
   protected void initScopeType()
   {
      Set<Annotation> xmlScopes = getXmlAnnotatedItem().getAnnotations(ScopeType.class);
      if (xmlScopes.size() > 1)
      {
         throw new RuntimeException("At most one scope may be specified in XML");
      }
      
      if (xmlScopes.size() == 1)
      {
         this.scopeType = xmlScopes.iterator().next();
         log.finest("Scope " + scopeType + " specified in XML");
         return;
      }
      
      Set<Annotation> scopes = getAnnotatedItem().getAnnotations(ScopeType.class);
      if (scopes.size() > 1)
      {
         throw new RuntimeException("At most one scope may be specified");
      }
      
      if (scopes.size() == 1)
      {
         this.scopeType = scopes.iterator().next();
         log.finest("Scope " + scopeType + " specified b annotation");
         return;
      }
      
      if (getMergedStereotypes().getPossibleScopeTypes().size() == 1)
      {
         this.scopeType = getMergedStereotypes().getPossibleScopeTypes().iterator().next();
         log.finest("Scope " + scopeType + " specified by stereotype");
         return;
      }
      else if (getMergedStereotypes().getPossibleScopeTypes().size() > 1)
      {
         throw new RuntimeException("All stereotypes must specify the same scope OR a scope must be specified on the component");
      }
      this.scopeType = new DependentAnnotationLiteral();
      log.finest("Using default @Dependent scope");
   }
   
   protected void initName()
   {
      boolean componentNameDefaulted = false;
      if (getXmlAnnotatedItem().isAnnotationPresent(Named.class))
      {
         String xmlName = getXmlAnnotatedItem().getAnnotation(Named.class).value();
         if ("".equals(xmlName))
         {
            log.finest("Using default name (specified in XML)");
            componentNameDefaulted = true;
         }
         else
         {
            log.finest("Using name " + xmlName + " specified in XML");
            this.name = xmlName;
            return;
         }
      }
      else if (getAnnotatedItem().isAnnotationPresent(Named.class))
      {
         String javaName = getAnnotatedItem().getAnnotation(Named.class).value();
         if ("".equals(javaName))
         {
            log.finest("Using default name (specified by annotations)");
            componentNameDefaulted = true;
         }
         else
         {
            log.finest("Using name " + javaName + " specified in XML");
            this.name = javaName;
            return;
         }
      }
      if (componentNameDefaulted || getMergedStereotypes().isComponentNameDefaulted())
      {
         this.name = getDefaultName();
         return;
      }
   }
   
   protected void initDeploymentType(ManagerImpl container)
   {
      Set<Annotation> xmlDeploymentTypes = getXmlAnnotatedItem().getAnnotations(DeploymentType.class);
      
      if (xmlDeploymentTypes.size() > 1)
      {
         throw new RuntimeException("At most one deployment type may be specified (" + xmlDeploymentTypes + " are specified)");
      }
      
      if (xmlDeploymentTypes.size() == 1)
      {
         this.deploymentType = xmlDeploymentTypes.iterator().next(); 
         log.finest("Deployment type " + deploymentType + " specified in XML");
         return;
      }
      
      if (getXmlAnnotatedItem().getDelegate() == null)
      {
      
         Set<Annotation> deploymentTypes = getAnnotatedItem().getAnnotations(DeploymentType.class);
         
         if (deploymentTypes.size() > 1)
         {
            throw new RuntimeException("At most one deployment type may be specified (" + deploymentTypes + " are specified) on " + getAnnotatedItem().getDelegate());
         }
         if (deploymentTypes.size() == 1)
         {
            this.deploymentType = deploymentTypes.iterator().next();
            log.finest("Deployment type " + deploymentType + " specified by annotation");
            return;
         }
      }
      
      if (getMergedStereotypes().getPossibleDeploymentTypes().size() > 0)
      {
         this.deploymentType = getDeploymentType(container.getEnabledDeploymentTypes(), getMergedStereotypes().getPossibleDeploymentTypes());
         log.finest("Deployment type " + deploymentType + " specified by stereotype");
         return;
      }
      
      if (getXmlAnnotatedItem().getDelegate() != null)
      {
         this.deploymentType = new ProductionAnnotationLiteral();
         log.finest("Using default @Production deployment type");
         return;
      }
   }
   
   protected void checkDeploymentType()
   {
      if (deploymentType == null)
      {
         throw new RuntimeException("type: " + getType() + " must specify a deployment type");
      }
   }
   
   public static Annotation getDeploymentType(List<Annotation> enabledDeploymentTypes, Map<Class<? extends Annotation>, Annotation> possibleDeploymentTypes)
   {
      for (int i = (enabledDeploymentTypes.size() - 1); i > 0; i--)
      {
         if (possibleDeploymentTypes.containsKey((enabledDeploymentTypes.get(i).annotationType())))
         {
            return enabledDeploymentTypes.get(i); 
         }
      }
      return null;
   }
   
   protected MergedStereotypesModel<T, E> getMergedStereotypes()
   {
      return mergedStereotypes;
   }
   
   protected abstract String getDefaultName();

   public Set<Annotation> getBindingTypes()
   {
      return bindingTypes;
   }

   public Annotation getScopeType()
   {
      return scopeType;
   }

   public Class<T> getType()
   {
      return type;
   }
   
   public Set<Class> getApiTypes()
   {
      return apiTypes;
   }

   public abstract ComponentConstructor<T> getConstructor();
   
   /**
    * Convenience method that return's the component's "location" for logging
    * and exception throwing
    */
   public abstract String getLocation();

   public Annotation getDeploymentType()
   {
      return deploymentType;
   }

   public String getName()
   {
      return name;
   }
   
   public InjectableMethod<?> getRemoveMethod()
   {
      return removeMethod;
   }

}