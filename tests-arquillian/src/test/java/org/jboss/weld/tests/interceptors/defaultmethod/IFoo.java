package org.jboss.weld.tests.interceptors.defaultmethod;

public interface IFoo {
   default boolean isIntercepted() {
      return false;
   }
}
