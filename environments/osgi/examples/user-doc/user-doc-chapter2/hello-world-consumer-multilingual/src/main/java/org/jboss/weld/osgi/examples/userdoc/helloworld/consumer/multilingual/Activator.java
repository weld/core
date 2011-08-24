package org.jboss.weld.osgi.examples.userdoc.helloworld.consumer.multilingual;

import org.jboss.weld.osgi.examples.userdoc.helloworld.provider.api.HelloWorld;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class Activator implements BundleActivator {

    HelloWorld helloWorldEnglish;
    HelloWorld helloWorldFrench;
    HelloWorld helloWorldGerman;

    @Override
    public void start(BundleContext context) throws Exception {
        ServiceReference helloWorldEnglishReference = context.getServiceReferences(HelloWorld.class.getName(),"(language.value=ENGLISH)")[0];
        ServiceReference helloWorldFrenchReference = context.getServiceReferences(HelloWorld.class.getName(),"(language.value=FRENCH)")[0];
        ServiceReference helloWorldGermanReference = context.getServiceReferences(HelloWorld.class.getName(),"(language.value=GERMAN)")[0];

        helloWorldEnglish = (HelloWorld)context.getService(helloWorldEnglishReference);
        helloWorldFrench = (HelloWorld)context.getService(helloWorldFrenchReference);
        helloWorldGerman = (HelloWorld)context.getService(helloWorldGermanReference);

        helloWorldEnglish.sayHello();
        helloWorldFrench.sayHello();
        helloWorldGerman.sayHello();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        helloWorldEnglish.sayGoodbye();
        helloWorldFrench.sayGoodbye();
        helloWorldGerman.sayGoodbye();
    }
}
