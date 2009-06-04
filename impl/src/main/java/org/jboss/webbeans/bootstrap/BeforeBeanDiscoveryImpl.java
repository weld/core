package org.jboss.webbeans.bootstrap;

import java.lang.annotation.Annotation;

import javax.enterprise.inject.spi.BeforeBeanDiscovery;

public class BeforeBeanDiscoveryImpl implements BeforeBeanDiscovery
{

   public void addBindingType(Class<? extends Annotation> bindingType)
   {
      throw new RuntimeException("Not Yet Implemented");

   }

   public void addInterceptorBindingType(Class<? extends Annotation> bindingType)
   {
      throw new RuntimeException("Not Yet Implemented");
   }

   public void addScopeType(Class<? extends Annotation> scopeType,
         boolean normal, boolean passivating)
   {
      throw new RuntimeException("Not Yet Implemented");
   }

   public void addStereotype(Class<? extends Annotation> stereotype,
         Annotation... stereotypeDef)
   {
      throw new RuntimeException("Not Yet Implemented");
   }

}
