package org.jboss.weld.lite.extension.translator;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.lang.model.types.VoidType;
import org.jboss.weld.lite.extension.translator.util.reflection.AnnotatedTypes;

class VoidTypeImpl extends TypeImpl<java.lang.reflect.AnnotatedType> implements VoidType {
    final Class<?> clazz;

    VoidTypeImpl(BeanManager bm) {
        super(AnnotatedTypes.from(void.class), null, bm);
        this.clazz = void.class;
    }

    @Override
    public String name() {
        return reflection.getType().getTypeName();
    }
}
