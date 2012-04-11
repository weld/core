package org.jboss.weld.annotated.slim.unbacked;

import static java.util.Collections.unmodifiableList;
import static org.jboss.weld.logging.messages.BeanMessage.PROXY_REQUIRED;

import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedParameter;

import org.jboss.weld.exceptions.InvalidObjectException;
import org.jboss.weld.util.AnnotatedTypes;
import org.jboss.weld.util.reflection.Formats;

public class UnbackedAnnotatedConstructor<X> extends UnbackedAnnotatedMember<X> implements AnnotatedConstructor<X>, Serializable {

    public static <X> AnnotatedConstructor<X> of(AnnotatedConstructor<X> originalConstructor, UnbackedAnnotatedType<X> declaringType) {
        return new UnbackedAnnotatedConstructor<X>(originalConstructor.getBaseType(), originalConstructor.getTypeClosure(), originalConstructor.getAnnotations(), declaringType,
                originalConstructor.getParameters(), originalConstructor.getJavaMember());
    }

    private final Constructor<X> constructor;
    private final List<AnnotatedParameter<X>> parameters;

    public UnbackedAnnotatedConstructor(Type baseType, Set<Type> typeClosure, Set<Annotation> annotations, UnbackedAnnotatedType<X> declaringType,
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

    @Override
    public String toString() {
        return Formats.formatAnnotatedConstructor(this);
    }

    // Serialization

    private Object writeReplace() throws ObjectStreamException {
        return new UnbackedMemberIdentifier<X>(getDeclaringType(), AnnotatedTypes.createConstructorId(constructor, getAnnotations(), getParameters()));
    }

    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException(PROXY_REQUIRED);
    }
}
