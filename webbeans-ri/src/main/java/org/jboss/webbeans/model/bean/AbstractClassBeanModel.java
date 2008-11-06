package org.jboss.webbeans.model.bean;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.webbeans.BindingType;
import javax.webbeans.DefinitionException;
import javax.webbeans.Destructor;
import javax.webbeans.Disposes;
import javax.webbeans.Initializer;
import javax.webbeans.Observes;
import javax.webbeans.Produces;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.injectable.InjectableField;
import org.jboss.webbeans.injectable.InjectableMethod;
import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.introspector.AnnotatedField;
import org.jboss.webbeans.introspector.AnnotatedMethod;
import org.jboss.webbeans.util.LoggerUtil;
import org.jboss.webbeans.util.Reflections;
import org.jboss.webbeans.util.Strings;



/**
 * Web Beans Bean meta model
 * 
 * @author Pete Muir
 * 
 */
public abstract class AbstractClassBeanModel<T> extends AbstractBeanModel<T, Class<T>>
{

   private static Logger log = LoggerUtil.getLogger(LOGGER_NAME);
   
   private AnnotatedClass<T> annotatedItem;
   private AnnotatedClass<T> xmlAnnotatedItem;
   private Set<InjectableField<?>> injectableFields;
   private Set<InjectableMethod<Object>> initializerMethods;
   protected boolean annotationDefined;
   
   /**
    * 
    * @param annotatedItem Annotations read from java classes
    * @param xmlAnnotatedItem Annotations read from XML
    * @param manager
    */
   public AbstractClassBeanModel(AnnotatedClass<T> annotatedItem, AnnotatedClass<T> xmlAnnotatedItem)
   {
      this.annotatedItem = annotatedItem;
      this.xmlAnnotatedItem = xmlAnnotatedItem;
      annotationDefined = annotatedItem != null ? true : false;
   }
   
   @Override
   protected void init(ManagerImpl container)
   {
      super.init(container);
      checkRequiredTypesImplemented();
      checkScopeAllowed();
      // TODO This is too high
      checkBeanImplementation();
      // TODO Interceptors
      initInitializerMethods();
   }
   
   @Override
   protected AnnotatedClass<T> getAnnotatedItem()
   {
      return annotatedItem;
   }
   
   @Override
   protected AnnotatedClass<T> getXmlAnnotatedItem()
   {
      return xmlAnnotatedItem;
   }
   
   protected void initType()
   {
      // TODO This is not the right way to check XML definition
      if (getAnnotatedItem() != null && getXmlAnnotatedItem() != null && !getAnnotatedItem().getDelegate().equals(getXmlAnnotatedItem().getDelegate()))
      {
         throw new IllegalArgumentException("Cannot build a bean which specifies different classes in XML and Java");
      }
      else if (getXmlAnnotatedItem() != null)
      {
         log.finest("Bean type specified in XML");
         this.type = getXmlAnnotatedItem().getDelegate();
      }
      else if (getAnnotatedItem() != null)
      {
         log.finest("Bean type specified in Java");
         this.type = getAnnotatedItem().getDelegate();
      }
      else
      {
         throw new IllegalArgumentException("Cannot build a bean which doesn't specify a type");
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
      // TODO Support XML
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
   
   @Override
   protected String getDefaultName()
   {
      String name = Strings.decapitalize(getType().getSimpleName()); 
      log.finest("Default name of " + type + " is " + name );
      return name;
   }
   
   /**
    * Check that the types required by the stereotypes on the bean are implemented
    */
   protected void checkRequiredTypesImplemented()
   {
      for (Class<?> requiredType : getMergedStereotypes().getRequiredTypes())
      {
         log.finest("Checking if required type " + requiredType + " is implemented");
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
      log.finest("Checking if " + getScopeType() + " is allowed for " + type);
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
   
   public Set<InjectableField<?>> getInjectableFields()
   {
      return injectableFields;
   }
   
   public Set<InjectableMethod<Object>> getInitializerMethods()
   {
      return initializerMethods;
   }

}
