package org.jboss.webbeans.bean;

import java.util.HashSet;
import java.util.Set;

import javax.webbeans.BindingType;
import javax.webbeans.DefinitionException;
import javax.webbeans.Destructor;
import javax.webbeans.Disposes;
import javax.webbeans.Initializer;
import javax.webbeans.Observes;
import javax.webbeans.Produces;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.introspector.AnnotatedField;
import org.jboss.webbeans.introspector.AnnotatedMethod;
import org.jboss.webbeans.introspector.impl.InjectableField;
import org.jboss.webbeans.introspector.impl.InjectableMethod;
import org.jboss.webbeans.introspector.impl.SimpleAnnotatedClass;
import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;
import org.jboss.webbeans.util.Reflections;
import org.jboss.webbeans.util.Strings;

public abstract class AbstractClassBean<T> extends AbstractBean<T, Class<T>>
{
   
   private static final LogProvider log = Logging.getLogProvider(AbstractClassBean.class);
   
   private AnnotatedClass<T> annotatedItem;
   private Set<InjectableField<?>> injectableFields;
   private Set<InjectableMethod<Object>> initializerMethods;
   protected boolean annotationDefined;
   
   /**
    * 
    * @param annotatedItem Annotations read from java classes
    * @param xmlAnnotatedItem Annotations read from XML
    * @param manager
    */
   public AbstractClassBean(Class<T> type, ManagerImpl manager)
   {
      super(manager);
      this.annotatedItem = new SimpleAnnotatedClass<T>(type);
   }
   
   @Override
   protected void init()
   {
      super.init();
      checkRequiredTypesImplemented();
      checkScopeAllowed();
      checkBeanImplementation();
      // TODO Interceptors
      initInitializerMethods();
   }
   
   protected void initType()
   {
      if (isDefinedInXml())
      {
         log.trace("Bean type specified in Java");
      }
      else
      {
         log.trace("Bean type specified in Java");
         this.type = getAnnotatedItem().getType();
      }
   }
   
   @Override
   protected void initInjectionPoints()
   {
      super.initInjectionPoints();
      injectableFields = new HashSet<InjectableField<?>>();
      for (AnnotatedField<Object> annotatedField : annotatedItem.getMetaAnnotatedFields(BindingType.class))
      {
         if (annotatedField.isStatic())
         {
            throw new DefinitionException("Don't place binding annotations on static fields " + annotatedField);
         }
         if (annotatedField.isFinal())
         {
            throw new DefinitionException("Don't place binding annotations on final fields " + annotatedField);
         }
         InjectableField<?> injectableField = new InjectableField<Object>(annotatedField);
         injectableFields.add(injectableField);
         super.injectionPoints.add(injectableField);
      }
   }
   
   protected void initInitializerMethods()
   {
      if (isDefinedInXml())
      {
        
      }
      else
      {
         initializerMethods = new HashSet<InjectableMethod<Object>>();
         for (AnnotatedMethod<Object> annotatedMethod : annotatedItem.getAnnotatedMethods(Initializer.class))
         {
            if (annotatedMethod.isStatic())
            {
               throw new DefinitionException("Initializer method " + annotatedMethod.toString() + " cannot be static");
            }
            else if (annotatedMethod.getAnnotation(Produces.class) != null)
            {
               throw new DefinitionException("Initializer method " + annotatedMethod.toString() + " cannot be annotated @Produces");
            }
            else if (annotatedMethod.getAnnotation(Destructor.class) != null)
            {
               throw new DefinitionException("Initializer method " + annotatedMethod.toString() + " cannot be annotated @Destructor");
            }
            else if (annotatedMethod.getAnnotatedParameters(Disposes.class).size() > 0)
            {
               throw new DefinitionException("Initializer method " + annotatedMethod.toString() + " cannot have parameters annotated @Disposes");
            }
            else if (annotatedMethod.getAnnotatedParameters(Observes.class).size() > 0)
            {
               throw new DefinitionException("Initializer method " + annotatedMethod.toString() + " cannot be annotated @Observes");
            }
            else
            {
               initializerMethods.add(new InjectableMethod<Object>(annotatedMethod));
            }
         }
      }
   }
   
   protected void checkRequiredTypesImplemented()
   {
      for (Class<?> requiredType : getMergedStereotypes().getRequiredTypes())
      {
         log.trace("Checking if required type " + requiredType + " is implemented");
         if (!requiredType.isAssignableFrom(type))
         {
            throw new DefinitionException("Required type " + requiredType + " isn't implemented on " + type);
         }
      }
   }
   
   /**
    * Check that the scope type is allowed by the stereotypes on the bean and the bean type
    * @param type 
    */
   protected void checkScopeAllowed()
   {
      log.trace("Checking if " + getScopeType() + " is allowed for " + type);
      if (getMergedStereotypes().getSupportedScopes().size() > 0)
      {
         if (!getMergedStereotypes().getSupportedScopes().contains(getScopeType()))
         {
            throw new DefinitionException("Scope " + getScopeType() + " is not an allowed by the stereotype for " + type);
         }
      }
   }
   
   protected void checkBeanImplementation()
   {
      if (Reflections.isAbstract(getType()))
      {
         throw new DefinitionException("Web Bean implementation class " + type + " cannot be declared abstract");
      }
   }
   
   @Override
   protected AnnotatedClass<T> getAnnotatedItem()
   {
      return annotatedItem;
   }
   
   @Override
   protected String getDefaultName()
   {
      String name = Strings.decapitalize(getType().getSimpleName()); 
      log.trace("Default name of " + type + " is " + name );
      return name;
   }
   
   public Set<InjectableField<?>> getInjectableFields()
   {
      return injectableFields;
   }
   
   public Set<InjectableMethod<Object>> getInitializerMethods()
   {
      return initializerMethods;
   }

   
}
