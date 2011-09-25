package org.jboss.weld.tests.builtinBeans.ee;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Foo {

    @Id
    @GeneratedValue
    private int id;

}
