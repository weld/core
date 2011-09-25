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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Sets;
import org.jboss.weld.introspector.ConstructorSignature;
import org.jboss.weld.introspector.DiscoveredExternalAnnotatedType;
import org.jboss.weld.introspector.ExternalAnnotatedType;
import org.jboss.weld.introspector.MethodSignature;
import org.jboss.weld.introspector.TypeClosureLazyValueHolder;
import org.jboss.weld.introspector.WeldClass;
import org.jboss.weld.introspector.WeldConstructor;
import org.jboss.weld.introspector.WeldField;
import org.jboss.weld.introspector.WeldMethod;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.resources.SharedObjectFacade;
import org.jboss.weld.util.LazyValueHolder;
import org.jboss.weld.util.collections.ArraySet;
import org.jboss.weld.util.collections.ArraySetMultimap;
import org.jboss.weld.util.reflection.Formats;
import org.jboss.weld.util.reflection.Reflections;
import org.jboss.weld.util.reflection.SecureReflections;

import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents an annotated class
 * <p/>
 * This class is immutable, and therefore threadsafe
 *
 * @param <T> the type of the class
 * @author Pete Muir
 * @author David Allen
 * @author Ales Justin
 * @author Marko Luksa
 */
public class WeldClassImpl<T> extends AbstractWeldAnnotated<T, Class<T>> implements WeldClass<T> {

    // Class attributes
    private final WeldClass<? super T> superclass;

    // The set of abstracted fields
    private final Set<WeldField<?, ?>> fields;
    // The map from annotation type to abstracted field with annotation
    private final ArrayListMultimap<Class<? extends Annotation>, WeldField<?, ?>> annotatedFields;

    // The set of abstracted fields
    private final ArraySet<WeldField<?, ?>> declaredFields;
    // The map from annotation type to abstracted field with annotation
    private final ArrayListMultimap<Class<? extends Annotation>, WeldField<?, ? super T>> declaredAnnotatedFields;
    // The map from annotation type to abstracted field with meta-annotation
    private final ArrayListMultimap<Class<? extends Annotation>, WeldField<?, ?>> declaredMetaAnnotatedFields;

    // The set of abstracted methods
    private final Set<WeldMethod<?, ? super T>> methods;
    // The map from annotation type to abstracted method with annotation
    private final ArrayListMultimap<Class<? extends Annotation>, WeldMethod<?, ?>> annotatedMethods;

    // The set of abstracted methods
    private final ArraySet<WeldMethod<?, ? super T>> declaredMethods;
    // The map from annotation type to abstracted method with annotation
    private final ArrayListMultimap<Class<? extends Annotation>, WeldMethod<?, ? super T>> declaredAnnotatedMethods;
    // The map from annotation type to method with a parameter with annotation
    private final ArrayListMultimap<Class<? extends Annotation>, WeldMethod<?, ? super T>> declaredMethodsByAnnotatedParameters;

    // The set of abstracted constructors
    private final ArraySet<WeldConstructor<T>> constructors;
    private final Map<ConstructorSignature, WeldConstructor<?>> declaredConstructorsBySignature;

    // The meta-annotation map (annotation type -> set of annotations containing
    // meta-annotation) of the item
    private final ArraySetMultimap<Class<? extends Annotation>, Annotation> declaredMetaAnnotationMap;

    private final boolean discovered;
    private final boolean modified;

    public static <T> WeldClass<T> of(String contextId, Class<T> clazz, ClassTransformer classTransformer) {
        return new WeldClassImpl<T>(contextId, clazz, clazz, null, new TypeClosureLazyValueHolder(contextId, clazz), buildAnnotationMap(clazz.getAnnotations()), buildAnnotationMap(clazz.getDeclaredAnnotations()), classTransformer);
    }

    public static <T> WeldClass<T> of(String contextId, AnnotatedType<T> annotatedType, ClassTransformer classTransformer) {
        return new WeldClassImpl<T>(contextId, annotatedType.getJavaClass(), annotatedType.getBaseType(), annotatedType, new TypeClosureLazyValueHolder(contextId, annotatedType.getTypeClosure()), buildAnnotationMap(annotatedType.getAnnotations()), buildAnnotationMap(annotatedType.getAnnotations()), classTransformer);
    }

    public static <T> WeldClass<T> of(String contextId, Class<T> rawType, Type type, ClassTransformer classTransformer) {
        return new WeldClassImpl<T>(contextId, rawType, type, null, new TypeClosureLazyValueHolder(contextId, type), buildAnnotationMap(rawType.getAnnotations()), buildAnnotationMap(rawType.getDeclaredAnnotations()), classTransformer);
    }

    protected WeldClassImpl(String contextId, Class<T> rawType, Type type, AnnotatedType<T> annotatedType, LazyValueHolder<Set<Type>> typeClosure, Map<Class<? extends Annotation>, Annotation> annotationMap, Map<Class<? extends Annotation>, Annotation> declaredAnnotationMap, ClassTransformer classTransformer) {
        super(contextId, annotationMap, declaredAnnotationMap, classTransformer, rawType, type, typeClosure);

        if (annotatedType instanceof DiscoveredExternalAnnotatedType) {
            discovered = true;
            modified = DiscoveredExternalAnnotatedType.class.cast(annotatedType).isModifed();
        } else if (annotatedType instanceof ExternalAnnotatedType) {
            discovered = false;
            modified = false;
        } else {
            discovered = true;
            modified = false;
        }

        if (modified) {
            this.superclass = classTransformer.loadClass(Object.class);
        } else if (rawType.getSuperclass() != null) {
            this.superclass = classTransformer.loadClass(rawType.getSuperclass());
        } else {
            this.superclass = null;
        }

        // Assign class field information
        this.declaredAnnotatedFields = ArrayListMultimap.create();
        this.declaredMetaAnnotatedFields = ArrayListMultimap.create();

        ArrayList<WeldField<?, ?>> declaredFieldsTemp = new ArrayList<WeldField<?, ?>>();
        if (annotatedType == null) {
            this.annotatedFields = null;
            this.fields = initFieldsFromRawType(contextId, rawType, classTransformer, declaredFieldsTemp);
        } else {
            this.annotatedFields = ArrayListMultimap.create();
            this.fields = initFieldsFromAnnotatedType(contextId, rawType, annotatedType, classTransformer, declaredFieldsTemp);
            this.annotatedFields.trimToSize();
        }
        this.declaredFields = new ArraySet<WeldField<?, ?>>(declaredFieldsTemp).trimToSize();
        this.declaredAnnotatedFields.trimToSize();
        this.declaredMetaAnnotatedFields.trimToSize();

        // Assign constructor information
        this.constructors = new ArraySet<WeldConstructor<T>>();
        this.declaredConstructorsBySignature = new HashMap<ConstructorSignature, WeldConstructor<?>>();
        if (annotatedType == null) {
            initConstructorsFromRawType(contextId, rawType, classTransformer);
        } else {
            initConstructorsFromAnnotatedType(contextId, annotatedType, classTransformer);
        }
        this.constructors.trimToSize();

        // Assign method information
        this.declaredAnnotatedMethods = ArrayListMultimap.create();
        this.declaredMethodsByAnnotatedParameters = ArrayListMultimap.create();

        ArrayList<WeldMethod<?, ? super T>> declaredMethodsTemp = new ArrayList<WeldMethod<?, ? super T>>();
        if (annotatedType == null) {
            this.annotatedMethods = null;
            this.methods = initMethodsFromRawType(contextId, rawType, classTransformer, declaredMethodsTemp);
        } else {
            this.annotatedMethods = ArrayListMultimap.create();
            this.methods = initMethodsFromAnnotatedType(contextId, rawType, annotatedType, classTransformer, declaredMethodsTemp);
            this.annotatedMethods.trimToSize();
        }
        this.declaredMethods = new ArraySet<WeldMethod<?, ? super T>>(declaredMethodsTemp).trimToSize();
        this.declaredAnnotatedMethods.trimToSize();
        this.declaredMethodsByAnnotatedParameters.trimToSize();

        ArraySetMultimap<Class<? extends Annotation>, Annotation> declaredMetaAnnotationMap = new ArraySetMultimap<Class<? extends Annotation>, Annotation>();
        for (Annotation declaredAnnotation : declaredAnnotationMap.values()) {
            addMetaAnnotations(declaredMetaAnnotationMap, declaredAnnotation, declaredAnnotation.annotationType().getAnnotations(), true);
            addMetaAnnotations(declaredMetaAnnotationMap, declaredAnnotation, classTransformer.getTypeStore().get(declaredAnnotation.annotationType()), true);
            declaredMetaAnnotationMap.putSingleElement(declaredAnnotation.annotationType(), declaredAnnotation);
        }
        declaredMetaAnnotationMap.trimToSize();
        this.declaredMetaAnnotationMap = SharedObjectFacade.wrap(contextId, declaredMetaAnnotationMap);
    }

    private ArraySet<WeldMethod<?, ? super T>> initMethodsFromAnnotatedType(String contextId, Class<T> rawType, AnnotatedType<T> annotatedType, ClassTransformer classTransformer, ArrayList<WeldMethod<?, ? super T>> declaredMethodsTemp) {
        Set<WeldMethod<?, ? super T>> methods = new HashSet<WeldMethod<?, ? super T>>();
        for (AnnotatedMethod<? super T> method : annotatedType.getMethods()) {
            WeldMethod<?, ? super T> weldMethod = WeldMethodImpl.of(contextId, method, this, classTransformer);
            methods.add(weldMethod);
            if (method.getDeclaringType().getJavaClass() == rawType) {
                declaredMethodsTemp.add(weldMethod);
            }
            for (Annotation annotation : weldMethod.getAnnotations()) {
                annotatedMethods.put(annotation.annotationType(), weldMethod);
                if (method.getDeclaringType().getJavaClass() == rawType) {
                    declaredAnnotatedMethods.put(annotation.annotationType(), weldMethod);
                }
            }
            for (Class<? extends Annotation> annotationType : WeldMethod.MAPPED_PARAMETER_ANNOTATIONS) {
                if (weldMethod.getWeldParameters(annotationType).size() > 0) {
                    if (method.getDeclaringType().getJavaClass() == rawType) {
                        declaredMethodsByAnnotatedParameters.put(annotationType, weldMethod);
                    }
                }
            }
        }
        return new ArraySet<WeldMethod<?, ? super T>>(methods).trimToSize();
    }

    private Set<WeldMethod<?, ? super T>> initMethodsFromRawType(String contextId, Class<T> rawType, ClassTransformer classTransformer, ArrayList<WeldMethod<?, ? super T>> declaredMethodsTemp) {
        Set<WeldMethod<?, ? super T>> methodsTemp = null;
        if (rawType != Object.class) {
            for (Method method : SecureReflections.getDeclaredMethods(rawType)) {
                WeldMethod<?, T> weldMethod = WeldMethodImpl.of(contextId, method, this.<T>getDeclaringWeldClass(method, classTransformer), classTransformer);
                declaredMethodsTemp.add(weldMethod);
                for (Annotation annotation : weldMethod.getAnnotations()) {
                    this.declaredAnnotatedMethods.put(annotation.annotationType(), weldMethod);
                }
                for (Class<? extends Annotation> annotationType : WeldMethod.MAPPED_PARAMETER_ANNOTATIONS) {
                    if (weldMethod.getWeldParameters(annotationType).size() > 0) {
                        this.declaredMethodsByAnnotatedParameters.put(annotationType, weldMethod);
                    }
                }
            }
            methodsTemp = new ArraySet<WeldMethod<?, ? super T>>(declaredMethodsTemp).trimToSize();
            if (superclass != null) {
                WeldClass<?> current = superclass;
                while (current.getJavaClass() != Object.class) {
                    Set<WeldMethod<?, ? super T>> superClassMethods = Reflections.cast(current.getDeclaredWeldMethods());
                    methodsTemp = Sets.union(methodsTemp, superClassMethods);
                    current = current.getWeldSuperclass();
                }
            }
        }
        return methodsTemp;
    }

    private Set<WeldField<?, ?>> initFieldsFromAnnotatedType(String contextId, Class<T> rawType, AnnotatedType<T> annotatedType, ClassTransformer classTransformer, ArrayList<WeldField<?, ?>> declaredFieldsTemp) {
        Set<WeldField<?, ?>> fieldsTemp = new HashSet<WeldField<?, ?>>();
        for (AnnotatedField<? super T> annotatedField : annotatedType.getFields()) {
            WeldField<?, ? super T> weldField = WeldFieldImpl.of(contextId, annotatedField, this, classTransformer);
            fieldsTemp.add(weldField);
            if (annotatedField.getDeclaringType().getJavaClass() == rawType) {
                declaredFieldsTemp.add(weldField);
            }
            for (Annotation annotation : weldField.getAnnotations()) {
                annotatedFields.put(annotation.annotationType(), weldField);
                if (annotatedField.getDeclaringType().getJavaClass() == rawType) {
                    addDeclaredAnnotatedField(weldField, annotation);
                }
            }
        }
        return new ArraySet<WeldField<?, ?>>(fieldsTemp).trimToSize();
    }

    private Set<WeldField<?, ?>> initFieldsFromRawType(String contextId, Class<T> rawType, ClassTransformer classTransformer, ArrayList<WeldField<?, ?>> declaredFieldsTemp) {
        Set<WeldField<?, ?>> fieldsTemp = null;
        if (rawType != Object.class) {
            for (Field field : SecureReflections.getDeclaredFields(rawType)) {
                WeldField<?, T> annotatedField = WeldFieldImpl.of(contextId, field, this.<T>getDeclaringWeldClass(field, classTransformer), classTransformer);
                declaredFieldsTemp.add(annotatedField);
                for (Annotation annotation : annotatedField.getAnnotations()) {
                    addDeclaredAnnotatedField(annotatedField, annotation);
                }
            }
            fieldsTemp = new ArraySet<WeldField<?, ?>>(declaredFieldsTemp).trimToSize();
            if ((superclass != null) && (superclass.getJavaClass() != Object.class)) {
                fieldsTemp = Sets.union(fieldsTemp, Reflections.<Set<WeldField<?, ?>>>cast(superclass.getFields()));
            }
        }
        return fieldsTemp;
    }

    private void initConstructorsFromAnnotatedType(String contextId, AnnotatedType<T> annotatedType, ClassTransformer classTransformer) {
        for (AnnotatedConstructor<T> constructor : annotatedType.getConstructors()) {
            WeldConstructor<T> weldConstructor = WeldConstructorImpl.of(contextId, constructor, this, classTransformer);
            addConstructor(weldConstructor);
        }
    }

    private void initConstructorsFromRawType(String contextId, Class<T> rawType, ClassTransformer classTransformer) {
        for (Constructor<?> constructor : SecureReflections.getDeclaredConstructors(rawType)) {
            Constructor<T> c = Reflections.cast(constructor);
            WeldConstructor<T> annotatedConstructor = WeldConstructorImpl.of(contextId, c, this.<T>getDeclaringWeldClass(c, classTransformer), classTransformer);
            addConstructor(annotatedConstructor);
        }
    }

    private void addConstructor(WeldConstructor<T> weldConstructor) {
        this.constructors.add(weldConstructor);
        this.declaredConstructorsBySignature.put(weldConstructor.getSignature(), weldConstructor);
    }

    private void addDeclaredAnnotatedField(WeldField<?, ? super T> annotatedField, Annotation annotation) {
        this.declaredAnnotatedFields.put(annotation.annotationType(), annotatedField);
        for (Annotation metaAnnotation : annotation.annotationType().getAnnotations()) {
            this.declaredMetaAnnotatedFields.put(metaAnnotation.annotationType(), annotatedField);
        }
    }

    private <X> WeldClass<X> getDeclaringWeldClass(Member member, ClassTransformer transformer) {
        if (member.getDeclaringClass().equals(getJavaClass())) {
            return cast(this);
        } else {
            return transformer.loadClass(Reflections.<Class<X>>cast(member.getDeclaringClass()));
        }
    }

    /**
     * Gets the implementing class
     *
     * @return The class
     */
    public Class<? extends T> getAnnotatedClass() {
        return getJavaClass();
    }

    /**
     * Gets the delegate (class)
     *
     * @return The class
     */
    @Override
    public Class<T> getDelegate() {
        return getJavaClass();
    }

    /**
     * Gets the abstracted fields of the class
     * <p/>
     * Initializes the fields if they are null
     *
     * @return The set of abstracted fields
     */
    public Collection<WeldField<?, ?>> getWeldFields() {
        return Collections.unmodifiableCollection(fields);
    }

    public Collection<WeldField<?, ?>> getDeclaredFields() {
        return Collections.unmodifiableCollection(declaredFields);
    }

    public <F> WeldField<F, ?> getDeclaredWeldField(String fieldName) {
        for (WeldField<?, ?> field : declaredFields) {
            if (field.getName().equals(fieldName)) {
                return cast(field);
            }
        }
        return null;
    }

    public Collection<WeldField<?, ? super T>> getDeclaredWeldFields(Class<? extends Annotation> annotationType) {
        return Collections.unmodifiableCollection(declaredAnnotatedFields.get(annotationType));
    }

    public WeldConstructor<T> getDeclaredWeldConstructor(ConstructorSignature signature) {
        return cast(declaredConstructorsBySignature.get(signature));
    }

    /**
     * Gets the abstracted field annotated with a specific annotation type
     * <p/>
     * If the fields map is null, initialize it first
     *
     * @param annotationType The annotation type to match
     * @return A set of matching abstracted fields, null if none are found.
     */
    public Collection<WeldField<?, ?>> getWeldFields(Class<? extends Annotation> annotationType) {
        if (annotatedFields == null) {
            // Build collection from class hierarchy
            ArrayList<WeldField<?, ?>> aggregatedFields = new ArrayList<WeldField<?, ?>>(this.declaredAnnotatedFields.get(annotationType));
            if ((superclass != null) && (superclass.getJavaClass() != Object.class)) {
                aggregatedFields.addAll(superclass.getWeldFields(annotationType));
            }
            return Collections.unmodifiableCollection(aggregatedFields);
        } else {
            // Return results collected directly from AnnotatedType
            return Collections.unmodifiableCollection(annotatedFields.get(annotationType));
        }
    }

    public boolean isLocalClass() {
        return getJavaClass().isLocalClass();
    }

    public boolean isAnonymousClass() {
        return getJavaClass().isAnonymousClass();
    }

    public boolean isMemberClass() {
        return getJavaClass().isMemberClass();
    }

    public boolean isAbstract() {
        return Modifier.isAbstract(getJavaClass().getModifiers());
    }

    public boolean isEnum() {
        return getJavaClass().isEnum();
    }

    public boolean isSerializable() {
        return Reflections.isSerializable(getJavaClass());
    }

    /**
     * Gets the abstracted methods that have a certain annotation type present
     * <p/>
     * If the annotated methods map is null, initialize it first
     *
     * @param annotationType The annotation type to match
     * @return A set of matching method abstractions. Returns an empty set if no
     *         matches are found.
     * @see org.jboss.weld.introspector.WeldClass#getWeldMethods(Class)
     */
    public Collection<WeldMethod<?, ?>> getWeldMethods(Class<? extends Annotation> annotationType) {
        if (annotatedMethods == null) {
            ArrayList<WeldMethod<?, ?>> aggregateMethods = new ArrayList<WeldMethod<?, ?>>(this.declaredAnnotatedMethods.get(annotationType));
            if ((superclass != null) && (superclass.getJavaClass() != Object.class)) {
                aggregateMethods.addAll(superclass.getDeclaredWeldMethods(annotationType));
            }
            return Collections.unmodifiableCollection(aggregateMethods);
        } else {
            return Collections.unmodifiableCollection(annotatedMethods.get(annotationType));
        }
    }

    public Collection<WeldMethod<?, ? super T>> getDeclaredWeldMethods(Class<? extends Annotation> annotationType) {
        return Collections.unmodifiableCollection(declaredAnnotatedMethods.get(annotationType));
    }

    /**
     * Gets constructors with given annotation type
     *
     * @param annotationType The annotation type to match
     * @return A set of abstracted constructors with given annotation type. If
     *         the constructors set is empty, initialize it first. Returns an
     *         empty set if there are no matches.
     * @see org.jboss.weld.introspector.WeldClass#getWeldConstructors(Class)
     */
    public Collection<WeldConstructor<T>> getWeldConstructors(Class<? extends Annotation> annotationType) {
        Set<WeldConstructor<T>> ret = new HashSet<WeldConstructor<T>>();
        for (WeldConstructor<T> constructor : constructors) {
            if (constructor.isAnnotationPresent(annotationType)) {
                ret.add(constructor);
            }
        }
        return ret;
    }

    public WeldConstructor<T> getNoArgsWeldConstructor() {
        for (WeldConstructor<T> constructor : constructors) {
            if (constructor.getJavaMember().getParameterTypes().length == 0) {
                return constructor;
            }
        }
        return null;
    }

    public Collection<WeldMethod<?, ? super T>> getDeclaredWeldMethodsWithAnnotatedParameters(Class<? extends Annotation> annotationType) {
        return Collections.unmodifiableCollection(declaredMethodsByAnnotatedParameters.get(annotationType));
    }

    public WeldMethod<?, ?> getWeldMethod(Method methodDescriptor) {
        // TODO Should be cached
        for (WeldMethod<?, ?> annotatedMethod : getWeldMethods()) {
            if (annotatedMethod.getName().equals(methodDescriptor.getName()) && Arrays.equals(annotatedMethod.getParameterTypesAsArray(), methodDescriptor.getParameterTypes())) {
                return annotatedMethod;
            }
        }
        return null;
    }

    public Collection<WeldMethod<?, ? super T>> getWeldMethods() {
        return Collections.unmodifiableSet(methods);
    }

    public WeldMethod<?, ?> getDeclaredWeldMethod(Method method) {
        // TODO Should be cached
        for (WeldMethod<?, ?> annotatedMethod : declaredMethods) {
            if (annotatedMethod.getName().equals(method.getName()) && Arrays.equals(annotatedMethod.getParameterTypesAsArray(), method.getParameterTypes())) {
                return annotatedMethod;
            }
        }
        return null;
    }

    public Collection<WeldMethod<?, ? super T>> getDeclaredWeldMethods() {
        return Collections.unmodifiableSet(declaredMethods);
    }

    public <M> WeldMethod<M, ?> getDeclaredWeldMethod(MethodSignature signature) {
        for (WeldMethod<?, ? super T> method : declaredMethods) {
            if (method.getSignature().equals(signature)) {
                return cast(method);
            }
        }
        return null;
    }

    public <M> WeldMethod<M, ?> getWeldMethod(MethodSignature signature) {
        WeldMethod<M, ?> method = cast(getDeclaredWeldMethod(signature));
        if ((method == null) && (superclass != null) && (superclass.getJavaClass() != Object.class)) {
            method = superclass.getWeldMethod(signature);
        }
        return method;
    }

    /**
     * Gets a string representation of the class
     *
     * @return A string representation
     */
    @Override
    public String toString() {
        return Formats.formatModifiers(getJavaClass().getModifiers()) + Formats.formatAnnotations(getAnnotations()) + " class " + getName() + Formats.formatActualTypeArguments(getActualTypeArguments());
    }

    public String getSimpleName() {
        return getJavaClass().getSimpleName();
    }

    /**
     * Indicates if the type is static
     *
     * @return True if static, false otherwise
     * @see org.jboss.weld.introspector.WeldAnnotated#isStatic()
     */
    public boolean isStatic() {
        return Reflections.isStatic(getDelegate());
    }

    /**
     * Indicates if the type if final
     *
     * @return True if final, false otherwise
     * @see org.jboss.weld.introspector.WeldAnnotated#isFinal()
     */
    public boolean isFinal() {
        return Reflections.isFinal(getDelegate());
    }

    public boolean isPublic() {
        return Modifier.isFinal(getJavaClass().getModifiers());
    }

    public boolean isGeneric() {
        return getJavaClass().getTypeParameters().length > 0;
    }

    /**
     * Gets the name of the type
     *
     * @return The name
     * @see org.jboss.weld.introspector.WeldAnnotated#getName()
     */
    public String getName() {
        return getJavaClass().getName();
    }

    /**
     * Gets the superclass abstraction of the type
     *
     * @return The superclass abstraction
     */
    public WeldClass<? super T> getWeldSuperclass() {
        return superclass;
    }

    public boolean isEquivalent(Class<?> clazz) {
        return getDelegate().equals(clazz);
    }

    public boolean isPrivate() {
        return Modifier.isPrivate(getJavaClass().getModifiers());
    }

    public boolean isPackagePrivate() {
        return Reflections.isPackagePrivate(getJavaClass().getModifiers());
    }

    public Package getPackage() {
        return getJavaClass().getPackage();
    }

    public <U> WeldClass<? extends U> asWeldSubclass(WeldClass<U> clazz) {
        return cast(this);
    }

    public <S> S cast(Object object) {
        return Reflections.cast(object);
    }

    public Set<AnnotatedConstructor<T>> getConstructors() {
        return Collections.unmodifiableSet(Reflections.<Set<AnnotatedConstructor<T>>>cast(constructors));
    }

    public Set<AnnotatedField<? super T>> getFields() {
        return cast(fields);
    }

    public Set<AnnotatedMethod<? super T>> getMethods() {
        return cast(methods);
    }

    public Set<Annotation> getDeclaredMetaAnnotations(Class<? extends Annotation> metaAnnotationType) {
        return Collections.unmodifiableSet(new ArraySet<Annotation>(declaredMetaAnnotationMap.get(metaAnnotationType)));
    }

    public boolean isDiscovered() {
        return discovered;
    }

    public boolean isModified() {
        return modified;
    }
}
