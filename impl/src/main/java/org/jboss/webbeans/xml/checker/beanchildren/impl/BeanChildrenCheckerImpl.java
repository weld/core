package org.jboss.webbeans.xml.checker.beanchildren.impl;

import java.util.Iterator;
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
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import javax.xml.ws.WebServiceRef;

import org.dom4j.Element;
import org.dom4j.Namespace;
import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.xml.ParseXmlHelper;
import org.jboss.webbeans.xml.XmlConstants;
import org.jboss.webbeans.xml.XmlEnvironment;
import org.jboss.webbeans.xml.checker.beanchildren.BeanChildrenChecker;

public abstract class BeanChildrenCheckerImpl implements BeanChildrenChecker
{
   private final XmlEnvironment environment;
   
   private final Map<String, Set<String>> packagesMap;
   
   private boolean haveBeanDeploymentTypeDeclaration = false;
   
   private boolean haveBeanScopeTypeDeclaration = false;
   
   protected boolean haveBeanInterceptorDeclaration = false;
   
   protected boolean haveBeanDecoratorDeclaration = false;
         
   public abstract void checkForInterceptorChild(Element beanElement);
   
   public abstract void checkForDecoratorChild(Element beanElement);
   
   public abstract void checkChildForInterceptorType(Element beanElement);
   
   public abstract void checkChildForDecoratorType(Element beanElement);
   
   public BeanChildrenCheckerImpl(XmlEnvironment environment, Map<String, Set<String>> packagesMap)
   {
      this.environment = environment;
      this.packagesMap = packagesMap;
   }
   
   public void checkChildren(Element beanElement, AnnotatedClass<?> beanClass)
   {
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
         checkBeanChild(beanChildElement);
      }
   }
   
   private void checkBeanChild(Element beanChildElement)
   {
      //TODO: not finished
      try
      {
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
            //TODO: bean child element declaring a parameter of the bean constructor
            return;
         }
         throw new DefinitionException();//bean child element can't be interpreted as a Java class or interface or Java Annotation type
      }
      catch(DefinitionException e)
      {
         Element beanElement = beanChildElement.getParent();
         Namespace beanNamespace = beanElement.getNamespace();
         Namespace beanChildNamespace = beanChildElement.getNamespace();
         if(beanChildNamespace.equals(beanNamespace))
         {
            //TODO: bean child element declaring a method or field of the bean.
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
      //TODO: add interceptor binding type
      if(beanChildClass.isAnnotationPresent(BindingType.class) || beanChildClass.isAnnotationPresent(Stereotype.class) || 
            beanChildClass.isAnnotationPresent(Named.class) || beanChildClass.isAnnotationPresent(Specializes.class) ||  
            beanChildClass.isAnnotationPresent(Realizes.class) || beanChildClass.isAnnotationPresent(Resource.class) || 
            beanChildClass.isAnnotationPresent(EJB.class) || beanChildClass.isAnnotationPresent(WebServiceRef.class) || 
            beanChildClass.isAnnotationPresent(PersistenceContext.class) || beanChildClass.isAnnotationPresent(PersistenceUnit.class))
         return;
      
      throw new DefinitionException("Can't determine annotation type of <" + beanChildElement.getName() + "> element in bean '" + 
            beanChildElement.getParent().getName() + "'");
   }

}
