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

import org.jboss.weld.introspector.WeldClass;
import org.jboss.weld.introspector.WeldMember;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.util.LazyValueHolder;
import org.jboss.weld.util.reflection.Reflections;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

/**
 * Represents an abstract annotated memeber (field, method or constructor)
 * <p/>
 * This class is immutable, and therefore threadsafe
 *
 * @param <T>
 * @param <S>
 * @author Pete Muir
 */
public abstract class AbstractWeldMember<T, X, S extends Member> extends AbstractWeldAnnotated<T, S> implements WeldMember<T, X, S> {

    // Cached string representation
    private String toString;
    private final WeldClass<X> declaringType;

    /**
     * Constructor
     *
     * @param annotationMap The annotation map
     */
    protected AbstractWeldMember(String contextId, Map<Class<? extends Annotation>, Annotation> annotationMap, Map<Class<? extends Annotation>, Annotation> declaredAnnotationMap, ClassTransformer classTransformer, Member member, Class<T> rawType, Type type, LazyValueHolder<Set<Type>> typeClosure, WeldClass<X> declaringType) {
        super(contextId, annotationMap, declaredAnnotationMap, classTransformer, rawType, type, typeClosure);
        this.declaringType = declaringType;
    }

    /**
     * Indicates if the member is static
     *
     * @return True if static, false otherwise
     * @see org.jboss.weld.introspector.WeldAnnotated#isStatic()
     */
    public boolean isStatic() {
        return Reflections.isStatic(getDelegate());
    }

    /**
     * Indicates if the member if final
     *
     * @return True if final, false otherwise
     * @see org.jboss.weld.introspector.WeldAnnotated#isFinal()
     */
    public boolean isFinal() {
        return Reflections.isFinal(getDelegate());
    }

    public boolean isTransient() {
        return Reflections.isTransient(getDelegate());
    }

    public boolean isPublic() {
        return Modifier.isPublic(getJavaMember().getModifiers());
    }

    public boolean isPrivate() {
        return Modifier.isPrivate(getJavaMember().getModifiers());
    }

    public boolean isPackagePrivate() {
        return Reflections.isPackagePrivate(getJavaMember().getModifiers());
    }

    public Package getPackage() {
        return getJavaMember().getDeclaringClass().getPackage();
    }

    /**
     * Gets the current value of the member
     *
     * @param beanManager The Bean manager
     * @return The current value
     *
    public T getValue(BeanManager beanManager)
    {
    return beanManager.getInstance(getRawType(), getMetaAnnotationsAsArray(BindingType.class));
    }*/

    /**
     * Gets the name of the member
     *
     * @returns The name
     * @see org.jboss.weld.introspector.WeldAnnotated#getName()
     */
    public String getName() {
        return getJavaMember().getName();
    }

    /**
     * Gets a string representation of the member
     *
     * @return A string representation
     */
    @Override
    public String toString() {
        if (toString != null) {
            return toString;
        }
        toString = "Abstract annotated member " + getName();
        return toString;
    }

    public S getJavaMember() {
        return getDelegate();
    }

    public WeldClass<X> getDeclaringType() {
        return declaringType;
    }

}
