package org.jboss.weld.tests.interceptors.exceptions;

import org.jboss.weld.exceptions.WeldException;

/**
 *
 */
@FooBinding
public class Foo {

    public void throwCheckedException() throws FooCheckedException {
        throw new FooCheckedException();
    }

    public void throwUncheckedException() throws FooUncheckedException {
        throw new FooUncheckedException();
    }

}
