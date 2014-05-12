package org.jboss.weld.tests.beanDeployment.noclassdeffound;

public class FooBarProducer {
    
    public GenericBar<Foo> produce() {
        return new GenericBar<Foo>();
    }
}
