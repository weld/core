package org.jboss.weld.util.reflection;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;

/**
 * @author Marko Luksa
*/
class GenericArrayTypeImpl implements GenericArrayType {

    private Type genericComponentType;

    GenericArrayTypeImpl(Type genericComponentType) {
        this.genericComponentType = genericComponentType;
    }

    public Type getGenericComponentType() {
        return genericComponentType;
    }
}
