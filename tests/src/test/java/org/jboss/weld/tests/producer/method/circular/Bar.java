package org.jboss.weld.tests.producer.method.circular;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

class Bar {

    @Inject void injectFoo(Foo foo) {}
    @Produces Foo produceFoo() { return new Foo() {}; }

}