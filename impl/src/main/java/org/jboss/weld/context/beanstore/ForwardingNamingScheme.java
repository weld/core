package org.jboss.weld.context.beanstore;

import java.util.Collection;

import org.jboss.weld.serialization.spi.BeanIdentifier;

public abstract class ForwardingNamingScheme implements NamingScheme {

    protected abstract NamingScheme delegate();

    public boolean accept(String id) {
        return delegate().accept(id);
    }

    public BeanIdentifier deprefix(String id) {
        return delegate().deprefix(id);
    }

    public String prefix(BeanIdentifier id) {
        return delegate().prefix(id);
    }

    public Collection<String> filterIds(Collection<String> ids) {
        return delegate().filterIds(ids);
    }

    public Collection<BeanIdentifier> deprefix(Collection<String> ids) {
        return delegate().deprefix(ids);
    }

    public Collection<String> prefix(Collection<BeanIdentifier> ids) {
        return delegate().prefix(ids);
    }

    @Override
    public boolean equals(Object obj) {
        return delegate().equals(obj);
    }

    @Override
    public int hashCode() {
        return delegate().hashCode();
    }

    public String toString() {
        return delegate().toString();
    }

}
