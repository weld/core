package org.jboss.weld.tests.resolution;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

@RequestScoped
@Named
public class Wibble implements Map<String, ResourceBundle>, Serializable {
    @Inject
    BeanManager beanManager;

    public int size() {
        // TODO Auto-generated method stub
        return 0;
    }

    public boolean isEmpty() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean containsKey(Object key) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean containsValue(Object value) {
        // TODO Auto-generated method stub
        return false;
    }

    public ResourceBundle get(Object key) {
        if (beanManager == null) {
            throw new NullPointerException();
        }
        return null;
    }

    public ResourceBundle put(String key, ResourceBundle value) {
        // TODO Auto-generated method stub
        return null;
    }

    public ResourceBundle remove(Object key) {
        // TODO Auto-generated method stub
        return null;
    }

    public void putAll(Map<? extends String, ? extends ResourceBundle> m) {
        // TODO Auto-generated method stub

    }

    public void clear() {
        // TODO Auto-generated method stub

    }

    public Set<String> keySet() {
        // TODO Auto-generated method stub
        return null;
    }

    public Collection<ResourceBundle> values() {
        // TODO Auto-generated method stub
        return null;
    }

    public Set<java.util.Map.Entry<String, ResourceBundle>> entrySet() {
        // TODO Auto-generated method stub
        return null;
    }

}
