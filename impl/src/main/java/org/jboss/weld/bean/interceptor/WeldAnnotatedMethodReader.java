package org.jboss.weld.bean.interceptor;

import org.jboss.interceptor.reader.AnnotatedMethodReader;
import org.jboss.weld.introspector.WeldMethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @author Marius Bogoevici
 */
public class WeldAnnotatedMethodReader implements AnnotatedMethodReader<WeldMethod<?, ?>> {

    private static WeldAnnotatedMethodReader INSTANCE = new WeldAnnotatedMethodReader();

    private WeldAnnotatedMethodReader() {
    }

    public static AnnotatedMethodReader<WeldMethod<?, ?>> getInstance() {
        return INSTANCE;
    }

    public Annotation getAnnotation(Class<? extends Annotation> annotationClass, WeldMethod<?, ?> methodReference) {
        return methodReference.getAnnotation(annotationClass);
    }

    public Method getJavaMethod(WeldMethod<?, ?> methodReference) {
        return methodReference.getJavaMember();
    }
}
