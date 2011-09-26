package org.jboss.weld.osgi.examples.userdoc.helloworld.helloworld.multilingual.impl;

import org.jboss.weld.osgi.examples.userdoc.helloworld.helloworld.multilingual.api.HelloWorld;
import org.jboss.weld.osgi.examples.userdoc.helloworld.helloworld.multilingual.api.Language;
import org.jboss.weld.osgi.examples.userdoc.helloworld.helloworld.multilingual.api.Presentation;

@Language("GERMAN")
public class HelloWorldGerman implements HelloWorld {

    @Override @Presentation
    public void sayHello() {
        System.out.println("Hallo Welt!");
    }

    @Override
    public void sayGoodbye() {
        System.out.println("Auf Wiedersehen Welt!");
    }
}
