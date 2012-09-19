package org.jboss.weld.tests.beanDeployment.noclassdeffound;

/**
 *
 */
public class Bar extends Foo {

    public static class ThisWillCauseAnError {
    }
}
