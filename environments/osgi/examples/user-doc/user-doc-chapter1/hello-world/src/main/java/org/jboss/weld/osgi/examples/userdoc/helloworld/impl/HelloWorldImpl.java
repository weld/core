package org.jboss.weld.osgi.examples.userdoc.helloworld.impl;

import org.jboss.weld.osgi.examples.userdoc.helloworld.api.HelloWorld;

public class HelloWorldImpl implements HelloWorld {

    @Override
    public void sayHello() {
        System.out.println("Hello World!");
    }

    @Override
    public void sayGoodbye() {
        System.out.println("Goodbye World!");
    }
}
