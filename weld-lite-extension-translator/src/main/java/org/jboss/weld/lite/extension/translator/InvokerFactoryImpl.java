package org.jboss.weld.lite.extension.translator;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import jakarta.enterprise.inject.build.compatible.spi.BeanInfo;
import jakarta.enterprise.inject.build.compatible.spi.InvokerInfo;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Decorator;
import jakarta.enterprise.inject.spi.Interceptor;
import jakarta.enterprise.lang.model.declarations.MethodInfo;

import org.jboss.weld.bean.ClassBean;
import org.jboss.weld.invokable.InvokerInfoBuilder;
import org.jboss.weld.invokable.TargetMethod;
import org.jboss.weld.invoke.WeldInvokerBuilder;
import org.jboss.weld.invoke.WeldInvokerFactory;
import org.jboss.weld.lite.extension.translator.logging.LiteExtensionTranslatorLogger;

class InvokerFactoryImpl implements WeldInvokerFactory {
    private final BeanManager beanManager;

    InvokerFactoryImpl(BeanManager beanManager) {
        this.beanManager = beanManager;
    }

    @Override
    public WeldInvokerBuilder<InvokerInfo> createInvoker(BeanInfo bean, MethodInfo method) {
        Bean<?> cdiBean = ((BeanInfoImpl) bean).cdiBean;
        if (!(cdiBean instanceof ClassBean)) {
            throw LiteExtensionTranslatorLogger.LOG.cannotBuildInvoker("a bean which is not a managed bean", cdiBean);
        }
        if (cdiBean instanceof Interceptor) {
            throw LiteExtensionTranslatorLogger.LOG.cannotBuildInvoker("an interceptor", cdiBean);
        }
        if (cdiBean instanceof Decorator) { // not representable in BCE, but can happen
            throw LiteExtensionTranslatorLogger.LOG.cannotBuildInvoker("a decorator", cdiBean);
        }

        if (method.isConstructor()) {
            throw LiteExtensionTranslatorLogger.LOG.cannotBuildInvoker("a constructor", method);
        }
        if (Modifier.isPrivate(method.modifiers())) {
            throw LiteExtensionTranslatorLogger.LOG.cannotBuildInvoker("a private method", method);
        }
        if ("java.lang.Object".equals(method.declaringClass().name())
                && !"toString".equals(method.name())) {
            throw LiteExtensionTranslatorLogger.LOG.cannotBuildInvoker("a method declared on java.lang.Object", method);
        }

        if (method instanceof MethodInfoImpl) {
            // at this point, it is always a Method, not a Constructor
            Method reflectionMethod = (Method) ((MethodInfoImpl) method).reflection;

            // verify that the `methodInfo` belongs to this bean
            if (!ReflectionMembers.allMethods(cdiBean.getBeanClass()).contains(reflectionMethod)) {
                throw LiteExtensionTranslatorLogger.LOG.invokerMethodNotOnBean(cdiBean.getBeanClass().getName(), method);
            }

            AnnotatedType<?> cdiBeanClass = ((ClassBean<?>) cdiBean).getAnnotated();
            AnnotatedMethod<?> cdiMethod = (AnnotatedMethod<?>) ((MethodInfoImpl) method).cdiDeclaration;
            TargetMethod targetMethod = cdiMethod != null ? new TargetMethod(cdiMethod) : new TargetMethod(reflectionMethod);
            return new InvokerInfoBuilder<>(cdiBeanClass, targetMethod, beanManager);
        } else {
            throw LiteExtensionTranslatorLogger.LOG.customMethodInfoNotSupported();
        }
    }
}
