package org.jboss.weld.tests.producer.method.parameterized;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class TestBean
{
   @Inject
   @Parameterized
   Parameterized1<Parameterized2<Double>> parameterized;
   
}
