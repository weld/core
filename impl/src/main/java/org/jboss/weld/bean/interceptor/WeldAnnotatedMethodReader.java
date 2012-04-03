package org.jboss.weld.bean.interceptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedMethod;
import org.jboss.weld.interceptor.reader.AnnotatedMethodReader;

/**
 * @author Marius Bogoevici
 */
public class WeldAnnotatedMethodReader implements AnnotatedMethodReader<EnhancedAnnotatedMethod<?, ?>> {

    private static WeldAnnotatedMethodReader INSTANCE = new WeldAnnotatedMethodReader();

    private WeldAnnotatedMethodReader() {
    }

    public static AnnotatedMethodReader<EnhancedAnnotatedMethod<?, ?>> getInstance() {
        return INSTANCE;
    }

    public Annotation getAnnotation(Class<? extends Annotation> annotationClass, EnhancedAnnotatedMethod<?, ?> methodReference) {
        return methodReference.getAnnotation(annotationClass);
    }

    public Method getJavaMethod(EnhancedAnnotatedMethod<?, ?> methodReference) {
        return methodReference.getJavaMember();
    }
}
