package org.jboss.webbeans.test.unit.implementation.producer.method;

import java.util.Date;

import javax.enterprise.inject.Named;
import javax.enterprise.inject.Produces;

/**
 * @author Dan Allen
 */
public class NamedProducerWithBinding {
   public @Produces @Important @Named Date getDate() {
      return new Date();
   }
}
