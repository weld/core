package org.jboss.weld.tests.builtinBeans;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;

@SessionScoped
public class FieldInjectionPointConsumer implements Serializable
{
   
   @Inject Dog dogField;
   
   public void ping() {}

}
