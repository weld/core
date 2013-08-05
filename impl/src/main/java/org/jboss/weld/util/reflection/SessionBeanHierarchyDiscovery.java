package org.jboss.weld.util.reflection;

import java.lang.reflect.Type;

public class SessionBeanHierarchyDiscovery extends HierarchyDiscovery {

    public SessionBeanHierarchyDiscovery(Type type) {
        super(type);
    }

    protected void discoverFromClass(Class<?> clazz) {
        /*
         * If the type is a class then super types are discovered but not interfaces.
         * If the type is an interface then its super interfaces are discovered.
         */
        if (clazz.getSuperclass() != null) {
            discoverTypes(processAndResolveType(clazz.getGenericSuperclass(), clazz.getSuperclass()));
        } else if (clazz.isInterface()) {
            discoverInterfaces(clazz);
        }
    }
}
