package org.jboss.weld.tests.producer.method;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;

@Dependent
public class Kitchen {

    private static Food compostedFood;

    public static Food getCompostedFood() {
        return compostedFood;
    }

    public static void reset() {
        compostedFood = null;
    }

    @Produces
    public Food getFood() {
        return new Food("biodegradeable");
    }

    public void compost(@Disposes Food food) {
        compostedFood = food;
    }

}
