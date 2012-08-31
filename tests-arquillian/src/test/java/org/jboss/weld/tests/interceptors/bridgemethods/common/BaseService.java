package org.jboss.weld.tests.interceptors.bridgemethods.common;

/**
 *
 */
public interface BaseService<T> {

    public void doSomething(T param);

    T returnSomething();
}