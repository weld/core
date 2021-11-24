package org.jboss.weld.lite.extension.translator;

import jakarta.enterprise.lang.model.AnnotationInfo;
import jakarta.enterprise.lang.model.AnnotationMember;
import jakarta.enterprise.lang.model.declarations.ClassInfo;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

class AnnotationInfoImpl implements AnnotationInfo {
    final Annotation annotation;

    AnnotationInfoImpl(Annotation annotation) {
        this.annotation = annotation;
    }

    @Override
    public ClassInfo declaration() {
        return new ClassInfoImpl(BeanManagerAccess.createAnnotatedType(annotation.annotationType()));
    }

    @Override
    public boolean hasMember(String name) {
        try {
            annotation.annotationType().getDeclaredMethod(name);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    @Override
    public AnnotationMember member(String name) {
        try {
            java.lang.reflect.Method member = annotation.annotationType().getDeclaredMethod(name);
            member.setAccessible(true); // TODO!
            Object value = member.invoke(annotation);
            return new AnnotationMemberImpl(value);
        } catch (NoSuchMethodException e) {
            return null;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, AnnotationMember> members() {
        try {
            java.lang.reflect.Method[] members = annotation.annotationType().getDeclaredMethods();
            Map<String, AnnotationMember> result = new HashMap<>();
            for (java.lang.reflect.Method member : members) {
                member.setAccessible(true); // TODO!
                String name = member.getName();
                Object value = member.invoke(annotation);
                result.put(name, new AnnotationMemberImpl(value));
            }
            return result;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AnnotationInfoImpl that = (AnnotationInfoImpl) o;
        return Objects.equals(annotation, that.annotation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(annotation);
    }

    @Override
    public String toString() {
        return annotation.toString();
    }
}
