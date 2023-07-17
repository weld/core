package org.jboss.weld.tests.invokable.transformers.input;

import java.math.BigDecimal;

/**
 * Offers various transformation methods
 */
public class Transformer {

    // arg transformers
    // different input, same output
    public static Number transformArg1(String s) {
        return Integer.valueOf(s);
    }

    // different input, output is a subclass
    public static BigDecimal transformArg2(String s) {
        return BigDecimal.valueOf(Long.valueOf(s));
    }

    // instance transformers
    // different input, same output
    public static ActualBean transformInstance1(Integer i) {
        if (i != null) {
            throw new IllegalArgumentException("Should never happen as nothing but null passes for an Integer");
        }
        return new ActualBean(100);
    }

    // different input, output is a subclass
    public static AugmentedBean transformInstance2(Integer i) {
        if (i != null) {
            throw new IllegalArgumentException("Should never happen as nothing but null passes for an Integer");
        }
        return new AugmentedBean(100);
    }
}
