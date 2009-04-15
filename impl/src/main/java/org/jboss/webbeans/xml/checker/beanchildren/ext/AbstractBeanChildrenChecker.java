package org.jboss.webbeans.xml.checker.beanchildren.ext;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Named;
import javax.annotation.Resource;
import javax.annotation.Stereotype;
import javax.context.ScopeType;
import javax.decorator.Decorator;
import javax.ejb.EJB;
import javax.inject.BindingType;
import javax.inject.DefinitionException;
import javax.inject.DeploymentType;
import javax.inject.Realizes;
import javax.inject.Specializes;
import javax.interceptor.Interceptor;
import javax.interceptor.InterceptorBindingType;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import javax.xml.ws.WebServiceRef;

import org.dom4j.Element;
import org.dom4j.Namespace;
import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.introspector.AnnotatedField;
import org.jboss.webbeans.xml.ParseXmlHelper;
import org.jboss.webbeans.xml.XmlConstants;
import org.jboss.webbeans.xml.XmlEnvironment;
import org.jboss.webbeans.xml.checker.beanchildren.BeanChildrenChecker;
import org.jboss.webbeans.xml.checker.beanchildren.impl.BeanChildrenCheckerImpl;

public abstract class AbstractBeanChildrenChecker extends BeanChildrenCheckerImpl
{
   
   private Set<AnnotatedField<?>> beanFields;
   
   private List<Method> beanMethods; 
   
   private boolean haveBeanDeploymentTypeDeclaration = false;
   
   private boolean haveBeanScopeTypeDeclaration = false;
   
   protected boolean haveBeanInterceptorDeclaration = false;
   
   protected boolean haveBeanDecoratorDeclaration = false;
   
   protected List<AnnotatedClass<?>> constructorParameters = new ArrayList<AnnotatedClass<?>>();
         
   protected abstract void checkForInterceptorChild(Element beanElement);
   
   protected abstract void checkForDecoratorChild(Element beanElement);
   
   protected abstract void checkChildForInterceptorType(Element beanElement);
   
   protected abstract void checkChildForDecoratorType(Element beanElement);
   
   protected abstract void checkForConstructor(Element beanElement, AnnotatedClass<?> beanClass);
   
   protected AbstractBeanChildrenChecker(XmlEnvironment environment, Map<String, Set<String>> packagesMap)
   {
      super(environment, packagesMap);
   }
   
   public void checkChildren(Element beanElement, AnnotatedClass<?> beanClass)
   {
      beanFields = beanClass.getFields();
      beanMethods = Arrays.asList(beanClass.getRawType().getDeclaredMethods());
      
      checkForInterceptorChild(beanElement);
      checkForDecoratorChild(beanElement);
      
      haveBeanDeploymentTypeDeclaration = false;
      haveBeanScopeTypeDeclaration = false;
      haveBeanInterceptorDeclaration = ParseXmlHelper.findElementsInEeNamespace(beanElement, XmlConstants.INTERCEPTOR).size() > 0;
      haveBeanDecoratorDeclaration = ParseXmlHelper.findElementsInEeNamespace(beanElement, XmlConstants.DECORATOR).size() > 0;
      
      Iterator<?> beanIterator = beanElement.elementIterator();
      while(beanIterator.hasNext())
      {
         Element beanChildElement = (Element)beanIterator.next();
         checkBeanChild(beanChildElement, beanClass);
      }
      checkForConstructor(beanElement, beanClass);
   }
   
   private void checkBeanChild(Element beanChildElement, AnnotatedClass<?> beanClass)
   {
      try
      {
         if(beanChildElement.getName().equalsIgnoreCase(XmlConstants.ARRAY))
            return;
         AnnotatedClass<?> beanChildClass = ParseXmlHelper.loadElementClass(beanChildElement, Object.class, environment, packagesMap);
         Class<?> beanChildType = beanChildClass.getRawType();
         boolean isJavaClass = !beanChildType.isEnum() && !beanChildType.isPrimitive() && !beanChildType.isInterface();
         boolean isInterface = beanChildType.isInterface() && !beanChildType.isAnnotation();
         if(beanChildType.isAnnotation())
         {
            //bean child element declaring type-level metadata
            checkAnnotationChild(beanChildElement, beanChildClass);
            return;
         }
         if(isJavaClass || isInterface)
         {
            //bean child element declaring a parameter of the bean constructor
            constructorParameters.add(beanChildClass);
            return;
         }
         throw new DefinitionException(new DefinitionException(beanChildElement.getName() + " can't be interpreted as a Java class or interface or Java Annotation type"));
      }
      catch(DefinitionException e)
      {
         if(!(e.getCause() instanceof DefinitionException))
         {
            throw new DefinitionException(e);
         }
         
         Element beanElement = beanChildElement.getParent();
         Namespace beanNamespace = beanElement.getNamespace();
         Namespace beanChildNamespace = beanChildElement.getNamespace();
         if(beanChildNamespace.equals(beanNamespace))
         {
            //bean child element declaring a method or field of the bean.
            checkFieldOrMethodChild(beanChildElement, beanClass);
            return;
         }
         throw new DefinitionException("Can't determine type of element <" + beanChildElement.getName() + "> in bean '" + 
               beanElement.getName() + "'");
      }
   }
   
   private void checkAnnotationChild(Element beanChildElement, AnnotatedClass<?> beanChildClass)
   {
      if(beanChildClass.isAnnotationPresent(DeploymentType.class))
      {
         if(haveBeanDeploymentTypeDeclaration)
            throw new DefinitionException("Only one deployment type declaration allowed for bean '" + 
                  beanChildElement.getParent().getName() + "'");
         haveBeanDeploymentTypeDeclaration = true;
         return;
      }
      if(beanChildClass.isAnnotationPresent(ScopeType.class))
      {
         if(haveBeanScopeTypeDeclaration)
            throw new DefinitionException("Only one scope type declaration allowed for bean '" + 
                  beanChildElement.getParent().getName() + "'");
         haveBeanScopeTypeDeclaration = true;
         return;
      }
      if(beanChildClass.isAnnotationPresent(Interceptor.class))
      {
         checkChildForInterceptorType(beanChildElement);
         return;
      }
      if(beanChildClass.isAnnotationPresent(Decorator.class))
      {
         checkChildForDecoratorType(beanChildElement);
         return;
      }
      if(beanChildClass.isAnnotationPresent(BindingType.class) || beanChildClass.isAnnotationPresent(InterceptorBindingType.class) || 
            beanChildClass.isAnnotationPresent(Stereotype.class) || beanChildClass.isAnnotationPresent(Named.class) || 
            beanChildClass.isAnnotationPresent(Specializes.class) || beanChildClass.isAnnotationPresent(Realizes.class) || 
            beanChildClass.isAnnotationPresent(Resource.class) || beanChildClass.isAnnotationPresent(EJB.class) || 
            beanChildClass.isAnnotationPresent(WebServiceRef.class) || beanChildClass.isAnnotationPresent(PersistenceContext.class) || 
            beanChildClass.isAnnotationPresent(PersistenceUnit.class))
         return;
      
      throw new DefinitionException("Can't determine annotation type of <" + beanChildElement.getName() + "> element in bean '" + 
            beanChildElement.getParent().getName() + "'");
   }
   
   private void checkFieldOrMethodChild(Element beanChildElement, AnnotatedClass<?> beanClass)
   {  //TODO: not finished    
      boolean isField = false;
      boolean isMethod = false;
      
      for(AnnotatedField<?> field : beanFields)
      {
         if(beanChildElement.getName().equalsIgnoreCase(field.getName()))
         {
            if(isField)
               throw new DefinitionException("Bean class '" + beanClass.getName() + "' does not have exactly one field " +
               		"with the specified name '" + beanChildElement.getName() + "'");
            isField = true;
         }
      }
      
      for(Method method : beanMethods)
      {
         if(beanChildElement.getName().equalsIgnoreCase(method.getName()))
            isMethod = true;
      }
      
      if(isField && isMethod)
         throw new DefinitionException("The name of the child element <" + beanChildElement.getName() + 
               "> matches the name of both a method and a field of the bean class '" + beanClass.getName() + "'");
      
      if(isField)
         checkFieldChild(beanChildElement, beanClass);
      
      if(isMethod)
         checkMethodChild(beanChildElement, beanClass);
   }
   
   private void checkFieldChild(Element beanChildElement, AnnotatedClass<?> beanClass)
   {//TODO: not finished
      Element elementParent = beanChildElement.getParent();
      String elementName = beanChildElement.getName();
      String elementPrefix = beanChildElement.getNamespacePrefix();
      String elementUri = beanChildElement.getNamespaceURI();
      
      if(ParseXmlHelper.findElements(elementParent, elementName, elementPrefix, elementUri).size() > 1)
         throw new DefinitionException("More than one child element of a bean '" + elementParent.getName() + 
               "' declaration represents the same field'" + elementName + "'");
      
      if(beanChildElement.elements().size() > 1 && 
            beanChildElement.elements().size() != ParseXmlHelper.findElementsInEeNamespace(beanChildElement, XmlConstants.VALUE).size())
         throw new DefinitionException("Declaration of a field '" + beanChildElement.getName() + "' has more than one direct child element, " +
         		"and at least one of these elements is something other than a <value> element in the Java EE namespace");
      
      
   }
   
   private void checkMethodChild(Element beanChildElement, AnnotatedClass<?> beanClass)
   {
      //TODO: not finished
   }
}
