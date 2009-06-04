package javax.enterprise.inject.spi;

import java.lang.reflect.Field;

public interface AnnotatedField<X> extends AnnotatedMember<X> {
    public Field getJavaMember();
}
