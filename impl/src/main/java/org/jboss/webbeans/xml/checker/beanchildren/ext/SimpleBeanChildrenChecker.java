package org.jboss.webbeans.xml.checker.beanchildren.ext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.DefinitionException;

import org.dom4j.Element;
import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.introspector.AnnotatedConstructor;
import org.jboss.webbeans.introspector.AnnotatedParameter;
import org.jboss.webbeans.xml.ParseXmlHelper;
import org.jboss.webbeans.xml.XmlConstants;
import org.jboss.webbeans.xml.XmlEnvironment;
import org.jboss.webbeans.xml.checker.beanchildren.impl.BeanChildrenCheckerImpl;

public class SimpleBeanChildrenChecker extends BeanChildrenCheckerImpl
{
   public SimpleBeanChildrenChecker(XmlEnvironment environment, Map<String, Set<String>> packagesMap)
   {
      super(environment, packagesMap);
   }

   public void checkForDecoratorChild(Element beanElement)
   {
      if(ParseXmlHelper.findElementsInEeNamespace(beanElement, XmlConstants.INTERCEPTOR).size() > 1)
         throw new DefinitionException("A simple bean element <" + beanElement.getName() + "> has more than one direct child <" + 
               XmlConstants.INTERCEPTOR + ">");            
   }
   
   public void checkForInterceptorChild(Element beanElement)
   {
      if(ParseXmlHelper.findElementsInEeNamespace(beanElement, XmlConstants.DECORATOR).size() > 1)
         throw new DefinitionException("A simple bean element <" + beanElement.getName() + "> has more than one direct child <" + 
               XmlConstants.DECORATOR + ">");
   }
   
   public void checkChildForInterceptorType(Element beanChildElement)
   {
      if(haveBeanInterceptorDeclaration)
         throw new DefinitionException("There is second element of interceptor type <" + beanChildElement.getName() + 
               "> in bean '" + beanChildElement.getParent().getName() + "'");
         haveBeanInterceptorDeclaration = true;
   }
   
   public void checkChildForDecoratorType(Element beanChildElement)
   {
      if(haveBeanDecoratorDeclaration)
         throw new DefinitionException("There is second element of decorator type <" + beanChildElement.getName() + 
               "> in bean '" + beanChildElement.getParent().getName() + "'");
         haveBeanDecoratorDeclaration = true;
   }
   
   public void checkForConstructor(Element beanElement, AnnotatedClass<?> beanClass)
   {
      if(constructorParameters.size() == 0)
         return;
      
      List<AnnotatedConstructor<?>> matchableConstructors = new ArrayList<AnnotatedConstructor<?>>(); 
      
      for(AnnotatedConstructor<?> constructor : beanClass.getConstructors())
      {
         List<? extends AnnotatedParameter<?>>  parameters = constructor.getParameters();
         
         if(parameters.size() != constructorParameters.size())
            continue;
         
         boolean isMacthable = true;
         
         for(int i = 0; i < parameters.size(); i++)
         {
            if(!parameters.get(i).isAssignableFrom(constructorParameters.get(i)))
            {
               isMacthable = false;
               break;
            }            
         }
         
         if(isMacthable)
            matchableConstructors.add(constructor);
      }
      
      if(matchableConstructors.size() == 0)
         throw new DefinitionException("There is no constructor of the simple bean '" + beanElement.getName() + 
               "' with the same number and types of parameters as declared");
      
      if(matchableConstructors.size() > 1)
         throw new DefinitionException("There is more than one constructor of the simple bean '" + beanElement.getName() + 
               "' with the same number and types of parameters as declared");
   }
}
