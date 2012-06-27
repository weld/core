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
package org.jboss.weld.annotated.enhanced.jlr;

import static org.jboss.weld.util.collections.WeldCollections.immutableMap;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;

import org.jboss.weld.annotated.enhanced.ConstructorSignature;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedConstructor;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedField;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedMethod;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.annotated.enhanced.MethodSignature;
import org.jboss.weld.annotated.slim.SlimAnnotatedType;
import org.jboss.weld.annotated.slim.backed.BackedAnnotatedType;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.util.collections.ArraySet;
import org.jboss.weld.util.collections.ArraySetMultimap;
import org.jboss.weld.util.reflection.Formats;
import org.jboss.weld.util.reflection.Reflections;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Sets;

/**
 * Represents an annotated class
 * <p/>
 * This class is immutable, and therefore threadsafe
 *
 * @param <T> the type of the class
 * @author Pete Muir
 * @author David Allen
 * @author Ales Justin
 */
public class EnhancedAnnotatedTypeImpl<T> extends AbstractEnhancedAnnotated<T, Class<T>> implements EnhancedAnnotatedType<T> {

    // Class attributes
    private final EnhancedAnnotatedType<? super T> superclass;

    // The set of abstracted fields
    private final Set<EnhancedAnnotatedField<?, ? super T>> fields;
    // The map from annotation type to abstracted field with annotation
    private final ArrayListMultimap<Class<? extends Annotation>, EnhancedAnnotatedField<?, ?>> annotatedFields;

    // The set of abstracted fields
    private final ArraySet<EnhancedAnnotatedField<?, ? super T>> declaredFields;
    // The map from annotation type to abstracted field with annotation
    private final ArrayListMultimap<Class<? extends Annotation>, EnhancedAnnotatedField<?, ? super T>> declaredAnnotatedFields;

    // The set of abstracted methods
    private final Set<EnhancedAnnotatedMethod<?, ? super T>> methods;
    // The map from annotation type to abstracted method with annotation
    private final ArrayListMultimap<Class<? extends Annotation>, EnhancedAnnotatedMethod<?, ?>> annotatedMethods;

    // The set of abstracted methods
    private final ArraySet<EnhancedAnnotatedMethod<?, ? super T>> declaredMethods;
    // The map from annotation type to abstracted method with annotation
    private final ArrayListMultimap<Class<? extends Annotation>, EnhancedAnnotatedMethod<?, ? super T>> declaredAnnotatedMethods;
    // The map from annotation type to method with a parameter with annotation
    private final ArrayListMultimap<Class<? extends Annotation>, EnhancedAnnotatedMethod<?, ? super T>> declaredMethodsByAnnotatedParameters;

    // The set of abstracted constructors
    private final ArraySet<EnhancedAnnotatedConstructor<T>> constructors;
    private final Map<ConstructorSignature, EnhancedAnnotatedConstructor<?>> declaredConstructorsBySignature;

    // The meta-annotation map (annotation type -> set of annotations containing
    // meta-annotation) of the item
    private final Map<Class<? extends Annotation>, List<Annotation>> declaredMetaAnnotationMap;

    private final boolean discovered;

    private final AnnotatedType<T> slim;

    public static <T> EnhancedAnnotatedType<T> of(SlimAnnotatedType<T> annotatedType, ClassTransformer classTransformer) {
        if (annotatedType instanceof BackedAnnotatedType<?>) {
            return new EnhancedAnnotatedTypeImpl<T>(annotatedType, buildAnnotationMap(annotatedType.getAnnotations()), buildAnnotationMap(classTransformer.getReflectionCache().getDeclaredAnnotations(annotatedType.getJavaClass())), classTransformer);
        } else {
            return new EnhancedAnnotatedTypeImpl<T>(annotatedType, buildAnnotationMap(annotatedType.getAnnotations()), buildAnnotationMap(annotatedType.getAnnotations()), classTransformer);
        }
    }

    protected EnhancedAnnotatedTypeImpl(SlimAnnotatedType<T> annotatedType, Map<Class<? extends Annotation>, Annotation> annotationMap, Map<Class<? extends Annotation>, Annotation> declaredAnnotationMap, ClassTransformer classTransformer) {
        super(annotatedType, annotationMap, declaredAnnotationMap, classTransformer);
        this.slim = annotatedType;
        discovered = annotatedType instanceof BackedAnnotatedType<?>;

        if (discovered) {
            Class<? super T> superclass = annotatedType.getJavaClass().getSuperclass();
            if (superclass == null) {
                this.superclass = null;
            } else {
                this.superclass = classTransformer.getEnhancedAnnotatedType(superclass);
            }
        } else {
            this.superclass = classTransformer.getEnhancedAnnotatedType(Object.class);
        }

        // Assign class field information
        this.declaredAnnotatedFields = ArrayListMultimap.<Class<? extends Annotation>, EnhancedAnnotatedField<?, ? super T>>create();
        Set<EnhancedAnnotatedField<?, ? super T>> fieldsTemp = null;
        ArrayList<EnhancedAnnotatedField<?, ? super T>> declaredFieldsTemp = new ArrayList<EnhancedAnnotatedField<?, ? super T>>();

        Class<T> javaClass = annotatedType.getJavaClass();

        if (discovered) {
            this.annotatedFields = null;
            if (javaClass != Object.class) {
                for (AnnotatedField<? super T> field : annotatedType.getFields()) {
                    if (field.getJavaMember().getDeclaringClass().equals(javaClass)) {
                        EnhancedAnnotatedField<?, ? super T> annotatedField = EnhancedAnnotatedFieldImpl.of(field, this, classTransformer);
                        declaredFieldsTemp.add(annotatedField);
                        for (Annotation annotation : annotatedField.getAnnotations()) {
                            this.declaredAnnotatedFields.put(annotation.annotationType(), annotatedField);
                        }
                    }
                }
                fieldsTemp = new ArraySet<EnhancedAnnotatedField<?, ? super T>>(declaredFieldsTemp).trimToSize();
                if ((superclass != null) && (superclass.getJavaClass() != Object.class)) {
                    fieldsTemp = Sets.union(fieldsTemp, Reflections.<Set<EnhancedAnnotatedField<?, ? super T>>>cast(superclass.getFields()));
                }
            }
            this.declaredFields = new ArraySet<EnhancedAnnotatedField<?, ? super T>>(declaredFieldsTemp);
        } else {
            this.annotatedFields = ArrayListMultimap.<Class<? extends Annotation>, EnhancedAnnotatedField<?, ?>>create();
            fieldsTemp = new HashSet<EnhancedAnnotatedField<?, ? super T>>();
            for (AnnotatedField<? super T> annotatedField : annotatedType.getFields()) {
                EnhancedAnnotatedField<?, ? super T> weldField = EnhancedAnnotatedFieldImpl.of(annotatedField, this, classTransformer);
                fieldsTemp.add(weldField);
                if (annotatedField.getDeclaringType().getJavaClass().equals(javaClass)) {
                    declaredFieldsTemp.add(weldField);
                }
                for (Annotation annotation : weldField.getAnnotations()) {
                    this.annotatedFields.put(annotation.annotationType(), weldField);
                    if (annotatedField.getDeclaringType().getJavaClass().equals(javaClass)) {
                        this.declaredAnnotatedFields.put(annotation.annotationType(), weldField);
                    }
                }
            }
            this.declaredFields = new ArraySet<EnhancedAnnotatedField<?, ? super T>>(declaredFieldsTemp);
            fieldsTemp = new ArraySet<EnhancedAnnotatedField<?, ? super T>>(fieldsTemp).trimToSize();
            this.annotatedFields.trimToSize();
        }
        this.fields = fieldsTemp;
        this.declaredFields.trimToSize();
        this.declaredAnnotatedFields.trimToSize();

        // Assign constructor information
        this.constructors = new ArraySet<EnhancedAnnotatedConstructor<T>>();

        this.declaredConstructorsBySignature = new HashMap<ConstructorSignature, EnhancedAnnotatedConstructor<?>>();
        for (AnnotatedConstructor<T> constructor : annotatedType.getConstructors()) {
            EnhancedAnnotatedConstructor<T> weldConstructor = EnhancedAnnotatedConstructorImpl.of(constructor, this, classTransformer);
            this.constructors.add(weldConstructor);
            this.declaredConstructorsBySignature.put(weldConstructor.getSignature(), weldConstructor);
        }
        this.constructors.trimToSize();

        // Assign method information
        this.declaredAnnotatedMethods = ArrayListMultimap.<Class<? extends Annotation>, EnhancedAnnotatedMethod<?, ? super T>>create();
        this.declaredMethodsByAnnotatedParameters = ArrayListMultimap.<Class<? extends Annotation>, EnhancedAnnotatedMethod<?, ? super T>>create();

        Set<EnhancedAnnotatedMethod<?, ? super T>> methodsTemp = null;
        ArrayList<EnhancedAnnotatedMethod<?, ? super T>> declaredMethodsTemp = new ArrayList<EnhancedAnnotatedMethod<?, ? super T>>();
        if (discovered) {
            this.annotatedMethods = null;
            if (!(javaClass.equals(Object.class))) {
                for (AnnotatedMethod<? super T> method : annotatedType.getMethods()) {
                    if (method.getJavaMember().getDeclaringClass().equals(javaClass)) {
                        EnhancedAnnotatedMethod<?, ? super T> weldMethod = EnhancedAnnotatedMethodImpl.of(method, this, classTransformer);
                        declaredMethodsTemp.add(weldMethod);
                        for (Annotation annotation : weldMethod.getAnnotations()) {
                            this.declaredAnnotatedMethods.put(annotation.annotationType(), weldMethod);
                        }
                        for (Class<? extends Annotation> annotationType : EnhancedAnnotatedMethod.MAPPED_PARAMETER_ANNOTATIONS) {
                            if (weldMethod.getEnhancedParameters(annotationType).size() > 0) {
                                this.declaredMethodsByAnnotatedParameters.put(annotationType, weldMethod);
                            }
                        }
                    }
                }
                methodsTemp = new ArraySet<EnhancedAnnotatedMethod<?, ? super T>>(declaredMethodsTemp).trimToSize();
                if (superclass != null) {
                    EnhancedAnnotatedType<?> current = superclass;
                    while (current.getJavaClass() != Object.class) {
                        Set<EnhancedAnnotatedMethod<?, ? super T>> superClassMethods = Reflections.cast(current.getDeclaredEnhancedMethods());
                        methodsTemp = Sets.union(methodsTemp, superClassMethods);
                        current = current.getEnhancedSuperclass();
                    }
                }
            }
            this.declaredMethods = new ArraySet<EnhancedAnnotatedMethod<?, ? super T>>(declaredMethodsTemp);
        } else {
            this.annotatedMethods = ArrayListMultimap.<Class<? extends Annotation>, EnhancedAnnotatedMethod<?, ?>> create();
            methodsTemp = new HashSet<EnhancedAnnotatedMethod<?, ? super T>>();
            for (AnnotatedMethod<? super T> method : annotatedType.getMethods()) {
                EnhancedAnnotatedMethod<?, ? super T> enhancedMethod = EnhancedAnnotatedMethodImpl.of(method, this, classTransformer);
                methodsTemp.add(enhancedMethod);
                if (method.getJavaMember().getDeclaringClass().equals(javaClass)) {
                    declaredMethodsTemp.add(enhancedMethod);
                }
                for (Annotation annotation : enhancedMethod.getAnnotations()) {
                    annotatedMethods.put(annotation.annotationType(), enhancedMethod);
                    if (method.getJavaMember().getDeclaringClass().equals(javaClass)) {
                        this.declaredAnnotatedMethods.put(annotation.annotationType(), enhancedMethod);
                    }
                }
                for (Class<? extends Annotation> annotationType : EnhancedAnnotatedMethod.MAPPED_PARAMETER_ANNOTATIONS) {
                    if (enhancedMethod.getEnhancedParameters(annotationType).size() > 0) {
                        if (method.getJavaMember().getDeclaringClass().equals(javaClass)) {
                            this.declaredMethodsByAnnotatedParameters.put(annotationType, enhancedMethod);
                        }
                    }
                }
            }
            this.declaredMethods = new ArraySet<EnhancedAnnotatedMethod<?, ? super T>>(declaredMethodsTemp);
            methodsTemp = new ArraySet<EnhancedAnnotatedMethod<?, ? super T>>(methodsTemp).trimToSize();
            this.annotatedMethods.trimToSize();
        }

        this.methods = methodsTemp;
        this.declaredMethods.trimToSize();
        this.declaredAnnotatedMethods.trimToSize();
        this.declaredMethodsByAnnotatedParameters.trimToSize();

        ArraySetMultimap<Class<? extends Annotation>, Annotation> declaredMetaAnnotationMap = new ArraySetMultimap<Class<? extends Annotation>, Annotation>();
        for (Annotation declaredAnnotation : declaredAnnotationMap.values()) {
            addMetaAnnotations(declaredMetaAnnotationMap, declaredAnnotation, classTransformer.getReflectionCache().getAnnotations(declaredAnnotation.annotationType()), true);
            addMetaAnnotations(declaredMetaAnnotationMap, declaredAnnotation, classTransformer.getTypeStore().get(declaredAnnotation.annotationType()), true);
            declaredMetaAnnotationMap.putSingleElement(declaredAnnotation.annotationType(), declaredAnnotation);
        }
        this.declaredMetaAnnotationMap = immutableMap(declaredMetaAnnotationMap);
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
    public Collection<EnhancedAnnotatedField<?, ? super T>> getEnhancedFields() {
        return Collections.unmodifiableCollection(fields);
    }

    public Collection<EnhancedAnnotatedField<?, ? super T>> getDeclaredEnhancedFields() {
        return Collections.unmodifiableCollection(declaredFields);
    }

    public <F> EnhancedAnnotatedField<F, ?> getDeclaredEnhancedField(String fieldName) {
        for (EnhancedAnnotatedField<?, ?> field : declaredFields) {
            if (field.getName().equals(fieldName)) {
                return cast(field);
            }
        }
        return null;
    }

    public Collection<EnhancedAnnotatedField<?, ? super T>> getDeclaredEnhancedFields(Class<? extends Annotation> annotationType) {
        return Collections.unmodifiableCollection(declaredAnnotatedFields.get(annotationType));
    }

    public EnhancedAnnotatedConstructor<T> getDeclaredEnhancedConstructor(ConstructorSignature signature) {
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
    public Collection<EnhancedAnnotatedField<?, ?>> getEnhancedFields(Class<? extends Annotation> annotationType) {
        if (annotatedFields == null) {
            // Build collection from class hierarchy
            ArrayList<EnhancedAnnotatedField<?, ?>> aggregatedFields = new ArrayList<EnhancedAnnotatedField<?, ?>>(this.declaredAnnotatedFields.get(annotationType));
            if ((superclass != null) && (superclass.getJavaClass() != Object.class)) {
                aggregatedFields.addAll(superclass.getEnhancedFields(annotationType));
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
     * @see org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType#getEnhancedMethods(Class)
     */
    public Collection<EnhancedAnnotatedMethod<?, ?>> getEnhancedMethods(Class<? extends Annotation> annotationType) {
        if (annotatedMethods == null) {
            ArrayList<EnhancedAnnotatedMethod<?, ?>> aggregateMethods = new ArrayList<EnhancedAnnotatedMethod<?, ?>>(this.declaredAnnotatedMethods.get(annotationType));
            if ((superclass != null) && (superclass.getJavaClass() != Object.class)) {
                aggregateMethods.addAll(superclass.getDeclaredEnhancedMethods(annotationType));
            }
            return Collections.unmodifiableCollection(aggregateMethods);
        } else {
            return Collections.unmodifiableCollection(annotatedMethods.get(annotationType));
        }
    }

    public Collection<EnhancedAnnotatedMethod<?, ? super T>> getDeclaredEnhancedMethods(Class<? extends Annotation> annotationType) {
        return Collections.unmodifiableCollection(declaredAnnotatedMethods.get(annotationType));
    }

    public Collection<EnhancedAnnotatedConstructor<T>> getEnhancedConstructors() {
        return Collections.unmodifiableCollection(constructors);
    }

    /**
     * Gets constructors with given annotation type
     *
     * @param annotationType The annotation type to match
     * @return A set of abstracted constructors with given annotation type. If
     *         the constructors set is empty, initialize it first. Returns an
     *         empty set if there are no matches.
     * @see org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType#getEnhancedConstructors(Class)
     */
    public Collection<EnhancedAnnotatedConstructor<T>> getEnhancedConstructors(Class<? extends Annotation> annotationType) {
        Set<EnhancedAnnotatedConstructor<T>> ret = new HashSet<EnhancedAnnotatedConstructor<T>>();
        for (EnhancedAnnotatedConstructor<T> constructor : constructors) {
            if (constructor.isAnnotationPresent(annotationType)) {
                ret.add(constructor);
            }
        }
        return ret;
    }

    public EnhancedAnnotatedConstructor<T> getNoArgsEnhancedConstructor() {
        for (EnhancedAnnotatedConstructor<T> constructor : constructors) {
            if (constructor.getJavaMember().getParameterTypes().length == 0) {
                return constructor;
            }
        }
        return null;
    }

    public Collection<EnhancedAnnotatedMethod<?, ? super T>> getDeclaredEnhancedMethodsWithAnnotatedParameters(Class<? extends Annotation> annotationType) {
        return Collections.unmodifiableCollection(declaredMethodsByAnnotatedParameters.get(annotationType));
    }

    public EnhancedAnnotatedMethod<?, ?> getEnhancedMethod(Method methodDescriptor) {
        // TODO Should be cached
        for (EnhancedAnnotatedMethod<?, ?> annotatedMethod : getEnhancedMethods()) {
            if (annotatedMethod.getName().equals(methodDescriptor.getName()) && Arrays.equals(annotatedMethod.getParameterTypesAsArray(), methodDescriptor.getParameterTypes())) {
                return annotatedMethod;
            }
        }
        return null;
    }

    public Collection<EnhancedAnnotatedMethod<?, ? super T>> getEnhancedMethods() {
        return Collections.unmodifiableSet(methods);
    }

    public EnhancedAnnotatedMethod<?, ?> getDeclaredEnhancedMethod(Method method) {
        // TODO Should be cached
        for (EnhancedAnnotatedMethod<?, ?> annotatedMethod : declaredMethods) {
            if (annotatedMethod.getName().equals(method.getName()) && Arrays.equals(annotatedMethod.getParameterTypesAsArray(), method.getParameterTypes())) {
                return annotatedMethod;
            }
        }
        return null;
    }

    public Collection<EnhancedAnnotatedMethod<?, ? super T>> getDeclaredEnhancedMethods() {
        return Collections.unmodifiableSet(declaredMethods);
    }

    public <M> EnhancedAnnotatedMethod<M, ?> getDeclaredEnhancedMethod(MethodSignature signature) {
        for (EnhancedAnnotatedMethod<?, ? super T> method : declaredMethods) {
            if (method.getSignature().equals(signature)) {
                return cast(method);
            }
        }
        return null;
    }

    public <M> EnhancedAnnotatedMethod<M, ?> getEnhancedMethod(MethodSignature signature) {
        EnhancedAnnotatedMethod<M, ?> method = cast(getDeclaredEnhancedMethod(signature));
        if ((method == null) && (superclass != null) && (superclass.getJavaClass() != Object.class)) {
            method = superclass.getEnhancedMethod(signature);
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
        return Formats.formatAnnotatedType(this);
    }

    public String getSimpleName() {
        return getJavaClass().getSimpleName();
    }

    /**
     * Indicates if the type is static
     *
     * @return True if static, false otherwise
     * @see org.jboss.weld.annotated.enhanced.EnhancedAnnotated#isStatic()
     */
    public boolean isStatic() {
        return Reflections.isStatic(getDelegate());
    }

    /**
     * Indicates if the type if final
     *
     * @return True if final, false otherwise
     * @see org.jboss.weld.annotated.enhanced.EnhancedAnnotated#isFinal()
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
     * @returns The name
     * @see org.jboss.weld.annotated.enhanced.EnhancedAnnotated#getName()
     */
    public String getName() {
        return getJavaClass().getName();
    }

    /**
     * Gets the superclass abstraction of the type
     *
     * @return The superclass abstraction
     */
    public EnhancedAnnotatedType<? super T> getEnhancedSuperclass() {
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

    public <U> EnhancedAnnotatedType<? extends U> asEnhancedSubclass(EnhancedAnnotatedType<U> clazz) {
        return cast(this);
    }

    public <S> S cast(Object object) {
        return Reflections.<S>cast(object);
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

    @Override
    public AnnotatedType<T> slim() {
        return slim;
    }

}
