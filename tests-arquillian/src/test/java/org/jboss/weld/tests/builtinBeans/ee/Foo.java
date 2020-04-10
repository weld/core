package org.jboss.weld.tests.builtinBeans.ee;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class Foo {

    @Id
    @GeneratedValue
    private int id;

}
