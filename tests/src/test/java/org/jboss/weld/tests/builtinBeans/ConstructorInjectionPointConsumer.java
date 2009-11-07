package org.jboss.weld.tests.builtinBeans;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;

@SessionScoped
public class ConstructorInjectionPointConsumer implements Serializable
{
   
   public ConstructorInjectionPointConsumer() {}
   
   @Inject
   public ConstructorInjectionPointConsumer(Dog dog) {}
   
   public void ping() {}

}
