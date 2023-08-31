package org.jboss.weld.tests.invokable.transformers.output;


/**
 * Offers various transformation methods
 */
public class Transformer {

    // return type transformers
    // same return value, input value is superclass
    public static Beta transformReturn1(Alpha alpha) {
        return new Beta(42, 42);
    }

    // return value is completely different, input value is identical
    public static String transformReturn2(Beta beta) {
        return beta.ping() + beta.getInteger();
    }

    // exception transformers
    // same return type
    public static Beta transformException1(Throwable t) {
        return new Beta(42, 42);
    }

    // return type is a subclass
    // note that exception transformers (via method handles) cannot have completely different ret. types
    public static Gamma transformException2(Throwable t) {
        return new Gamma(42, 42);
    }

    // return type is completely different
    public static String transformException3(Throwable t) {
        return "foobar";
    }
}
