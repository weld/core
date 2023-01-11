package org.jboss.weld.tests.assignability.recursiveGenerics;

public class DuplicateRecursion {
    interface FooBar<T extends FooBar<?, U>, U extends Comparable<U>> {
    }

    static class FooBarImpl implements FooBar<FooBarImpl, String> {
    }
}
