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

    private static boolean postConstructCalled;
    private static AtomicInteger countOfPostConstructCalled = new AtomicInteger(0);
    private static AtomicInteger countOfConstructCalled = new AtomicInteger(0);

    public static boolean isPostConstructCalled() {
        return postConstructCalled;
    }

    public Foo() {
        Foo.countOfConstructCalled.addAndGet(1);
    }

    public static void reset() {
        postConstructCalled = false;
        countOfPostConstructCalled.set(0);
        countOfConstructCalled.set(0);
    }

    @PostConstruct
    public void postConstruct() {
        postConstructCalled = true;
        countOfPostConstructCalled.addAndGet(1);
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
