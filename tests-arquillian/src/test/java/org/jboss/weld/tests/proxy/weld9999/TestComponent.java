package org.jboss.weld.tests.proxy.weld9999;

import java.io.Serializable;

public class TestComponent implements Serializable {
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
