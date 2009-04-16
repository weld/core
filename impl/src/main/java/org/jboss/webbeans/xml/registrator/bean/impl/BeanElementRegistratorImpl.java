package org.jboss.webbeans.xml.registrator.bean.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Named;
import javax.annotation.Stereotype;
import javax.context.ScopeType;
import javax.inject.DefinitionException;
import javax.inject.DeploymentType;

import org.dom4j.Element;
import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.introspector.AnnotatedField;
import org.jboss.webbeans.xml.ParseXmlHelper;
import org.jboss.webbeans.xml.XmlConstants;
import org.jboss.webbeans.xml.XmlEnvironment;
import org.jboss.webbeans.xml.checker.beanchildren.BeanChildrenChecker;
import org.jboss.webbeans.xml.registrator.bean.BeanElementRegistrator;


public abstract class BeanElementRegistratorImpl implements BeanElementRegistrator
{
   protected final BeanChildrenChecker childrenChecker;
   
   protected final XmlEnvironment environment;
   
   protected final Map<String, Set<String>> packagesMap;
   
   protected BeanElementRegistratorImpl(BeanChildrenChecker childrenChecker)
   {
      this.childrenChecker = childrenChecker;
      this.environment = childrenChecker.getXmlEnvironment();
      this.packagesMap = childrenChecker.getPackagesMap();
   }
   
   public abstract boolean accept(Element beanElement, AnnotatedClass<?> beanClass);

   protected abstract void checkElementDeclaration(Element beanElement, AnnotatedClass<?> beanClass);
   
   public void registerBeanElement(Element beanElement, AnnotatedClass<?> beanClass)
   {
      checkElementDeclaration(beanElement, beanClass);
      childrenChecker.checkChildren(beanElement, beanClass);
      checkProduces(beanElement, beanClass);
      register(beanElement, beanClass);
   }
   
   protected void register(Element beanElement, AnnotatedClass<?> beanClass)
   {
      environment.getClasses().add(beanClass);
   }
   
   private void checkProduces(Element beanElement, AnnotatedClass<?> beanClass)
   {//TODO: will refactor
      Iterator<?> beanIterator = beanElement.elementIterator();
      while(beanIterator.hasNext())
      {
         Element beanChild = (Element)beanIterator.next();  
         List<Element> producesElements = ParseXmlHelper.findElementsInEeNamespace(beanChild, XmlConstants.PRODUCES);
         
         if(producesElements.size() == 0)
            continue;
         
         if(producesElements.size() > 1)
            throw new DefinitionException("There is more than one child <Produces> element for <" + beanChild.getName()  + "> element");
         
         List<AnnotatedClass<?>> producesChildTypes = new ArrayList<AnnotatedClass<?>>();
                  
         Element producesElement = producesElements.get(0);
         Iterator<?> producesIt = producesElement.elementIterator();
         while(producesIt.hasNext())
         {
            Element producesChild = (Element)producesIt.next();
            AnnotatedClass<?> producesChildClass = ParseXmlHelper.loadElementClass(producesChild, Object.class, environment, packagesMap);
            Class<?> producesChildType = producesChildClass.getRawType();
            boolean isJavaClass = !producesChildType.isEnum() && !producesChildType.isPrimitive() && !producesChildType.isInterface();
            boolean isInterface = producesChildType.isInterface() && !producesChildType.isAnnotation();
            if(isJavaClass || isInterface)
            {
               producesChildTypes.add(producesChildClass);
               continue;
            }
            if(producesChildType.isAnnotation())
            {
               if(producesChildClass.isAnnotationPresent(DeploymentType.class) || 
                     producesChildClass.isAnnotationPresent(ScopeType.class) || 
                     producesChildClass.isAnnotationPresent(Stereotype.class) ||
                     producesChildClass.isAnnotationPresent(Named.class))
                  continue;
                                             
               throw new DefinitionException("<" + producesChild.getName() + "> direct child of <Produces> element for <" + beanChild.getName() 
                     + "> in bean" + beanElement.getName() + "must be DeploymentType or ScopeType or Stereotype or Named");
            }
            throw new DefinitionException("Only Java class, interface type and Java annotation type can be " +
                  "direct child of <Produces> element for <" + beanChild.getName() + "> in bean" + beanElement.getName() + 
                  ". Element <" + producesChild.getName() + "> is incorrect");
         }
         
         if(producesChildTypes.size() != 1)
            throw new DefinitionException("More than one or no one child element of <Produces> element for <" + beanChild.getName() + 
                  "> in bean" + beanElement.getName() + " represents a Java class or interface type");
         
         AnnotatedClass<?> expectedType = producesChildTypes.get(0);
                  
         Method beanMethod = null;         
         AnnotatedField<?> beanField = beanClass.getDeclaredField(beanChild.getName(), expectedType);

         try
         {
            List<Class<?>> paramClassesList = new ArrayList<Class<?>>();
            Iterator<?> beanChildIt = beanChild.elementIterator();
            while(beanChildIt.hasNext())
            {
               Element methodChild = (Element)beanChildIt.next();
               if(methodChild.getName().equalsIgnoreCase(XmlConstants.PRODUCES))
                  continue;
               paramClassesList.add(ParseXmlHelper.loadElementClass(methodChild, Object.class, environment, packagesMap).getRawType());
            }
            Class<?>[] paramClasses = (Class<?>[])paramClassesList.toArray(new Class[0]);
            beanMethod = beanClass.getRawType().getDeclaredMethod(beanChild.getName(), paramClasses);
         }
         catch (SecurityException e)
         {}
         catch (NoSuchMethodException e)
         {}
         
         if(beanField != null && beanMethod != null)
            throw new DefinitionException("Class '" + beanClass.getName() + "' has produser field and method with the same name '" + 
                  beanField.getName() + "'");
         
         if(beanField != null)
         {
            if(beanChild.elements().size() > 1)
               throw new DefinitionException("There is more than one direct child element for producer field <" + beanChild.getName() + ">");
            continue;
         }
         
         if(beanMethod != null)
         {
            Iterator<?> beanChildIt = beanChild.elementIterator();
            while(beanChildIt.hasNext())
            {
               Element element = (Element)beanChildIt.next();
               if(!element.getName().equalsIgnoreCase(XmlConstants.PRODUCES) && 
                     ParseXmlHelper.findElementsInEeNamespace(beanChild, XmlConstants.INTERCEPTOR).size() == 0)
                  throw new DefinitionException("Only Produces and interceptor binding types can be direct childs of a producer " +
                        "method '" + beanChild.getName() + "' declaration in bean '" + beanElement.getName() + "'");
            }
            continue;
         }
         
         throw new DefinitionException("A producer '" + beanChild.getName() + "' doesn't declared in '" + beanElement.getName() + 
               "' class file as method or field");
      }                  
   }
}
