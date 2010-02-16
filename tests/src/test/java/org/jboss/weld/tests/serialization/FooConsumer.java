package org.jboss.weld.tests.serialization;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;

@SessionScoped
public class FooConsumer implements Serializable
{
   
   @Inject Foo foo;
   
   public void ping()
   {
      
   }

}
