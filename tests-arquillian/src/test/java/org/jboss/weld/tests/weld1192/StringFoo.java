package org.jboss.weld.tests.weld1192;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Typed;

@RequestScoped
@Typed(value = StringFoo.class)
public class StringFoo extends Foo<String> {

    public StringFoo() {
        super("foo");
    }
}
