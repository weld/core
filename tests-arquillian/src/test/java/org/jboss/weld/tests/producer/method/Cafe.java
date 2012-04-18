package org.jboss.weld.tests.producer.method;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

public class Cafe {

    @Inject
    private Instance<Food> food;

    public Food getSalad() {
        Food salad = food.get();
        salad.make();
        return salad;
    }

}
