package org.jboss.weld.osgi.examples.userdoc.helloworld.provider.impl;

import org.jboss.weld.environment.osgi.api.annotation.Publish;
import org.jboss.weld.osgi.examples.userdoc.helloworld.provider.api.HelloWorld;
import org.jboss.weld.osgi.examples.userdoc.helloworld.provider.api.Language;
import org.jboss.weld.osgi.examples.userdoc.helloworld.provider.api.Presentation;

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
