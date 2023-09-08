package org.jboss.weld.invokable;

class SneakyThrow {
    private SneakyThrow() {
    }

    /**
     * This method can and should be used as part of a {@code throw} statement,
     * such as: {@code throw sneakyThrow(exception);}. It is guaranteed to never return normally,
     * and this style of usage makes sure that the Java compiler is aware of that.
     */
    @SuppressWarnings("unchecked")
    static <E extends Throwable> RuntimeException sneakyThrow(Throwable e) throws E {
        throw (E) e;
    }
}
