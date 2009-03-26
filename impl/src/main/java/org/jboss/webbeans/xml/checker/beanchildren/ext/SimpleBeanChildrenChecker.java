package org.jboss.webbeans.xml.checker.beanchildren.ext;

import java.util.Map;
import java.util.Set;

import javax.inject.DefinitionException;

import org.dom4j.Element;
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
}
