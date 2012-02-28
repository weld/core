package org.jboss.weld.tests.alternatives.weld930;

/**
 * @author Marko Luksa
 */
public class Product {
    private String name;

    public Product() {
    }

    public Product(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
