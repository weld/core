package org.jboss.weld.tests.arrays;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;

/**
 *
 */
@Dependent
public class ArrayProducer {

    public static final Bar<Integer> INTEGER_BAR = new Bar<Integer>(Integer.MAX_VALUE);
    public static final Bar<Integer>[] INTEGER_BAR_ARRAY = new Bar[]{new Bar<Integer>(1)};
    public static final Bar<String> STRING_BAR = new Bar<String>("string");
    public static final Bar<String>[] STRING_BAR_ARRAY = new Bar[]{new Bar<String>("string")};

    @Produces
    public int[] produceIntArray() {
        return new int[] {1, 2, 3};
    }

    @Produces
    public Foo[] produceFooArray() {
        return new Foo[0];
    }

    @Produces
    public Bar<String> produceStringBar() {
        return STRING_BAR;
    }

    @Produces
    public Bar<Integer> produceIntegerBar() {
        return INTEGER_BAR;
    }

    @Produces
    public Bar<String>[] produceStringBarArray() {
        return STRING_BAR_ARRAY;
    }

    @Produces
    public Bar<Integer>[] produceIntegerBarArray() {
        return INTEGER_BAR_ARRAY;
    }

}
