package org.jboss.weld.tests.producer.method;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

@RequestScoped
public class BarConsumer
{
   @Inject
   private Bar bar;

   public Bar getBar()
   {
      return bar;
   }
}
