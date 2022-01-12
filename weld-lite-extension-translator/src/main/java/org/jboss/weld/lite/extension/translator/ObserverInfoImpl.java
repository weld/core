package org.jboss.weld.lite.extension.translator;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.Reception;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.enterprise.inject.build.compatible.spi.BeanInfo;
import jakarta.enterprise.inject.build.compatible.spi.ObserverInfo;
import jakarta.enterprise.lang.model.AnnotationInfo;
import jakarta.enterprise.lang.model.declarations.ClassInfo;
import jakarta.enterprise.lang.model.declarations.MethodInfo;
import jakarta.enterprise.lang.model.declarations.ParameterInfo;
import jakarta.enterprise.lang.model.types.Type;
import org.jboss.weld.lite.extension.translator.logging.LiteExtensionTranslatorLogger;
import org.jboss.weld.lite.extension.translator.util.reflection.AnnotatedTypes;

import java.util.Collection;
import java.util.stream.Collectors;

class ObserverInfoImpl implements ObserverInfo {
    final jakarta.enterprise.inject.spi.ObserverMethod<?> cdiObserver;
    final jakarta.enterprise.inject.spi.AnnotatedMethod<?> cdiDeclaration;

    ObserverInfoImpl(jakarta.enterprise.inject.spi.ObserverMethod<?> cdiObserver,
            jakarta.enterprise.inject.spi.AnnotatedMethod<?> cdiDeclaration) {
        this.cdiObserver = cdiObserver;
        this.cdiDeclaration = cdiDeclaration;
    }

    @Override
    public Type eventType() {
        java.lang.reflect.Type observedType = cdiObserver.getObservedType();
        return TypeImpl.fromReflectionType(AnnotatedTypes.from(observedType));
    }

    @Override
    public Collection<AnnotationInfo> qualifiers() {
        return cdiObserver.getObservedQualifiers()
                .stream()
                .map(AnnotationInfoImpl::new)
                .collect(Collectors.toList());
    }

    @Override
    public ClassInfo declaringClass() {
        jakarta.enterprise.inject.spi.AnnotatedType<?> beanClass = BeanManagerAccess.createAnnotatedType(cdiObserver.getBeanClass());
        return new ClassInfoImpl(beanClass);
    }

    @Override
    public MethodInfo observerMethod() {
        if (cdiDeclaration == null) {
            return null;
        }

        return new MethodInfoImpl(cdiDeclaration);
    }

    @Override
    public ParameterInfo eventParameter() {
        if (cdiDeclaration == null) {
            return null;
        }

        for (jakarta.enterprise.inject.spi.AnnotatedParameter<?> parameter : cdiDeclaration.getParameters()) {
            if (parameter.isAnnotationPresent(Observes.class)) {
                return new ParameterInfoImpl(parameter);
            }
        }
        throw LiteExtensionTranslatorLogger.LOG.missingObservesAnnotation(cdiDeclaration);
    }

    @Override
    public BeanInfo bean() {
        if (cdiDeclaration == null) {
            return null;
        }
        // disposer declaration should be null because the declaring bean will be a class-based bean
        return new BeanInfoImpl(cdiObserver.getDeclaringBean(), cdiDeclaration, null);
    }

    @Override
    public boolean isSynthetic() {
        return cdiDeclaration == null;
    }

    @Override
    public int priority() {
        return cdiObserver.getPriority();
    }

    @Override
    public boolean isAsync() {
        return cdiObserver.isAsync();
    }

    @Override
    public Reception reception() {
        return cdiObserver.getReception();
    }

    @Override
    public TransactionPhase transactionPhase() {
        return cdiObserver.getTransactionPhase();
    }

    @Override
    public String toString() {
        return "observer [type=" + cdiObserver.getObservedType()
                + ", qualifiers=" + cdiObserver.getObservedQualifiers() + "]"
                + (cdiDeclaration != null ? " declared at " + cdiDeclaration : "");
    }
}
