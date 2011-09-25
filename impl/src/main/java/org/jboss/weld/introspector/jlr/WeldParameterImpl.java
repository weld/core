/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.weld.introspector.jlr;

import org.jboss.weld.exceptions.IllegalArgumentException;
import org.jboss.weld.introspector.TypeClosureLazyValueHolder;
import org.jboss.weld.introspector.WeldCallable;
import org.jboss.weld.introspector.WeldClass;
import org.jboss.weld.introspector.WeldParameter;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.util.LazyValueHolder;

import javax.enterprise.inject.spi.AnnotatedCallable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import static org.jboss.weld.logging.messages.ReflectionMessage.UNABLE_TO_GET_PARAMETER_NAME;
import static org.jboss.weld.util.reflection.Reflections.EMPTY_ANNOTATIONS;

/**
 * Represents a parameter
 * <p/>
 * This class is immutable and therefore threadsafe
 *
 * @param <T>
 * @author Pete Muir
 */
public class WeldParameterImpl<T, X> extends AbstractWeldAnnotated<T, Object> implements WeldParameter<T, X> {

    public static <T, X> WeldParameter<T, X> of(String contextId, Annotation[] annotations, Class<T> rawType, Type type, WeldCallable<?, X, ?> declaringMember, int position, ClassTransformer classTransformer) {
        return new WeldParameterImpl<T, X>(contextId, annotations, rawType, type, new TypeClosureLazyValueHolder(contextId, type), declaringMember, position, classTransformer);
    }

    public static <T, X> WeldParameter<T, X> of(String contextId, Set<Annotation> annotations, Class<T> rawType, Type type, WeldCallable<?, X, ?> declaringMember, int position, ClassTransformer classTransformer) {
        return new WeldParameterImpl<T, X>(contextId, annotations.toArray(EMPTY_ANNOTATIONS), rawType, type, new TypeClosureLazyValueHolder(contextId, type), declaringMember, position, classTransformer);
    }

    private final int position;
    private final WeldCallable<?, X, ?> declaringMember;

    /**
     * Constructor
     *
     * @param annotations The annotations array
     * @param type        The type of the parameter
     */
    protected WeldParameterImpl(String contextId, Annotation[] annotations, Class<T> rawType, Type type, LazyValueHolder<Set<Type>> typeClosure, WeldCallable<?, X, ?> declaringMember, int position, ClassTransformer classTransformer) {
        super(contextId, buildAnnotationMap(annotations), buildAnnotationMap(annotations), classTransformer, rawType, type, typeClosure);
        this.declaringMember = declaringMember;
        this.position = position;
    }

    /**
     * Indicates if the parameter is final
     *
     * @return True if final, false otherwise
     * @see org.jboss.weld.introspector.WeldAnnotated#isFinal()
     */
    public boolean isFinal() {
        return false;
    }

    /**
     * Indicates if the parameter is static
     *
     * @return True if static, false otherwise
     * @see org.jboss.weld.introspector.WeldAnnotated#isStatic()
     */
    public boolean isStatic() {
        return false;
    }

    public boolean isPublic() {
        return false;
    }

    public boolean isPrivate() {
        return false;
    }

    public boolean isPackagePrivate() {
        return false;
    }

    public boolean isGeneric() {
        return false;
    }

    public Package getPackage() {
        return declaringMember.getPackage();
    }

    /**
     * Gets the name of the parameter
     *
     * @throws IllegalArgumentException (not supported)
     * @see org.jboss.weld.introspector.WeldAnnotated#getName()
     */
    public String getName() {
        throw new IllegalArgumentException(UNABLE_TO_GET_PARAMETER_NAME);
    }

    /**
     * Gets a string representation of the parameter
     *
     * @return A string representation
     */
    @Override
    public String toString() {
        return "[parameter " + (getPosition() + 1) + "] of " + getDeclaringWeldCallable().toString();
    }

    public AnnotatedCallable<X> getDeclaringCallable() {
        return declaringMember;
    }

    public WeldCallable<?, X, ?> getDeclaringWeldCallable() {
        return declaringMember;
    }

    public int getPosition() {
        return position;
    }

    @Override
    public Object getDelegate() {
        return null;
    }

    public WeldClass<X> getDeclaringType() {
        return getDeclaringWeldCallable().getDeclaringType();
    }

}
