package org.jboss.weld.tests.ejb.duplicatenames.second;

import javax.ejb.Stateless;
import org.jboss.weld.tests.ejb.duplicatenames.Logged;

@Stateless
@Logged
public class MyEjbImpl {

    public static final String MESSAGE = "second";

    public String call() {
        return MESSAGE;
    }
}