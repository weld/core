package org.jboss.weld.lite.extension.translator.util.reflection;

import java.util.Arrays;

final class WildcardTypeImpl implements java.lang.reflect.WildcardType {
    private static final java.lang.reflect.Type[] NO_UPPER_BOUND = new java.lang.reflect.Type[]{Object.class};
    private static final java.lang.reflect.Type[] NO_LOWER_BOUND = new java.lang.reflect.Type[0];
    private static final java.lang.reflect.WildcardType UNBOUNDED = new WildcardTypeImpl(NO_UPPER_BOUND, NO_LOWER_BOUND);

    static java.lang.reflect.WildcardType unbounded() {
        return UNBOUNDED;
    }

    static java.lang.reflect.WildcardType withUpperBound(java.lang.reflect.Type type) {
        return new WildcardTypeImpl(new java.lang.reflect.Type[]{type}, NO_LOWER_BOUND);
    }

    static java.lang.reflect.WildcardType withLowerBound(java.lang.reflect.Type type) {
        return new WildcardTypeImpl(NO_UPPER_BOUND, new java.lang.reflect.Type[]{type});
    }

    private final java.lang.reflect.Type[] upperBounds;
    private final java.lang.reflect.Type[] lowerBounds;

    private WildcardTypeImpl(java.lang.reflect.Type[] upperBounds, java.lang.reflect.Type[] lowerBounds) {
        this.upperBounds = upperBounds;
        this.lowerBounds = lowerBounds;
    }

    @Override
    public java.lang.reflect.Type[] getUpperBounds() {
        return upperBounds;
    }

    @Override
    public java.lang.reflect.Type[] getLowerBounds() {
        return lowerBounds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof java.lang.reflect.WildcardType) {
            java.lang.reflect.WildcardType that = (java.lang.reflect.WildcardType) o;
            return Arrays.equals(upperBounds, that.getUpperBounds())
                    && Arrays.equals(lowerBounds, that.getLowerBounds());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(lowerBounds) ^ Arrays.hashCode(upperBounds);
    }

    @Override
    public String toString() {
        boolean noUpperBound = Arrays.equals(upperBounds, NO_UPPER_BOUND);
        boolean noLowerBound = Arrays.equals(lowerBounds, NO_LOWER_BOUND);

        if (noUpperBound && noLowerBound) {
            return "?";
        } else if (noUpperBound) {
            return "? super " + lowerBounds[0];
        } else if (noLowerBound) {
            return "? extends " + upperBounds[0];
        } else {
            // should never happen
            return "? extends " + upperBounds[0] + " super " + lowerBounds[0];
        }
    }
}
