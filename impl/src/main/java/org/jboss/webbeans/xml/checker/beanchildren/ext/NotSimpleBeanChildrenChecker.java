package org.jboss.webbeans.xml.checker.beanchildren.ext;

import java.util.Map;
import java.util.Set;

import javax.inject.DefinitionException;

import org.dom4j.Element;
import org.jboss.webbeans.xml.ParseXmlHelper;
import org.jboss.webbeans.xml.XmlConstants;
import org.jboss.webbeans.xml.XmlEnvironment;
import org.jboss.webbeans.xml.checker.beanchildren.impl.BeanChildrenCheckerImpl;

public class NotSimpleBeanChildrenChecker extends BeanChildrenCheckerImpl
{
   public NotSimpleBeanChildrenChecker(XmlEnvironment environment, Map<String, Set<String>> packagesMap)
   {
      super(environment, packagesMap);
   }

   public void checkForInterceptorChild(Element beanElement)
   {
      if(ParseXmlHelper.findElementsInEeNamespace(beanElement, XmlConstants.INTERCEPTOR).size() > 1)
         throw new DefinitionException("Not a simple bean '" + beanElement.getName() + "' contains direct child <" + 
               XmlConstants.INTERCEPTOR + ">");            
   }
   
   public void checkForDecoratorChild(Element beanElement)
   {
      if(ParseXmlHelper.findElementsInEeNamespace(beanElement, XmlConstants.DECORATOR).size() > 1)
         throw new DefinitionException("Not a simple bean '" + beanElement.getName() + "' contains direct child <" + 
               XmlConstants.DECORATOR + ">");
   }
   
   public void checkChildForInterceptorType(Element beanChildElement)
   {
      throw new DefinitionException("Declaration of not a simple bean '" + beanChildElement.getParent().getName() + 
         "' contains a child <" + beanChildElement.getName() + "> which type is javax.interceptor.Interceptor");
   }
   
   public void checkChildForDecoratorType(Element beanChildElement)
   {
      throw new DefinitionException("Declaration of not a simple bean '" + beanChildElement.getParent().getName() + 
         "' contains a child <" + beanChildElement.getName() + "> which type is javax.decorator.Decorator");
   }
}
