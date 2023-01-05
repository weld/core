package org.jboss.weld.tests.assignability.recursiveGenerics;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;

import java.util.ArrayList;
import java.util.List;

@Dependent
public class RecursiveGenericProducer {

    @Produces
    @Dependent
    <T extends Comparable<T>> List<T> produce() {
        return new ArrayList<T>();
    }

}
