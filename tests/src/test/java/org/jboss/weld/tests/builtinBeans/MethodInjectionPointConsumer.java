package org.jboss.weld.tests.builtinBeans;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;

@SessionScoped
public class MethodInjectionPointConsumer implements Serializable
{
   
   @Inject
   public void setDog(Dog dog) {}
   
   public void ping() {}

}
