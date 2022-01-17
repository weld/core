package org.jboss.weld.lite.extension.translator;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.lang.model.declarations.PackageInfo;

import java.util.Objects;

class PackageInfoImpl extends DeclarationInfoImpl<Package, /*always null*/ jakarta.enterprise.inject.spi.Annotated> implements PackageInfo {
    // only for equals/hashCode
    private final String name;

    PackageInfoImpl(Package pkg, BeanManager bm) {
        super(pkg, null, bm);
        this.name = reflection.getName();
    }

    @Override
    public String name() {
        return reflection.getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PackageInfoImpl that = (PackageInfoImpl) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
