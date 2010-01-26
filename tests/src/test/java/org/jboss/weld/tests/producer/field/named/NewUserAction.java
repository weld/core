package org.jboss.weld.tests.producer.field.named;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;

@RequestScoped
@Named
public class NewUserAction
{

   @Produces
   @Named("newUser")
   private User newUser = new User();

   public void persist()
   {
      System.out.println("new user's name: " + newUser.getName());
   }
}