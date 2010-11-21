package org.jboss.weld.annotated.unbacked;

import static java.util.Collections.unmodifiableList;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;

public class UnbackedAnnotatedConstructor<X> extends UnbackedAnnotatedMember<X> implements AnnotatedConstructor<X> {

    public static <X> AnnotatedConstructor<X> of(AnnotatedConstructor<X> originalConstructor, AnnotatedType<X> declaringType) {
        return new UnbackedAnnotatedConstructor<X>(originalConstructor.getBaseType(), originalConstructor.getTypeClosure(), originalConstructor.getAnnotations(), declaringType,
                originalConstructor.getParameters(), originalConstructor.getJavaMember());
    }

    private final Constructor<X> constructor;
    private final List<AnnotatedParameter<X>> parameters;

    public UnbackedAnnotatedConstructor(Type baseType, Set<Type> typeClosure, Set<Annotation> annotations, AnnotatedType<X> declaringType,
            List<AnnotatedParameter<X>> originalParameters, Constructor<X> constructor) {
        super(baseType, typeClosure, annotations, declaringType);
        this.constructor = constructor;
        List<AnnotatedParameter<X>> parameters = new ArrayList<AnnotatedParameter<X>>(originalParameters.size());
        for (AnnotatedParameter<X> originalParameter : originalParameters) {
            parameters.add(new UnbackedAnnotatedParameter<X>(originalParameter.getBaseType(), originalParameter.getTypeClosure(), originalParameter.getAnnotations(),
                    originalParameter.getPosition(), this));
        }
        this.parameters = unmodifiableList(parameters);
    }

    public Constructor<X> getJavaMember() {
        return constructor;
    }

    public List<AnnotatedParameter<X>> getParameters() {
        return parameters;
    }

}
