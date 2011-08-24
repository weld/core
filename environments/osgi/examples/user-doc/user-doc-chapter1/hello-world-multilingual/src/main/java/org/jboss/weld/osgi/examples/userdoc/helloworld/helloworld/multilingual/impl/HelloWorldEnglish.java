package org.jboss.weld.osgi.examples.userdoc.helloworld.helloworld.multilingual.impl;

import org.jboss.weld.osgi.examples.userdoc.helloworld.helloworld.multilingual.api.HelloWorld;
import org.jboss.weld.osgi.examples.userdoc.helloworld.helloworld.multilingual.api.Language;
import org.jboss.weld.osgi.examples.userdoc.helloworld.helloworld.multilingual.api.Presentation;

@Language("ENGLISH")
public class HelloWorldEnglish implements HelloWorld {

    @Override @Presentation
    public void sayHello() {
        System.out.println("Hello World!");
    }

    @Override
    public void sayGoodbye() {
        System.out.println("Goodbye World!");
    }
}
