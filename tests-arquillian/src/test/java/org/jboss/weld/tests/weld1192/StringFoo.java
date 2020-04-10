package org.jboss.weld.tests.weld1192;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Typed;

@RequestScoped
@Typed(value = StringFoo.class)
public class StringFoo extends Foo<String> {

    public StringFoo() {
        super("foo");
    }
}
