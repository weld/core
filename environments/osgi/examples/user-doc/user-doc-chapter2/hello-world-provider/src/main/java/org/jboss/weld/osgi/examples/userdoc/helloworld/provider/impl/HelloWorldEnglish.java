package org.jboss.weld.osgi.examples.userdoc.helloworld.provider.impl;

import org.jboss.weld.environment.osgi.api.annotation.Publish;
import org.jboss.weld.osgi.examples.userdoc.helloworld.provider.api.HelloWorld;
import org.jboss.weld.osgi.examples.userdoc.helloworld.provider.api.Language;
import org.jboss.weld.osgi.examples.userdoc.helloworld.provider.api.Presentation;

@Language("ENGLISH")
@Publish
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
