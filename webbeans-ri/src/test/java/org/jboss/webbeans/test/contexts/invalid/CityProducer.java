package org.jboss.webbeans.test.contexts.invalid;

import javax.webbeans.Dependent;
import javax.webbeans.Produces;

public class CityProducer
{
   @Produces @Dependent public Violation reference = new Violation();
}
