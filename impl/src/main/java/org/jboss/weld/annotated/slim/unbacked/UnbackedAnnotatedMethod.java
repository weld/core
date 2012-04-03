package org.jboss.weld.annotated.slim.unbacked;

import static java.util.Collections.unmodifiableList;
import static org.jboss.weld.util.reflection.Reflections.cast;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;

import org.jboss.weld.util.reflection.Formats;

public class UnbackedAnnotatedMethod<X> extends UnbackedAnnotatedMember<X> implements AnnotatedMethod<X> {

    public static <X, Y extends X> AnnotatedMethod<X> of(AnnotatedMethod<X> originalMethod, AnnotatedType<Y> declaringType) {
        AnnotatedType<X> downcastDeclaringType = cast(declaringType);
        return new UnbackedAnnotatedMethod<X>(originalMethod.getBaseType(), originalMethod.getTypeClosure(), originalMethod.getAnnotations(), downcastDeclaringType,
                originalMethod.getParameters(), originalMethod.getJavaMember());
    }

    private final Method method;
    private final List<AnnotatedParameter<X>> parameters;

    public UnbackedAnnotatedMethod(Type baseType, Set<Type> typeClosure, Set<Annotation> annotations, AnnotatedType<X> declaringType,
            List<AnnotatedParameter<X>> originalParameters, Method method) {
        super(baseType, typeClosure, annotations, declaringType);
        this.method = method;
        List<AnnotatedParameter<X>> parameters = new ArrayList<AnnotatedParameter<X>>(originalParameters.size());
        for (AnnotatedParameter<X> originalParameter : originalParameters) {
            parameters.add(new UnbackedAnnotatedParameter<X>(originalParameter.getBaseType(), originalParameter.getTypeClosure(), originalParameter.getAnnotations(),
                    originalParameter.getPosition(), this));
        }
        this.parameters = unmodifiableList(parameters);
    }

    public Method getJavaMember() {
        return method;
    }

    public List<AnnotatedParameter<X>> getParameters() {
        return parameters;
    }

    @Override
    public String toString() {
        return Formats.formatAnnotatedMethod(this);
    }
}
