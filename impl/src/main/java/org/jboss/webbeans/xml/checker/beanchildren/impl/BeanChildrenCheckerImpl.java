package org.jboss.webbeans.xml.checker.beanchildren.impl;

import java.util.Map;
import java.util.Set;

import org.jboss.webbeans.xml.XmlEnvironment;
import org.jboss.webbeans.xml.checker.beanchildren.BeanChildrenChecker;

public abstract class BeanChildrenCheckerImpl implements BeanChildrenChecker
{
   protected final XmlEnvironment environment;

   protected final Map<String, Set<String>> packagesMap;

   public BeanChildrenCheckerImpl(XmlEnvironment environment, Map<String, Set<String>> packagesMap)
   {
      this.environment = environment;
      this.packagesMap = packagesMap;
   }

   public XmlEnvironment getXmlEnvironment()
   {
      return this.environment;
   }

   public Map<String, Set<String>> getPackagesMap()
   {
      return this.packagesMap;
   }
}
