package org.jboss.weld.annotated;

import javax.enterprise.inject.spi.AnnotatedType;

import org.jboss.weld.annotated.backed.BackedAnnotatedType;
import org.jboss.weld.annotated.unbacked.UnbackedAnnotatedType;
import org.jboss.weld.introspector.WeldClass;

public class Annotateds {
    public static <X> AnnotatedType<X> slim(AnnotatedType<X> fatType, boolean unmodified) {
        if (fatType instanceof BackedAnnotatedType<?> || fatType instanceof UnbackedAnnotatedType<?>) {
            // Already been slimmed to the max
            return fatType;
        } else if (unmodified) {
            // Use java.lang.reflection Annotated implementations to obtain annotations (save heap space)
            return BackedAnnotatedType.of(fatType);
        } else {
            // Use map backed Annotated implementations
            return UnbackedAnnotatedType.of(fatType);
        }
    }

    public static <X> AnnotatedType<X> slim(WeldClass<X> clazz) {
        return slim(clazz, clazz.isDiscovered());
    }

}
