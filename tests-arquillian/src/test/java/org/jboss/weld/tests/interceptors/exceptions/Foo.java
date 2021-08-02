package org.jboss.weld.tests.interceptors.exceptions;

import jakarta.enterprise.context.Dependent;

/**
 *
 */
@FooBinding
@Dependent
public class Foo {

    public void throwCheckedException() throws FooCheckedException {
        throw new FooCheckedException();
    }

    public void throwUncheckedException() throws FooUncheckedException {
        throw new FooUncheckedException();
    }

}
