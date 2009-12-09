package org.jboss.weld.tests.resolution.circular.resource;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class Baz
{

   @Produces @FooDB @PersistenceContext private EntityManager db;
   @Inject @FooDB EntityManager fooDb; 
   
   public EntityManager getFooDb()
   {
      return fooDb;
   }
   
}
