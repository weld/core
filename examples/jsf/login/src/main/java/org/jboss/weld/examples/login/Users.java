package org.jboss.weld.examples.login;

import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class Users
{
   
   @PersistenceContext
   private EntityManager userDatabase;
   
   @SuppressWarnings("unchecked")
   @Produces @Named @RequestScoped
   public List<User> getUsers()
   {
      return userDatabase.createQuery("select u from User u").getResultList();
   }

}
