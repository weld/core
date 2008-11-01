package org.jboss.webbeans.model.bean;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.webbeans.BindingType;
import javax.webbeans.DefinitionException;
import javax.webbeans.Dependent;
import javax.webbeans.DeploymentType;
import javax.webbeans.Named;
import javax.webbeans.Production;
import javax.webbeans.ScopeType;
import javax.webbeans.Specializes;
import javax.webbeans.Standard;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.bindings.CurrentAnnotationLiteral;
import org.jboss.webbeans.injectable.Injectable;
import org.jboss.webbeans.injectable.InjectableMethod;
import org.jboss.webbeans.injectable.InjectableParameter;
import org.jboss.webbeans.introspector.AnnotatedItem;
import org.jboss.webbeans.model.MergedStereotypesModel;
import org.jboss.webbeans.util.LoggerUtil;
import org.jboss.webbeans.util.Reflections;

public abstract class AbstractBeanModel<T, E> implements BeanModel<T, E>
{
   
   public static final String LOGGER_NAME = "beanModel";
   
   private static Logger log = LoggerUtil.getLogger(LOGGER_NAME);
   
   private Set<Annotation> bindingTypes;
   protected String name;
   protected Class<? extends Annotation> scopeType;
   private MergedStereotypesModel<T, E> mergedStereotypes;
   protected Class<? extends Annotation> deploymentType;
   protected Class<T> type;
   protected InjectableMethod<?> removeMethod;
   private Set<Class<?>> apiTypes;
   protected Set<Injectable<?, ?>> injectionPoints;
   private boolean primitive;
   protected ManagerImpl container;
   
   protected void init(ManagerImpl container)
   {
      this.container = container;
      mergedStereotypes = new MergedStereotypesModel<T, E>(getAnnotatedItem(), getXmlAnnotatedItem(), container);
      initType();
      initPrimitive();
      log.fine("Building Web Bean bean metadata for " +  getType());
      initBindingTypes();
      initName();
      initDeploymentType();
      checkDeploymentType();
      initScopeType();
      initApiTypes();
   }
   
   protected AbstractClassBeanModel<? extends T> getSpecializedType() {
      throw new UnsupportedOperationException();
   }
   
   protected void initInjectionPoints()
   {
      injectionPoints = new HashSet<Injectable<?,?>>();
      if (removeMethod != null)
      {
         for (InjectableParameter<?> injectable : removeMethod.getParameters())
         {
            injectionPoints.add(injectable);
         }
      }
   }
   
   protected abstract void initType();
   
   protected void initApiTypes()
   {
      apiTypes = getTypeHierachy(getType());
   }
   
   protected void initPrimitive()
   {
      this.primitive = Reflections.isPrimitive(getType());
   }
   
   protected Set<Class<?>> getTypeHierachy(Class<?> clazz)
   {
      Set<Class<?>> classes = new HashSet<Class<?>>();
      if (clazz != null)
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

   protected abstract AnnotatedItem<T, E> getAnnotatedItem();
   
   protected abstract AnnotatedItem<T, E> getXmlAnnotatedItem();

   protected void initBindingTypes()
   {
      Set<Annotation> bindingTypes = getAnnotatedItem().getAnnotations(BindingType.class);
      Set<Annotation> xmlBindingTypes = getXmlAnnotatedItem().getAnnotations(BindingType.class);
      boolean xmlSpecialization = getXmlAnnotatedItem().isAnnotationPresent(Specializes.class);
      boolean specialization = getAnnotatedItem().isAnnotationPresent(Specializes.class);
      
      if (xmlBindingTypes.size() > 0 || mergedStereotypes.isDeclaredInXml())
      {
         // TODO support producer expression default binding type
         if (xmlSpecialization)
         {
            xmlBindingTypes.addAll(bindingTypes);
            log.finest("Using binding types " + xmlBindingTypes + " specified in XML and specialized type");
         }
         else {
            log.finest("Using binding types " + xmlBindingTypes + " specified in XML");
         }
         this.bindingTypes= xmlBindingTypes;
         return;
      }
      else
      {
         if (specialization)
         {
            bindingTypes.addAll(getSpecializedType().getBindingTypes());
            log.finest("Using binding types " + bindingTypes + " specified by annotations and specialized supertype");
         }
         else if (bindingTypes.size() == 0)
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
    * Return the scope of the bean
    */
   protected void initScopeType()
   {
      Set<Annotation> xmlScopes = getXmlAnnotatedItem().getAnnotations(ScopeType.class);
      if (xmlScopes.size() > 1)
      {
         throw new DefinitionException("At most one scope may be specified in XML");
      }
      
      if (xmlScopes.size() == 1)
      {
         this.scopeType = xmlScopes.iterator().next().annotationType();
         log.finest("Scope " + scopeType + " specified in XML");
         return;
      }
      
      Set<Annotation> scopes = getAnnotatedItem().getAnnotations(ScopeType.class);
      if (scopes.size() > 1)
      {
         throw new DefinitionException("At most one scope may be specified");
      }
      
      if (scopes.size() == 1)
      {
         this.scopeType = scopes.iterator().next().annotationType();
         log.finest("Scope " + scopeType + " specified b annotation");
         return;
      }
      
      if (getMergedStereotypes().getPossibleScopeTypes().size() == 1)
      {
         this.scopeType = getMergedStereotypes().getPossibleScopeTypes().iterator().next().annotationType();
         log.finest("Scope " + scopeType + " specified by stereotype");
         return;
      }
      else if (getMergedStereotypes().getPossibleScopeTypes().size() > 1)
      {
         throw new RuntimeException("All stereotypes must specify the same scope OR a scope must be specified on the bean");
      }
      this.scopeType = Dependent.class;
      log.finest("Using default @Dependent scope");
   }
   
   protected void initName()
   {
      boolean xmlSpecialization = getXmlAnnotatedItem().isAnnotationPresent(Specializes.class);
      boolean specialization = getAnnotatedItem().isAnnotationPresent(Specializes.class);
      boolean beanNameDefaulted = false;
      if (getXmlAnnotatedItem().isAnnotationPresent(Named.class))
      {
         if (xmlSpecialization) 
         {
            throw new DefinitionException("Name specified for specialized bean (declared in XML)");
         }
         String xmlName = getXmlAnnotatedItem().getAnnotation(Named.class).value();
         if ("".equals(xmlName))
         {
            log.finest("Using default name (specified in XML)");
            beanNameDefaulted = true;
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
         if (specialization)
         {
            throw new DefinitionException("Name specified for specialized bean");
         }
         String javaName = getAnnotatedItem().getAnnotation(Named.class).value();
         if ("".equals(javaName))
         {
            log.finest("Using default name (specified by annotations)");
            beanNameDefaulted = true;
         }
         else
         {
            log.finest("Using name " + javaName + " specified by annotations");
            this.name = javaName;
            return;
         }
      }
      else if (specialization)
      {
         this.name = getSpecializedType().getName();
         log.finest("Using supertype name");
         return;
      }
      if (beanNameDefaulted || getMergedStereotypes().isBeanNameDefaulted())
      {
         this.name = getDefaultName();
         return;
      }
   }
   
   protected void initDeploymentType()
   {
      Set<Annotation> xmlDeploymentTypes = getXmlAnnotatedItem().getAnnotations(DeploymentType.class);
      
      if (xmlDeploymentTypes.size() > 1)
      {
         throw new RuntimeException("At most one deployment type may be specified (" + xmlDeploymentTypes + " are specified)");
      }
      
      if (xmlDeploymentTypes.size() == 1)
      {
         this.deploymentType = xmlDeploymentTypes.iterator().next().annotationType(); 
         log.finest("Deployment type " + deploymentType + " specified in XML");
         return;
      }
      
      
      Set<Annotation> deploymentTypes = getAnnotatedItem().getAnnotations(DeploymentType.class);
      
      if (deploymentTypes.size() > 1)
      {
         throw new DefinitionException("At most one deployment type may be specified (" + deploymentTypes + " are specified) on " + getAnnotatedItem().getDelegate());
      }
      if (deploymentTypes.size() == 1)
      {
         this.deploymentType = deploymentTypes.iterator().next().annotationType();
         log.finest("Deployment type " + deploymentType + " specified by annotation");
         return;
      }
      
      if (getMergedStereotypes().getPossibleDeploymentTypes().size() > 0)
      {
         this.deploymentType = getDeploymentType(container.getEnabledDeploymentTypes(), getMergedStereotypes().getPossibleDeploymentTypes());
         log.finest("Deployment type " + deploymentType + " specified by stereotype");
         return;
      }
      
      if (getXmlAnnotatedItem().getDelegate() != null)
      {
         this.deploymentType = Production.class;
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
      
      if (deploymentType.equals(Standard.class))
      {
         throw new DefinitionException();
      }
   }
   
   public static Class<? extends Annotation> getDeploymentType(List<Class<? extends Annotation>> enabledDeploymentTypes, Map<Class<? extends Annotation>, Annotation> possibleDeploymentTypes)
   {
      for (int i = (enabledDeploymentTypes.size() - 1); i > 0; i--)
      {
         if (possibleDeploymentTypes.containsKey((enabledDeploymentTypes.get(i))))
         {
            return enabledDeploymentTypes.get(i); 
         }
      }
      return null;
   }
   
   public MergedStereotypesModel<T, E> getMergedStereotypes()
   {
      return mergedStereotypes;
   }
   
   protected abstract String getDefaultName();

   public Set<Annotation> getBindingTypes()
   {
      return bindingTypes;
   }

   public Class<? extends Annotation> getScopeType()
   {
      return scopeType;
   }

   public Class<T> getType()
   {
      return type;
   }
   
   public Set<Class<?>> getApiTypes()
   {
      return apiTypes;
   }

   public Class<? extends Annotation> getDeploymentType()
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

   public Set<Injectable<?, ?>> getInjectionPoints()
   {
      return injectionPoints;
   }

   public boolean isAssignableFrom(AnnotatedItem<?, ?> annotatedItem)
   {
      return this.getAnnotatedItem().isAssignableFrom(annotatedItem);
   }
   
   public boolean isPrimitive()
   {
      return primitive;
   }
   
}