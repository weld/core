package org.jboss.webbeans.xml.check;

import javax.decorator.Decorator;
import javax.inject.DefinitionException;
import javax.interceptor.Interceptor;

import org.dom4j.Element;
import org.jboss.webbeans.CurrentManager;
import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.xml.ParseXmlHelper;
import org.jboss.webbeans.xml.XmlConstants;

public class SimpleBeanTypeObtainer implements BeanTypeObtainer
{

   public boolean accept(Element beanElement, AnnotatedClass<?> beanClass)
   {
      ManagerImpl manager = CurrentManager.rootManager();
      boolean isSessionBean = manager.getEjbDescriptorCache().containsKey(beanElement.getName()) || 
                                          beanElement.attribute(XmlConstants.EJB_NAME) != null;
      
      if (!beanClass.isAbstract() && !isSessionBean && !beanClass.isParameterizedType())
         return true;

      return false;
   }

   public BeanType obtainType(Element beanElement, AnnotatedClass<?> beanClass)
   {
      if(beanClass.isNonStaticMemberClass())
         throw new DefinitionException("Bean class '" + beanClass.getName() + "' of a simple bean <" + beanElement.getName() + 
               "> is a non static member class");
      
      if(beanClass.getRawType().isAnnotationPresent(Interceptor.class) && 
            ParseXmlHelper.findElementsInEeNamespace(beanElement, XmlConstants.INTERCEPTOR).size() != 1)
         throw new DefinitionException("a simple bean defined in XML as <" + beanElement.getName() +  "> has a bean class '" + 
               beanClass.getName() + "' annotated @Interceptor and is not declared as an interceptor in XML");
      
      if(beanClass.getRawType().isAnnotationPresent(Decorator.class) && 
            ParseXmlHelper.findElementsInEeNamespace(beanElement, XmlConstants.DECORATOR).size() != 1)
         throw new DefinitionException("a simple bean defined in XML as <" + beanElement.getName() +  "> has a bean class '" + 
               beanClass.getName() + "' annotated @Decorator and is not declared as an interceptor in XML");
      
      return BeanType.SIMPLE_BEAN;
   }
}
