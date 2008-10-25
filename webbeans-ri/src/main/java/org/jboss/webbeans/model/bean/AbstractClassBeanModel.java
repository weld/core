package org.jboss.webbeans.model.bean;

import java.util.logging.Logger;

import javax.webbeans.BindingType;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.introspector.AnnotatedType;
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
   
   private AnnotatedType<T> annotatedItem;
   private AnnotatedType<T> xmlAnnotatedItem;
   
   /**
    * 
    * @param annotatedItem Annotations read from java classes
    * @param xmlAnnotatedItem Annotations read from XML
    * @param manager
    */
   public AbstractClassBeanModel(AnnotatedType<T> annotatedItem, AnnotatedType<T> xmlAnnotatedItem)
   {
      if (annotatedItem == null)
      {
         throw new NullPointerException("annotatedItem must not be null. If the bean is declared just in XML, pass in an empty annotatedItem");
      }
      
      if (xmlAnnotatedItem == null)
      {
         throw new NullPointerException("xmlAnnotatedItem must not be null. If the bean is declared just in Java, pass in an empty xmlAnnotatedItem");
      }
      this.annotatedItem = annotatedItem;
      this.xmlAnnotatedItem = xmlAnnotatedItem;
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
   }
   
   @Override
   protected AnnotatedType<T> getAnnotatedItem()
   {
      return annotatedItem;
   }
   
   @Override
   protected AnnotatedType<T> getXmlAnnotatedItem()
   {
      return xmlAnnotatedItem;
   }
   
   protected void initType()
   {
      if (getAnnotatedItem().getDelegate() != null && getXmlAnnotatedItem().getDelegate() != null && !getAnnotatedItem().getDelegate().equals(getXmlAnnotatedItem().getDelegate()))
      {
         throw new IllegalArgumentException("Cannot build a bean which specifies different classes in XML and Java");
      }
      else if (getXmlAnnotatedItem().getDelegate() != null)
      {
         log.finest("Bean type specified in XML");
         this.type = getXmlAnnotatedItem().getDelegate();
      }
      else if (getAnnotatedItem().getDelegate() != null)
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
      annotatedItem.getMetaAnnotatedFields(BindingType.class);
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
            throw new RuntimeException("Required type " + requiredType + " isn't implemented on " + type);
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
            throw new RuntimeException("Scope " + getScopeType() + " is not an allowed by the stereotype for " + type);
         }
      }
   }
   
   protected void checkBeanImplementation()
   {
      if (Reflections.isAbstract(getType()))
      {
         throw new RuntimeException("Web Bean implementation class " + type + " cannot be declared abstract");
      }
   }

}
