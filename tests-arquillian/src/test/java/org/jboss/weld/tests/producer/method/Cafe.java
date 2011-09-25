package org.jboss.weld.tests.producer.method;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;

public class Cafe {

    @Produces
    @Compostable
    public Food getSalad(Instance<Food> food) {
        Food salad = food.get();
        salad.make();
        return salad;
    }

}
