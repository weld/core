package org.jboss.weld.tests.invokable;

// non-bean intentionally
public class ExceptionTransformer {

    public static String transform(Throwable t) {
        if (t instanceof IllegalArgumentException) {
            return IllegalArgumentException.class.getSimpleName();
        } else if (t instanceof IllegalStateException) {
            return IllegalStateException.class.getSimpleName();
        }
        return ExceptionTransformer.class.getSimpleName();
    }
}
