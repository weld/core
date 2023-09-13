package org.jboss.weld.tests.ejb.duplicatenames.second;

import jakarta.ejb.Stateless;

@Stateless
public class MyEjbImpl {

    public static final String MESSAGE = "second";

    public String call() {
        return MESSAGE;
    }
}
