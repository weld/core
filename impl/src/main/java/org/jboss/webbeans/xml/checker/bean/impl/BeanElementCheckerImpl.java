package org.jboss.webbeans.xml.checker.bean.impl;

import org.dom4j.Element;
import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.xml.checker.bean.BeanElementChecker;
import org.jboss.webbeans.xml.checker.beanchildren.BeanChildrenChecker;


public abstract class BeanElementCheckerImpl implements BeanElementChecker
{
   private final BeanChildrenChecker childrenChecker;
   
   public BeanElementCheckerImpl(BeanChildrenChecker childrenChecker)
   {
      this.childrenChecker = childrenChecker;
   }
   
   public abstract boolean accept(Element beanElement, AnnotatedClass<?> beanClass);

   public abstract void checkElementDeclaration(Element beanElement, AnnotatedClass<?> beanClass);
   
   public void checkBeanElement(Element beanElement, AnnotatedClass<?> beanClass)
   {
      checkElementDeclaration(beanElement, beanClass);
      childrenChecker.checkChildren(beanElement, beanClass);
   }
}
