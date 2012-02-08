package org.jboss.weld.bean.interceptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.jboss.weld.interceptor.reader.AnnotatedMethodReader;
import org.jboss.weld.introspector.WeldMethod;

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
