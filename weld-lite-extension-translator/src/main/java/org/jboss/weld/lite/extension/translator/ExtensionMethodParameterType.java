package org.jboss.weld.lite.extension.translator;

import jakarta.enterprise.inject.build.compatible.spi.BeanInfo;
import jakarta.enterprise.inject.build.compatible.spi.ClassConfig;
import jakarta.enterprise.inject.build.compatible.spi.FieldConfig;
import jakarta.enterprise.inject.build.compatible.spi.InterceptorInfo;
import jakarta.enterprise.inject.build.compatible.spi.Messages;
import jakarta.enterprise.inject.build.compatible.spi.MetaAnnotations;
import jakarta.enterprise.inject.build.compatible.spi.MethodConfig;
import jakarta.enterprise.inject.build.compatible.spi.ObserverInfo;
import jakarta.enterprise.inject.build.compatible.spi.ScannedClasses;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticComponents;
import jakarta.enterprise.inject.build.compatible.spi.Types;
import jakarta.enterprise.lang.model.declarations.ClassInfo;
import jakarta.enterprise.lang.model.declarations.FieldInfo;
import jakarta.enterprise.lang.model.declarations.MethodInfo;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

enum ExtensionMethodParameterType {
    META_ANNOTATIONS(MetaAnnotations.class, false, ExtensionPhase.DISCOVERY),
    SCANNED_CLASSES(ScannedClasses.class, false, ExtensionPhase.DISCOVERY),

    CLASS_INFO(ClassInfo.class, true, ExtensionPhase.ENHANCEMENT),
    METHOD_INFO(MethodInfo.class, true, ExtensionPhase.ENHANCEMENT),
    FIELD_INFO(FieldInfo.class, true, ExtensionPhase.ENHANCEMENT),

    CLASS_CONFIG(ClassConfig.class, true, ExtensionPhase.ENHANCEMENT),
    METHOD_CONFIG(MethodConfig.class, true, ExtensionPhase.ENHANCEMENT),
    FIELD_CONFIG(FieldConfig.class, true, ExtensionPhase.ENHANCEMENT),

    BEAN_INFO(BeanInfo.class, true, ExtensionPhase.REGISTRATION),
    INTERCEPTOR_INFO(InterceptorInfo.class, true, ExtensionPhase.REGISTRATION),
    OBSERVER_INFO(ObserverInfo.class, true, ExtensionPhase.REGISTRATION),

    SYNTHETIC_COMPONENTS(SyntheticComponents.class, false, ExtensionPhase.SYNTHESIS),

    MESSAGES(Messages.class, false, ExtensionPhase.DISCOVERY, ExtensionPhase.ENHANCEMENT,
            ExtensionPhase.REGISTRATION, ExtensionPhase.SYNTHESIS, ExtensionPhase.VALIDATION),
    TYPES(Types.class, false, ExtensionPhase.ENHANCEMENT, ExtensionPhase.REGISTRATION,
            ExtensionPhase.SYNTHESIS, ExtensionPhase.VALIDATION),

    UNKNOWN(null, false),
    ;

    private final Class<?> type;
    private final boolean isQuery;
    private final Set<ExtensionPhase> validPhases;

    ExtensionMethodParameterType(Class<?> type, boolean isQuery, ExtensionPhase... validPhases) {
        this.type = type;
        this.isQuery = isQuery;
        if (validPhases == null || validPhases.length == 0) {
            this.validPhases = EnumSet.noneOf(ExtensionPhase.class);
        } else {
            this.validPhases = EnumSet.copyOf(Arrays.asList(validPhases));
        }
    }

    boolean isQuery() {
        return isQuery;
    }

    void verifyAvailable(ExtensionPhase phase, java.lang.reflect.Method method) {
        if (!validPhases.contains(phase)) {
            throw new IllegalArgumentException(phase + " methods can't declare a parameter of type "
                    + (type != null ? type.getSimpleName() : this.name()) + ", found at "
                    + method.getDeclaringClass().getSimpleName() + "." + method.getName());
        }
    }

    static ExtensionMethodParameterType of(Class<?> type) {
        for (ExtensionMethodParameterType candidate : ExtensionMethodParameterType.values()) {
            if (candidate.type.equals(type)) {
                return candidate;
            }
        }

        return UNKNOWN;
    }
}
