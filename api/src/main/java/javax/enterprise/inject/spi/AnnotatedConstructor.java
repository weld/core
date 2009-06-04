package javax.enterprise.inject.spi;

import java.lang.reflect.Constructor;

public interface AnnotatedConstructor<X> extends AnnotatedCallable<X> {
    public Constructor<X> getJavaMember();
}
