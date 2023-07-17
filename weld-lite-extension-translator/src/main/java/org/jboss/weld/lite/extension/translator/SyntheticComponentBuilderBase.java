package org.jboss.weld.lite.extension.translator;

import jakarta.enterprise.inject.build.compatible.spi.InvokerInfo;
import jakarta.enterprise.lang.model.AnnotationInfo;
import jakarta.enterprise.lang.model.declarations.ClassInfo;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

class SyntheticComponentBuilderBase<THIS extends SyntheticComponentBuilderBase<THIS>> {
    final Map<String, Object> params = new HashMap<>();

    @SuppressWarnings("unchecked")
    private THIS self() {
        return (THIS) this;
    }

    public THIS withParam(String key, boolean value) {
        this.params.put(key, value);
        return self();
    }

    public THIS withParam(String key, boolean[] value) {
        this.params.put(key, value);
        return self();
    }

    public THIS withParam(String key, int value) {
        this.params.put(key, value);
        return self();
    }

    public THIS withParam(String key, int[] value) {
        this.params.put(key, value);
        return self();
    }

    public THIS withParam(String key, long value) {
        this.params.put(key, value);
        return self();
    }

    public THIS withParam(String key, long[] value) {
        this.params.put(key, value);
        return self();
    }

    public THIS withParam(String key, double value) {
        this.params.put(key, value);
        return self();
    }

    public THIS withParam(String key, double[] value) {
        this.params.put(key, value);
        return self();
    }

    public THIS withParam(String key, String value) {
        this.params.put(key, value);
        return self();
    }

    public THIS withParam(String key, String[] value) {
        this.params.put(key, value);
        return self();
    }

    public THIS withParam(String key, Enum<?> value) {
        this.params.put(key, value);
        return self();
    }

    public THIS withParam(String key, Enum<?>[] value) {
        this.params.put(key, value);
        return self();
    }

    public THIS withParam(String key, Class<?> value) {
        this.params.put(key, value);
        return self();
    }

    public THIS withParam(String key, ClassInfo value) {
        this.params.put(key, ((ClassInfoImpl) value).cdiDeclaration.getJavaClass());
        return self();
    }

    public THIS withParam(String key, Class<?>[] value) {
        this.params.put(key, value);
        return self();
    }

    public THIS withParam(String key, ClassInfo[] value) {
        Class<?>[] array = new Class<?>[value.length];
        for (int i = 0; i < value.length; i++) {
            array[i] = ((ClassInfoImpl) value[i]).cdiDeclaration.getJavaClass();
        }
        this.params.put(key, array);
        return self();
    }

    public THIS withParam(String key, AnnotationInfo value) {
        this.params.put(key, ((AnnotationInfoImpl) value).annotation);
        return self();
    }

    public THIS withParam(String key, Annotation value) {
        this.params.put(key, value);
        return self();
    }

    public THIS withParam(String key, AnnotationInfo[] value) {
        Annotation[] array = new Annotation[value.length];
        for (int i = 0; i < value.length; i++) {
            array[i] = ((AnnotationInfoImpl) value[i]).annotation;
        }
        this.params.put(key, array);
        return self();
    }

    public THIS withParam(String key, Annotation[] value) {
        this.params.put(key, value);
        return self();
    }

    public THIS withParam(String key, InvokerInfo value) {
        this.params.put(key, value);
        return self();
    }

    public THIS withParam(String key, InvokerInfo[] value) {
        this.params.put(key, value);
        return self();
    }
}
