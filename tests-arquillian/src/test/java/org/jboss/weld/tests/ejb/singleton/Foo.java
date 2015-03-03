package org.jboss.weld.tests.ejb.singleton;

import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;

@Singleton
@Startup
@ApplicationScoped
public class Foo {

    private static AtomicInteger countOfPostConstructCalled = new AtomicInteger();
    private static AtomicInteger countOfConstructCalled = new AtomicInteger();

    public static boolean isPostConstructCalled() {
        return countOfPostConstructCalled.get() > 0;
    }

    public Foo() {
        Foo.countOfConstructCalled.incrementAndGet();
    }

    public static void reset() {
        countOfPostConstructCalled.set(0);
        countOfConstructCalled.set(0);
    }

    @PostConstruct
    public void postConstruct() {
        countOfPostConstructCalled.incrementAndGet();
    }

    public static int getCountOfPostConstructCalled() {
        return countOfPostConstructCalled.get();
    }

    public static int getCountOfConstructCalled() {
        return countOfConstructCalled.get();
    }

    public boolean getSomeValue() {
        return true;
    }

}
