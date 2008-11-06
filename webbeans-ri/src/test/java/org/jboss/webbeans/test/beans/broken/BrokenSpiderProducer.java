package org.jboss.webbeans.test.beans.broken;

import javax.webbeans.Destructor;
import javax.webbeans.Disposes;
import javax.webbeans.Observes;
import javax.webbeans.Produces;
import javax.webbeans.Production;
import javax.webbeans.RequestScoped;

import org.jboss.webbeans.test.beans.Spider;

@Production
public class BrokenSpiderProducer
{

   @Produces
   public String observe(@Observes String foo)
   {
      return "foo";
   }
   
   @Produces
   public String dispose(@Disposes String foo)
   {
      return "foo";
   }
   
   @Produces @Destructor
   public String destroy()
   {
      return "foo";
   }
   
   @Produces @RequestScoped public Spider getRequestScopedSpider()
   {
      return null;
   }
   
}
