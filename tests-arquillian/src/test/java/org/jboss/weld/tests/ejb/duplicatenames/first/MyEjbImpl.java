package org.jboss.weld.tests.ejb.duplicatenames.first;

import javax.ejb.Stateless;

@Stateless
public class MyEjbImpl {

    public static final String MESSAGE = "first";

    public String call() {
        return MESSAGE;
    }
}
