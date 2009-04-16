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

public class SimpleBeanChildrenChecker extends AbstractBeanChildrenChecker
{
   public SimpleBeanChildrenChecker(XmlEnvironment environment, Map<String, Set<String>> packagesMap)
   {
      super(environment, packagesMap);
   }

   protected void checkForDecoratorChild(Element beanElement)
   {
      if(ParseXmlHelper.findElementsInEeNamespace(beanElement, XmlConstants.INTERCEPTOR).size() > 1)
         throw new DefinitionException("A simple bean element <" + beanElement.getName() + "> has more than one direct child <" + 
               XmlConstants.INTERCEPTOR + ">");            
   }
   
   protected void checkForInterceptorChild(Element beanElement)
   {
      if(ParseXmlHelper.findElementsInEeNamespace(beanElement, XmlConstants.DECORATOR).size() > 1)
         throw new DefinitionException("A simple bean element <" + beanElement.getName() + "> has more than one direct child <" + 
               XmlConstants.DECORATOR + ">");
   }
   
   protected void checkChildForInterceptorType(Element beanChildElement)
   {
      if(haveBeanInterceptorDeclaration)
         throw new DefinitionException("There is second element of interceptor type <" + beanChildElement.getName() + 
               "> in bean '" + beanChildElement.getParent().getName() + "'");
         haveBeanInterceptorDeclaration = true;
   }
   
   protected void checkChildForDecoratorType(Element beanChildElement)
   {
      if(haveBeanDecoratorDeclaration)
         throw new DefinitionException("There is second element of decorator type <" + beanChildElement.getName() + 
               "> in bean '" + beanChildElement.getParent().getName() + "'");
         haveBeanDecoratorDeclaration = true;
   }
   
   protected void checkForConstructor(Element beanElement, AnnotatedClass<?> beanClass)
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
   
   protected void checkRIBean(Element beanElement, AnnotatedClass<?> beanClass){
      throw new DefinitionException("It is impossible determine some kind of resource in not Resource Bean");
   }
}
