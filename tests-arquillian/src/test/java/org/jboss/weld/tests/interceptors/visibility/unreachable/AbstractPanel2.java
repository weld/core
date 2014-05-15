package org.jboss.weld.tests.interceptors.visibility.unreachable;

public abstract class AbstractPanel2 implements MyPackagePrivateInterface {

    @Override
    public String drawPanel() {
        return null;
    }

}
