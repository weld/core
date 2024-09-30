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

import static org.jboss.weld.util.collections.WeldCollections.immutableSetView;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.spi.AnnotatedConstructor;
import jakarta.enterprise.inject.spi.AnnotatedField;
import jakarta.enterprise.inject.spi.AnnotatedMethod;

import org.jboss.weld.annotated.enhanced.ConstructorSignature;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedConstructor;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedField;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedMethod;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.annotated.enhanced.MethodSignature;
import org.jboss.weld.annotated.slim.AnnotatedTypeIdentifier;
import org.jboss.weld.annotated.slim.SlimAnnotatedType;
import org.jboss.weld.annotated.slim.backed.BackedAnnotatedType;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.util.collections.ImmutableSet;
import org.jboss.weld.util.collections.ListMultimap;
import org.jboss.weld.util.collections.Multimap;
import org.jboss.weld.util.collections.Multimaps;
import org.jboss.weld.util.collections.SetMultimap;
import org.jboss.weld.util.collections.Sets;
import org.jboss.weld.util.reflection.Formats;
import org.jboss.weld.util.reflection.Reflections;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

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

    @SuppressFBWarnings("unchecked")
    private static final Set<Class<? extends Annotation>> MAPPED_METHOD_PARAMETER_ANNOTATIONS = ImmutableSet.of(Observes.class,
            ObservesAsync.class);
    @SuppressFBWarnings("unchecked")
    private static final Set<Class<? extends Annotation>> MAPPED_DECLARED_METHOD_PARAMETER_ANNOTATIONS = ImmutableSet
            .of(Disposes.class);

    // Class attributes
    private final EnhancedAnnotatedType<? super T> superclass;

    // The set of abstracted fields
    private final Set<EnhancedAnnotatedField<?, ? super T>> fields;
    // The map from annotation type to abstracted field with annotation
    private final Multimap<Class<? extends Annotation>, EnhancedAnnotatedField<?, ?>> annotatedFields;

    // The set of abstracted fields
    private final Set<EnhancedAnnotatedField<?, ? super T>> declaredFields;
    // The map from annotation type to abstracted field with annotation
    private final Multimap<Class<? extends Annotation>, EnhancedAnnotatedField<?, ? super T>> declaredAnnotatedFields;

    // The set of abstracted methods
    private final Set<EnhancedAnnotatedMethod<?, ? super T>> methods;
    // The map from annotation type to abstracted method with annotation
    private final Multimap<Class<? extends Annotation>, EnhancedAnnotatedMethod<?, ? super T>> annotatedMethods;
    // Methods that are overridden by other methods
    private Set<EnhancedAnnotatedMethod<?, ? super T>> overriddenMethods;

    private final Multimap<Class<? extends Annotation>, EnhancedAnnotatedMethod<?, ? super T>> annotatedMethodsByAnnotatedParameters;

    // The set of abstracted methods
    private final Set<EnhancedAnnotatedMethod<?, ? super T>> declaredMethods;
    // The map from annotation type to abstracted method with annotation
    private final Multimap<Class<? extends Annotation>, EnhancedAnnotatedMethod<?, ? super T>> declaredAnnotatedMethods;
    // The map from annotation type to method with a parameter with annotation
    private final Multimap<Class<? extends Annotation>, EnhancedAnnotatedMethod<?, ? super T>> declaredMethodsByAnnotatedParameters;

    // The set of abstracted constructors
    private final Set<EnhancedAnnotatedConstructor<T>> constructors;
    private final Map<ConstructorSignature, EnhancedAnnotatedConstructor<?>> declaredConstructorsBySignature;

    // The meta-annotation map (annotation type -> set of annotations containing
    // meta-annotation) of the item
    private final Multimap<Class<? extends Annotation>, Annotation> declaredMetaAnnotationMap;

    private final boolean discovered;

    private final SlimAnnotatedType<T> slim;

    public static <T> EnhancedAnnotatedType<T> of(SlimAnnotatedType<T> annotatedType, ClassTransformer classTransformer) {
        if (annotatedType instanceof BackedAnnotatedType<?>) {
            return new EnhancedAnnotatedTypeImpl<T>(annotatedType, buildAnnotationMap(annotatedType.getAnnotations()),
                    buildAnnotationMap(
                            classTransformer.getReflectionCache().getDeclaredAnnotations(annotatedType.getJavaClass())),
                    classTransformer);
        } else {
            return new EnhancedAnnotatedTypeImpl<T>(annotatedType, buildAnnotationMap(annotatedType.getAnnotations()),
                    buildAnnotationMap(annotatedType.getAnnotations()), classTransformer);
        }
    }

    @SuppressWarnings("unchecked")
    protected EnhancedAnnotatedTypeImpl(SlimAnnotatedType<T> annotatedType,
            Map<Class<? extends Annotation>, Annotation> annotationMap,
            Map<Class<? extends Annotation>, Annotation> declaredAnnotationMap, ClassTransformer classTransformer) {
        super(annotatedType, annotationMap, declaredAnnotationMap, classTransformer);
        this.slim = annotatedType;
        discovered = annotatedType instanceof BackedAnnotatedType<?>;

        if (discovered) {
            Class<? super T> superclass = annotatedType.getJavaClass().getSuperclass();
            if (superclass == null) {
                this.superclass = null;
            } else {
                this.superclass = classTransformer.getEnhancedAnnotatedType(superclass, slim.getIdentifier().getBdaId());
            }
        } else {
            EnhancedAnnotatedType<? super T> superclassAt;
            Class<? super T> superclass = annotatedType.getJavaClass().getSuperclass();
            if (superclass == null) {
                superclassAt = classTransformer.getEnhancedAnnotatedType(Object.class, AnnotatedTypeIdentifier.NULL_BDA_ID);
            } else {
                superclassAt = classTransformer.getEnhancedAnnotatedType(superclass, slim.getIdentifier().getBdaId());
            }
            this.superclass = superclassAt;
        }

        // Assign class field information
        Multimap<Class<? extends Annotation>, EnhancedAnnotatedField<?, ? super T>> declaredAnnotatedFields = new ListMultimap<Class<? extends Annotation>, EnhancedAnnotatedField<?, ? super T>>();
        Set<EnhancedAnnotatedField<?, ? super T>> fieldsTemp = null;
        ArrayList<EnhancedAnnotatedField<?, ? super T>> declaredFieldsTemp = new ArrayList<EnhancedAnnotatedField<?, ? super T>>();

        Class<T> javaClass = annotatedType.getJavaClass();

        if (discovered) {
            this.annotatedFields = null;
            if (javaClass != Object.class) {
                for (AnnotatedField<? super T> field : annotatedType.getFields()) {
                    if (field.getJavaMember().getDeclaringClass().equals(javaClass)) {
                        EnhancedAnnotatedField<?, ? super T> annotatedField = EnhancedAnnotatedFieldImpl.of(field, this,
                                classTransformer);
                        declaredFieldsTemp.add(annotatedField);
                        for (Annotation annotation : annotatedField.getAnnotations()) {
                            declaredAnnotatedFields.put(annotation.annotationType(), annotatedField);
                        }
                    }
                }
                fieldsTemp = new HashSet<EnhancedAnnotatedField<?, ? super T>>(declaredFieldsTemp);
                if ((superclass != null) && (superclass.getJavaClass() != Object.class)) {
                    fieldsTemp = Sets.union(fieldsTemp,
                            Reflections.<Set<EnhancedAnnotatedField<?, ? super T>>> cast(superclass.getFields()));
                }
            }
            this.declaredFields = new HashSet<EnhancedAnnotatedField<?, ? super T>>(declaredFieldsTemp);
        } else {
            Multimap<Class<? extends Annotation>, EnhancedAnnotatedField<?, ?>> annotatedFields = new ListMultimap<>();
            fieldsTemp = new HashSet<EnhancedAnnotatedField<?, ? super T>>();
            for (AnnotatedField<? super T> annotatedField : annotatedType.getFields()) {
                EnhancedAnnotatedField<?, ? super T> weldField = EnhancedAnnotatedFieldImpl.of(annotatedField, this,
                        classTransformer);
                fieldsTemp.add(weldField);
                if (annotatedField.getJavaMember().getDeclaringClass().equals(javaClass)) {
                    declaredFieldsTemp.add(weldField);
                }
                for (Annotation annotation : weldField.getAnnotations()) {
                    annotatedFields.put(annotation.annotationType(), weldField);
                    if (annotatedField.getJavaMember().getDeclaringClass().equals(javaClass)) {
                        declaredAnnotatedFields.put(annotation.annotationType(), weldField);
                    }
                }
            }
            this.annotatedFields = Multimaps.unmodifiableMultimap(annotatedFields);
            this.declaredFields = new HashSet<EnhancedAnnotatedField<?, ? super T>>(declaredFieldsTemp);
        }
        this.fields = fieldsTemp;
        this.declaredAnnotatedFields = Multimaps.unmodifiableMultimap(declaredAnnotatedFields);

        // Assign constructor information
        this.constructors = new HashSet<EnhancedAnnotatedConstructor<T>>();

        this.declaredConstructorsBySignature = new HashMap<ConstructorSignature, EnhancedAnnotatedConstructor<?>>();
        for (AnnotatedConstructor<T> constructor : annotatedType.getConstructors()) {
            EnhancedAnnotatedConstructor<T> weldConstructor = EnhancedAnnotatedConstructorImpl.of(constructor, this,
                    classTransformer);
            this.constructors.add(weldConstructor);
            this.declaredConstructorsBySignature.put(weldConstructor.getSignature(), weldConstructor);
        }

        // Assign method information
        Multimap<Class<? extends Annotation>, EnhancedAnnotatedMethod<?, ? super T>> declaredAnnotatedMethods = new ListMultimap<Class<? extends Annotation>, EnhancedAnnotatedMethod<?, ? super T>>();
        Multimap<Class<? extends Annotation>, EnhancedAnnotatedMethod<?, ? super T>> declaredMethodsByAnnotatedParameters = new ListMultimap<Class<? extends Annotation>, EnhancedAnnotatedMethod<?, ? super T>>();

        Set<EnhancedAnnotatedMethod<?, ? super T>> methodsTemp = new HashSet<EnhancedAnnotatedMethod<?, ? super T>>();
        ArrayList<EnhancedAnnotatedMethod<?, ? super T>> declaredMethodsTemp = new ArrayList<EnhancedAnnotatedMethod<?, ? super T>>();
        if (discovered) {
            if (!(javaClass.equals(Object.class))) {
                for (AnnotatedMethod<? super T> method : annotatedType.getMethods()) {
                    if (method.getJavaMember().getDeclaringClass().equals(javaClass)) {
                        EnhancedAnnotatedMethod<?, ? super T> weldMethod = EnhancedAnnotatedMethodImpl.of(method, this,
                                classTransformer);
                        declaredMethodsTemp.add(weldMethod);
                        for (Annotation annotation : weldMethod.getAnnotations()) {
                            declaredAnnotatedMethods.put(annotation.annotationType(), weldMethod);
                        }
                        for (Class<? extends Annotation> annotationType : MAPPED_DECLARED_METHOD_PARAMETER_ANNOTATIONS) {
                            if (weldMethod.getEnhancedParameters(annotationType).size() > 0) {
                                declaredMethodsByAnnotatedParameters.put(annotationType, weldMethod);
                            }
                        }
                    }
                }
                methodsTemp.addAll(declaredMethodsTemp);
                if (superclass != null) {
                    EnhancedAnnotatedType<?> current = superclass;
                    while (current.getJavaClass() != Object.class) {
                        Set<EnhancedAnnotatedMethod<?, ? super T>> superClassMethods = Reflections
                                .cast(current.getDeclaredEnhancedMethods());
                        methodsTemp.addAll(superClassMethods);
                        current = current.getEnhancedSuperclass();
                    }
                }
                // Also add default methods
                for (Class<?> interfaceClazz : Reflections.getInterfaceClosure(javaClass)) {
                    EnhancedAnnotatedType<?> interfaceType = classTransformer.getEnhancedAnnotatedType(interfaceClazz,
                            slim.getIdentifier().getBdaId());
                    for (EnhancedAnnotatedMethod<?, ?> interfaceMethod : interfaceType.getEnhancedMethods()) {
                        if (Reflections.isDefault(interfaceMethod.getJavaMember())) {
                            methodsTemp.add((EnhancedAnnotatedMethod<?, ? super T>) interfaceMethod);
                        }
                    }
                }

            }
            this.declaredMethods = new HashSet<>(declaredMethodsTemp);
        } else {
            for (AnnotatedMethod<? super T> method : annotatedType.getMethods()) {
                EnhancedAnnotatedMethod<?, ? super T> enhancedMethod = EnhancedAnnotatedMethodImpl.of(method, this,
                        classTransformer);
                methodsTemp.add(enhancedMethod);
                if (method.getJavaMember().getDeclaringClass().equals(javaClass)) {
                    declaredMethodsTemp.add(enhancedMethod);
                }
                for (Annotation annotation : enhancedMethod.getAnnotations()) {
                    if (method.getJavaMember().getDeclaringClass().equals(javaClass)) {
                        declaredAnnotatedMethods.put(annotation.annotationType(), enhancedMethod);
                    }
                }
                for (Class<? extends Annotation> annotationType : MAPPED_DECLARED_METHOD_PARAMETER_ANNOTATIONS) {
                    if (enhancedMethod.getEnhancedParameters(annotationType).size() > 0) {
                        if (method.getJavaMember().getDeclaringClass().equals(javaClass)) {
                            declaredMethodsByAnnotatedParameters.put(annotationType, enhancedMethod);
                        }
                    }
                }
            }
            this.declaredMethods = ImmutableSet.copyOf(declaredMethodsTemp);
        }
        this.declaredAnnotatedMethods = Multimaps.unmodifiableMultimap(declaredAnnotatedMethods);
        this.declaredMethodsByAnnotatedParameters = Multimaps.unmodifiableMultimap(declaredMethodsByAnnotatedParameters);

        SetMultimap<Class<? extends Annotation>, Annotation> declaredMetaAnnotationMap = SetMultimap.newSetMultimap();
        processMetaAnnotations(declaredMetaAnnotationMap, declaredAnnotationMap.values(), classTransformer, true);
        this.declaredMetaAnnotationMap = Multimaps.unmodifiableMultimap(declaredMetaAnnotationMap);
        this.overriddenMethods = getOverriddenMethods(this, methodsTemp);

        // WELD-1548 remove all overriden methods except for those which are overriden by a bridge method
        methodsTemp.removeAll(getOverriddenMethods(this, methodsTemp, true));

        this.methods = methodsTemp;
        this.annotatedMethods = buildAnnotatedMethodMultimap(this.methods);
        this.annotatedMethodsByAnnotatedParameters = buildAnnotatedParameterMethodMultimap(this.methods);
    }

    protected Set<EnhancedAnnotatedMethod<?, ? super T>> getOverriddenMethods(EnhancedAnnotatedType<T> annotatedType,
            Set<EnhancedAnnotatedMethod<?, ? super T>> methods) {
        return getOverriddenMethods(annotatedType, methods, false);
    }

    /**
     *
     * @param annotatedType
     * @param methods
     * @param skipOverridingBridgeMethods If set to <code>true</code> the returning set will not contain methods overriden by a
     *        bridge method
     * @return the set of overriden methods
     */
    protected Set<EnhancedAnnotatedMethod<?, ? super T>> getOverriddenMethods(EnhancedAnnotatedType<T> annotatedType,
            Set<EnhancedAnnotatedMethod<?, ? super T>> methods, boolean skipOverridingBridgeMethods) {
        Set<EnhancedAnnotatedMethod<?, ? super T>> overriddenMethods = new HashSet<EnhancedAnnotatedMethod<?, ? super T>>();
        Multimap<MethodSignature, Package> seenMethods = SetMultimap.newSetMultimap();
        for (Class<? super T> clazz = annotatedType.getJavaClass(); clazz != null
                && clazz != Object.class; clazz = clazz.getSuperclass()) {
            for (EnhancedAnnotatedMethod<?, ? super T> method : methods) {
                if (method.getJavaMember().getDeclaringClass().equals(clazz)) {
                    if (skipOverridingBridgeMethods && method.getJavaMember().isBridge()) {
                        continue;
                    }
                    if (isOverridden(method, seenMethods)) {
                        overriddenMethods.add(method);
                    }
                    seenMethods.put(method.getSignature(), method.getPackage());
                }
            }
        }
        return immutableSetView(overriddenMethods);
    }

    protected Multimap<Class<? extends Annotation>, EnhancedAnnotatedMethod<?, ? super T>> buildAnnotatedMethodMultimap(
            Set<EnhancedAnnotatedMethod<?, ? super T>> effectiveMethods) {
        Multimap<Class<? extends Annotation>, EnhancedAnnotatedMethod<?, ? super T>> result = SetMultimap.newSetMultimap();
        for (EnhancedAnnotatedMethod<?, ? super T> method : effectiveMethods) {
            for (Annotation ann : method.getAnnotations()) {
                result.put(ann.annotationType(), method);
            }
        }
        return Multimaps.unmodifiableMultimap(result);
    }

    protected Multimap<Class<? extends Annotation>, EnhancedAnnotatedMethod<?, ? super T>> buildAnnotatedParameterMethodMultimap(
            Set<EnhancedAnnotatedMethod<?, ? super T>> effectiveMethods) {
        Multimap<Class<? extends Annotation>, EnhancedAnnotatedMethod<?, ? super T>> result = SetMultimap.newSetMultimap();
        for (EnhancedAnnotatedMethod<?, ? super T> method : effectiveMethods) {
            for (Class<? extends Annotation> annotation : MAPPED_METHOD_PARAMETER_ANNOTATIONS) {
                if (!method.getEnhancedParameters(annotation).isEmpty()) {
                    result.put(annotation, method);
                }
            }
        }
        return Multimaps.unmodifiableMultimap(result);
    }

    private static boolean isOverridden(EnhancedAnnotatedMethod<?, ?> method, Multimap<MethodSignature, Package> seenMethods) {
        if (method.isPrivate()) {
            return false;
        } else if (method.isPackagePrivate() && seenMethods.containsKey(method.getSignature())) {
            return seenMethods.get(method.getSignature()).contains(method.getPackage());
        } else {
            return seenMethods.containsKey(method.getSignature());
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
    @Override
    public Collection<EnhancedAnnotatedField<?, ? super T>> getEnhancedFields() {
        return Collections.unmodifiableCollection(fields);
    }

    @Override
    public Collection<EnhancedAnnotatedField<?, ? super T>> getDeclaredEnhancedFields() {
        return Collections.unmodifiableCollection(declaredFields);
    }

    @Override
    public <F> EnhancedAnnotatedField<F, ?> getDeclaredEnhancedField(String fieldName) {
        for (EnhancedAnnotatedField<?, ?> field : declaredFields) {
            if (field.getName().equals(fieldName)) {
                return cast(field);
            }
        }
        return null;
    }

    @Override
    public Collection<EnhancedAnnotatedField<?, ? super T>> getDeclaredEnhancedFields(
            Class<? extends Annotation> annotationType) {
        return declaredAnnotatedFields.get(annotationType);
    }

    @Override
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
    @Override
    public Collection<EnhancedAnnotatedField<?, ?>> getEnhancedFields(Class<? extends Annotation> annotationType) {
        if (annotatedFields == null) {
            // Build collection from class hierarchy
            ArrayList<EnhancedAnnotatedField<?, ?>> aggregatedFields = new ArrayList<>(
                    this.declaredAnnotatedFields.get(annotationType));
            if ((superclass != null) && (superclass.getJavaClass() != Object.class)) {
                aggregatedFields.addAll(superclass.getEnhancedFields(annotationType));
            }
            return Collections.unmodifiableCollection(aggregatedFields);
        } else {
            // Return results collected directly from AnnotatedType
            return annotatedFields.get(annotationType);
        }
    }

    @Override
    public boolean isLocalClass() {
        return getJavaClass().isLocalClass();
    }

    @Override
    public boolean isAnonymousClass() {
        return getJavaClass().isAnonymousClass();
    }

    @Override
    public boolean isSealed() {
        return getJavaClass().isSealed();
    }

    @Override
    public boolean isMemberClass() {
        return getJavaClass().isMemberClass();
    }

    @Override
    public boolean isAbstract() {
        return Modifier.isAbstract(getJavaClass().getModifiers());
    }

    @Override
    public boolean isEnum() {
        return getJavaClass().isEnum();
    }

    @Override
    public boolean isSerializable() {
        return Reflections.isSerializable(getJavaClass());
    }

    /**
     * Gets the abstracted methods that have a certain annotation type present
     *
     * @param annotationType The annotation type to match
     * @return A set of matching method abstractions. Returns an empty set if no
     *         matches are found.
     * @see org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType#getEnhancedMethods(Class)
     */
    @Override
    public Collection<EnhancedAnnotatedMethod<?, ? super T>> getEnhancedMethods(Class<? extends Annotation> annotationType) {
        return Collections.unmodifiableCollection(annotatedMethods.get(annotationType));
    }

    @Override
    public Collection<EnhancedAnnotatedMethod<?, ? super T>> getDeclaredEnhancedMethods(
            Class<? extends Annotation> annotationType) {
        return Collections.unmodifiableCollection(declaredAnnotatedMethods.get(annotationType));
    }

    @Override
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
    @Override
    public Collection<EnhancedAnnotatedConstructor<T>> getEnhancedConstructors(Class<? extends Annotation> annotationType) {
        Set<EnhancedAnnotatedConstructor<T>> ret = new HashSet<EnhancedAnnotatedConstructor<T>>();
        for (EnhancedAnnotatedConstructor<T> constructor : constructors) {
            if (constructor.isAnnotationPresent(annotationType)) {
                ret.add(constructor);
            }
        }
        return ret;
    }

    @Override
    public EnhancedAnnotatedConstructor<T> getNoArgsEnhancedConstructor() {
        return cast(declaredConstructorsBySignature.get(ConstructorSignatureImpl.NO_ARGS_SIGNATURE));
    }

    @Override
    public Collection<EnhancedAnnotatedMethod<?, ? super T>> getDeclaredEnhancedMethodsWithAnnotatedParameters(
            Class<? extends Annotation> annotationType) {
        return Collections.unmodifiableCollection(declaredMethodsByAnnotatedParameters.get(annotationType));
    }

    @Override
    public Collection<EnhancedAnnotatedMethod<?, ? super T>> getEnhancedMethods() {
        return methods;
    }

    @Override
    public Collection<EnhancedAnnotatedMethod<?, ? super T>> getDeclaredEnhancedMethods() {
        return Collections.unmodifiableSet(declaredMethods);
    }

    @Override
    public <M> EnhancedAnnotatedMethod<M, ?> getDeclaredEnhancedMethod(MethodSignature signature) {
        for (EnhancedAnnotatedMethod<?, ? super T> method : declaredMethods) {
            if (method.getSignature().equals(signature)) {
                return cast(method);
            }
        }
        return null;
    }

    @Override
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

    @Override
    public String getSimpleName() {
        return getJavaClass().getSimpleName();
    }

    /**
     * Indicates if the type is static
     *
     * @return True if static, false otherwise
     * @see org.jboss.weld.annotated.enhanced.EnhancedAnnotated#isStatic()
     */
    @Override
    public boolean isStatic() {
        return Reflections.isStatic(getDelegate());
    }

    /**
     * Indicates if the type if final
     *
     * @return True if final, false otherwise
     * @see org.jboss.weld.annotated.enhanced.EnhancedAnnotated#isFinal()
     */
    @Override
    public boolean isFinal() {
        return Reflections.isFinal(getDelegate());
    }

    @Override
    public boolean isPublic() {
        return Modifier.isFinal(getJavaClass().getModifiers());
    }

    @Override
    public boolean isGeneric() {
        return getJavaClass().getTypeParameters().length > 0;
    }

    /**
     * Gets the name of the type
     *
     * @returns The name
     * @see org.jboss.weld.annotated.enhanced.EnhancedAnnotated#getName()
     */
    @Override
    public String getName() {
        return getJavaClass().getName();
    }

    /**
     * Gets the superclass abstraction of the type
     *
     * @return The superclass abstraction
     */
    @Override
    public EnhancedAnnotatedType<? super T> getEnhancedSuperclass() {
        return superclass;
    }

    @Override
    public boolean isEquivalent(Class<?> clazz) {
        return getDelegate().equals(clazz);
    }

    @Override
    public boolean isPrivate() {
        return Modifier.isPrivate(getJavaClass().getModifiers());
    }

    @Override
    public boolean isPackagePrivate() {
        return Reflections.isPackagePrivate(getJavaClass().getModifiers());
    }

    @Override
    public Package getPackage() {
        return getJavaClass().getPackage();
    }

    @Override
    public <U> EnhancedAnnotatedType<? extends U> asEnhancedSubclass(EnhancedAnnotatedType<U> clazz) {
        return cast(this);
    }

    @Override
    public <S> S cast(Object object) {
        return Reflections.<S> cast(object);
    }

    @Override
    public Set<AnnotatedConstructor<T>> getConstructors() {
        return Collections.unmodifiableSet(Reflections.<Set<AnnotatedConstructor<T>>> cast(constructors));
    }

    @Override
    public Set<AnnotatedField<? super T>> getFields() {
        return cast(fields);
    }

    @Override
    public Set<AnnotatedMethod<? super T>> getMethods() {
        return cast(Sets.union(methods, overriddenMethods));
    }

    @Override
    public Set<Annotation> getDeclaredMetaAnnotations(Class<? extends Annotation> metaAnnotationType) {
        return declaredMetaAnnotationMap.containsKey(metaAnnotationType)
                ? ImmutableSet.copyOf(declaredMetaAnnotationMap.get(metaAnnotationType))
                : Collections.emptySet();
    }

    @Override
    public boolean isDiscovered() {
        return discovered;
    }

    @Override
    public SlimAnnotatedType<T> slim() {
        return slim;
    }

    @Override
    public Collection<EnhancedAnnotatedMethod<?, ? super T>> getEnhancedMethodsWithAnnotatedParameters(
            Class<? extends Annotation> annotationType) {
        return annotatedMethodsByAnnotatedParameters.get(annotationType);
    }

    @Override
    public int hashCode() {
        return slim.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        EnhancedAnnotatedTypeImpl<?> that = cast(obj);
        return slim.equals(that.slim);
    }
}
