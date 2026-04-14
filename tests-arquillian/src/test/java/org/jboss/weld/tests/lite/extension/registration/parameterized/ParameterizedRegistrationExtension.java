package org.jboss.weld.tests.lite.extension.registration.parameterized;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.enterprise.inject.build.compatible.spi.BeanInfo;
import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.build.compatible.spi.Messages;
import jakarta.enterprise.inject.build.compatible.spi.ObserverInfo;
import jakarta.enterprise.inject.build.compatible.spi.Registration;
import jakarta.enterprise.inject.build.compatible.spi.Validation;
import jakarta.enterprise.util.TypeLiteral;

public class ParameterizedRegistrationExtension implements BuildCompatibleExtension {

    private final AtomicInteger genericStringBeanCounter = new AtomicInteger();
    private final AtomicInteger rawBeanCounter = new AtomicInteger();
    private final AtomicInteger collectionStringObserverCounter = new AtomicInteger();
    private final AtomicInteger listStringObserverCounter = new AtomicInteger();

    // Match beans whose types include MyGenericService<String>
    @Registration(types = MyGenericServiceOfString.class)
    public void genericStringBeans(BeanInfo bean) {
        genericStringBeanCounter.incrementAndGet();
    }

    // Match beans whose types include MyGenericService (raw type)
    @Registration(types = MyGenericService.class)
    public void rawBeans(BeanInfo bean) {
        rawBeanCounter.incrementAndGet();
    }

    // Match observers whose observed type is assignable to Collection<String>
    @Registration(types = CollectionOfString.class)
    public void collectionStringObservers(ObserverInfo observer) {
        collectionStringObserverCounter.incrementAndGet();
    }

    // Match observers whose observed type is assignable to List<String>
    @Registration(types = ListOfString.class)
    public void listStringObservers(ObserverInfo observer) {
        listStringObserverCounter.incrementAndGet();
    }

    @Validation
    public void validate(Messages msg) {
        // MyStringService and ObservingBean both implement MyGenericService<String>
        if (genericStringBeanCounter.get() != 2) {
            msg.error("Expected 2 beans with type MyGenericService<String>, got " + genericStringBeanCounter.get());
        }
        // MyStringService, MyIntegerService, and ObservingBean all implement MyGenericService
        if (rawBeanCounter.get() != 3) {
            msg.error("Expected 3 beans with raw type MyGenericService, got " + rawBeanCounter.get());
        }
        // ObservingBean.observeListOfString matches both Collection<String> and List<String>
        // List<String> is assignable to Collection<String>
        if (collectionStringObserverCounter.get() != 1) {
            msg.error("Expected 1 observer assignable to Collection<String>, got "
                    + collectionStringObserverCounter.get());
        }
        if (listStringObserverCounter.get() != 1) {
            msg.error("Expected 1 observer assignable to List<String>, got " + listStringObserverCounter.get());
        }
    }

    static class MyGenericServiceOfString extends TypeLiteral<MyGenericService<String>> {
    }

    static class CollectionOfString extends TypeLiteral<Collection<String>> {
    }

    static class ListOfString extends TypeLiteral<List<String>> {
    }
}
