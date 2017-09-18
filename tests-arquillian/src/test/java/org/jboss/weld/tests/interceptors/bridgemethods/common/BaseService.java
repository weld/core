package org.jboss.weld.tests.interceptors.bridgemethods.common;

/**
 *
 */
public interface BaseService<T> {

    void doSomething(T param);

    T returnSomething();

    void setSomething(T param);

    T getSomething();
}