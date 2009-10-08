package org.jboss.weld.test.unit.cluster;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;

@SessionScoped
public class Stable implements Serializable
{
   
   /**
    * 
    */
   private static final long serialVersionUID = 4007799511309218679L;

   @Inject
   private Horse horse;
   
   @Inject
   private Fodder fodder;
   
   public Horse getHorse()
   {
      return horse;
   }
   
   public Fodder getFodder()
   {
      return fodder;
   }

}
