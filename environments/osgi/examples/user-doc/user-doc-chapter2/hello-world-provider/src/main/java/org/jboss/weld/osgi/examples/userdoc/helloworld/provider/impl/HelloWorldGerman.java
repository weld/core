package org.jboss.weld.osgi.examples.userdoc.helloworld.provider.impl;

import org.jboss.weld.environment.osgi.api.annotation.Publish;
import org.jboss.weld.osgi.examples.userdoc.helloworld.provider.api.HelloWorld;
import org.jboss.weld.osgi.examples.userdoc.helloworld.provider.api.Language;
import org.jboss.weld.osgi.examples.userdoc.helloworld.provider.api.Presentation;

@Language("GERMAN")
@Publish
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
