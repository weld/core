package org.jboss.weld.lite.extension.translator;

import jakarta.enterprise.inject.build.compatible.spi.ScannedClasses;

class ScannedClassesImpl implements ScannedClasses {
    private final jakarta.enterprise.inject.spi.BeforeBeanDiscovery bbd;

    ScannedClassesImpl(jakarta.enterprise.inject.spi.BeforeBeanDiscovery bbd) {
        this.bbd = bbd;
    }

    @Override
    public void add(String className) {
        try {
            bbd.addAnnotatedType(Class.forName(className), className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
