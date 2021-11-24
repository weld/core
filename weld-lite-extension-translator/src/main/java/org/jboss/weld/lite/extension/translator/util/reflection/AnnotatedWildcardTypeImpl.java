package org.jboss.weld.lite.extension.translator.util.reflection;

import java.util.Arrays;

final class AnnotatedWildcardTypeImpl extends AbstractEmptyAnnotatedType implements java.lang.reflect.AnnotatedWildcardType {
    private final java.lang.reflect.WildcardType wildcardType;

    AnnotatedWildcardTypeImpl(java.lang.reflect.WildcardType wildcardType) {
        this.wildcardType = wildcardType;
    }

    @Override
    public java.lang.reflect.AnnotatedType[] getAnnotatedLowerBounds() {
        return Arrays.stream(wildcardType.getLowerBounds())
                .map(AnnotatedTypes::from)
                .toArray(java.lang.reflect.AnnotatedType[]::new);
    }

    @Override
    public java.lang.reflect.AnnotatedType[] getAnnotatedUpperBounds() {
        return Arrays.stream(wildcardType.getUpperBounds())
                .map(AnnotatedTypes::from)
                .toArray(java.lang.reflect.AnnotatedType[]::new);
    }

    // added in Java 9
    /*
    @Override
    */
    public java.lang.reflect.AnnotatedType getAnnotatedOwnerType() {
        return null;
    }

    @Override
    public java.lang.reflect.Type getType() {
        return wildcardType;
    }

    @Override
    public String toString() {
        return wildcardType.toString();
    }
}
