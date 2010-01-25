package org.jboss.weld.tests.enterprise;

import javax.ejb.Stateful;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;

@Stateful
@RequestScoped
public class Capercaillie implements Scottish, Bird
{

   private Feed feed;
   
   public void observe(@Observes Feed feed)
   {
      this.feed = feed;
   }
   
   public Feed getFeed()
   {
      return feed;
   }
   
}
