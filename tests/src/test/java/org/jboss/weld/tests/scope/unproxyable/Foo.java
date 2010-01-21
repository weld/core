package org.jboss.weld.tests.scope.unproxyable;

import java.io.Serializable;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

@SessionScoped
public class Foo implements Serializable
{

   @Inject
   @HttpParam("username")
   String username;

   @Produces
   @RequestScoped
   @HttpParam("")
   public String produceHttpParam(InjectionPoint ip)
   {
      return "pete";
   }

}