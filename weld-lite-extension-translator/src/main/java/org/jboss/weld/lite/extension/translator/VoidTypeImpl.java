package org.jboss.weld.lite.extension.translator;

import jakarta.enterprise.lang.model.types.VoidType;
import org.jboss.weld.lite.extension.translator.util.reflection.AnnotatedTypes;

class VoidTypeImpl extends TypeImpl<java.lang.reflect.AnnotatedType> implements VoidType {
    final Class<?> clazz;

    VoidTypeImpl() {
        super(AnnotatedTypes.from(void.class), null);
        this.clazz = void.class;
    }

    @Override
    public String name() {
        return reflection.getType().getTypeName();
    }
}
