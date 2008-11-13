package org.jboss.webbeans.bean;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.webbeans.BindingType;
import javax.webbeans.DefinitionException;
import javax.webbeans.Dependent;
import javax.webbeans.DeploymentType;
import javax.webbeans.Named;
import javax.webbeans.Production;
import javax.webbeans.ScopeType;
import javax.webbeans.Specializes;
import javax.webbeans.Standard;
import javax.webbeans.Stereotype;
import javax.webbeans.manager.Bean;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.bindings.CurrentAnnotationLiteral;
import org.jboss.webbeans.ejb.DefaultEnterpriseBeanLookup;
import org.jboss.webbeans.introspector.AnnotatedItem;
import org.jboss.webbeans.introspector.AnnotatedMethod;
import org.jboss.webbeans.introspector.AnnotatedParameter;
import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;
import org.jboss.webbeans.util.Reflections;

public abstract class AbstractBean<T, E> extends Bean<T>
{
 
   private static Set<Class<?>> STANDARD_WEB_BEAN_CLASSES = new HashSet<Class<?>>(Arrays.asList(DefaultEnterpriseBeanLookup.class));
   
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
   
   // Logger
   private LogProvider log = Logging.getLogProvider(AbstractBean.class);
   
   // Reference to WBRI manager
   private ManagerImpl manager;
   private Set<Annotation> bindingTypes;
   protected String name;
   protected Class<? extends Annotation> scopeType;
   private MergedStereotypes<T, E> mergedStereotypes;
   protected Class<? extends Annotation> deploymentType;
   protected Class<T> type;
   protected AnnotatedMethod<Object> removeMethod;
   protected Set<Class<?>> apiTypes;
   protected Set<AnnotatedItem<?, ?>> injectionPoints;
   
   private boolean primitive;
   
   // Cached values
   private Type declaredBeanType;
   
   public AbstractBean(ManagerImpl manager)
   {
      super(manager);
      this.manager = manager;
   }
   
   protected void init()
   {
      mergedStereotypes = new MergedStereotypes<T, E>(getAnnotatedItem().getMetaAnnotations(Stereotype.class), manager);
      initType();
      initPrimitive();
      log.debug("Building Web Bean bean metadata for " +  getType());
      initBindingTypes();
      initName();
      initDeploymentType();
      checkDeploymentType();
      initScopeType();
      initApiTypes();
   }
   
   protected void initApiTypes()
   {
      apiTypes = getTypeHierachy(getType());
   }
   
   protected void initBindingTypes()
   {
      
      this.bindingTypes = new HashSet<Annotation>();
      if (isDefinedInXml())
      {
         boolean xmlSpecialization = false;
         Set<Annotation> xmlBindingTypes = null;
         this.bindingTypes.addAll(xmlBindingTypes);
         if (xmlSpecialization)
         {
            this.bindingTypes.addAll(bindingTypes);
            log.trace("Using binding types " + this.bindingTypes + " specified in XML and specialized type");
         }
         else 
         {
            log.trace("Using binding types " + this.bindingTypes + " specified in XML");
         }
         return;
      }
      else if (!mergedStereotypes.isDeclaredInXml())
      {
         boolean specialization = getAnnotatedItem().isAnnotationPresent(Specializes.class);
         this.bindingTypes.addAll(getAnnotatedItem().getMetaAnnotations(BindingType.class));
         if (specialization)
         {
            this.bindingTypes.addAll(getSpecializedType().getBindingTypes());
            log.trace("Using binding types " + bindingTypes + " specified by annotations and specialized supertype");
         }
         else if (bindingTypes.size() == 0)
         {
            log.trace("Adding default @Current binding type");
            this.bindingTypes.add(new CurrentAnnotationLiteral());
         }
         else
         {
            log.trace("Using binding types " + bindingTypes + " specified by annotations");
         }
         return;
      }
   }

   protected void initDeploymentType()
   {
      if (isDefinedInXml())
      {
         Set<Annotation> xmlDeploymentTypes = null;
         if (xmlDeploymentTypes.size() > 1)
         {
            throw new RuntimeException("At most one deployment type may be specified (" + xmlDeploymentTypes + " are specified)");
         }
         
         if (xmlDeploymentTypes.size() == 1)
         {
            this.deploymentType = xmlDeploymentTypes.iterator().next().annotationType(); 
            log.trace("Deployment type " + deploymentType + " specified in XML");
            return;
         }
      }
      else
      {
         Set<Annotation> deploymentTypes = getAnnotatedItem().getMetaAnnotations(DeploymentType.class);
         
         if (deploymentTypes.size() > 1)
         {
            throw new DefinitionException("At most one deployment type may be specified (" + deploymentTypes + " are specified) on " + getAnnotatedItem().toString());
         }
         if (deploymentTypes.size() == 1)
         {
            this.deploymentType = deploymentTypes.iterator().next().annotationType();
            log.trace("Deployment type " + deploymentType + " specified by annotation");
            return;
         }
         
         if (getMergedStereotypes().getPossibleDeploymentTypes().size() > 0)
         {
            this.deploymentType = getDeploymentType(manager.getEnabledDeploymentTypes(), getMergedStereotypes().getPossibleDeploymentTypes());
            log.trace("Deployment type " + deploymentType + " specified by stereotype");
            return;
         }
      }
      
      this.deploymentType = Production.class;
      log.trace("Using default @Production deployment type");
      return;
   }

   protected void initInjectionPoints()
   {
      injectionPoints = new HashSet<AnnotatedItem<?,?>>();
      if (removeMethod != null)
      {
         for (AnnotatedParameter<?> injectable : removeMethod.getParameters())
         {
            injectionPoints.add(injectable);
         }
      }
   }

   protected void initName()
   {
      boolean beanNameDefaulted = false;
      if (isDefinedInXml())
      {
         boolean xmlSpecialization = false;
         if (xmlSpecialization) 
         {
            throw new DefinitionException("Name specified for specialized bean (declared in XML)");
         }
         String xmlName = "";
         if ("".equals(xmlName))
         {
            log.trace("Using default name (specified in XML)");
            beanNameDefaulted = true;
         }
         else
         {
            log.trace("Using name " + xmlName + " specified in XML");
            this.name = xmlName;
            return;
         }
      }
      else
      {
         boolean specialization = getAnnotatedItem().isAnnotationPresent(Specializes.class);
         if (getAnnotatedItem().isAnnotationPresent(Named.class))
         {
            if (specialization)
            {
               throw new DefinitionException("Name specified for specialized bean");
            }
            String javaName = getAnnotatedItem().getAnnotation(Named.class).value();
            if ("".equals(javaName))
            {
               log.trace("Using default name (specified by annotations)");
               beanNameDefaulted = true;
            }
            else
            {
               log.trace("Using name " + javaName + " specified by annotations");
               this.name = javaName;
               return;
            }
         }
         else if (specialization)
         {
            this.name = getSpecializedType().getName();
            log.trace("Using supertype name");
            return;
         }
      }
      
      if (beanNameDefaulted || getMergedStereotypes().isBeanNameDefaulted())
      {
         this.name = getDefaultName();
         return;
      }
   }

   protected void initPrimitive()
   {
      this.primitive = Reflections.isPrimitive(getType());
   }

   /**
    * Return the scope of the bean
    */
   protected void initScopeType()
   {
      if (isDefinedInXml())
      {
         Set<Class<? extends Annotation>> scopeTypes = null;
         if (scopeTypes.size() > 1)
         {
            throw new DefinitionException("At most one scope may be specified in XML");
         }
         
         if (scopeTypes.size() == 1)
         {
            this.scopeType = scopeTypes.iterator().next();
            log.trace("Scope " + scopeType + " specified in XML");
            return;
         }
      }
      else
      {
         if (getAnnotatedItem().getMetaAnnotations(ScopeType.class).size() > 1)
         {
            throw new DefinitionException("At most one scope may be specified");
         }
         
         if (getAnnotatedItem().getMetaAnnotations(ScopeType.class).size() == 1)
         {
            this.scopeType = getAnnotatedItem().getMetaAnnotations(ScopeType.class).iterator().next().annotationType();
            log.trace("Scope " + scopeType + " specified b annotation");
            return;
         }
      }
      
      if (getMergedStereotypes().getPossibleScopeTypes().size() == 1)
      {
         this.scopeType = getMergedStereotypes().getPossibleScopeTypes().iterator().next().annotationType();
         log.trace("Scope " + scopeType + " specified by stereotype");
         return;
      }
      else if (getMergedStereotypes().getPossibleScopeTypes().size() > 1)
      {
         throw new RuntimeException("All stereotypes must specify the same scope OR a scope must be specified on the bean");
      }
      this.scopeType = Dependent.class;
      log.trace("Using default @Dependent scope");
   }
   
   protected abstract void initType();
   
   protected void checkDeploymentType()
   {
      if (deploymentType == null)
      {
         throw new RuntimeException("type: " + getType() + " must specify a deployment type");
      }
      else if (deploymentType.equals(Standard.class) && !STANDARD_WEB_BEAN_CLASSES.contains(getAnnotatedItem().getType()))
      {
         throw new DefinitionException(getAnnotatedItem() + " cannot have deployment type @Standard");
      }
   }
   
   @Override
   public void destroy(T instance)
   {
      // TODO Auto-generated method stub
   }
   
   protected void bindDecorators()
   {
      // TODO
   }
   
   protected void bindInterceptors()
   {
      // TODO
   }
   
   protected abstract AnnotatedItem<T, E> getAnnotatedItem();

   public Set<Annotation> getBindingTypes()
   {
      return bindingTypes;
   }

   protected Type getDeclaredBeanType()
   {
      if (declaredBeanType == null)
      {
         Type type = getClass();
         if (type instanceof ParameterizedType)
         {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            if (parameterizedType.getActualTypeArguments().length == 1)
            {
               declaredBeanType = parameterizedType.getActualTypeArguments()[0];
            }
         }
      }
      return declaredBeanType;
   }

   protected abstract String getDefaultName();
   
   public Class<? extends Annotation> getDeploymentType()
   {
      return deploymentType;
   }
   
   public Set<AnnotatedItem<?, ?>> getInjectionPoints()
   {
      return injectionPoints;
   }
   
   @Override
   protected ManagerImpl getManager()
   {
      return manager;
   }
   
   public MergedStereotypes<T, E> getMergedStereotypes()
   {
      return mergedStereotypes;
   }
   
   public String getName()
   {
      return name;
   }

   public AnnotatedMethod<?> getRemoveMethod()
   {
      return removeMethod;
   }

   public Class<? extends Annotation> getScopeType()
   {
      return scopeType;
   }
   
   protected AbstractBean<? extends T, E> getSpecializedType() 
   {
      throw new UnsupportedOperationException();
   }
   

   
   public Class<T> getType()
   {
      return type;
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
   
   @Override
   public Set<Class<?>> getTypes()
   {
      return apiTypes;
   }
   
   public boolean isAssignableFrom(AnnotatedItem<?, ?> annotatedItem)
   {
      return this.getAnnotatedItem().isAssignableFrom(annotatedItem);
   }

   protected boolean isDefinedInXml()
   {
      return false;
   }
   
   @Override
   public boolean isNullable()
   {
      return !isPrimitive();
   }
   
   public boolean isPrimitive()
   {
      return primitive;
   }
   
   @Override
   public boolean isSerializable()
   {
      // TODO Auto-generated method stub
      return false;
   }

}
