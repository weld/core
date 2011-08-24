package org.jboss.weld.osgi.examples.userdoc.helloworld.consumer;

import org.jboss.weld.osgi.examples.userdoc.helloworld.provider.api.HelloWorld;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class Activator implements BundleActivator {

    HelloWorld helloWorld;

    @Override
    public void start(BundleContext context) throws Exception {
        ServiceReference helloWorldReference = context.getServiceReference(HelloWorld.class.getName());
        helloWorld = (HelloWorld)context.getService(helloWorldReference);
        helloWorld.sayHello();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        helloWorld.sayGoodbye();
    }
}
