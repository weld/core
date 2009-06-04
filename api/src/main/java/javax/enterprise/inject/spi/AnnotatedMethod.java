package javax.enterprise.inject.spi;

import java.lang.reflect.Method;

public interface AnnotatedMethod<X> extends AnnotatedCallable<X> {
    public Method getJavaMember();
}
