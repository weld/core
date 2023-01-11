package org.jboss.weld.tests.assignability.recursiveGenerics;

public class MutualRecursion {
    interface Graph<G extends Graph<G, E, N>, E extends Edge<G, E, N>, N extends Node<G, E, N>> {
    }

    interface Edge<G extends Graph<G, E, N>, E extends Edge<G, E, N>, N extends Node<G, E, N>> {
    }

    interface Node<G extends Graph<G, E, N>, E extends Edge<G, E, N>, N extends Node<G, E, N>> {
    }

    static class Map implements Graph<Map, Route, City> {
    }

    static class Route implements Edge<Map, Route, City> {
    }

    static class City implements Node<Map, Route, City> {
    }
}
