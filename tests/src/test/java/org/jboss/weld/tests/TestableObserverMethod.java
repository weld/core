package org.jboss.weld.tests;

import javax.enterprise.inject.spi.ObserverMethod;

public interface TestableObserverMethod<T> extends ObserverMethod<T>
{
   
   public boolean isObserved();
   
}