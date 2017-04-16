package org.jboss.weld.tests.ejb.duplicatenames.first;

import javax.ejb.Stateless;
import org.jboss.weld.tests.ejb.duplicatenames.Logged;

@Stateless
@Logged
public class MyEjbImpl {

    public static final String MESSAGE = "first";

    public String call() {
        return MESSAGE;
    }
}
