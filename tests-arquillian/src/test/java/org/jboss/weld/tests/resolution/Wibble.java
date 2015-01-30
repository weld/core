package org.jboss.weld.tests.resolution;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.inject.Named;

@RequestScoped
@Named
public class Wibble implements Map<String, ResourceBundle>, Serializable {
    private static final long serialVersionUID = 1L;

    @Inject
    BeanManager beanManager;

    public int size() {
        return 0;
    }

    public boolean isEmpty() {
        return false;
    }

    public boolean containsKey(Object key) {
        return false;
    }

    public boolean containsValue(Object value) {
        return false;
    }

    public ResourceBundle get(Object key) {
        if (beanManager == null) {
            throw new NullPointerException();
        }
        return null;
    }

    public ResourceBundle put(String key, ResourceBundle value) {
        return null;
    }

    public ResourceBundle remove(Object key) {
        return null;
    }

    public void putAll(Map<? extends String, ? extends ResourceBundle> m) {
    }

    public void clear() {
    }

    public Set<String> keySet() {
        return null;
    }

    public Collection<ResourceBundle> values() {
        return null;
    }

    public Set<java.util.Map.Entry<String, ResourceBundle>> entrySet() {
        return null;
    }

}
