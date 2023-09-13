package org.jboss.weld.tests.assignability.recursiveGenerics;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;

import org.jboss.weld.tests.assignability.recursiveGenerics.DuplicateRecursion.FooBar;
import org.jboss.weld.tests.assignability.recursiveGenerics.MutualRecursion.Edge;
import org.jboss.weld.tests.assignability.recursiveGenerics.MutualRecursion.Graph;
import org.jboss.weld.tests.assignability.recursiveGenerics.MutualRecursion.Node;

@Dependent
public class RecursiveGenericProducer {

    @Produces
    @Dependent
    <T extends Comparable<T>> List<T> produce() {
        return new ArrayList<T>();
    }

    @Produces
    @Dependent
    <T extends FooBar<T, U>, U extends Comparable<U>> FooBar<T, U> produceFooBar() {
        return null;
    }

    @Produces
    @Dependent
    <G extends Graph<G, E, N>, E extends Edge<G, E, N>, N extends Node<G, E, N>> Graph<G, E, N> produceGraph() {
        return null;
    }
}
