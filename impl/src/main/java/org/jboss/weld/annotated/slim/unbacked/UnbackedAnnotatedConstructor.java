package org.jboss.weld.annotated.slim.unbacked;

import static org.jboss.weld.util.collections.WeldCollections.immutableListView;

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
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.resources.SharedObjectCache;
import org.jboss.weld.util.AnnotatedTypes;
import org.jboss.weld.util.reflection.Formats;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

@SuppressWarnings(value = { "SE_BAD_FIELD", "SE_NO_SUITABLE_CONSTRUCTOR", "SE_NO_SERIALVERSIONID" }, justification = "False positive from FindBugs - serialization is handled by SerializationProxy.")
public class UnbackedAnnotatedConstructor<X> extends UnbackedAnnotatedMember<X> implements AnnotatedConstructor<X>, Serializable {

    public static <X> AnnotatedConstructor<X> of(AnnotatedConstructor<X> originalConstructor, UnbackedAnnotatedType<X> declaringType, SharedObjectCache cache) {
        return new UnbackedAnnotatedConstructor<X>(originalConstructor.getBaseType(), originalConstructor.getTypeClosure(), originalConstructor.getAnnotations(), declaringType,
                originalConstructor.getParameters(), originalConstructor.getJavaMember(), cache);
    }

    private final Constructor<X> constructor;
    private final List<AnnotatedParameter<X>> parameters;

    public UnbackedAnnotatedConstructor(Type baseType, Set<Type> typeClosure, Set<Annotation> annotations, UnbackedAnnotatedType<X> declaringType,
            List<AnnotatedParameter<X>> originalParameters, Constructor<X> constructor, SharedObjectCache cache) {
        super(baseType, typeClosure, cache.getSharedSet(annotations), declaringType);
        this.constructor = constructor;
        List<AnnotatedParameter<X>> parameters = new ArrayList<AnnotatedParameter<X>>(originalParameters.size());
        for (AnnotatedParameter<X> originalParameter : originalParameters) {
            parameters.add(new UnbackedAnnotatedParameter<X>(originalParameter.getBaseType(), originalParameter.getTypeClosure(), cache.getSharedSet(originalParameter.getAnnotations()),
                    originalParameter.getPosition(), this));
        }
        this.parameters = immutableListView(parameters);
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
        throw BeanLogger.LOG.serializationProxyRequired();
    }
}
