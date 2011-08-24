package org.jboss.weld.osgi.examples.userdoc.helloworld.helloworld.multilingual.impl;

import org.jboss.weld.environment.osgi.api.annotation.Publish;
import org.jboss.weld.osgi.examples.userdoc.helloworld.helloworld.multilingual.api.HelloWorld;
import org.jboss.weld.osgi.examples.userdoc.helloworld.helloworld.multilingual.api.Language;
import org.jboss.weld.osgi.examples.userdoc.helloworld.helloworld.multilingual.api.Presentation;

@Language("FRENCH")
@Publish
public class HelloWorldFrench implements HelloWorld {

    @Override @Presentation
    public void sayHello() {
        System.out.println("Bonjour le Monde !");
    }

    @Override
    public void sayGoodbye() {
        System.out.println("Au revoir le Monde !");
    }
}
