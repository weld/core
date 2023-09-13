package org.jboss.weld.lite.extension.translator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.lang.model.AnnotationInfo;
import jakarta.enterprise.lang.model.declarations.MethodInfo;
import jakarta.enterprise.lang.model.declarations.ParameterInfo;
import jakarta.enterprise.lang.model.types.Type;

import org.jboss.weld.lite.extension.translator.util.AnnotationOverrides;

class ParameterInfoImpl extends DeclarationInfoImpl<Parameter, jakarta.enterprise.inject.spi.AnnotatedParameter<?>>
        implements ParameterInfo {
    // only for equals/hashCode and going back to the method
    private final MethodInfoImpl method;
    private final int position;
    private final Boolean isEnumConstructorParam;

    ParameterInfoImpl(jakarta.enterprise.inject.spi.AnnotatedParameter<?> cdiDeclaration, BeanManager bm) {
        super(cdiDeclaration.getJavaParameter(), cdiDeclaration, bm);
        this.method = new MethodInfoImpl(cdiDeclaration.getDeclaringCallable(), bm);
        this.position = cdiDeclaration.getPosition();
        // doesn't matter if it's enum when we have CDI declaration
        this.isEnumConstructorParam = null;
    }

    ParameterInfoImpl(Parameter reflectionDeclaration, MethodInfoImpl backReference, int position,
            boolean isEnumConstructorParam, BeanManager bm) {
        super(reflectionDeclaration, null, bm);
        this.method = backReference;
        this.position = position;
        this.isEnumConstructorParam = isEnumConstructorParam;
    }

    @Override
    public String name() {
        return reflection.getName();
    }

    @Override
    public Type type() {
        // in the vast majority of cases, this condition holds true
        if (canSuperHandleAnnotations()) {
            return TypeImpl.fromReflectionType(reflection.getAnnotatedType(), bm);
        }

        // this only applies in a very specific situation: a method that has synthetic
        // parameters and also annotations on (non-synthetic) parameters (for example,
        // an enum constructor, as in the Lang Model TCK)
        //
        // in such case, reflective access to annotations is inconsistent and we have to
        // compensate for that
        AnnotationOverrides overrides = new AnnotationOverrides(reflection.getDeclaringExecutable()
                .getAnnotatedParameterTypes()[position].getAnnotations());
        return TypeImpl.fromReflectionType(reflection.getAnnotatedType(), overrides, bm);
    }

    @Override
    public MethodInfo declaringMethod() {
        return method;
    }

    private boolean canSuperHandleAnnotations() {
        if (cdiDeclaration != null) {
            return true;
        }

        // we need special handling in case of enum constructor parameters as there are synth parameters in play
        if (isEnumConstructorParam != null && isEnumConstructorParam.booleanValue()) {
            return false;
        }

        // all other cases should work just fine
        return true;
    }

    @Override
    public boolean hasAnnotation(Class<? extends Annotation> annotationType) {
        if (canSuperHandleAnnotations()) {
            return super.hasAnnotation(annotationType);
        }

        Annotation[] annotations;
        if (isEnumConstructorParam && method.reflection.getParameterAnnotations()[0].length == 0) { // in JDK 11, all annotations belong to first member, in 17, they belong to correct member
            annotations = method.reflection.getParameterAnnotations()[position + 2]; // add the synth parameters
        } else {
            annotations = method.reflection.getParameterAnnotations()[position];
        }
        return Arrays.stream(annotations)
                .anyMatch(it -> annotationType.isAssignableFrom(it.annotationType()));
    }

    @Override
    public boolean hasAnnotation(Predicate<AnnotationInfo> predicate) {
        if (canSuperHandleAnnotations()) {
            return super.hasAnnotation(predicate);
        }

        Annotation[] annotations = method.reflection.getParameterAnnotations()[position];
        return Arrays.stream(annotations)
                .anyMatch(it -> predicate.test(new AnnotationInfoImpl(it, bm)));
    }

    @Override
    public <T extends Annotation> AnnotationInfo annotation(Class<T> annotationType) {
        if (canSuperHandleAnnotations()) {
            return super.annotation(annotationType);
        }

        Annotation[] annotations = method.reflection.getParameterAnnotations()[position];
        T annotation = new AnnotationOverrides(annotations).getAnnotation(annotationType);
        return annotation != null ? new AnnotationInfoImpl(annotation, bm) : null;
    }

    @Override
    public <T extends Annotation> Collection<AnnotationInfo> repeatableAnnotation(Class<T> annotationType) {
        if (canSuperHandleAnnotations()) {
            return super.repeatableAnnotation(annotationType);
        }

        Annotation[] annotations;
        if (isEnumConstructorParam && method.reflection.getParameterAnnotations()[0].length == 0) { // in JDK 11, all annotations belong to first member, in 17, they belong to correct member
            annotations = method.reflection.getParameterAnnotations()[position + 2]; // add the synth parameters
        } else {
            annotations = method.reflection.getParameterAnnotations()[position];
        }
        return Arrays.stream(new AnnotationOverrides(annotations).getAnnotationsByType(annotationType))
                .map(t -> new AnnotationInfoImpl(t, bm))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<AnnotationInfo> annotations(Predicate<AnnotationInfo> predicate) {
        if (canSuperHandleAnnotations()) {
            return super.annotations(predicate);
        }

        Annotation[] annotations;
        if (isEnumConstructorParam && method.reflection.getParameterAnnotations()[0].length == 0) { // in JDK 11, all annotations belong to first member, in 17, they belong to correct member
            annotations = method.reflection.getParameterAnnotations()[position + 2]; // add the synth parameters
        } else {
            annotations = method.reflection.getParameterAnnotations()[position];
        }
        return Arrays.stream(annotations)
                .map(annotation -> new AnnotationInfoImpl(annotation, bm))
                .filter(predicate)
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        String name = name();
        return "parameter " + (name != null ? name : position) + " of method "
                + cdiDeclaration.getDeclaringCallable().getJavaMember().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ParameterInfoImpl that = (ParameterInfoImpl) o;
        return position == that.position
                && Objects.equals(method, that.method);
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, position);
    }
}
