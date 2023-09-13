package org.jboss.weld.annotated.slim.unbacked;

import static org.jboss.weld.util.reflection.Reflections.cast;

import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.AnnotatedParameter;

import org.jboss.weld.exceptions.InvalidObjectException;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.resources.SharedObjectCache;
import org.jboss.weld.util.AnnotatedTypes;
import org.jboss.weld.util.collections.ImmutableList;
import org.jboss.weld.util.reflection.Formats;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = { "SE_BAD_FIELD", "SE_NO_SUITABLE_CONSTRUCTOR",
        "SE_NO_SERIALVERSIONID" }, justification = "False positive from FindBugs - serialization is handled by SerializationProxy.")
public class UnbackedAnnotatedMethod<X> extends UnbackedAnnotatedMember<X> implements AnnotatedMethod<X>, Serializable {

    public static <X, Y extends X> AnnotatedMethod<X> of(AnnotatedMethod<X> originalMethod,
            UnbackedAnnotatedType<Y> declaringType, SharedObjectCache cache) {
        UnbackedAnnotatedType<X> downcastDeclaringType = cast(declaringType);
        return new UnbackedAnnotatedMethod<X>(originalMethod.getBaseType(), originalMethod.getTypeClosure(),
                originalMethod.getAnnotations(), downcastDeclaringType,
                originalMethod.getParameters(), originalMethod.getJavaMember(), cache);
    }

    private final Method method;
    private final List<AnnotatedParameter<X>> parameters;

    public UnbackedAnnotatedMethod(Type baseType, Set<Type> typeClosure, Set<Annotation> annotations,
            UnbackedAnnotatedType<X> declaringType,
            List<AnnotatedParameter<X>> originalParameters, Method method, SharedObjectCache cache) {
        super(baseType, typeClosure, cache.getSharedSet(annotations), declaringType);
        this.method = method;
        List<AnnotatedParameter<X>> parameters = new ArrayList<AnnotatedParameter<X>>(originalParameters.size());
        for (AnnotatedParameter<X> originalParameter : originalParameters) {
            parameters.add(new UnbackedAnnotatedParameter<X>(originalParameter.getBaseType(),
                    originalParameter.getTypeClosure(), cache.getSharedSet(originalParameter.getAnnotations()),
                    originalParameter.getPosition(), this));
        }
        this.parameters = ImmutableList.copyOf(parameters);
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

    // Serialization

    private Object writeReplace() throws ObjectStreamException {
        return new UnbackedMemberIdentifier<X>(getDeclaringType(),
                AnnotatedTypes.createMethodId(method, getAnnotations(), getParameters()));
    }

    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw BeanLogger.LOG.serializationProxyRequired();
    }
}
